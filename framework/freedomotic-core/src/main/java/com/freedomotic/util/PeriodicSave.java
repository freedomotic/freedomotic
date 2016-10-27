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

    private TriggerRepository triggerRepository;
    private CommandRepository commandRepository;
    private ReactionRepository reactionRepository;
    private String savedDataRoot;

    public PeriodicSave(String savedDataRoot) {
        this.savedDataRoot = savedDataRoot;
    }

    public void delegateRepositories(TriggerRepository triggerRepository, CommandRepository commandRepository,
                                     ReactionRepository reactionRepository) {
        this.triggerRepository = triggerRepository;
        this.commandRepository = commandRepository;
        this.reactionRepository = reactionRepository;
    }

    // TODO: get delays as configuration property from config.xml (e.g. data-saving-interval)
    public void startExecutorService() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        long initDelay = 15;
        long delay = 15;
        executor.scheduleWithFixedDelay(runnable, initDelay, delay, TimeUnit.MINUTES);
    }

    private Runnable runnable = new Runnable() {
        public void run() {
            LOG.info("Periodic save of triggers, commands and reactions");
            triggerRepository.saveTriggers(new File(savedDataRoot + "/trg"));
            commandRepository.saveCommands(new File(savedDataRoot + "/cmd"));
            reactionRepository.saveReactions(new File(savedDataRoot + "/rea"));
        }
    };
}