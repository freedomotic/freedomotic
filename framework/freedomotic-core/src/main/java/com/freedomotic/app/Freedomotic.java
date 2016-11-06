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
package com.freedomotic.app;

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
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.LogManager;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import com.freedomotic.util.PeriodicSave;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.freedomotic.api.Client;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.bus.BootStatus;
import com.freedomotic.bus.BusConsumer;
import com.freedomotic.bus.BusMessagesListener;
import com.freedomotic.bus.BusService;
import com.freedomotic.core.Autodiscovery;
import com.freedomotic.core.SynchManager;
import com.freedomotic.core.TopologyManager;
import com.freedomotic.environment.EnvironmentRepository;
import com.freedomotic.events.PluginHasChanged;
import com.freedomotic.events.PluginHasChanged.PluginActions;
import com.freedomotic.exceptions.FreedomoticException;
import com.freedomotic.exceptions.PluginLoadingException;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.i18n.I18n;
import com.freedomotic.marketplace.ClassPathUpdater;
import com.freedomotic.marketplace.IPluginCategory;
import com.freedomotic.marketplace.MarketPlaceService;
import com.freedomotic.nlp.CommandsNlpService;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.PluginsManager;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandRepository;
import com.freedomotic.reactions.ReactionRepository;
import com.freedomotic.reactions.TriggerRepository;
import com.freedomotic.security.Auth;
import com.freedomotic.security.UserRealm;
import com.freedomotic.settings.AppConfig;
import com.freedomotic.settings.Info;
import com.freedomotic.things.ThingRepository;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * This is the starting class of the project
 *
 * @author Enrico Nicoletti
 */
