/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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
package com.freedomotic.app;

import com.freedomotic.api.API;
import com.freedomotic.api.Client;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.bus.BootStatus;
import com.freedomotic.bus.BusConsumer;
import com.freedomotic.bus.BusMessagesListener;
import com.freedomotic.bus.BusService;
import com.freedomotic.bus.StompDispatcher;
import com.freedomotic.core.BehaviorManager;
import com.freedomotic.environment.EnvironmentDAO;
import com.freedomotic.environment.EnvironmentDAOFactory;
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.EnvironmentPersistence;
import com.freedomotic.events.PluginHasChanged;
import com.freedomotic.events.PluginHasChanged.PluginActions;
import com.freedomotic.exceptions.DaoLayerException;
import com.freedomotic.exceptions.FreedomoticException;
import com.freedomotic.exceptions.PluginLoadingException;
import com.freedomotic.marketplace.ClassPathUpdater;
import com.freedomotic.marketplace.IPluginCategory;
import com.freedomotic.marketplace.MarketPlaceService;
import com.freedomotic.model.ds.ColorList;
import com.freedomotic.model.environment.Environment;
import com.freedomotic.objects.EnvObjectPersistence;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.filesystem.PluginsManager;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandPersistence;
import com.freedomotic.reactions.ReactionPersistence;
import com.freedomotic.reactions.TriggerPersistence;
import com.freedomotic.security.Auth;
import com.freedomotic.security.UserRealm;
import com.freedomotic.serial.SerialConnectionProvider;
import com.freedomotic.util.Info;
import com.freedomotic.util.LogFormatter;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
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
import java.util.Collection;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadState;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * This is the starting class of the project
 *
 * @author Enrico Nicoletti
 */
public class Freedomotic implements BusConsumer {

    /**
     *
     * @deprecated
     */
    @Deprecated
    public static final Logger logger = Logger.getLogger("com.freedomotic");
    //this should replace Freedomotic.logger reference
    private static final Logger LOG = Logger.getLogger(Freedomotic.class.getName());
    private static String INSTANCE_ID;

    /**
     *
     */
    public static ArrayList<IPluginCategory> onlinePluginCategories;
    /**
     * Should NOT be used. Reserved for una tantum internal freedomotic core use
     * only!!
     */
    public static final Injector INJECTOR = Guice.createInjector(new DependenciesInjector());
    //dependencies
    private final EnvironmentDAOFactory environmentDaoFactory;
    private final ClientStorage clientStorage;
    private final PluginsManager pluginsManager;
    private AppConfig config;
    private Auth auth;
    private API api;
    private BusMessagesListener listener;
    // TODO remove static modifier once static methods sendEvent & sendCommand are erased.
    private static BusService busService;

    /**
     *
     * @param pluginsLoader
     * @param joinDevice
     * @param joinPlugin
     * @param api
     */
    @Inject
    public Freedomotic(
            PluginsManager pluginsLoader,
            EnvironmentDAOFactory environmentDaoFactory,
            ClientStorage clientStorage,
            AppConfig config,
            API api) {
        this.pluginsManager = pluginsLoader;
        this.environmentDaoFactory = environmentDaoFactory;
        this.clientStorage = clientStorage;
        this.config = config;
        this.api = api;
        this.auth = api.getAuth();
    }

