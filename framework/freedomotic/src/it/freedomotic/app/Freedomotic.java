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

import it.freedomotic.reactions.CommandPersistence;
import it.freedomotic.reactions.ReactionPersistence;
import it.freedomotic.reactions.TriggerPersistence;
import it.freedomotic.environment.EnvironmentPersistence;
import it.freedomotic.objects.EnvObjectPersistence;
import it.freedomotic.api.Client;
import it.freedomotic.api.EventTemplate;
import it.freedomotic.bus.CommandChannel;
import it.freedomotic.bus.EventChannel;
import it.freedomotic.core.BehaviorManager;
import it.freedomotic.core.JoinDevice;
import it.freedomotic.core.JoinPlugin;
import it.freedomotic.environment.EnvironmentLogic;
import it.freedomotic.events.PluginHasChanged;
import it.freedomotic.events.PluginHasChanged.PluginActions;
import java.io.File;
import java.io.IOException;




import it.freedomotic.plugins.AddonLoader;
import it.freedomotic.model.ds.ColorList;
import it.freedomotic.model.ds.Config;

import it.freedomotic.plugins.ClientStorage;
import it.freedomotic.reactions.Command;
import it.freedomotic.service.ClassPathUpdater;
import it.freedomotic.util.Info;
import it.freedomotic.util.LogFormatter;
import it.freedomotic.serial.SerialConnectionProvider;
import it.freedomotic.service.IPluginCategory;
import it.freedomotic.service.MarketPlaceService;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the starting class of the project
 *
 * @author Enrico Nicoletti
 */
public class Freedomotic {

    public static Config config;
//    public static ArrayList<EnvironmentLogic> environmentAreas;
//    public static EnvironmentLogic environment;
    public static ClientStorage clients;
    private static String INSTANCE_ID;
    public static final Logger logger = Logger.getLogger("app.log");
    private static EventChannel eventChannel;
    private static CommandChannel commandChannel;
    public static ArrayList<IPluginCategory> onlinePluginCategories;
    private static ExecutorService executor = Executors.newCachedThreadPool();

