/*
 Copyright (c) Matteo Mazzoni 2012-2014
   
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */
package com.freedomotic.plugins.devices.freedomchat;

import com.skype.ChatMessage;
import com.skype.ChatMessageAdapter;
import com.skype.Skype;
import com.skype.SkypeException;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;

import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

public class FreedomChat extends Protocol {

    public static String ACCEPTED = "Just wait and see.";
    public static String NOT_ACCEPTED = "";
    
    final int POLLING_WAIT;
    private String hostname;
    private int port;
    private String username;
    private String password;
    private String acceptancePassword;
    private boolean acceptAllCertificates;
    private boolean useSkype;
    private boolean useXMPP;
    private boolean manualHostname;
    XMPPConnection conn;
    private ChatMessageAdapter chatListener;


    
    private final MessageService msg;
    
    @Inject
    Injector injector;

    public FreedomChat() {
        //every plugin needs a name and a manifest XML file
        super("Chat console", "/chat/chat-manifest.xml");
        //read a property from the manifest file below which is in
        //FREEDOMOTIC_FOLDER/plugins/devices/com.freedomotic.hello/hello-world.xml
        POLLING_WAIT = configuration.getIntProperty("time-between-reads", -1);
        //POLLING_WAIT is the value of the property "time-between-reads" or 2000 millisecs,
        //default value if the property does not exist in the manifest
        setPollingWait(POLLING_WAIT); //millisecs interval between hardware device status reads
        msg = injector.getInstance(MessageService.class);        
    }

    @Override
    protected void onStart() {
        setDescription("Chat plugin starting");
        useSkype = configuration.getBooleanProperty("enable-skype", false);
        useXMPP = configuration.getBooleanProperty("enable-XMPP", false);
        if (!useSkype && !useXMPP) {
            setDescription("Enable XMPP or Skype before starting");
            return;
        }
        String newDescription = "";
        acceptancePassword = configuration.getStringProperty("acceptance-password", "");
        boolean skypeInited = false;
        if (useSkype) {
            skypeInited = initSkype();
        }
        boolean xmppInited = false;
        if (useXMPP) {
            xmppInited = initXMPP();
            if (xmppInited) {
                newDescription = "XMPP: " + username + ",accept. pwd: " + acceptancePassword;
            }
        }

        if (xmppInited || skypeInited) {
            if (skypeInited) {
                setDescription("Skype enabled " + newDescription);
            } else {
                setDescription(newDescription);
            }
            LOG.info("Chat plugin has started");
        } else {
            setDescription("Chat plugin");
        }
    }

    @Override
    protected void onStop() {
        teardownXMPP();
        teardownSkype();
        setDescription("Chat plugin");
        LOG.info("Chat plugin has stopped ");
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        // Assume we've created a Connection name "connection".

        LOG.log(Level.INFO, "Chat plugin receives a command called {0} with parameters {1}", new Object[]{c.getName(), c.getProperties().toString()});
    }

    @Override
    protected boolean canExecute(Command c) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

   
    public static String unsplit(String[] parts, int index, int length, String splitter) {
        if (parts == null) {
            return null;
        }
        if ((index < 0) || (index >= parts.length)) {
            return null;
        }
        if (index + length > parts.length) {
            return null;
        }

        StringBuilder buf = new StringBuilder();
        for (int i = index; i < index + length; i++) {
            if (parts[i] != null) {
                buf.append(parts[i]);
            }
            buf.append(splitter);
        }

        // remove the trailing splitter
        buf.setLength(buf.length() - splitter.length());
        return buf.toString();
    }

    @Override
    protected void onRun() {
    }
    private static final Logger LOG = Logger.getLogger(FreedomChat.class.getName());


