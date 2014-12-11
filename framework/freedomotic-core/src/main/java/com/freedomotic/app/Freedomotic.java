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
import com.freedomotic.core.SynchManager;
import com.freedomotic.core.TopologyManager;
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.EnvironmentRepository;
import com.freedomotic.events.PluginHasChanged;
import com.freedomotic.events.PluginHasChanged.PluginActions;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.exceptions.FreedomoticException;
import com.freedomotic.exceptions.PluginLoadingException;
import com.freedomotic.marketplace.ClassPathUpdater;
import com.freedomotic.marketplace.IPluginCategory;
import com.freedomotic.marketplace.MarketPlaceService;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.ThingRepository;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.PluginsManager;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandPersistence;
import com.freedomotic.reactions.ReactionPersistence;
import com.freedomotic.reactions.TriggerPersistence;
import com.freedomotic.security.Auth;
import com.freedomotic.security.UserRealm;
import com.freedomotic.util.Info;
import com.freedomotic.util.LogFormatter;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    private static final Logger LOG = Logger.getLogger(Freedomotic.class.getName());
    public static String INSTANCE_ID;
    public static ArrayList<IPluginCategory> onlinePluginCategories;
    /**
     * Should NOT be used. Reserved for una tantum internal freedomotic core use
     * only!!
     */
    @Deprecated
    public static Injector INJECTOR;
    //dependencies
    private final EnvironmentRepository environmentRepository;
    private final ThingRepository thingsRepository;
    private final TopologyManager topologyManager;
    private final SynchManager synchManager;
    private final ClientStorage clientStorage;
    private final PluginsManager pluginsManager;
    private AppConfig config;
    private final Auth auth;
    private final API api;
    private BusMessagesListener listener;
    // TODO remove static modifier once static methods sendEvent & sendCommand are erased.
    private static BusService busService;

    /**
     *
     * @param pluginsLoader
     * @param environmentRepository
     * @param thingsRepository
     * @param clientStorage
     * @param config
     * @param api
     * @param busService
     * @param topologyManager
     * @param synchManager
     */
    @Inject
    public Freedomotic(
            PluginsManager pluginsLoader,
            EnvironmentRepository environmentRepository,
            ThingRepository thingsRepository,
            ClientStorage clientStorage,
            AppConfig config,
            API api,
            BusService busService,
            TopologyManager topologyManager,
            SynchManager synchManager) {
        this.pluginsManager = pluginsLoader;
        this.environmentRepository = environmentRepository;
        this.thingsRepository = thingsRepository;
        this.busService = busService;
        this.topologyManager = topologyManager;
        this.synchManager = synchManager;
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
        // Relocate base data folder according to configuration (if specified in the config file)
        // Do not move it in AppConfigImpl otherwise unit tests will become dependent to the presence of the data folder
        String defaultPath = Info.PATHS.PATH_DATA_FOLDER.getAbsolutePath();
        Info.relocateDataPath(new File(config.getStringProperty("KEY_DATA_PATH", defaultPath)));

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
            LOG.info("Booting as user:" + auth.getSubject().getPrincipal() + ". Session will last:" + auth.getSubject().getSession().getTimeout());
        }

        String resourcesPath
                = new File(Info.PATHS.PATH_WORKDIR
                        + config.getStringProperty("KEY_RESOURCES_PATH", "/build/classes/it/freedom/resources/")).getPath();
        LOG.info("\nOS: " + System.getProperty("os.name") + "\n" + api.getI18n().msg("architecture") + ": "
                + System.getProperty("os.arch") + "\n" + "OS Version: " + System.getProperty("os.version")
                + "\n" + api.getI18n().msg("user") + ": " + System.getProperty("user.name") + "\n" + "Java Home: "
                + System.getProperty("java.home") + "\n" + "Java Library Path: {"
                + System.getProperty("java.library.path") + "}\n" + "Program path: "
                + System.getProperty("user.dir") + "\n" + "Java Version: " + System.getProperty("java.version")
                + "\n" + "Resources Path: " + resourcesPath);

        //check if topology manager is initiated
        if (topologyManager == null) {
            throw new IllegalStateException("Topology manager has not started");
        }

        if (synchManager == null) {
            throw new IllegalStateException("Synch manager has not started");
        }

        // register listener
        this.listener = new BusMessagesListener(this, busService);
        // this class is a BusConsumer too
        // listen for exit signal (an event) and call onExit method if received
        listener.consumeEventFrom("app.event.system.exit");

        // Stop on initialization error.
        final BootStatus currentStatus = BootStatus.getCurrentStatus();
        if (!BootStatus.STARTED.equals(currentStatus)) {

            kill(currentStatus.getCode());
        }

        /**
         * ******************************************************************
         * Starting the logger and popup it in the browser
         * *****************************************************************
         */
        if (config.getBooleanProperty("KEY_SAVE_LOG_TO_FILE", false)) {
            try {
                File logdir = new File(Info.PATHS.PATH_WORKDIR + "/log/");
                logdir.mkdir();

                File logfile = new File(logdir + "/freedomotic.html");
                logfile.createNewFile();

                FileHandler handler = new FileHandler(logfile.getAbsolutePath(),
                        false);
                handler.setFormatter(new LogFormatter());
                LOG.setLevel(Level.ALL);
                LOG.addHandler(handler);
                LOG.config(api.getI18n().msg("INIT_MESSAGE"));

                if ((config.getBooleanProperty("KEY_LOGGER_POPUP", true) == true)
                        && (java.awt.Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))) {
                    java.awt.Desktop.getDesktop()
                            .browse(new File(Info.PATHS.PATH_WORKDIR + "/log/freedomotic.html").toURI());
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
//                logger.warning("unable to saveAll a backup copy of application data " + getStackTraceInfo(ex));
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
         * Dynamically load all plugins
         * *****************************************************************
         */
        try {
            pluginsManager.loadAllPlugins();
        } catch (PluginLoadingException ex) {
            LOG.log(Level.WARNING,"Error while loading all plugins. Impossible to load " + ex.getPluginName(), ex);
        }

        /**
         * ******************************************************************
         * Dynamically load jar files in /plugin/providers folder for plugins
         * *****************************************************************
         */
        try {
            ClassPathUpdater.add(Info.PATHS.PATH_PROVIDERS_FOLDER);
        } catch (IOException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
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

        // Bootstrap Things in the environments
        // This should be done after loading all Things plugins otherwise
        // its java class will not be recognized by the system
        environmentRepository.init();
        for (EnvironmentLogic env : environmentRepository.findAll()) {
            // Load all the Things in this environment
            File thingsFolder = env.getObjectFolder();
            List<EnvObjectLogic> loadedThings = thingsRepository.loadAll(thingsFolder);
            for (EnvObjectLogic thing: loadedThings) {
                thing.setEnvironment(env);
                // Actvates the Thing. Important, otherwise it will be not visible in the environment
                thingsRepository.create(thing);
            }
        }

        // Loads the entire Reactions system (Trigger + Commands + Reactions)
        TriggerPersistence.loadTriggers(new File(Info.PATHS.PATH_DATA_FOLDER + "/trg/"));
        CommandPersistence.loadCommands(new File(Info.PATHS.PATH_DATA_FOLDER + "/cmd/"));
        ReactionPersistence.loadReactions(new File(Info.PATHS.PATH_DATA_FOLDER + "/rea/"));

        // Starting plugins
        for (Client plugin : clientStorage.getClients()) {
            String startupTime = plugin.getConfiguration().getStringProperty("startup-time", "undefined");

            if (startupTime.equalsIgnoreCase("on load")) {
                plugin.start();

                PluginHasChanged event = new PluginHasChanged(this,
                        plugin.getName(),
                        PluginActions.DESCRIPTION);
                busService.send(event);
            }
        }

        double MB = 1024 * 1024;
        Runtime runtime = Runtime.getRuntime();
        LOG.config("Used Memory:" + ((runtime.totalMemory() - runtime.freeMemory()) / MB));

        LOG.info("Freedomotic startup completed");
    }

    /**
     *
     * @return
     */
    public static String getInstanceID() {
        return INSTANCE_ID;
    }

    // FIXME This shouldn't be done through this method
    /**
     *
     * @param event
     */
    @Deprecated
    public static void sendEvent(EventTemplate event) {
        busService.send(event);
    }

    // FIXME This shouldn't be done through this method
    /**
     *
     * @param command
     * @return
     */
    @Deprecated
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
            INSTANCE_ID = UUID.randomUUID().toString();
        }

        LOG.info("Freedomotic instance ID: " + INSTANCE_ID);

        try {
            INJECTOR = Guice.createInjector(new FreedomoticInjector());
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
            savedDataRoot = Info.PATHS.PATH_DATA_FOLDER.getAbsolutePath();
        } else {
            savedDataRoot = Info.PATHS.PATH_WORKDIR + "/testSave/data";
        }

        TriggerPersistence.saveTriggers(new File(savedDataRoot + "/trg"));
        CommandPersistence.saveCommands(new File(savedDataRoot + "/cmd"));
        ReactionPersistence.saveReactions(new File(savedDataRoot + "/rea"));

        //save the environment
        String environmentFilePath = Info.PATHS.PATH_DATA_FOLDER + "/furn/" + config.getProperty("KEY_ROOM_XML_PATH");
        File folder = null;

        try {
            folder = new File(environmentFilePath).getParentFile();
            environmentRepository.saveEnvironmentsToFolder(folder);

            if (config.getBooleanProperty("KEY_OVERRIDE_OBJECTS_ON_EXIT", false) == true) {
                File saveDir = null;
                try {
                    saveDir = new File(folder + "/data/obj");
                    thingsRepository.saveAll(saveDir);
                } catch (RepositoryException ex) {
                    LOG.log(Level.SEVERE, "Cannot save objects in {0}", saveDir.getAbsolutePath());
                }
            }
        } catch (RepositoryException ex) {
            LOG.log(Level.SEVERE, "Cannot save environment to folder {0} due to {1}", new Object[]{folder, ex.getCause()});
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