    public Freedomotic() {
        /**
         * ******************************************************************
         * First of all the configuration file is loaded into a data structure
         * *****************************************************************
         */
        loadAppConfig();
        String resourcesPath = new File(Info.getApplicationPath() + Freedomotic.config.getStringProperty("KEY_RESOURCES_PATH", "/build/classes/it/freedom/resources/")).getPath();
        logger.info("\nOS: " + System.getProperty("os.name") + "\n"
                + "Architecture: " + System.getProperty("os.arch") + "\n"
                + "OS Version: " + System.getProperty("os.version") + "\n"
                + "Utente: " + System.getProperty("user.name") + "\n"
                + "Java Home: " + System.getProperty("java.home") + "\n"
                + "Java Library Path: {" + System.getProperty("java.library.path") + "}\n"
                + "Program path: " + System.getProperty("user.dir") + "\n"
                + "Java Version: " + System.getProperty("java.version") + "\n"
                + "Resources Path: " + resourcesPath);

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
                logger.setLevel(Level.ALL);
                logger.addHandler(handler);
                logger.config("Freedomotic startup begins. (Press F5 to read the other messages)");
                if ((Freedomotic.config.getBooleanProperty("KEY_LOGGER_POPUP", true) == true)
                        && (java.awt.Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))) {
                    java.awt.Desktop.getDesktop().browse(new File(Info.getApplicationPath() + "/log/freedomotic.html").toURI());
                }
            } catch (IOException ex) {
                Logger.getLogger(Freedomotic.class.getName()).log(Level.SEVERE, null, ex);
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
            try {
                java.awt.Desktop.getDesktop().browse(new URI("www.freedomotic.com"));
            } catch (URISyntaxException ex) {
                Logger.getLogger(Freedomotic.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Freedomotic.class.getName()).log(Level.SEVERE, null, ex);
            }

        }


        /**
         * ******************************************************************
         * Dynamically load events jar files in /plugin/events folder
         * *****************************************************************
         */
        try {
            AddonLoader.load(new File(Info.PATH_PLUGINS_FOLDER + "/events/"));
        } catch (Exception ex) {
            Logger.getLogger(Freedomotic.class.getName()).log(Level.SEVERE, null, ex);
        }

        /**
         * ******************************************************************
         * Dynamically load objects jar files in /plugin/objects folder
         * *****************************************************************
         */
        try {
            AddonLoader.recursiveSearchIn(new File(Info.PATH_PLUGINS_FOLDER + "/objects/"));
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
                                onlinePluginCategories = mps.getCategoryList();
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
         * Deserialize the default environment (its shape + zones)
         * *****************************************************************
         */
        loadDefaultEnvironment();

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
        // REMOVED: now it's up to EnvironmentPersistence to load objects.
       // EnvObjectPersistence.loadObjects(EnvironmentPersistence.getEnvironments().get(0).getObjectFolder(), false);
        /**
         * ******************************************************************
         * Init frontends sending an object changed behavior event
         * *****************************************************************
         */
//        for (EnvObjectLogic object : EnvObjectPersistence.getObjectList()) {
//            ObjectHasChangedBehavior event = new ObjectHasChangedBehavior(
//                    this,
//                    object);
//            sendEvent(event);
//        }
        /**
         * ******************************************************************
         * Updating zone object list
         * *****************************************************************
         */
//        Freedomotic.logger.config("---- Checking zones topology ----");
//        for (ZoneLogic z : environment.getZones()) {
//            z.checkTopology();
//        }
        /**
         * ******************************************************************
         * Loads the entire Reactions system (Trigger + Commands + Reactions)
         * *****************************************************************
         */
        TriggerPersistence.loadTriggers(new File(Info.PATH_DATA_FOLDER + "/trg/"));
        CommandPersistence.loadCommands(new File(Info.PATH_DATA_FOLDER + "/cmd/"));
        ReactionPersistence.loadReactions(new File(Info.PATH_DATA_FOLDER + "/rea/"));


        /**
         * A service to add environment objects using XML commands
         */
        new JoinDevice();
        new JoinPlugin();
        new BehaviorManager();
        new SerialConnectionProvider();


        /**
         * ******************************************************************
         * Starting plugins
         * *****************************************************************
         */
        double MB = 1024 * 1024;
        Runtime runtime = Runtime.getRuntime();
        double memory = ((runtime.totalMemory() - runtime.freeMemory()) / MB);
        logger.config("Freedomotic + data uses " + memory + "MB");
        for (Client plugin : ClientStorage.getClients()) {
            String startupTime = plugin.getConfiguration().getStringProperty("startup-time", "undefined");
            if (startupTime.equalsIgnoreCase("on load")) {
                plugin.start();
                PluginHasChanged event = new PluginHasChanged(this, plugin.getName(), PluginActions.DESCRIPTION);
                sendEvent(event);
                double snapshot = (((runtime.totalMemory() - runtime.freeMemory()) / MB) - memory);
                logger.config(plugin.getName() + " uses " + snapshot + "MB of memory");
                memory += snapshot;
            }
        }

        logger.config("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / MB);
    }

    public static void loadDefaultEnvironment() {
        try {
            String envFolder = config.getProperty("KEY_ROOM_XML_PATH").substring(0, config.getProperty("KEY_ROOM_XML_PATH").lastIndexOf("/") );
            File folder = new File(Info.getApplicationPath() + "/data/furn/" + envFolder);
            EnvironmentPersistence.loadEnvironmentsFromDir(folder, false);
        } catch (Exception e) {
            Freedomotic.logger.severe(getStackTraceInfo(e));
        }
    }

    public static String getInstanceID() {
        return INSTANCE_ID;
    }

    public void loadGraphics(Dimension dimension) {
        new ColorList();
    }

    private void loadPlugins() {
        try {
            File pluginFolder = new File(Info.PATH_PLUGINS_FOLDER + "/devices/");
            AddonLoader.recursiveSearchIn(pluginFolder);
        } catch (Exception e) {
            Freedomotic.logger.warning("Error while loading this plugin: " + e.getMessage());
            Freedomotic.logger.severe(getStackTraceInfo(e));
        }
    }

    private void loadAppConfig() {
        try {
            config = ConfigPersistence.deserialize(new File(Info.getApplicationPath() + "/config/config.xml"));
        } catch (IOException ex) {
            Logger.getLogger(Freedomotic.class.getName()).log(Level.SEVERE, null, ex);
        }
        Freedomotic.logger.info(config.toString());
    }

    public static void sendEvent(EventTemplate event) {
        eventChannel.send(event);
    }

    public static Command sendCommand(final Command command) {
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
        //Freedomotic.logger.info("Sending the exit signal (TODO: not yet implemented)");
        //...send the signal on a topic channel
        for (Client plugin : ClientStorage.getClients()) {
            plugin.stop();
        }
        //AbstractBusConnector.disconnect();
        ConfigPersistence.serialize(config, new File(Info.getApplicationPath() + "/config/config.xml"));

        //save changes to object in the default test environment
        //on error there is a copy (manually created) of original test environment in the data/furn folder
        if (Freedomotic.config.getBooleanProperty("KEY_OVERRIDE_OBJECTS_ON_EXIT", false) == true) {
            EnvObjectPersistence.saveObjects(EnvironmentPersistence.getEnvironments().get(0).getObjectFolder());
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

        //save the environment
        String environmentFilePath = Info.getApplicationPath() + "/data/furn/" + Freedomotic.config.getProperty("KEY_ROOM_XML_PATH");
        EnvironmentPersistence.saveEnvironmentsToFolder(new File(environmentFilePath).getParentFile());
        Freedomotic.logger.info(Profiler.print());
        Profiler.saveToFile();
        System.exit(0);
    }

    public static void kill() {
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
