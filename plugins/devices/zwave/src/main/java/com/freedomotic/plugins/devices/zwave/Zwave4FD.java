/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
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
package com.freedomotic.plugins.devices.zwave;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zwave4j.ControllerCallback;
import org.zwave4j.ControllerCommand;
import org.zwave4j.ControllerError;
import org.zwave4j.ControllerState;
import org.zwave4j.Manager;
import org.zwave4j.NativeLibraryLoader;
import org.zwave4j.Notification;
import org.zwave4j.NotificationWatcher;
import org.zwave4j.Options;
import org.zwave4j.ValueGenre;
import org.zwave4j.ValueId;
import org.zwave4j.ValueType;
import org.zwave4j.ZWave4j;

/**
 *
 * @author Mauro Cicolella
 */
public class Zwave4FD
        extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(Zwave4FD.class.getName());
    final int POLLING_WAIT;
    private Manager manager;
    private NotificationWatcher watcher;
    private String controllerPort;
    private Options options;
    private long homeId;
    private boolean ready;
    private static ControllerCallback GENERIC_COMMAND_CALLBACK = new ControllerCallback() {

        @Override
        public void onCallback(ControllerState cs, ControllerError ce, Object o) {
            if (cs.equals(ControllerState.COMPLETED)) {
                LOG.info("Successfully complete operation");
            } else if (cs.equals(ControllerState.CANCEL)) {
                LOG.info("Operation canceled");
            }
        }
    };

    /**
     *
     */
    public Zwave4FD() {
        super("Zwave4FD", "/zwave/zwave-manifest.xml");
        POLLING_WAIT = configuration.getIntProperty("time-between-reads", -1);
        setPollingWait(-1); //millisecs interval between hardware device status reads
        NativeLibraryLoader.loadLibrary(ZWave4j.LIBRARY_NAME, ZWave4j.class);
    }

    private Object getValue(ValueId valueId) {
        switch (valueId.getType()) {
            case BOOL:
                AtomicReference<Boolean> b = new AtomicReference<Boolean>();
                Manager.get().getValueAsBool(valueId, b);
                return b.get();
            case BYTE:
                AtomicReference<Short> bb = new AtomicReference<Short>();
                Manager.get().getValueAsByte(valueId, bb);
                return bb.get();
            case DECIMAL:
                AtomicReference<Float> f = new AtomicReference<Float>();
                Manager.get().getValueAsFloat(valueId, f);
                return f.get();
            case INT:
                AtomicReference<Integer> i = new AtomicReference<Integer>();
                Manager.get().getValueAsInt(valueId, i);
                return i.get();
            case LIST:
                return null;
            case SCHEDULE:
                return null;
            case SHORT:
                AtomicReference<Short> s = new AtomicReference<Short>();
                Manager.get().getValueAsShort(valueId, s);
                return s.get();
            case STRING:
                AtomicReference<String> ss = new AtomicReference<String>();
                Manager.get().getValueAsString(valueId, ss);
                return ss.get();
            case BUTTON:
                return null;
            case RAW:
                AtomicReference<short[]> sss = new AtomicReference<short[]>();
                Manager.get().getValueAsRaw(valueId, sss);
                return sss.get();
            default:
                return null;
        }
    }

    @Override
    protected void onShowGui() {
    }

    @Override
    protected void onHideGui() {
        //implement here what to do when the this plugin GUI is closed
        //for example you can change the plugin description
        setDescription("My GUI is now hidden");
    }

    @Override
    protected void onRun() {
    }

    @Override
    protected void onStart() {
        LOG.info("Zwave plugin started");
        controllerPort = configuration.getStringProperty("zw-controller-port", "/dev/ttyS0");
        options = Options.create(new File(getFile().getParentFile() + "/data/config").getAbsolutePath(), "", "");
        options.addOptionBool("ConsoleOutput", configuration.getBooleanProperty("zw-console-output", false));
        options.addOptionInt("DriverMaxAttempts", configuration.getIntProperty("zw-driver-max-attempts", 1));
        options.addOptionBool("SaveConfiguration", configuration.getBooleanProperty("zw-save-configuration", false));
        options.addOptionBool("Logging", configuration.getBooleanProperty("zw-logging", false));
        options.addOptionInt("SaveLogLevel", configuration.getIntProperty("zw-save-log-level", 6));
        //  more options could be picked up from https://code.google.com/p/open-zwave/wiki/Config_Options

        options.lock();
        watcher = new NotificationWatcher() {

            @Override
            public void onNotification(Notification notification, Object obj) {
                switch (notification.getType()) {
                    case DRIVER_READY:
                        homeId = notification.getHomeId();
                        setDescription("Zwave is using device '" + controllerPort + "'");
                        LOG.info(String.format("Driver ready - home id: %d", homeId));
                        break;
                    case DRIVER_FAILED:
                        LOG.warn("Driver failed - controller port was '{}'", controllerPort);
                        stop();
                        break;
                    case DRIVER_RESET:
                        LOG.info("Driver reset");
                        break;
                    case AWAKE_NODES_QUERIED:
                        LOG.info("Awake nodes queried");
                        break;
                    case ALL_NODES_QUERIED:
                        LOG.info("All nodes queried");
                        manager.writeConfig(homeId);
                        ready = true;
                        break;
                    case ALL_NODES_QUERIED_SOME_DEAD:
                        LOG.warn("All nodes queried some dead");
                        manager.writeConfig(homeId);
                        ready = true;
                        break;
                    case POLLING_ENABLED:
                        LOG.info("Polling enabled");
                        break;
                    case POLLING_DISABLED:
                        LOG.info("Polling disabled");
                        break;
                    case NODE_NEW:
                        LOG.info(String.format("Node new - id: %d", notification.getNodeId()));
                        break;
                    case NODE_ADDED:
                        LOG.info(String.format("Node added - id: %d, name: %s, manufacturer: %s, product: %s, type: %s",
                                notification.getNodeId(),
                                manager.getNodeName(homeId, notification.getNodeId()),
                                manager.getNodeManufacturerName(homeId, notification.getNodeId()),
                                manager.getNodeProductName(homeId, notification.getNodeId()),
                                manager.getNodeProductType(homeId, notification.getNodeId())));
                        break;
                    case NODE_REMOVED:
                        LOG.info(String.format("Node removed -id: %d", notification.getNodeId()));
                        break;
                    case ESSENTIAL_NODE_QUERIES_COMPLETE:
                        LOG.info(String.format("Node essential queries complete - id: %d", notification.getNodeId()));
                        break;
                    case NODE_QUERIES_COMPLETE:
                        LOG.info(String.format("Node queries complete - id: %d", notification.getNodeId()));
                        break;
                    case NODE_EVENT:
                        LOG.info(String.format("Node event {node: %d, event: %d}",
                                notification.getNodeId(),
                                notification.getEvent()));
                        break;
                    case NODE_NAMING:
                        LOG.info(String.format("Node naming - id: %d", notification.getNodeId()));
                        break;
                    case NODE_PROTOCOL_INFO:
                        LOG.info(String.format("Node protocol info {node: %d, type: %s}",
                                notification.getNodeId(),
                                manager.getNodeType(notification.getHomeId(), notification.getNodeId())));
                        break;
                    case VALUE_ADDED:
                        LOG.info(String.format("Value added {node: %d, command class: %d, instance: %d, index: %d, genre: %s, type: %s, label: %s, value: %s}",
                                notification.getValueId().getNodeId(),
                                notification.getValueId().getCommandClassId(),
                                notification.getValueId().getInstance(),
                                notification.getValueId().getIndex(),
                                notification.getValueId().getGenre().name(),
                                notification.getValueId().getType().name(),
                                manager.getValueLabel(notification.getValueId()),
                                getValue(notification.getValueId())));
                        sendNotification(notification.getValueId());
                        break;
                    case VALUE_REMOVED:
                        LOG.info(String.format("Value removed {node: %d, command class: %d, instance: %d,index: %d}",
                                notification.getNodeId(),
                                notification.getValueId().getCommandClassId(),
                                notification.getValueId().getInstance(),
                                notification.getValueId().getIndex()));
                        break;
                    case VALUE_CHANGED:
                        LOG.info(String.format("Value changed {node: %d, command class: %d, instance: %d, index: %d, value: %s}",
                                notification.getNodeId(),
                                notification.getValueId().getCommandClassId(),
                                notification.getValueId().getInstance(),
                                notification.getValueId().getIndex(),
                                getValue(notification.getValueId())));
                        sendNotification(notification.getValueId());
                        break;
                    case VALUE_REFRESHED:
                        LOG.info(String.format("Value refreshed {node: %d, command class: %d,instance: %d, index: %d, value: %s}",
                                notification.getNodeId(),
                                notification.getValueId().getCommandClassId(),
                                notification.getValueId().getInstance(),
                                notification.getValueId().getIndex(),
                                getValue(notification.getValueId())));
                        break;
                    case GROUP:
                        LOG.info(String.format("Group { node id: %d, group id: %d }",
                                notification.getNodeId(),
                                notification.getGroupIdx()));
                        break;
                    case SCENE_EVENT:
                        LOG.info(String.format("Scene event - id: %d", notification.getSceneId()));
                        break;
                    case CREATE_BUTTON:
                        LOG.info(String.format("Button create - id: %d", notification.getButtonId()));
                        break;
                    case DELETE_BUTTON:
                        LOG.info(String.format("Button delete - id: %d", notification.getButtonId()));
                        break;
                    case BUTTON_ON:
                        LOG.info(String.format("Button on - id: %d", notification.getButtonId()));
                        break;
                    case BUTTON_OFF:
                        LOG.info(String.format("Button off - id: %d", notification.getButtonId()));
                        break;
                    case NOTIFICATION:
                        LOG.info("Notification");
                        break;
                    default:
                        LOG.info(notification.getType().name());
                        break;
                }
            }
        };
        manager = Manager.create();
        manager.addWatcher(watcher, null);
        manager.addDriver(controllerPort);

    }

    @Override
    protected void onStop() {
        manager.removeWatcher(watcher, null);
        manager.removeDriver(controllerPort);
        Manager.destroy();
        Options.destroy();
        LOG.info("Zwave plugin stopped ");
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        LOG.info("Zwave plugin receives a command called {} with parameters {}", new Object[]{c.getName(), c.getProperties().toString()});
        String commandName = c.getProperty("command");

        /*
         * if (commandName.equalsIgnoreCase("SET-VALUE")) { String[] address =
         * c.getProperty("address").split(":"); short nodeId =
         * Short.parseShort(address[0]); short commandClassId =
         * Short.parseShort(address[1]); short instance =
         * Short.parseShort(address[2]);
         *
         * ValueId vID = new ValueId(homeId, nodeId, ValueGenre.USER,
         * commandClassId, instance, (short) 1, ValueType.DECIMAL);
         * manager.setValueAsString(vID,
         * c.getProperty("owner.object.behavior.temperature"));
         *
         * } else if (commandName.equalsIgnoreCase("SWITCH")) { String[] address
         * = c.getProperty("address").split(":"); short nodeId =
         * Short.parseShort(address[0]); short instance =
         * Short.parseShort(address[2]);
         *
         * ValueId vID = new ValueId(homeId, nodeId, ValueGenre.USER, (short)
         * 37, instance, (short) 0, ValueType.BOOL);
         * manager.setValueAsString(vID,
         * c.getProperty("owner.object.behavior.powered"));
         *
         * }
         * else if (commandName.equalsIgnoreCase("TOGGLE")) { String[] address =
         * c.getProperty("address").split(":"); short nodeId =
         * Short.parseShort(address[0]); short instance =
         * Short.parseShort(address[2]);
         *
         * ValueId vID = new ValueId(homeId, nodeId, ValueGenre.USER, (short)
         * 40, instance, (short) 0, ValueType.BOOL);
         * manager.setValueAsString(vID,
         * c.getProperty("owner.object.behavior.powered"));
         *
         * } else
         */
        if (commandName != null && commandName.equalsIgnoreCase("INCLUDE-DEVICE")) {
            // code to let a Zwave device associate to the master 
            manager.cancelControllerCommand(homeId);
            LOG.info("Started accepting device inclusion request");
            manager.beginControllerCommand(homeId, ControllerCommand.ADD_DEVICE, GENERIC_COMMAND_CALLBACK);
            TimerTask tt = new TimerTask() {

                @Override
                public void run() {
                    manager.cancelControllerCommand(homeId);
                }
            };
            new Timer().schedule(tt, Long.parseLong(c.getProperty("timeout")));

        } else if (commandName != null && commandName.equalsIgnoreCase("EXCLUDE-DEVICE")) {
            // code to let or foce a device disconnect from master
            manager.cancelControllerCommand(homeId);
            LOG.info("Started accepting device exclusion request");

            manager.beginControllerCommand(homeId, ControllerCommand.REMOVE_DEVICE, GENERIC_COMMAND_CALLBACK);
            TimerTask tt = new TimerTask() {

                @Override
                public void run() {
                    manager.cancelControllerCommand(homeId);
                }
            };
            new Timer().schedule(tt, Long.parseLong(c.getProperty("timeout")));
        } else {
            // generic control command
            String[] address = c.getProperty("address").split(":");
            short nodeId = Short.parseShort(address[0]);
            if (c.getProperty("zwave.nodeId") != null && !c.getProperty("zwave.nodeIdclass").isEmpty()) {
                nodeId = Short.parseShort(c.getProperty("zwave.nodeId"));
            }
            short commandClassId = Short.parseShort(address[1]);
            if (c.getProperty("zwave.class") != null && !c.getProperty("zwave.class").isEmpty()) {
                commandClassId = Short.parseShort(c.getProperty("zwave.class"));
            }

            short instance = Short.parseShort(address[2]);
            if (c.getProperty("zwave.instance") != null && !c.getProperty("zwave.instance").isEmpty()) {
                instance = Short.parseShort(c.getProperty("zwave.instance"));
            }

            short index = 0;
            if (c.getProperty("zwave.index") != null && !c.getProperty("zwave.index").isEmpty()) {
                index = Short.parseShort(c.getProperty("zwave.index"));
            }

            String valueType = "STRING";
            if (c.getProperty("zwave.valueType") != null && !c.getProperty("zwave.valueType").isEmpty()) {
                valueType = c.getProperty("zwave.valueType");
            }

            ValueId vID = new ValueId(homeId, nodeId, ValueGenre.USER, commandClassId, instance, index, ValueType.valueOf(valueType));
            manager.setValueAsString(vID, c.getProperty("zwave.value"));

        }
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

    /*
     * Notifies event to Freedomotic
     *
     * @param ValueId 
     *
     */
    private void sendNotification(ValueId id) {

        // object address format  nodeID:commadClass:instance:index
        String address = id.getNodeId() + ":" + id.getCommandClassId() + ":" + id.getInstance() + ":" + id.getIndex();

        ProtocolRead event = new ProtocolRead(this, "zwave", address);
        event.addProperty("inputValue", getValue(id) != null ? getValue(id).toString() : "0");
        event.addProperty("zwave.index", new Short(id.getIndex()).toString());
        event.addProperty("value.label", manager.getValueLabel(id));
        event.addProperty("value.unit", manager.getValueUnits(id));
        event.addProperty("zwave.command", new Short(id.getCommandClassId()).toString());
        if (configuration.getBooleanProperty("auto-configuration", true)) {
            // for sensorMultilevel the object is mapped to the value type (temperature, luminescence, relative humidity etc)
            if (id.getCommandClassId() == 49) {
                if ((configuration.getStringProperty(manager.getValueLabel(id), null) != null)) {
                    event.addProperty("object.class", configuration.getStringProperty(manager.getValueLabel(id), null));
                }
                // otherwise to command class 
            } else {
                if ((configuration.getStringProperty(new Short(id.getCommandClassId()).toString(), null) != null)) {
                    event.addProperty("object.class", configuration.getStringProperty(new Short(id.getCommandClassId()).toString(), null));
                }
            }
            String devName = manager.getNodeManufacturerName(homeId, id.getNodeId()) + " - " + manager.getNodeProductName(homeId, id.getNodeId());
            event.addProperty("object.name", devName);
        }
        this.notifyEvent(event);
    }
}
