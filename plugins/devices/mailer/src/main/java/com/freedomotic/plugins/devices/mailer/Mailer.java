/**
 *
 * Copyright (c) 2009-2015 Freedomotic team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.plugins.devices.mailer;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.MessageEvent;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author http://www.javapractices.com/topic/TopicAction.do?Id=144
 */
public final class Mailer extends Protocol {

    public static final Logger LOG = Logger.getLogger(Mailer.class.getName());
    private int sentMails;

    public Mailer() {
        super("Mailer", "/mailer/mailer.xml");
        addEventListener("app.event.sensor.messages.mail");
    }

    @Override
    protected void onRun() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        String from = c.getProperty("from");
        if (from == null) {
            from = "your.home@freedomotic.com";
        }
        String to = c.getProperty("to");
        String subject = c.getProperty("subject");
        final String text = c.getProperty("message");
        try {
            send(from, to, subject, text);
        } catch (MessagingException ex) {
            this.notifyCriticalError("Error while sending email '" + subject + "' to '" + to + "' " + ex.getMessage());
        }

    }

    private void send(String from, String to, String subject, String text) throws AddressException, MessagingException {
        final String username = configuration.getProperty("username");
        final String password = configuration.getProperty("password");
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", configuration.getProperty("mail.smtp.host"));
        props.put("mail.smtp.port", configuration.getProperty("mail.smtp.port"));

        if (to == null || to.isEmpty()) {
            to = username;
        }
        if (subject == null || subject.isEmpty()) {
            subject = "A notification from freedomotic";
        }

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("you@home.com"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(text);

        Transport.send(message);
        sentMails++;
        setDescription(sentMails + " emails sent");
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        if (event instanceof MessageEvent) {
            MessageEvent mail = (MessageEvent) event;
            try {
                send(mail.getFrom(), mail.getTo(), null, mail.getText());
            } catch (MessagingException ex) {
                this.notifyCriticalError("Error while sending email to " + mail.getTo() + ": " + ex.getMessage());
            }
        }
    }
}
