/*Copyright 2009 Enrico Nicoletti
 eMail: enrico.nicoletti84@gmail.com

 This file is part of Freedomotic.

 Freedomotic is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 any later version.

 Freedomotic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with EventEngine; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.freedomotic.app;

import it.freedomotic.api.Client;
import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Plugin;
import it.freedomotic.bus.AbstractBusConnector;
import it.freedomotic.bus.CommandChannel;
import it.freedomotic.bus.EventChannel;
import it.freedomotic.core.BehaviorManagerForObjects;
import it.freedomotic.core.JoinDevice;
import it.freedomotic.core.Profiler;
import it.freedomotic.environment.EnvironmentLogic;
import it.freedomotic.environment.ZoneLogic;
import it.freedomotic.events.ObjectHasChangedBehavior;
import it.freedomotic.events.PluginHasChanged;
import it.freedomotic.events.PluginHasChanged.PluginActions;
import java.io.File;
import java.io.IOException;


import java.util.List;


import it.freedomotic.plugins.AddonLoader;
import it.freedomotic.model.ds.ColorList;
import it.freedomotic.model.ds.Config;
import it.freedomotic.objects.EnvObjectLogic;

import it.freedomotic.persistence.*;
import it.freedomotic.reactions.Reaction;
import it.freedomotic.plugins.ClientStorage;
import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.Trigger;
import it.freedomotic.service.ClassPathUpdater;
import it.freedomotic.util.Info;
import it.freedomotic.util.InternetBrowser;
import it.freedomotic.util.LogFormatter;
import it.freedomotic.serial.SerialConnectionProvider;
import it.freedomotic.service.MarketPlaceService;
import it.freedomotic.service.PluginPackage;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the starting class of the project
 *
 * @author Enrico Nicoletti
 */
public final class Freedomotic {

    public static Config config;
    public static EnvironmentLogic environment;
    public static ClientStorage clients;
    private static String INSTANCE_ID;
    public static final Logger logger = Logger.getLogger("app.log");
    private static EventChannel eventChannel;
    private static CommandChannel commandChannel;
    public static ArrayList<PluginPackage> onlinePlugins;


