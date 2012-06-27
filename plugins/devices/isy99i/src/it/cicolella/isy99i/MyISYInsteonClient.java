/* 
 * -----------------------------------------------------------------
 * MyISYInsteonClient.java
 * --------------
 *
 * -----------------------------------------------------------------
 * Copyright (C) 2007  Universal Devices
 * -----------------------------------------------------------------
 * 
 * -----------------------------------------------------------------
 */
package it.cicolella.isy99i;

import com.nanoxml.XMLElement;
import com.udi.insteon.client.InsteonOps;
import com.udi.isy.jsdk.insteon.ISYInsteonClient;
import com.universaldevices.client.NoDeviceException;
import com.universaldevices.common.Constants;
import com.universaldevices.common.properties.UDProperty;
import com.universaldevices.device.model.*;
import com.universaldevices.security.upnp.UPnPSecurity;
import com.universaldevices.upnp.UDProxyDevice;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.ProtocolRead;
import it.freedomotic.api.EventTemplate;

/**
 *
 * This class implements a very simple ISY client which prints out events as
 * they occur in ISY
 *
 * @author UD Architect
 *
 */
public class MyISYInsteonClient extends ISYInsteonClient {

    /**
     * Constructor Registers this class as IModelChangeListener
     *
     * @see IModelChangeListener
     *
     */
    public MyISYInsteonClient() {
        super();
    }

    public synchronized MyISYInsteonClient getISY() {
        return this;
    }

    /**
     * This method is called when a new ISY is announced or discovered on the
     * network. For this sample, we simply authenticate ourselves
     */
    public void onNewDeviceAnnounced(UDProxyDevice device) {
        Freedomotic.logger.info("NEW DEVICE: " + device.getFriendlyName());
        Isy99iFrame.writeAreaLog(Isy99iUtilities.getDateTime() +": New Device found: " + device.getFriendlyName());
    }

    /**
     * This method is invoked when ISY goes into Linking mode
     */
    public void onDiscoveringNodes() {
        System.out.println("I am in Linking Mode ...");
    }

    /**
     * This method is invoked when ISY is no longer in Linking mode
     */
    public void onNodeDiscoveryStopped() {
        System.out.println("I am no longer in Linking mode ...");

    }

    /**
     * This method is invoked when a group/scene is removed
     */
    public void onGroupRemoved(String groupAddress) {
        Freedomotic.logger.info("Scene: " + groupAddress + " was removed by someone or something!");
    }

    /**
     * This method is invoked when a group/scene is renamed
     */
    public void onGroupRenamed(UDGroup group) {
        Freedomotic.logger.info("Scene: " + group.address + " was renamed to " + group.name);
    }

    /**
     * This method is invoked everytime there's a change in the state of a
     * control for a node (Insteon Device)
     */
    public void onModelChanged(UDControl control, Object value, UDNode node) {
        if (control == null || value == null || node == null) {
            return;
        }
        Freedomotic.logger.info("Someone or something changed " + ((control.label == null) ? control.name : control.label) + " to " + value + " at " + node.name);
        System.out.println("Someone or something changed " + ((control.label == null) ? control.name : control.label) + " to " + InsteonOps.convertOnLevelToPercent(value.toString()) + " at " + node.address + " type " + node.typeReadable + " uom " + control.numericUnit);
        ProtocolRead event = new ProtocolRead(this, "Isy99i", node.address); //IP:PORT:RELAYLINE
        int valuePercent = InsteonOps.convertOnLevelToPercent(value.toString());
        event.addProperty("type", node.typeReadable);
        // lighting support
        if (valuePercent > 0) {
            event.addProperty("isOn", "true");
        } else {
            event.addProperty("isOn", "false");
        }
        event.addProperty("value", Integer.toString(valuePercent));
        Isy99i.aux.notifyIsyEvent(event);
    }

    /**
     * This method is invoked when the network is renamed. Network is the top
     * most node in the tree in our applet
     */
    public void onNetworkRenamed(String newName) {
        System.out.println("Ah, the network was renamed to " + newName);
    }

    /**
     * This method is called when a new group/scene has been created
     */
    public void onNewGroup(UDGroup newGroup) {
        System.out.println("Yummy: we now have a new scene with address " + newGroup.address + " and name " + newGroup.name);
    }

    /**
     * This method is called when a new node (Insteon Device) has been added
     */
    public void onNewNode(UDNode newNode) {
        System.out.println("Yummy: we now have a new Insteon device with address " + newNode.address + " and name " + newNode.name);

    }