    /**
     *
     * @throws FreedomoticException
     */
    public void start() throws FreedomoticException {
        /**
         * ******************************************************************
         * First of all the configuration file is loaded into a data structure
         * *****************************************************************
         */
        loadAppConfig();

        // init localization
        api.getI18n().setDefaultLocale(config.getStringProperty("KEY_ENABLE_I18N", "no"));

        // init auth* framework
        auth.initBaseRealm();
        auth.load();
        if (auth.isInited()) {
            PrincipalCollection principals = new SimplePrincipalCollection("system", UserRealm.USER_REALM_NAME);
            Subject SysSubject = new Subject.Builder().principals(principals).buildSubject();
            SysSubject.getSession().setTimeout(-1);
            ThreadState threadState = new SubjectThreadState(SysSubject);
            threadState.bind();
            LOG.info("Booting as user:" + auth.getSubject().getPrincipal() +". Session will last:"+auth.getSubject().getSession().getTimeout());
        }

        String resourcesPath =
                new File(Info.getApplicationPath()
                + config.getStringProperty("KEY_RESOURCES_PATH", "/build/classes/it/freedom/resources/")).getPath();
        LOG.info("\nOS: " + System.getProperty("os.name") + "\n" + api.getI18n().msg("architecture") + ": "
                + System.getProperty("os.arch") + "\n" + "OS Version: " + System.getProperty("os.version")
                + "\n" + api.getI18n().msg("user") + ": " + System.getProperty("user.name") + "\n" + "Java Home: "
                + System.getProperty("java.home") + "\n" + "Java Library Path: {"
                + System.getProperty("java.library.path") + "}\n" + "Program path: "
                + System.getProperty("user.dir") + "\n" + "Java Version: " + System.getProperty("java.version")
                + "\n" + "Resources Path: " + resourcesPath);

        // Initialize bus here!
        busService = INJECTOR.getInstance(BusService.class);
        busService.init();

        // register listener
        this.listener = new BusMessagesListener(this);
        // this class is a BusConsumer too
        // listen for exit signal (an event) and call onExit method if received
        listener.consumeEventFrom("app.event.system.exit");

        // Stop on initialization error.
        final BootStatus currentStatus = BootStatus.getCurrentStatus();
        if (!BootStatus.STARTED.equals(currentStatus)) {

            kill(currentStatus.getCode());
        }

        // just for testing, don't mind it
        new StompDispatcher();

        // TODO change this object to an enum and do init in another location.
        new ColorList(); //initialize an ordered list of colors used for various purposes, eg: people colors

        /**
         * ******************************************************************
         * Starting the logger and popup it in the browser
         * *****************************************************************
         */
        if (config.getBooleanProperty("KEY_SAVE_LOG_TO_FILE", false)) {
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
                logger.config(api.getI18n().msg("INIT_MESSAGE"));

                if ((config.getBooleanProperty("KEY_LOGGER_POPUP", true) == true)
                        && (java.awt.Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))) {
                    java.awt.Desktop.getDesktop()
                            .browse(new File(Info.getApplicationPath() + "/log/freedomotic.html").toURI());
                }
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }

        /**
         * ******************************************************************
         * Create data backup folder (FEATURE DISABLED!!!)
         * *****************************************************************
         */
//        if (getConfig().getBooleanProperty("KEY_BACKUP_DATA_BEFORE_START", true) == true) {
//            try {
//                CopyFile.copy(new File(Info.getDatafilePath()), new File(Info.getApplicationPath() + "/backup"));
//            } catch (Exception ex) {
//                logger.warning("unable to save a backup copy of application data " + getStackTraceInfo(ex));
//            }
//        }
        /**
         * ******************************************************************
         * Shows the freedomotic website if stated in the config file
         * *****************************************************************
         */
        if (config.getBooleanProperty("KEY_SHOW_WEBSITE_ON_STARTUP", false)) {
            try {
                java.awt.Desktop.getDesktop().browse(new URI("www.freedomotic.com"));
            } catch (URISyntaxException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }

        /**
         * ******************************************************************
         * Dynamically load events jar files in /plugin/events folder
         * *****************************************************************
         */
        try {
            pluginsManager.loadAllPlugins(PluginsManager.TYPE_EVENT);
        } catch (PluginLoadingException ex) {
            LOG.log(Level.WARNING,
                    "Cannot load event plugin {0}. {1}",
                    new Object[]{ex.getPluginName(), ex.getMessage()});
        }
        /* ******************************************************************
         * Loads sensors and actuators This must be loaded before object
         * deserialization because objects can user hardware level commands and
         * trigger that are loaded at this stage
         * *****************************************************************
         */
        try {
            pluginsManager.loadAllPlugins(PluginsManager.TYPE_DEVICE);
        } catch (PluginLoadingException ex) {
            LOG.warning("Cannot load device plugin " + ex.getPluginName() + ": " + ex.getMessage());
            ex.printStackTrace();
        }
        /**
         * ******************************************************************
         * Dynamically load objects jar files in /plugin/objects folder
         * *****************************************************************
         */
        try {
            pluginsManager.loadAllPlugins(PluginsManager.TYPE_OBJECT);
        } catch (PluginLoadingException ex) {
            LOG.log(Level.WARNING,
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
            LOG.log(Level.SEVERE, null, ex);
        }

        /**
         * ******************************************************************
         * Cache online plugins
         * *****************************************************************
         */
        if (config.getBooleanProperty("CACHE_MARKETPLACE_ON_STARTUP", false)) {
            try {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                LOG.info("Starting marketplace service");

                                MarketPlaceService mps = MarketPlaceService.getInstance();
                                onlinePluginCategories = mps.getCategoryList();
                            }
                        }).start();
                    }
                });
            } catch (Exception e) {
                LOG.warning("Unable to cache plugins package from marketplace");
            }
        }

        /**
         * ******************************************************************
         * Deserialize the default environment (its shape + zones)
         * *****************************************************************
         */
        /**
         *
         *
         * /**
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
//        LOG.config("---- Checking zones topology ----");
//        for (ZoneLogic z : environment.getZones()) {
//            z.checkTopology();
//        }
        /**
         * ******************************************************************
         * Loads the entire Reactions system (Trigger + Commands + Reactions)
         * *****************************************************************
         */
        TriggerPersistence.loadTriggers(new File(Info.PATHS.PATH_DATA_FOLDER + "/trg/"));
        CommandPersistence.loadCommands(new File(Info.PATHS.PATH_DATA_FOLDER + "/cmd/"));
        ReactionPersistence.loadReactions(new File(Info.PATHS.PATH_DATA_FOLDER + "/rea/"));

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
        LOG.config("Freedomotic + data uses " + memory + "MB");

        for (Client plugin : clientStorage.getClients()) {
            String startupTime = plugin.getConfiguration().getStringProperty("startup-time", "undefined");

            if (startupTime.equalsIgnoreCase("on load")) {
                plugin.start();

                PluginHasChanged event = new PluginHasChanged(this,
                        plugin.getName(),
                        PluginActions.DESCRIPTION);
                busService.send(event);

                double snapshot = (((runtime.totalMemory() - runtime.freeMemory()) / MB) - memory);
                LOG.config(plugin.getName() + " uses " + snapshot + "MB of memory");
                memory += snapshot;
            }
        }

        LOG.config("Used Memory:" + ((runtime.totalMemory() - runtime.freeMemory()) / MB));

        LOG.info("Freedomotic startup completed");
    }

    /**
     *
     * @throws FreedomoticException
     */
    public void loadDefaultEnvironment()
            throws FreedomoticException {
        String envFilePath = config.getProperty("KEY_ROOM_XML_PATH");
        File envFile = new File(Info.PATHS.PATH_WORKDIR + "/data/furn/" + envFilePath);
        File folder = envFile.getParentFile();

        if (!folder.exists()) {
            throw new FreedomoticException(
                    "Folder " + folder + " do not exists. Cannot load default "
                    + "environment from " + envFile.getAbsolutePath().toString());
        } else if (!folder.isDirectory()) {
            throw new FreedomoticException(
                    "Environment folder " + folder.getAbsolutePath()
                    + " is supposed to be a directory");
        }

        try {
            //EnvironmentPersistence.loadEnvironmentsFromDir(folder, false);
            EnvironmentDAO loader = environmentDaoFactory.create(folder);
            Collection<Environment> loaded = loader.load();

            if (loaded == null) {
                throw new IllegalStateException("Object data cannot be null at this stage");
            }
            for (Environment env : loaded) {
                EnvironmentLogic logic = INJECTOR.getInstance(EnvironmentLogic.class);
                logic.setPojo(env);
                logic.setSource(new File(folder + "/" + env.getUUID() + ".xenv"));
                EnvironmentPersistence.add(logic, false);
            }

            //now load related objects
            EnvObjectPersistence.loadObjects(new File(folder + "/data/obj"), false);
        } catch (DaoLayerException e) {
            throw new FreedomoticException(e.getMessage(), e);
        }
    }

    /**
     *
     * @return
     */
    public static String getInstanceID() {
        return INSTANCE_ID;
    }

    /**
     *
     * @param dimension
     */
    public void loadGraphics(Dimension dimension) {
        new ColorList();
    }

    private void loadAppConfig() {
        config = config.load();
        LOG.info(config.toString());
    }

    // FIXME This shouldn't be done through this method

    /**
     *
     * @param event
     */
        public static void sendEvent(EventTemplate event) {
        busService.send(event);
    }

    // FIXME This shouldn't be done through this method

    /**
     *
     * @param command
     * @return
     */
        public static Command sendCommand(final Command command) {
        return busService.send(command);
    }

    /**
     * Main entry point of freedomotic. All starts from here.
     *
     * @param args
     */
    public static void main(String[] args) {

        configureLogging();

        try {
            INSTANCE_ID = args[0];
        } catch (Exception e) {
            INSTANCE_ID = "";
        }

        if ((INSTANCE_ID == null) || (INSTANCE_ID.isEmpty())) {
            INSTANCE_ID = "A";
        }

        LOG.info("Freedomotic instance ID: " + INSTANCE_ID);

        try {
            Freedomotic freedomotic = INJECTOR.getInstance(Freedomotic.class);
            //start freedomotic
            freedomotic.start();
        } catch (FreedomoticException ex) {
            LOG.severe(ex.getMessage());
            System.exit(1);
        }
    }

    /**
     * Configures java.util.logging (hereafter JUL)
     *
     * While Freedomotic is still using JUL, here is configured SLF4J Brigde.
     *
     * Thereby now all logging (including third party packages) is done through
     * SLF4J.
     */
    private static void configureLogging() {

        // Remove all handlers in jul (java.util.logging) root logger
        SLF4JBridgeHandler.removeHandlersForRootLogger();

        // Register slf4j handler to jul root logger 
        SLF4JBridgeHandler.install();

        // Set jul root log level to ALL, because default slf4jbridge handler is INFO.
        LogManager.getLogManager().getLogger("").setLevel(Level.ALL);
    }

    @Override
    public void onMessage(ObjectMessage message) {

        Object payload = null;

        try {
            payload = message.getObject();

            if (payload instanceof EventTemplate) {
                final EventTemplate event = (EventTemplate) payload;
                onExit(event);
            }
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param event
     */
    public void onExit(EventTemplate event) {
        LOG.info("Received exit signal...");
        //stop all plugins
        for (Client plugin : clientStorage.getClients()) {
            plugin.stop();
        }
        BootStatus.setCurrentStatus(BootStatus.STOPPING);
        busService.destroy();
        config.save();
        auth.save();
        
        String savedDataRoot;

        if (config.getBooleanProperty("KEY_OVERRIDE_REACTIONS_ON_EXIT", false) == true) {
            savedDataRoot = Info.getApplicationPath() + "/data";
        } else {
            savedDataRoot = Info.getApplicationPath() + "/testSave/data";
        }

        TriggerPersistence.saveTriggers(new File(savedDataRoot + "/trg"));
        CommandPersistence.saveCommands(new File(savedDataRoot + "/cmd"));
        ReactionPersistence.saveReactions(new File(savedDataRoot + "/rea"));

        //save the environment
        String environmentFilePath =
                Info.getApplicationPath() + "/data/furn/" + config.getProperty("KEY_ROOM_XML_PATH");
        File folder = null;

        try {
            folder = new File(environmentFilePath).getParentFile();
            
            EnvironmentPersistence.saveEnvironmentsToFolder(folder);
            
            if (config.getBooleanProperty("KEY_OVERRIDE_OBJECTS_ON_EXIT", false) == true) {
                File saveDir = null;
                try {
                    saveDir = new File(folder + "/data/obj");
                    EnvObjectPersistence.saveObjects(saveDir);
                } catch (DaoLayerException ex) {
                    LOG.severe("Cannot save objects in " + saveDir.getAbsolutePath().toString());
                }
            }
        } catch (DaoLayerException ex) {
            LOG.severe("Cannot save environment to folder " + folder + "due to " + ex.getCause());
        }

        System.exit(0);
    }

    /**
     *
     */
    public static void kill() {

        kill(0);
    }

    /**
     *
     * @param status
     */
    public static void kill(int status) {

        System.exit(status);
    }

    /**
     *
     * @param t
     * @return
     */
    public static String getStackTraceInfo(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        pw.flush();
        sw.flush();

        return sw.toString();
    }
}
