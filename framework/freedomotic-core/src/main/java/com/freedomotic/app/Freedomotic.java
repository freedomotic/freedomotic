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

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
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
 * This is the starting class of the Freedomotic project.
 *
 * @author Enrico Nicoletti
 */
public class Freedomotic implements BusConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(Freedomotic.class.getName());
    private static String KEY_INSTANCE_ID;
    private static List<IPluginCategory> onlinePluginCategories;

    /**
     * Should NOT be used. Reserved for una tantum internal freedomotic core use
     * only!! TODO Maybe make that field private and provide a method with the
     * right javadoc
     */
    @Deprecated
    public static Injector INJECTOR;
    //dependencies
    private final EnvironmentRepository environmentRepository;
    private final TriggerRepository triggerRepository;
    private final TopologyManager topologyManager;
    private final SynchManager synchManager;
    private final ClientStorage clientStorage;
    private final PluginsManager pluginsManager;
    private AppConfig config;
    private final Auth auth;
    private final I18n i18n;
    // TODO remove static modifier once static methods sendEvent & sendCommand are erased.
    private static BusService busService;
    private static boolean logToFile;
    private final CommandRepository commandRepository;
    private final ReactionRepository reactionRepository;
    private final ThingRepository thingsRepository;
    private final CommandsNlpService commandsNlpService;
    private final Autodiscovery autodiscovery;
    private String savedDataRoot;
    private static final String LOG_PATH = Info.PATHS.PATH_WORKDIR + "/log/freedomotic.log";
    private static final double MB = 1024.0 * 1024.0;

    /**
     * Constructor of Freedomotic objects.
     *
     * @param auth authentication object
     * @param pluginsLoader plugin manager
     * @param environmentRepository repository of the environment
     * @param thingsRepository repository of things
     * @param triggerRepository repository of triggers
     * @param commandRepository repository of commands
     * @param clientStorage client storage
     * @param commandsNlpService nop commande service
     * @param config Application configuration
     * @param i18n internationalization
     * @param busService bus service
     * @param topologyManager topology manager
     * @param synchManager Sync Manager
     * @param autodiscovery auto discovery
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
        Freedomotic.busService = busService;
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
     * Configures a Freedomotic instance ID.
     *
     * @return true if the instance ID is configured, false otherwise
     */
    private boolean configureFreedomoticInstanceId() {
        setInstanceId(config.getStringProperty("KEY_INSTANCE_ID", UUID.randomUUID().toString()));
        config.setProperty("KEY_INSTANCE_ID", KEY_INSTANCE_ID);
        return true;
    }

    /**
     * Return the Freedomotic instance id or build it with a synchronized.
     *
     * @return the Freedomotic instance id
     */
    private static synchronized boolean setInstanceId(String instanceId) {
        if ((KEY_INSTANCE_ID == null) || (KEY_INSTANCE_ID.isEmpty())) {
            KEY_INSTANCE_ID = instanceId;
        }
        return true;
    }

    /**
     * Start the Freedomotic instance
     *
     * @throws FreedomoticException
     */
    public void start() throws FreedomoticException {

        this.configureFreedomoticInstanceId();
        LOG.info("Freedomotic instance ID is \"{}\"", KEY_INSTANCE_ID);

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
            Subject sysSubject = new Subject.Builder().principals(principals).buildSubject();
            sysSubject.getSession().setTimeout(-1);
            ThreadState threadState = new SubjectThreadState(sysSubject);
            threadState.bind();
            LOG.info("Booting as user \"{}\". Session will last {}", auth.getSubject().getPrincipal(), auth.getSubject().getSession().getTimeout());
        }

        String resourcesPath = new File(Info.PATHS.PATH_WORKDIR + config.getStringProperty("KEY_RESOURCES_PATH", "/build/classes/it/freedom/resources/")).getPath();
        LOG.info("\nOS: {}\n{}: {}\nOS Version: {}\n{}: {}\n" + "Java Home: {}\n" + "Java Library Path: {}\n"
                + "Program path: {}\n" + "Java Version: {}\nResources Path: {}",
                System.getProperty("os.name"), i18n.msg("architecture"), System.getProperty("os.arch"), System.getProperty("os.version"),
                i18n.msg("user"), System.getProperty("user.name"), System.getProperty("java.home"), System.getProperty("java.library.path"),
                System.getProperty("user.dir"), System.getProperty("java.version"), resourcesPath);

        //check if topology manager is initiated
        if (topologyManager == null) {
            throw new IllegalStateException("Topology manager not started");
        }

        if (synchManager == null) {
            throw new IllegalStateException("Synch manager not started");
        }

        // register listener
        BusMessagesListener listener = new BusMessagesListener(this, busService);
        // this class is a BusConsumer too
        // listen for exit signal (an event) and call onExit method if received
        listener.consumeEventFrom("app.event.system.exit");

        // Stop on initialization error.
        final BootStatus currentStatus = BootStatus.getCurrentStatus();
        if (BootStatus.STARTED != currentStatus) {
            kill(currentStatus.getCode());
        }

        /**
         * ******************************************************************
         * Dynamically load all plugins
         * *****************************************************************
         */
        try {
            pluginsManager.loadAllPlugins();
        } catch (PluginLoadingException ex) {
            LOG.warn("Error while loading all plugins. Impossible to load \"{}\" due to \"{}\"", ex.getPluginName(), Freedomotic.getStackTraceInfo(ex));
        }

        /**
         * ******************************************************************
         * Dynamically load jar files in /plugin/providers folder for plugins
         * *****************************************************************
         */
        try {
            ClassPathUpdater.add(Info.PATHS.PATH_PROVIDERS_FOLDER);
        } catch (IOException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            LOG.error("Error during loading jars in \"/plugin/providers\" due to {}", Freedomotic.getStackTraceInfo(ex));
        }

        /**
         * ******************************************************************
         * Cache online plugins
         * *****************************************************************
         */
        if (config.getBooleanProperty("KEY_CACHE_MARKETPLACE_ON_STARTUP", false)) {
            try {
                EventQueue.invokeLater(() -> new Thread(() -> {
                    LOG.info("Starting marketplace service");
                    Freedomotic.setOnlinePluginCategories(MarketPlaceService.getInstance().getCategoryList());
                }).start());
            } catch (Exception e) {
                LOG.warn("Unable to cache plugins package from marketplace", Freedomotic.getStackTraceInfo(e));
            }
        }

        // Bootstrap Things in the environments
        // This should be done after loading all Things plugins otherwise
        // its java class will not be recognized by the system
        environmentRepository.initFromDefaultFolder();

        // Loads the entire Reactions system (Trigger + Commands + Reactions)
        triggerRepository.loadTriggers(new File(Info.PATHS.PATH_DATA_FOLDER + "/trg/"));
        commandRepository.loadCommands(new File(Info.PATHS.PATH_DATA_FOLDER + "/cmd/"));
        reactionRepository.loadReactions(new File(Info.PATHS.PATH_DATA_FOLDER + "/rea/"));

        // Starting plugins
        for (Client plugin : clientStorage.getClients()) {
            String startupTime = plugin.getConfiguration().getStringProperty("startup-time", "undefined");

            if ("on load".equalsIgnoreCase(startupTime)) {
                plugin.start();

                PluginHasChanged event = new PluginHasChanged(this,
                        plugin.getName(),
                        PluginActions.DESCRIPTION);
                busService.send(event);
            }
        }

        Runtime runtime = Runtime.getRuntime();
        LOG.info("Used Memory: {}", (runtime.totalMemory() - runtime.freeMemory()) / MB);
        LOG.info("Freedomotic startup completed");

        setDataRootPath();
        activatePeriodicSave();
    }

    /**
     * Sets the online plugin categories.
     *
     * @param categoryList list of plugin to set
     */
    protected static synchronized void setOnlinePluginCategories(List<IPluginCategory> categoryList) {
        onlinePluginCategories = categoryList;
    }

    /**
     * Get the list of plugin categories.
     *
     * @return list of plugin category
     */
    protected static synchronized List<IPluginCategory> getOnlinePluginCategories() {
        return onlinePluginCategories;
    }

    /**
     * Enables periodic saving of commands, triggers and reactions.
     */
    private void activatePeriodicSave() {
        // TODO: Use BooleanUtils.toBoolean
        if ("true".equals(config.getProperty("KEY_SAVE_DATA_PERIODICALLY"))) {
            int executionInterval = Integer.parseInt(config.getProperty("KEY_DATA_SAVING_INTERVAL"));
            final PeriodicSave periodicSave = new PeriodicSave(savedDataRoot, executionInterval);
            periodicSave.delegateRepositories(triggerRepository, commandRepository, reactionRepository);
            periodicSave.startExecutorService();
        }
    }

    /**
     * Gets the Freedomotic instance ID.
     *
     * @return the Freedomotic instance ID
     */
    public static String getInstanceID() {
        return KEY_INSTANCE_ID;
    }

    /**
     * Enables logging on files.
     *
     * @return true if the log to file engine starts properly, false otherwise.
     */
    protected boolean enableLogToFile() {

        org.apache.log4j.Logger proxyLogger = org.apache.log4j.Logger.getRootLogger();
        String saveToLogConfigParam = config.getStringProperty("KEY_SAVE_LOG_TO_FILE", "OFF").trim();

        if (!"OFF".equalsIgnoreCase(saveToLogConfigParam)) {
            try {
                PatternLayout layout = new PatternLayout("%d{ISO8601} %-5p [%t] (%F:%L) %m%n");
                RollingFileAppender rollingFileAppender = new RollingFileAppender(layout, LOG_PATH);
                rollingFileAppender.setMaxBackupIndex(5);
                rollingFileAppender.setMaxFileSize("500KB");
                proxyLogger.setLevel(org.apache.log4j.Level.toLevel(saveToLogConfigParam));
                proxyLogger.setAdditivity(false);
                proxyLogger.addAppender(rollingFileAppender);
                // disable default.file appender
                proxyLogger.removeAppender("default.file");
                Freedomotic.setLogToFile(true);
            } catch (IOException ex) {
                LOG.error("Impossible to start logging: \"{}\"", Freedomotic.getStackTraceInfo(ex));
            }
        } else {
            LOG.info("This Freedomotic configuration does not require a \"log to file\" feature.");
            // disable default.file appender
            proxyLogger.removeAppender("default.file");
        }
        return Freedomotic.isLogToFileEnabled();
    }

    /*
     * Updates the value of the static variable logToFile, that represents a flag stating if the
     * current instance of Freedomotic comes with the logging to file feature.
     */
    private static void setLogToFile(boolean active) {
        logToFile = active;
    }

    /**
     * Checks if the log to file is enabled.
     *
     * @return boolean true if the log to file is enabled, false otherwise
     */
    public static boolean isLogToFileEnabled() {
        return logToFile;
    }

    /**
     * Sends the event template.
     *
     * @param event event tot sen // FIXME This shouldn't be done through this
     * method
     */
    @Deprecated
    public static void sendEvent(EventTemplate event) {
        busService.send(event);
    }

    /**
     * Sends a command.
     *
     * @param command
     * @return
     * @deprecated FIXME This shouldn't be done through this method
     */
    @Deprecated
    public static Command sendCommand(final Command command) {
        return busService.send(command);
    }

    /**
     * Main entry point of Freedomotic. All starts from here.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {

        configureLogging();

        try {
            KEY_INSTANCE_ID = args[0];
        } catch (Exception e) {
            KEY_INSTANCE_ID = "";
        }

        try {
            INJECTOR = Guice.createInjector(new FreedomoticInjector());
            Freedomotic freedomotic = INJECTOR.getInstance(Freedomotic.class);
            //start freedomotic
            freedomotic.enableLogToFile();
            freedomotic.start();
        } catch (FreedomoticException ex) {
            LOG.error("Error during Freedomotic starting due to {}", Freedomotic.getStackTraceInfo(ex));
            System.exit(1);
        }
    }

    /**
     * Configures java.util.logging (hereafter JUL).
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

    /**
     * {@inheritDoc}
     */
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
            LOG.error(Freedomotic.getStackTraceInfo(ex));
        }
    }

    /**
     * Receives an exit signal and stops Freedomotic.
     *
     * @param event received event
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

        // persist triggers, commands and reactions
        triggerRepository.saveTriggers(new File(savedDataRoot + "/trg"));
        commandRepository.saveCommands(new File(savedDataRoot + "/cmd"));
        reactionRepository.saveReactions(new File(savedDataRoot + "/rea"));

        //save the environment
        String environmentFilePath = Info.PATHS.PATH_DATA_FOLDER + "/furn/" + config.getProperty("KEY_ROOM_XML_PATH");
        File folder = null;

        try {
            folder = new File(environmentFilePath).getParentFile();
            environmentRepository.saveEnvironmentsToFolder(folder);
        } catch (RepositoryException ex) {
            LOG.error("Cannot save environment to folder \"{}\" due to \"{}\"", folder, Freedomotic.getStackTraceInfo(ex));
        }

        LOG.info("Freedomotic instance ID \"{}\" is shutting down. See you!", KEY_INSTANCE_ID);
        System.exit(0);
    }

    /**
     * Set the data root path.
     */
    private void setDataRootPath() {
        if (config.getBooleanProperty("KEY_OVERRIDE_REACTIONS_ON_EXIT", false)) {
            savedDataRoot = Info.PATHS.PATH_DATA_FOLDER.getAbsolutePath();
        } else {
            savedDataRoot = Info.PATHS.PATH_WORKDIR + "/testSave/data";
        }
    }

    /**
     * Exits with code "0".
     *
     */
    public static void kill() {
        kill(0);
    }

    /**
     * Esits with a status code.
     *
     * @param status status code
     */
    public static void kill(int status) {
        System.exit(status);
    }

    /**
     * Prints a stack trace.
     *
     * @param t the throwable object to print stack trace from
     * @return a string representing the stack trace
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
     * Returns the current path of the log file.
     *
     * @return the log path
     */
    public static String logPath() {
        return LOG_PATH;
    }

    /**
     * Given the current Freedomotic instance id, it returns an UUID object
     * representing the identifier.
     *
     * @return Freedomotic instance id as UUID
     */
    public static UUID getInstanceIdAsUUID() {
        return UUID.fromString(KEY_INSTANCE_ID);
    }

}