    /**
     * This method is called when an Insteon Device does not correctly
     * communicate with ISY
     */
    public void onNodeError(UDNode node) {
        System.out.println("What's going on? The Insteon device at address " + node.address + " and name " + node.name + " is no longer responding to my communication attempts!");

    }

    /**
     * This method is called with a node is enabled or disabled
     *
     * @param node
     * @param b
     */
    public void onNodeEnabled(UDNode node, boolean b) {
        Freedomotic.logger.info(String.format("Node %s is now %s", node.name, b ? "enabled" : "disabled"));
    }

    /**
     * This method is called when a node (Insteon Device) has been permanently
     * removed from ISY
     */
    public void onNodeRemoved(String nodeAddress) {
        System.out.println("Whooah ... node with address " + nodeAddress + " was permanently removed from ISY");

    }

    /**
     * This method is called when a node (Insteon Device) is removed from a
     * scene
     */
    public void onNodeRemovedFromGroup(UDNode node, UDGroup group) {
        Freedomotic.logger.info("Insteon device with address " + node.address + " and name " + node.name + " is no longer part of the " + group.name + " scene!");

    }

    /**
     * This method is called when a node's role changes in the given group
     * (master/slave role)
     */
    public void onNodeToGroupRoleChanged(UDNode node, UDGroup group, char new_role) {
        Freedomotic.logger.info("Insteon device with address " + node.address + " now has a new role in group with address " + group.address + " : ");
        if (new_role == Constants.UD_LINK_MODE_MASTER) {
            System.out.println("Controller/Master");
        } else {
            System.out.println("Responder/Slave");
        }
    }

    /**
     * This method is invoked when a node (Insteon Device) is renamed
     */
    public void onNodeRenamed(UDNode node) {
        Freedomotic.logger.info("Insteon device with address " + node.address + " was renamed to " + node.name);

    }

    /**
     * This method is invoked when a node (Insteon Device) has been moved to a
     * scene as controller/master
     */
    public void onNodeMovedAsMaster(UDNode node, UDGroup group) {
        Freedomotic.logger.info("Insteon device " + node.name + " is now part of the " + group.name + " scene as a master/controller");

    }

    /**
     * This method is invoked when a node (Insteon Device) has been moved to a
     * scene as responder/slave
     */
    public void onNodeMovedAsSlave(UDNode node, UDGroup group) {
        Freedomotic.logger.info("Insteon device " + node.name + " is now part of the " + group.name + " scene as a slave/responder");

    }

    /**
     * This method is invoked with the library does not receive announcements
     * from ISY and considers it offline
     */
    public void onDeviceOffLine() {
        Freedomotic.logger.info("ISY is offLine. Did you unplug it?");

    }

    /**
     * This method is invoked when a currently known ISY (UDProxyDevice) is back
     * on line
     */
    public void onDeviceOnLine() {
        Freedomotic.logger.info("ISY is online ...");
        final UDProxyDevice device = getDevice();
        if (device == null) {
            return;
        }
        if (device.isSecurityEnabled() || device.securityLevel > UPnPSecurity.NO_SECURITY) {
            if (device.isAuthenticated && device.isOnline) {
                return;
            }
            try {
                Freedomotic.logger.info("AUTHENICATING/SUBSCRIBING");
                Isy99iFrame.writeAreaLog(Isy99iUtilities.getDateTime() +": Authenticating/Subscribing");
                // passing user/pass
                authenticate("admin", "admin");
                Freedomotic.logger.info("AUTHENICATING/SUBSCRIBING DONE");
                Isy99iFrame.writeAreaLog(Isy99iUtilities.getDateTime() + ": Authenticating/Subscribing done");
            } catch (NoDeviceException e) {
                Freedomotic.logger.severe("Authenticating/Subscribing error");
                Isy99iFrame.writeAreaLog(Isy99iUtilities.getDateTime() + ": Authenticating/Subscribing error");
            }
        } else {
            //just subscribe to events
            Freedomotic.logger.info("Subscribing");
            Isy99iFrame.writeAreaLog(Isy99iUtilities.getDateTime() + ": Subscribing");
            device.subscribeToEvents(true);
            Freedomotic.logger.info("Subscription done");
            Isy99iFrame.writeAreaLog(Isy99iUtilities.getDateTime() + ": Subscription done");
        
        }
    }

    /**
     * This method is invoked when the state of the system (whether or not busy)
     * is changed
     *
     * @param busy - whether or not ISY is busy
     */
    public void onSystemStatus(boolean busy) {
        if (busy) {
            System.out.println("I am busy now; please give me some reprieve and don't ask me for more!");
        } else {
            System.out.println("I am ready and at your service");
        }
    }

