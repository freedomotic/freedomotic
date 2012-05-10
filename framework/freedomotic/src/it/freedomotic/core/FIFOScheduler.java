package it.freedomotic.core;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.bus.CommandChannel;
import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.CommandSequence;
import it.freedomotic.reactions.Reaction;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A first in first out scheduler for reactions
 *
 * @author Enrico
 */
public class FIFOScheduler implements Scheduler {

    private static BlockingQueue<Reaction> readyQueue = new LinkedBlockingQueue<Reaction>();
    private static long FIXED_DEADLINE_PERIOD = 1000; //millisecs
    private ExecutorService executor;

    public FIFOScheduler() {
        /*
         * this cannot be a single thread executor becuse in this way all
         * commands also from different sequences are executed in series and not
         * in parallel (one dispatcher thread per sequence)
         */
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public void schedule(Reaction r) {
        try {
            readyQueue.put(r); //add to tail
            System.out.println("Insering reaction " + r.getDescription());
            Profiler.incrementEnqueuedReactions();
        } catch (InterruptedException ex) {
            Logger.getLogger(FIFOScheduler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void schedule(Command c) {
        Reaction r = new Reaction(null, c);
        SchedulingData data = new SchedulingData(System.currentTimeMillis());
        r.setScheduling(data);
        schedule(r);
    }

    @Override
    public synchronized void start() {
        while (true) {
            try {
                Reaction currentReaction = readyQueue.take(); //gets from head
                final long creationTime = currentReaction.getScedulingData().getCreation();
                System.out.println("Taking reaction " + currentReaction.getDescription());
                displayOnFrontend(currentReaction.getDescription());
                Profiler.incrementDequeuedReactions();
                //set a deadline for the execution of old reactions in the bus
                long now = System.currentTimeMillis();

                if (now > (currentReaction.getScedulingData().getCreation() + FIXED_DEADLINE_PERIOD)) {
                    currentReaction.getScedulingData().getLog().append("Reaction ").append(currentReaction.getDescription()).append(" hits its deadline (+").append(now - currentReaction.getScedulingData().getCreation()).append("ms from event creation). Current fixed deadline is +").append(FIXED_DEADLINE_PERIOD).append("ms.");
                    Profiler.incrementMissedDeadlines();
                } else {
                    for (final CommandSequence seqs : currentReaction.getCommandSequences()) {
                        Callable executeInSequence = new Callable() {

                            @Override
                            public Command call() throws Exception {
                                Profiler.appendCommandLatency(System.currentTimeMillis() - creationTime);
                                for (Command command : seqs.getCommands()) {
                                    long before = System.currentTimeMillis();
                                    Command reply = Freedomotic.sendCommand(command); //blocking wait (in this case in a thread) until executed
                                    long after = System.currentTimeMillis();
                                    Profiler.appendExecutionTime(after - before);
                                    Profiler.appendTotalExecutionTime(after - creationTime);
                                    System.out.println("FIFOScheduler executes command " + command.getName());
                                }
                                return null;
                            }
                        };
                        Future<Command> result = executor.submit(executeInSequence);
//        try {
//            return result.get();
//        } catch (InterruptedException ex) {
//            Logger.getLogger(CommandChannel.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (ExecutionException ex) {
//            Logger.getLogger(CommandChannel.class.getName()).log(Level.SEVERE, null, ex);
//        }
                    }
                }
                Freedomotic.logger.info(currentReaction.getScedulingData().getLog().toString());
            } catch (InterruptedException ex) {
                Freedomotic.logger.severe("Interrupted exception in FIFODispatcher");
            }
        }
    }

    private void displayOnFrontend(String message) {
        final Command c = new Command();
        c.setName("A callout from the scheduler");
        c.setDelay(0);
        c.setExecuted(true);
        c.setEditable(false);
        c.setReceiver("app.actuators.frontend.javadesktop.in");
        c.setProperty("callout-message", message);
        //this method is supposed to be called in threads so it not instantiate others
        Freedomotic.sendCommand(c);
    }
}