    public Freedomotic() {
        /**
         * ******************************************************************
         * First of all the configuration file is loaded into a data structure
         * *****************************************************************
         */
        loadAppConfig();
        logger.setLevel(Level.ALL);
        logger.info("OS: " + System.getProperty("os.name") + "\n"
                + "Architecture: " + System.getProperty("os.arch") + "\n"
                + "OS Version: " + System.getProperty("os.version") + "\n"
                + "Utente: " + System.getProperty("user.name") + "\n"
                + "Java Home: " + System.getProperty("java.home") + "\n"
                + "Java Library Path: {" + System.getProperty("java.library.path") + "}\n"
                + "Program path: " + System.getProperty("user.dir") + "\n"
                + "Java Version: " + System.getProperty("java.version"));
        String resourcesPath = new File(Info.getApplicationPath() + Freedomotic.config.getStringProperty("KEY_RESOURCES_PATH", "/build/classes/it/freedom/resources/")).getPath();
        Freedomotic.logger.info("Resources Path: " + resourcesPath);



        eventChannel = new EventChannel();
        commandChannel = new CommandChannel();
        new ColorList(); //initialize an ordered list of colors used for various purposes, eg: people colors

        /**
         * ******************************************************************
         * Starting the logger and popup it in the browser
         * *****************************************************************
         */
        if (Freedomotic.config.getBooleanProperty("KEY_SAVE_LOG_TO_FILE", false)) {
            try {
                File logdir = new File(Info.getApplicationPath() + "/log/");
                logdir.mkdir();
                File logfile = new File(logdir + "/freedomotic.html");
                logfile.createNewFile();
                FileHandler handler = new FileHandler(logfile.getAbsolutePath(), false);
                handler.setFormatter(new LogFormatter());
                logger.addHandler(handler);
                logger.info("Freedomotic startup begins. (Press F5 to read the other messages)");
                if ((Freedomotic.config.getBooleanProperty("KEY_LOGGER_POPUP", true) == true) && 
                        (java.awt.Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))){
                    java.awt.Desktop.getDesktop().browse(new File(Info.getApplicationPath() + "/log/freedomotic.html").toURI());
                }
            } catch (IOException ex) {
                Logger.getLogger(AbstractBusConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /**
         * ******************************************************************
         * Create data backup folder (FEATURE DISABLED!!!)
         * *****************************************************************
         */
//        if (Freedomotic.config.getBooleanProperty("KEY_BACKUP_DATA_BEFORE_START", true) == true) {
//            try {
//                CopyFile.copy(new File(Info.getDatafilePath()), new File(Info.getApplicationPath() + "/backup"));
//            } catch (Exception ex) {
//                logger.warning("unable to create a backup copy of application data " + getStackTraceInfo(ex));
//            }
//        }
        /**
         * ******************************************************************
         * Init the plugin storage
         * *****************************************************************
         */
        clients = new ClientStorage();

        /**
         * ******************************************************************
         * Shows the freedomotic website if stated in the config file
         * *****************************************************************
         */
        if (Freedomotic.config.getBooleanProperty("KEY_SHOW_WEBSITE_ON_STARTUP", false)) {
            new InternetBrowser("http://freedomotic.com/");
        }


        /**
         * ******************************************************************
         * Dynamically load events jar files in /plugin/events folder
         * *****************************************************************
         */
        AddonLoader eventsLoader = new AddonLoader();
        try {
            eventsLoader.searchIn(new File(Info.getPluginsPath() + "/events/"));
        } catch (Exception ex) {
            Logger.getLogger(Freedomotic.class.getName()).log(Level.SEVERE, null, ex);
        }

        /**
         * ******************************************************************
         * Dynamically load objects jar files in /plugin/objects folder
         * *****************************************************************
         */
        AddonLoader objectsLoader = new AddonLoader();
        try {
            objectsLoader.recursiveSearchIn(new File(Info.getPluginsPath() + "/objects/"));
        } catch (Exception ex) {
            Logger.getLogger(Freedomotic.class.getName()).log(Level.SEVERE, null, ex);
        }

        /**
         * ******************************************************************
         * Dynamically load jar files in /plugin/providers folder for plugins
         * *****************************************************************
         */
        try {
            ClassPathUpdater.add(new File(Info.getApplicationPath() + "/plugins/providers/"));
        } catch (Exception ex) {
            Logger.getLogger(Freedomotic.class.getName()).log(Level.SEVERE, null, ex);
        }
        /**
         * ******************************************************************
         * Cache online plugins
         * *****************************************************************
         */
        if (Freedomotic.config.getBooleanProperty("CACHE_MARKETPLACE_ON_STARTUP", false)) {
            try {
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        new Thread(new Runnable() {

                            @Override
                            public void run() {

                                Freedomotic.logger.info("Starting marketplace service");
                                MarketPlaceService mps = MarketPlaceService.getInstance();
                                onlinePlugins = mps.getPackageList();
                            }
                        }).start();
                    }
                });
            } catch (Exception e) {
                Freedomotic.logger.warning("Unable to cache plugins package from marketplace");
                Freedomotic.logger.warning(Freedomotic.getStackTraceInfo(e));
            }
        }


        /**
         * ******************************************************************
         * Autoloading of the right RXTX system file
         * *****************************************************************
         */
        new SerialConnectionProvider();

        /**
         * ******************************************************************
         * Deserialize the default environment (its shape + zones)
         * *****************************************************************
         */
        loadDefaultEnvironment();

        /**
         * ******************************************************************
         * Creates the Behavior managers
         * *****************************************************************
         */
        //it takes user level commands like 'turn on light 1' and map it to the right hardware level command eg: 'turn on an x10 device'
        new BehaviorManagerForObjects();

        /**
         * ******************************************************************
         * Loads sensors and actuators This must be loaded before object
         * deserialization because objects can user hardware level commands and
         * trigger that are loaded at this stage
         * *****************************************************************
         */
        loadPlugins();


        /**
         * ******************************************************************
         * Deserialize objects from XML
         * *****************************************************************
         */
        EnvObjectPersistence.loadObjects(environment.getObjectFolder(), false);

