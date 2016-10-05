#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
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
package ${package};

import ${groupId}.api.EventTemplate;
import ${groupId}.api.Protocol;
import ${groupId}.app.Freedomotic;
import ${groupId}.exceptions.UnableToExecuteException;
import ${groupId}.reactions.Command;
import java.io.IOException;
import java.util.logging.Logger;

//TODO: please rename this class!
public class HelloWorld extends Protocol {

    private static final Logger LOG = Logger.getLogger(HelloWorld.class.getName());
    final int POLLING_WAIT;

    public HelloWorld() {
        // Every plugin needs a name and a manifest XML file.
        super("${artifactId}", "/${artifactId}/manifest.xml");
        // Read a property from the manifest file below which is in.
        // FREEDOMOTIC_FOLDER/plugins/devices/${artifactId}/manifest.xml
        POLLING_WAIT = configuration.getIntProperty("time-between-reads", 2000);
        // POLLING_WAIT is the value of the property "time-between-reads" or 2000 millisecs,
        // default value if the property does not exist in the manifest.
        // It controls the interval in milliseconds between the calls of onRun() method.
        // If POLLING_WAIT=-1 onRun() is called just one time after plugin startup (not in a loop).
        setPollingWait(POLLING_WAIT);
        // IMPORTANT: Initialization operations should be done in the onStart() method not in this contructor.
        // DO NOT ADD CODE HERE!
    }

    @Override
    protected void onShowGui() {
        /**
         * Uncomment the line below to add a GUI to this plugin. The GUI can be
         * started with a right-click on plugin list in the desktop front-end.
         * A GUI is useful for example to configure this plugin at runtime.
         */
        //bindGuiToPlugin(new HelloWorldGui(this));
    }

    @Override
    protected void onHideGui() {
        // Implement here what to do when the plugin GUI is closed.
        // For example you can change the plugin description.
        setDescription("My GUI is now hidden");
        // Or stop the plugin programmatically.
        // this.stop();
    }

    @Override
    // This method is always executed in a separate Thread, there is NO NEED to create additional
    // threads inside it.
    protected void onRun() {
        LOG.info("${artifactId} onRun() logs this message every " + "POLLINGWAIT=" + POLLING_WAIT
                + "milliseconds");

        // At the end of this method the system waits POLLINGTIME before calling it again.
        // The result is that this log message is printed every 2 seconds (2000 millisecs).
    }

    @Override
    // Executed when this plugin is started.
    protected void onStart() {
        LOG.info("${artifactId} plugin is started");
    }

    @Override
    // Executed when this plugin is stopped.
    protected void onStop() {
        LOG.info("${artifactId} plugin is stopped ");
    }

    @Override
    // Receive commands from freedomotic.
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        LOG.info("${artifactId} plugin receives a command called " + c.getName() + " with parameters "
                + c.getProperties().toString());
    }

    @Override
    // Receive events from freedomotic.
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean canExecute(Command c) {
        // Don't mind this method for now.
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
