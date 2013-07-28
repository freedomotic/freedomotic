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

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import it.freedomotic.api.Client;
import it.freedomotic.api.EventTemplate;

import it.freedomotic.bus.CommandChannel;
import it.freedomotic.bus.EventChannel;

import it.freedomotic.core.BehaviorManager;
import it.freedomotic.core.JoinDevice;
import it.freedomotic.core.JoinPlugin;
import it.freedomotic.core.TriggerCheck;

import it.freedomotic.environment.EnvironmentDAO;
import it.freedomotic.environment.EnvironmentDAOFactory;
import it.freedomotic.environment.EnvironmentLogic;
import it.freedomotic.environment.EnvironmentPersistence;

import it.freedomotic.events.PluginHasChanged;
import it.freedomotic.events.PluginHasChanged.PluginActions;

import it.freedomotic.exceptions.DaoLayerException;
import it.freedomotic.exceptions.FreedomoticException;
import it.freedomotic.exceptions.PluginLoadingException;
import it.freedomotic.marketplace.ClassPathUpdater;
import it.freedomotic.marketplace.IPluginCategory;
import it.freedomotic.marketplace.MarketPlaceService;

import it.freedomotic.model.ds.ColorList;
import it.freedomotic.model.ds.Config;
import it.freedomotic.model.environment.Environment;

import it.freedomotic.objects.EnvObjectPersistence;

import it.freedomotic.plugins.ClientStorage;
import it.freedomotic.plugins.filesystem.PluginLoaderFilesystem;

import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.CommandPersistence;
import it.freedomotic.reactions.ReactionPersistence;
import it.freedomotic.reactions.TriggerPersistence;

import it.freedomotic.serial.SerialConnectionProvider;