        /**
         * ******************************************************************
         * Init frontends sending an object changed behavior event
         * *****************************************************************
         */
        for (EnvObjectLogic object : EnvObjectPersistence.getObjectList()) {
            ObjectHasChangedBehavior event = new ObjectHasChangedBehavior(this, object.getPojo());
            sendEvent(event);
        }

        /**
         * ******************************************************************
         * Updating zone object list
         * *****************************************************************
         */
        Freedomotic.logger.config("---- Checking zones topology ----");
        for (ZoneLogic z : environment.getZones()) {
            z.checkTopology();
        }

        /**
         * ******************************************************************
         * Loads the entire Reactions system (Trigger + Commands + Reactions)
         * *****************************************************************
         */
        TriggerPersistence.loadTriggers(new File(Info.getDatafilePath() + "/trg/"));
        CommandPersistence.loadCommands(new File(Info.getDatafilePath() + "/cmd/"));
        ReactionPersistence.loadReactions(new File(Info.getDatafilePath() + "/rea/"));
        Freedomotic.logger.info("\nLoaded Reactions:");
        for (Iterator it = ReactionPersistence.iterator(); it.hasNext();) {
            Reaction r = (Reaction) it.next();
            Freedomotic.logger.info(r.toString());
        }

        /**
         * ******************************************************************
         * Starting plugins
         * *****************************************************************
         */
        for (Client plugin : clients.getClients()) {
            String startupTime = plugin.getConfiguration().getStringProperty("startup-time", "undefined");
            if (startupTime.equalsIgnoreCase("on load")) {
                plugin.start();
                PluginHasChanged event = new PluginHasChanged(this, plugin.getName(), PluginActions.DESCRIPTION);
                sendEvent(event);
            }
        }
        
        /**
         * A service to add environment objects using XML commands
         */
        new JoinDevice();

        Freedomotic.logger.info("---- FREEDOM IS READY TO WORK ----");

        /**
         * ******************************************************************
         * Logging a summary of loaded resources
         * *****************************************************************
         */
        Freedomotic.logger.info("-- Information Summary --");
        Freedomotic.logger.info("---- Loaded Triggers ----");
        Iterator itTrigger = TriggerPersistence.iterator();
        StringBuilder buffTrigger = new StringBuilder();

        while (itTrigger.hasNext()) {
            Trigger t = (Trigger) itTrigger.next();
            buffTrigger.append("'").append(t.getName()).append("' listening on channel ").append(t.getChannel()).append("; ");
        }
        buffTrigger.append("} ");
        Freedomotic.logger.info(buffTrigger.toString());

        Freedomotic.logger.info("---- Loaded Commands ----");
        Iterator itCommand = CommandPersistence.iterator();
        StringBuilder buffCommand = new StringBuilder();
        while (itCommand.hasNext()) {
            Command c = (Command) itCommand.next();
            buffCommand.append(c.getName()).append("\n");
        }
        buffCommand.append("} ");
        Freedomotic.logger.info(buffCommand.toString());

        Freedomotic.logger.info("---- Loaded Reactions ----");
        Iterator itReaction = ReactionPersistence.iterator();
        StringBuilder buffReaction = new StringBuilder();
        buffReaction.append("{ ");
        while (itReaction.hasNext()) {
            Reaction r = (Reaction) itReaction.next();
            buffReaction.append("'").append(r.toString()).append("; ");
        }
        buffReaction.append("} ");
        Freedomotic.logger.info(buffReaction.toString());

