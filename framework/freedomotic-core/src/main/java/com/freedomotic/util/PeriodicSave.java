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

    /**
     *
     * @param savedDataRoot
     * @param executionInterval
     */
    public PeriodicSave(String savedDataRoot, int executionInterval) {
        this.savedDataRoot = savedDataRoot;
        this.executionInterval = executionInterval;
    }

    /**
     *
     * @param triggerRepository
     * @param commandRepository
     * @param reactionRepository
     */
    public void delegateRepositories(TriggerRepository triggerRepository, CommandRepository commandRepository,
            ReactionRepository reactionRepository) {
        this.triggerRepository = triggerRepository;
        this.commandRepository = commandRepository;
        this.reactionRepository = reactionRepository;
    }

    /**
     *
     */
    public void startExecutorService() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        long initDelay = 5; //first saving after 5 minutes
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

    /**
     *
     */
    public void shutDown() {
        executorService.shutdown();
    }
}