import it.freedomotic.util.Info;
import it.freedomotic.util.LogFormatter;
import it.freedomotic.util.I18n;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
    private static String INSTANCE_ID;
    public static final Logger logger = Logger.getLogger("app.log");
    //TODO: remove this, any plugin should have access to his own bus instance
    private static EventChannel eventChannel = new EventChannel();
    private static CommandChannel commandChannel = new CommandChannel();
    public static ArrayList<IPluginCategory> onlinePluginCategories;
    /**
     * Should NOT be used. Reserved for una tantum internal freedomotic core use
     * only!!
     */
    public static final Injector INJECTOR = Guice.createInjector(new FreedomoticDI());
    //dependencies
    private final EnvironmentDAOFactory environmentDaoFactory;
    private final ClientStorage clientStorage;
    private final PluginLoaderFilesystem pluginsLoader;

    /**
     *
     * @param pluginsLoader
     * @param joinDevice
     * @param joinPlugin
     */
    @Inject
    public Freedomotic(PluginLoaderFilesystem pluginsLoader, EnvironmentDAOFactory environmentDaoFactory,
            ClientStorage clientStorage) {
        this.pluginsLoader = pluginsLoader;
        this.environmentDaoFactory = environmentDaoFactory;
        this.clientStorage = clientStorage;
    }

    public void start()
            throws FreedomoticException {
        /**
         * ******************************************************************
         * First of all the configuration file is loaded into a data structure
         * *****************************************************************
         */
        loadAppConfig();

        String resourcesPath =
                new File(Info.getApplicationPath()
                + Freedomotic.config.getStringProperty("KEY_RESOURCES_PATH", "/build/classes/it/freedom/resources/")).getPath();
        logger.info("\nOS: " + System.getProperty("os.name") + "\n" + I18n.msg("architecture") + ": "
                + System.getProperty("os.arch") + "\n" + "OS Version: " + System.getProperty("os.version")
                + "\n" + I18n.msg("user") + ": " + System.getProperty("user.name") + "\n" + "Java Home: "
                + System.getProperty("java.home") + "\n" + "Java Library Path: {"
                + System.getProperty("java.library.path") + "}\n" + "Program path: "
                + System.getProperty("user.dir") + "\n" + "Java Version: " + System.getProperty("java.version")
                + "\n" + "Resources Path: " + resourcesPath);
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

                FileHandler handler = new FileHandler(logfile.getAbsolutePath(),
                        false);
                handler.setFormatter(new LogFormatter());
                logger.setLevel(Level.ALL);
                logger.addHandler(handler);
                logger.config(I18n.msg("INIT_MESSAGE"));

                if ((Freedomotic.config.getBooleanProperty("KEY_LOGGER_POPUP", true) == true)
                        && (java.awt.Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))) {
                    java.awt.Desktop.getDesktop()
                            .browse(new File(Info.getApplicationPath() + "/log/freedomotic.html").toURI());
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
            pluginsLoader.loadPlugins(PluginLoaderFilesystem.PLUGIN_TYPE_EVENT);
        } catch (PluginLoadingException ex) {
            Freedomotic.logger.log(Level.WARNING,
                    "Cannot load event plugin {0}. {1}",
                    new Object[]{ex.getPluginName(), ex.getMessage()});
        }

        /**
         * ******************************************************************
         * Dynamically load objects jar files in /plugin/objects folder
         * *****************************************************************
         */
        try {
            pluginsLoader.loadPlugins(PluginLoaderFilesystem.PLUGIN_TYPE_OBJECT);
        } catch (PluginLoadingException ex) {
            Freedomotic.logger.log(Level.WARNING,
                    "Cannot load object plugin {0}. {1}",
                    new Object[]{ex.getPluginName(), ex.getMessage()});
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
        /**
         * ******************************************************************
         * Loads sensors and actuators This must be loaded before object
         * deserialization because objects can user hardware level commands and
         * trigger that are loaded at this stage
         * *****************************************************************
         */
        try {
            pluginsLoader.loadPlugins(PluginLoaderFilesystem.PLUGIN_TYPE_DEVICE);
        } catch (PluginLoadingException ex) {
            Freedomotic.logger.warning("Cannot load device plugin " + ex.getPluginName() + ": " + ex.getMessage());
        }

        /**
         * ******************************************************************
         * Deserialize objects from XML
         * *****************************************************************
         */
        // REMOVED: now it's up to EnvironmentPersistence to load objects.
        // EnvObjectPersistence.loadObjects(EnvironmentPersistence.getEnvironments().get(0).getObjectFolder(), false);
        loadDefaultEnvironment();

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

        for (Client plugin : clientStorage.getClients()) {
            String startupTime = plugin.getConfiguration().getStringProperty("startup-time", "undefined");

            if (startupTime.equalsIgnoreCase("on load")) {
                plugin.start();

                PluginHasChanged event = new PluginHasChanged(this,
                        plugin.getName(),
                        PluginActions.DESCRIPTION);
                sendEvent(event);

                double snapshot = (((runtime.totalMemory() - runtime.freeMemory()) / MB) - memory);
                logger.config(plugin.getName() + " uses " + snapshot + "MB of memory");
                memory += snapshot;
            }
        }

        logger.config("Used Memory:" + ((runtime.totalMemory() - runtime.freeMemory()) / MB));
        logger.info("FREEDOMOTIC IS STARTED AND READY");
    }

    public void loadDefaultEnvironment()
            throws FreedomoticException {
        String envFilePath = config.getProperty("KEY_ROOM_XML_PATH");
        File envFile = new File(Info.PATH_DATA_FOLDER + "/furn/" + envFilePath);
        File folder = envFile.getParentFile();

        if ((folder == null) || !folder.exists() || !folder.isDirectory()) {
            throw new FreedomoticException("Environment data folder (furn) is missing in " + Info.PATH_DATA_FOLDER);
        }

        try {
            //EnvironmentPersistence.loadEnvironmentsFromDir(folder, false);
            EnvironmentDAO loader = environmentDaoFactory.create(folder);
            Environment loaded = loader.load();
            EnvironmentLogic logic = new EnvironmentLogic();

            if (loaded == null) {
                throw new IllegalStateException("Object data cannot be null at this stage");
            }

            logic.setPojo(loaded);
            logic.setSource(folder);
            EnvironmentPersistence.add(logic, false);
        } catch (DaoLayerException e) {
            throw new FreedomoticException(e.getMessage(), e);
        }
    }

    public static String getInstanceID() {
        return INSTANCE_ID;
    }

    public void loadGraphics(Dimension dimension) {
        new ColorList();
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
     * Main entry point of freedomotic. All starts from here.
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

        try {
            Freedomotic freedomotic = INJECTOR.getInstance(Freedomotic.class);
            //start freedomotic
            freedomotic.start();
        } catch (FreedomoticException ex) {
            Freedomotic.logger.severe(ex.getMessage());
            System.exit(1);
        }
    }

    public static void onExit() {
        Freedomotic.logger.info("Exiting application...");
        //Freedomotic.logger.info("Sending the exit signal (TODO: not yet implemented)");
        //...send the signal on a topic channel
        //TODO: regression, enable it again
//        for (Client plugin : clientStorage.getClients()) {
//            plugin.stop();
//        }
        //AbstractBusConnector.disconnect();
        ConfigPersistence.serialize(config,
                new File(Info.getApplicationPath() + "/config/config.xml"));

        //save changes to object in the default test environment
        //on error there is a copy (manually created) of original test environment in the data/furn folder
        if (Freedomotic.config.getBooleanProperty("KEY_OVERRIDE_OBJECTS_ON_EXIT", false) == true) {
            try {
                EnvObjectPersistence.saveObjects(EnvironmentPersistence.getEnvironments().get(0).getObjectFolder());
            } catch (DaoLayerException ex) {
                Freedomotic.logger.severe("Cannot save objects in "
                        + EnvironmentPersistence.getEnvironments().get(0).getObjectFolder());
            }
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
        String environmentFilePath =
                Info.getApplicationPath() + "/data/furn/" + Freedomotic.config.getProperty("KEY_ROOM_XML_PATH");
        File folder = null;

        try {
            folder = new File(environmentFilePath).getParentFile();
            EnvironmentPersistence.saveEnvironmentsToFolder(folder);
        } catch (DaoLayerException ex) {
            Freedomotic.logger.severe("Cannot save environment to folder " + folder);
        }

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