public class Freedomotic implements BusConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(Freedomotic.class.getName());
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
    private final TriggerRepository triggerRepository;
    private final TopologyManager topologyManager;
    private final SynchManager synchManager;
    private final CommandsNlpService commandsNlpService;
    private final ClientStorage clientStorage;
    private final PluginsManager pluginsManager;
    private AppConfig config;
    private final Auth auth;
    private final I18n i18n;
    private BusMessagesListener listener;
    // TODO remove static modifier once static methods sendEvent & sendCommand are erased.
    private static BusService busService;
	private static boolean logToFile;
    private final CommandRepository commandRepository;
    private final ReactionRepository reactionRepository;
    private final Autodiscovery autodiscovery;
    private String savedDataRoot;
    private static final String LOG_PATH = Info.PATHS.PATH_WORKDIR + "/log/freedomotic.log";

    /**
     *
     * @param auth
     * @param pluginsLoader
     * @param environmentRepository
     * @param thingsRepository
     * @param triggerRepository
     * @param commandRepository
     * @param clientStorage
     * @param commandsNlpService
     * @param config
     * @param i18n
     * @param busService
     * @param topologyManager
     * @param synchManager
     * @param autodiscovery
     */
    @Inject
    public Freedomotic(
            PluginsManager pluginsLoader,
            EnvironmentRepository environmentRepository,
            ThingRepository thingsRepository,
            TriggerRepository triggerRepository,
            CommandRepository commandRepository,
            ReactionRepository reactionRepository,
            ClientStorage clientStorage,
            CommandsNlpService commandsNlpService,
            AppConfig config,
            I18n i18n,
            BusService busService,
            TopologyManager topologyManager,
            Auth auth,
            SynchManager synchManager,
            Autodiscovery autodiscovery) {
        this.pluginsManager = pluginsLoader;
        this.environmentRepository = environmentRepository;
        this.thingsRepository = thingsRepository;
        this.triggerRepository = triggerRepository;
        this.commandRepository = commandRepository;
        this.reactionRepository = reactionRepository;
        this.busService = busService;
        this.commandsNlpService = commandsNlpService;
        this.topologyManager = topologyManager;
        this.synchManager = synchManager;
        this.clientStorage = clientStorage;
        this.config = config;
        this.i18n = i18n;
        this.auth = auth;
        this.autodiscovery = autodiscovery;
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
        i18n.setDefaultLocale(config.getStringProperty("KEY_ENABLE_I18N", "no"));

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
        LOG.info("\nOS: " + System.getProperty("os.name") + "\n" + i18n.msg("architecture") + ": "
                + System.getProperty("os.arch") + "\n" + "OS Version: " + System.getProperty("os.version")
                + "\n" + i18n.msg("user") + ": " + System.getProperty("user.name") + "\n" + "Java Home: "
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
                LOG.error(ex.getMessage());
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
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
            LOG.warn("Error while loading all plugins. Impossible to load " + ex.getPluginName(), ex);
        }

        /**
         * ******************************************************************
         * Dynamically load jar files in /plugin/providers folder for plugins
         * *****************************************************************
         */
        try {
            ClassPathUpdater.add(Info.PATHS.PATH_PROVIDERS_FOLDER);
        } catch (IOException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            LOG.error(ex.getMessage());
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
                LOG.warn("Unable to cache plugins package from marketplace");
            }
        }

        // Bootstrap Things in the environments
        // This should be done after loading all Things plugins otherwise
        // its java class will not be recognized by the system
        environmentRepository.initFromDefaultFolder();
        // for (EnvironmentLogic env : environmentRepository.findAll()) {
        // Load all the Things in this environment
        //    File thingsFolder = env.getObjectFolder();
        //    List<EnvObjectLogic> loadedThings = thingsRepository.loadAll(thingsFolder);
        //    for (EnvObjectLogic thing : loadedThings) {
        //        thing.setEnvironment(env);
        // Actvates the Thing. Important, otherwise it will be not visible in the environment
        //        thingsRepository.create(thing);
        //    }
        // }

        // Loads the entire Reactions system (Trigger + Commands + Reactions)
        triggerRepository.loadTriggers(new File(Info.PATHS.PATH_DATA_FOLDER + "/trg/"));
        commandRepository.loadCommands(new File(Info.PATHS.PATH_DATA_FOLDER + "/cmd/"));
        reactionRepository.loadReactions(new File(Info.PATHS.PATH_DATA_FOLDER + "/rea/"));

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
        LOG.info("Used Memory:" + ((runtime.totalMemory() - runtime.freeMemory()) / MB));
        LOG.info("Freedomotic startup completed");

        setDataRootPath();
        activatePeriodicSave();
    }

    private void activatePeriodicSave() {
        // TODO: temporarily SAVE_DATA_PERIODICALLY is set to false until on/off feature of this property is implemented
        if ("true".equals(config.getProperty("SAVE_DATA_PERIODICALLY"))) {
            int executionInterval = Integer.parseInt(config.getProperty("DATA_SAVING_INTERVAL"));
            final PeriodicSave periodicSave = new PeriodicSave(savedDataRoot, executionInterval);
            periodicSave.delegateRepositories(triggerRepository, commandRepository, reactionRepository);
            periodicSave.startExecutorService();
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
     * It enables the feature to log on files.
     * 
     * @return true if the log to file engine starts properly, false otherwise.
     */
    protected boolean enableLogToFile() {
    	
    	String saveToLogConfigParam = config.getStringProperty("KEY_SAVE_LOG_TO_FILE", "OFF").trim();
    	
        if (!"OFF".equalsIgnoreCase(saveToLogConfigParam)) {
            try {
                PatternLayout layout = new PatternLayout("%d{HH:mm:ss.SSS} %-5p [%t] (%F:%L) %m%n");
                RollingFileAppender rollingFileAppender = new RollingFileAppender(layout, LOG_PATH);
                rollingFileAppender.setMaxBackupIndex(5);
                rollingFileAppender.setMaxFileSize("500KB");
                org.apache.log4j.Logger proxyLogger = org.apache.log4j.Logger.getRootLogger();
                proxyLogger.setLevel(org.apache.log4j.Level.toLevel(saveToLogConfigParam));
                proxyLogger.setAdditivity(false);
                proxyLogger.addAppender(rollingFileAppender);

                if (config.getBooleanProperty("KEY_LOGGER_POPUP", true)
                        && (java.awt.Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))) {
                    java.awt.Desktop.getDesktop()
                            .browse(new File(LOG_PATH).toURI());
                }
                
              Freedomotic.setLogToFile(true);  
              
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            }
        }
        
        else {
        	LOG.info("This Freedomotic configuration does not require a \"log to file\" feature.");
        }
        return Freedomotic.isLogToFileEnabled();
    }

    /*
     * This private method updates the value of the static variable logToFile, that represents a flag stating if the
     * current instance of Freedomotic comes with the logging to file feature
     */
    private static void setLogToFile(boolean active) {
		logToFile = active;
	}
    
    /**
     * This method returns true if the log to file feature has been enabled, false otherwise
     * @return boolean
     */
    public static boolean isLogToFileEnabled() {
		return logToFile;
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
     * Main entry point of Freedomotic.
     * All starts from here.
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
            freedomotic.enableLogToFile();
            freedomotic.start();
        } catch (FreedomoticException ex) {
            LOG.error(ex.getMessage());
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
            LOG.error(ex.getMessage());
        }
    }

    /**
     *
     * @param event
     */
    public void onExit(EventTemplate event) {
        LOG.info("Received exit signal...");
        // Stops and unloads all plugins
        for (Client plugin : clientStorage.getClients()) {
            plugin.destroy();
        }
        BootStatus.setCurrentStatus(BootStatus.STOPPING);
        busService.destroy();
        config.save();
        auth.save();

        triggerRepository.saveTriggers(new File(savedDataRoot + "/trg"));
        commandRepository.saveCommands(new File(savedDataRoot + "/cmd"));
        reactionRepository.saveReactions(new File(savedDataRoot + "/rea"));

        //save the environment
        String environmentFilePath = Info.PATHS.PATH_DATA_FOLDER + "/furn/" + config.getProperty("KEY_ROOM_XML_PATH");
        File folder = null;

        try {
            folder = new File(environmentFilePath).getParentFile();
            environmentRepository.saveEnvironmentsToFolder(folder);

            // block moved inside environmentRepository.saveEnvironmentsToFolder()
            // if (config.getBooleanProperty("KEY_OVERRIDE_OBJECTS_ON_EXIT", false) == true) {
            //    File saveDir = null;
            //    try {
            //        saveDir = new File(folder + "/data/obj");
            //        thingsRepository.saveAll(saveDir);
            //    } catch (RepositoryException ex) {
            //        LOG.log(Level.SEVERE, "Cannot save objects in {}", saveDir.getAbsolutePath());
            //    }
            // }
        } catch (RepositoryException ex) {
            LOG.error("Cannot save environment to folder {} due to {}", new Object[]{folder, ex.getCause()});
        }

        System.exit(0);
    }

    private void setDataRootPath() {
        if (config.getBooleanProperty("KEY_OVERRIDE_REACTIONS_ON_EXIT", false)) {
            savedDataRoot = Info.PATHS.PATH_DATA_FOLDER.getAbsolutePath();
        } else {
            savedDataRoot = Info.PATHS.PATH_WORKDIR + "/testSave/data";
        }
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

    /**
     * It returns the actual path of the log file
     * @return the log path
     */
	public static String logPath() {
		return LOG_PATH;
	}
}
