package com.freedomotic.util;

import com.freedomotic.reactions.CommandRepository;
import com.freedomotic.reactions.ReactionRepository;
import com.freedomotic.reactions.TriggerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.*;

/**
 * @author https://github.com/wiewioraseb
 */
public class PeriodicSave {
    private static final Logger LOG = LoggerFactory.getLogger(PeriodicSave.class.getName());

    private ScheduledExecutorService executorService;
    private TriggerRepository triggerRepository;
    private CommandRepository commandRepository;
    private ReactionRepository reactionRepository;
    private String savedDataRoot;
    private int executionInterval;

    public PeriodicSave(String savedDataRoot, int executionInterval) {
        this.savedDataRoot = savedDataRoot;
        this.executionInterval = executionInterval;
    }

    public void delegateRepositories(TriggerRepository triggerRepository, CommandRepository commandRepository,
                                     ReactionRepository reactionRepository) {
        this.triggerRepository = triggerRepository;
        this.commandRepository = commandRepository;
        this.reactionRepository = reactionRepository;
    }

    public void startExecutorService() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        long initDelay = 15;
        executorService.scheduleWithFixedDelay(runnable, initDelay, executionInterval, TimeUnit.MINUTES);
    }

    private Runnable runnable = new Runnable() {
        public void run() {
            LOG.info("Periodic save of triggers, commands and reactions");
            triggerRepository.saveTriggers(new File(savedDataRoot + "/trg"));
            commandRepository.saveCommands(new File(savedDataRoot + "/cmd"));
            reactionRepository.saveReactions(new File(savedDataRoot + "/rea"));
        }
    };

    public void shutDown() {
        executorService.shutdown();
    }
}