        /**
         * ******************************************************************
         * Init statistic logger
         * *****************************************************************
         */
        new Profiler();

    }

    public static void loadDefaultEnvironment() {
        try {
            File folder = new File(Info.getApplicationPath() + "/data/furn/" + config.getProperty("KEY_ROOM_XML_PATH"));
            loadEnvironment(folder);
        } catch (Exception e) {
            Freedomotic.logger.severe(getStackTraceInfo(e));
        }
    }

    public static boolean loadEnvironment(File folder) {
        boolean done = false;
        if (folder == null) {
            return false;
        }
        try {
            if (environment != null) {
                environment.clear();
            }
            environment = EnvironmentPersistence.load(folder);
            done = true;
        } catch (Exception e) {
            done = false;
            Freedomotic.logger.severe(getStackTraceInfo(e));
        }
        return done;
    }

    public static String getInstanceID() {
        return INSTANCE_ID;
    }

    public void loadGraphics(Dimension dimension) {
        new ColorList();
    }

    private void loadPlugins() {
        try {
            AddonLoader loader = new AddonLoader();
            File pluginFolder = new File(Info.getPluginsPath() + "/devices/");

            loader.recursiveSearchIn(pluginFolder);
        } catch (Exception e) {
            Freedomotic.logger.warning("Error while loading this plugin: " + e.getMessage());
            Freedomotic.logger.severe(getStackTraceInfo(e));
        }
    }

    private void loadAppConfig() {
        Freedomotic.logger.info("Loading App Configuration from " + new File(Info.getApplicationPath() + "/config/config.xml").toString());
        try {
            config = ConfigPersistence.deserialize(new File(Info.getApplicationPath() + "/config/config.xml"));
        } catch (IOException ex) {
            Logger.getLogger(Freedomotic.class.getName()).log(Level.SEVERE, null, ex);
        }
        Freedomotic.logger.info("Freedomotic starts with this configuration:\n" + "{" + config.toString() + "}");
    }

    public static void sendEvent(EventTemplate event) {
        eventChannel.send(event);
    }

    public static Command sendCommand(Command command) {
        return commandChannel.send(command);
    }

    /**
     * Punto di ingresso dell'intero programma
     *
     * @param args
     */
    public static void main(String[] args) {

        try {
            INSTANCE_ID = args[0];
        } catch (Exception e) {
            INSTANCE_ID = "";
        }
        if ((INSTANCE_ID == null) || (INSTANCE_ID.isEmpty())) {
            INSTANCE_ID = "A";
        }
        Freedomotic.logger.info("Freedomotic instance ID: " + INSTANCE_ID);

        new Freedomotic();
    }

    public static void onExit() {
        Freedomotic.logger.info("Exiting application...");
        ConfigPersistence.serialize(config, new File(Info.getApplicationPath() + "/config/config.xml"));

        //save changes to object in the default test environment
        //on error there is a copy (manually created) of original test environment in the data/furn folder
        if (Freedomotic.config.getBooleanProperty("KEY_OVERRIDE_OBJECTS_ON_EXIT", false) == true) {
            EnvObjectPersistence.saveObjects(Freedomotic.environment.getObjectFolder());
        }

        String savedDataRoot;
        if (Freedomotic.config.getBooleanProperty("KEY_OVERRIDE_REACTIONS_ON_EXIT", false) == true) {
            savedDataRoot = Info.getApplicationPath() + "/data";
        } else {
            savedDataRoot = Info.getApplicationPath() + "/testSave/data";
        }
        TriggerPersistence.saveTriggers(new File(savedDataRoot + "/trg"));
        CommandPersistence.saveCommands(new File(savedDataRoot + "/cmd"));
        ReactionPersistence.saveReactions(new File(savedDataRoot + "/rea"));

        try {
            //save the environment
            String environmentFilePath = Info.getApplicationPath() + "/data/furn/" + Freedomotic.config.getProperty("KEY_ROOM_XML_PATH");
            EnvironmentPersistence.save(new File(environmentFilePath));
        } catch (IOException ex) {
            Logger.getLogger(Freedomotic.class.getName()).log(Level.SEVERE, null, ex);
        }
        Freedomotic.logger.info("Sending the exit signal (TODO: not yet implemented)");
        //...send the signal on a topic channel
        Freedomotic.logger.info("Force stopping the plugins that are not already stopped");
        for (Client plugin : clients.getClients()) {
            plugin.stop();
        }
        Freedomotic.logger.info(Profiler.print());
        Profiler.saveToFile();
        Freedomotic.logger.info("DONE");
        System.exit(0);
    }
    
    public static void kill() {
        Freedomotic.logger.info("Raw kill is called... terminate immediately.");
        System.exit(0);
    }

    public static String getStackTraceInfo(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }
}