    private boolean initXMPP() {
        manualHostname = configuration.getBooleanProperty("manual-hostname", false);
        if (manualHostname) {
            hostname = configuration.getStringProperty("hostname", "jabber.org");
            port = configuration.getIntProperty("port", 5222);
        }
        username = configuration.getStringProperty("username", "");
        password = configuration.getStringProperty("password", "");
        acceptAllCertificates = configuration.getBooleanProperty("accept-all-certificates", false);

        ConnectionConfiguration config;
        if (manualHostname) {
            config = new ConnectionConfiguration(hostname, port);
        } else {
            config = new ConnectionConfiguration(StringUtils.parseServer(username));
        }
        config.setCompressionEnabled(true);
        if (acceptAllCertificates) {
            try {
                TLSUtils.acceptAllCertificates(config);
            } catch (KeyManagementException e) {
                LOG.log(Level.SEVERE, "Cannot start Chat plugin. Reason: {0}", e.getMessage());
                return false;
            } catch (NoSuchAlgorithmException e) {
                LOG.log(Level.SEVERE, "Cannot start Chat plugin. Reason: {0}", e.getMessage());
                return false;
            }
        }

        conn = new XMPPTCPConnection(config);
        try {
            conn.connect();
            conn.login(username, password, "Freedomotic");
            // Create a new presence. Pass in false to indicate we're unavailable.
            Presence presence = new Presence(Presence.Type.available);
            presence.setStatus("Ready");
            // Send the packet (assume we have a Connection instance called "con").
            conn.sendPacket(presence);

            // wait for messages
            ChatManager chatmanager = ChatManager.getInstanceFor(conn);
            chatmanager.addChatListener(new ChatManagerListener() {
                @Override
                public void chatCreated(Chat chat, boolean createdLocally) {
                    if (!createdLocally) {
                        chat.addMessageListener(new MessageListener() {
                            @Override
                            public void processMessage(Chat chat, Message message) {
                                try {
                                    // the user is in my list, OK accepc messages
                                    LOG.info(chat.getParticipant());
                                    String uname[] = chat.getParticipant().split("/");
                                    if (conn.getRoster().getEntry(uname[0]) != null) {
                                        // Send back the same text the other user sent us.
                                        //chat.sendMessage(message.getBody());
                                        chat.sendMessage(msg.manageMessage(message.getBody()));

                                    } else {
                                        // expect a password in order to add user to friends' list
                                        chat.sendMessage(manageSubscription(chat, message));
                                    }
                                } catch (Exception ex) {
                                    LOG.log(Level.SEVERE, null, ex);
                                }
                            }
                        });
                    }
                }

                private String manageSubscription(Chat chat, Message message) {
                    if (message.getBody().equals(acceptancePassword)) {
                        try {
                            conn.getRoster().createEntry(chat.getParticipant(), "", null);
                            return ACCEPTED;
                        } catch (Exception ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                    }
                    return NOT_ACCEPTED;
                }
            });
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Cannot start Chat plugin. Reason: {0}", ex.getMessage());
            teardownXMPP();
            return false;
        }
        return true;
    }

    private boolean initSkype() {
        Skype.setDaemon(false);
        try {
            chatListener = new ChatMessageAdapter() {
                @Override
                public void chatMessageReceived(ChatMessage received) {
                    try {
                        if (received.getSender().isAuthorized()) {
                            if (received.getType().equals(ChatMessage.Type.SAID)) {
                                received.getSender().send(msg.manageMessage(received.getContent()));
                            }
                        } else {
                            received.getSender().send(manageSubscription(received));
                        }
                    } catch (SkypeException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                }

                private String manageSubscription(ChatMessage received) {
                    try {
                        if (received.getContent().equals(acceptancePassword)) {
                            received.getSender().setAuthorized(true);
                            return ACCEPTED;
                        }
                    } catch (SkypeException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                    return NOT_ACCEPTED;
                }
            };
            Skype.addChatMessageListener(chatListener);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage());
            teardownSkype();
            return false;
        }
        return true;
    }

    private void teardownXMPP() {
        // Disconnect from the server
        if (conn != null && conn.isConnected()) {
            try {
                conn.disconnect();
            } catch (NotConnectedException e) {
                // Already disconnected, can ignore
            }
        }
        conn = null;
    }

    private void teardownSkype() {
        try {
            Skype.removeChatMessageListener(chatListener);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage());
        }
        Skype.setDaemon(true);
    }
}