    /**
     * This method is invoked when internet access is disabled on ISY
     */
    public void onInternetAccessDisabled() {
        System.out.println("You can no longer reach me through the internet");

    }

    /**
     * This method is invoked with internet access is enabled on ISY
     *
     * @param url - the external fully qualified url through which ISY can be
     * accessed
     */
    public void onInternetAccessEnabled(String url) {
        System.out.println("You can now reach me remotely at: " + url);

    }

    /**
     * This method is invoked when trigger status changes
     *
     * @param arg1 - the status
     * @param arg2 - extra information
     */
    public void onTriggerStatus(String arg1, XMLElement arg2) {
        System.out.println("Trigger status changed: " + arg1);

    }

    public void onDeviceSpecific(String arg1, String node, XMLElement arg2) {
        System.out.println("Device Specific action: ");
        System.out.println(arg2.toString());

    }

    public void onProgress(String arg1, XMLElement arg2) {
        System.out.println("Progress Report:");
        System.out.println(arg2.toString());
    }

    /**
     * Implement any cleanup Routines necessary here
     */
    @Override
    public void cleanUp() {
        System.out.println("Clean up whatever other static objects you have around");

    }

    @Override
    public void onSystemConfigChanged(String event, XMLElement eventInfo) {
        System.out.println("System configuration changed");

    }

    @Override
    public void onFolderRemoved(String folderAddress) {
        System.out.println(String.format("Folder removed %s", folderAddress));


    }

    @Override
    public void onFolderRenamed(UDFolder folder) {
        System.out.println(String.format("Folder renamed %s, new name %s", folder.address, folder.name));

    }

    @Override
    public void onNewFolder(UDFolder folder) {
        System.out.println(String.format("New Folder %s, name %s", folder.address, folder.name));

    }

    @Override
    public void onNodeHasPendingDeviceWrites(UDNode node, boolean hasPending) {
        System.out.println(String.format("Node %s, %s pending device writes", node.name, hasPending ? "has" : "does not have"));

    }

    @Override
    public void onNodeIsWritingToDevice(UDNode node, boolean isWriting) {
        System.out.println(String.format("Node %s, %s being programmed", node.name, isWriting ? "is" : "is not"));

    }

    @Override
    public void onNodeParentChanged(UDNode node, UDNode newParent) {
        System.out.println(String.format("Node %s, has new parent %s", node.name, newParent.name));

    }

    @Override
    public void onNodePowerInfoChanged(UDNode node) {
        System.out.println("Not supported ");

    }

    /*
     * (non-Javadoc) @see
     * com.universaldevices.device.model.IModelChangeListener#onNodeDeviceIdChanged(com.universaldevices.upnp.UDProxyDevice,
     * com.universaldevices.device.model.UDNode)
     */
    @Override
    public void onNodeDeviceIdChanged(UDProxyDevice device, UDNode node) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc) @see
     * com.universaldevices.device.model.IModelChangeListener#onNodeDevicePropertiesRefreshed(com.universaldevices.upnp.UDProxyDevice,
     * com.universaldevices.device.model.UDNode)
     */
    @Override
    public void onNodeDevicePropertiesRefreshed(UDProxyDevice device,
            UDNode node) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc) @see
     * com.universaldevices.device.model.IModelChangeListener#onNodeDevicePropertiesRefreshedComplete(com.universaldevices.upnp.UDProxyDevice)
     */
    @Override
    public void onNodeDevicePropertiesRefreshedComplete(
            UDProxyDevice proxyDevice) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc) @see
     * com.universaldevices.device.model.IModelChangeListener#onNodeDevicePropertyChanged(com.universaldevices.upnp.UDProxyDevice,
     * com.universaldevices.device.model.UDNode,
     * com.universaldevices.common.properties.UDProperty)
     */
    @Override
    public void onNodeDevicePropertyChanged(UDProxyDevice device, UDNode node,
            UDProperty<?> property) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc) @see
     * com.universaldevices.device.model.IModelChangeListener#onNodeRevised(com.universaldevices.upnp.UDProxyDevice,
     * com.universaldevices.device.model.UDNode)
     */
    @Override
    public void onNodeRevised(UDProxyDevice device, UDNode node) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc) @see
     * com.universaldevices.device.model.IModelChangeListener#onNodeErrorCleared(com.universaldevices.upnp.UDProxyDevice,
     * com.universaldevices.device.model.UDNode)
     */
    @Override
    public void onNodeErrorCleared(UDProxyDevice arg0, UDNode arg1) {
        // TODO Auto-generated method stub
    }
}
