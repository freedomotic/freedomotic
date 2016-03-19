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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deprecated: switch to AOP instead
 *
 * @author Enrico Nicoletti
 */
@Deprecated
public class Profiler {

    private static final Logger LOG = LoggerFactory.getLogger(Profiler.class.getName());
    private static final double STARTUP_TIME = System.currentTimeMillis();
    private static int sentEvents;
    private static int sentCommands;
    private static int sentReplies;
    private static int receivedEvents;
    private static int receivedCommands;
    private static int receivedReplies;
    private static int timeoutedReplies;
    private static double numExecutions;
    private static double averageExecutionTime;
    private static double maxExecutionTime;
    private static double numCheckedTriggers;
    private static double averageTriggerCheckingTime;
    private static double averageCommandsLatency;
    private static double maxCommandsLatency;
    private static double numReportedLatency = 1;
    private static int missedDeadlines;
    private static int enqueuedReactions;
    private static int dequeuedReactions;
    private static int[][] scores = new int[1000][4];

    static void incrementMissedDeadlines() {
        missedDeadlines++;
    }

    static void incrementDequeuedReactions() {
        dequeuedReactions++;
    }

    static void incrementEnqueuedReactions() {
        enqueuedReactions++;
    }

    static void appendCommandLatency(long ms) {
        if ((ms >= 0) && (ms < 999)) {
            scores[(int) ms][0] += 1;
        }

        if (ms > maxCommandsLatency) {
            maxCommandsLatency = ms;
        }

        double temp = (averageCommandsLatency * numReportedLatency) + ms;
        numReportedLatency++;
        averageCommandsLatency = temp / (double) numReportedLatency;
    }

    static void appendTotalExecutionTime(long ms) {
        if ((ms >= 0) && (ms < 999)) {
            scores[(int) ms][3] += 1;
        }
    }

    /**
     *
     */
    public static void incrementSentReplies() {
        sentReplies++;
    }

    /**
     *
     */
    public static void incrementReceivedEvents() {
        receivedEvents++;
    }

    /**
     *
     */
    public static void incrementReceivedCommands() {
        receivedCommands++;
    }

    /**
     *
     */
    public static void incrementTimeoutedReplies() {
        timeoutedReplies++;
    }

    /**
     *
     */
    public static void incrementSentEvents() {
        sentEvents++;
    }

    /**
     *
     */
    public static void incrementSentCommands() {
        sentCommands++;
    }

    /**
     *
     */
    public static void incrementReceivedReplies() {
        receivedReplies++;
    }

    private static double computeBusTroughtput() {
        double messages = sentEvents + sentCommands + sentReplies + receivedReplies;
        double runtime = System.currentTimeMillis() - STARTUP_TIME;
        double troughtput = -1.0;

        try {
            troughtput = (double) (messages * 1000) / (double) runtime; //messages on the bus per second
        } catch (Exception e) {
        }

        return troughtput;
    }

    /**
     *
     * @param ms
     */
    public static void appendExecutionTime(long ms) {
        if ((ms >= 0) && (ms < 999)) {
            scores[(int) ms][1] += 1;
        }

        double temp = (averageExecutionTime * numExecutions) + ms;
        numExecutions++;
        averageExecutionTime = temp / (double) numExecutions;

        if (ms > maxExecutionTime) {
            maxExecutionTime = ms;
        }
    }

    /**
     *
     * @param ms
     */
    public static void appendTriggerCheckingTime(long ms) {
        if ((ms >= 0) && (ms < 999)) {
            scores[(int) ms][2] += 1;
        }

        double temp = (averageTriggerCheckingTime * numCheckedTriggers) + ms;
        numCheckedTriggers++;
        averageTriggerCheckingTime = temp / (double) numCheckedTriggers;
    }

    /**
     *
     * @return
     */
    public static String print() {
        StringBuilder buff = null;

        try {
            buff = new StringBuilder();
            buff.append("Sensors have sent ").append(sentEvents).append(" events. This results in ")
                    .append(receivedEvents)
                    .append(" trigger checkings. " + "Trigger checking process takes an average of ")
                    .append(averageTriggerCheckingTime).append("ms per check (")
                    .append(receivedEvents * averageTriggerCheckingTime).append("ms in total).\n");
            buff.append("Freedomotic have sent ").append(sentCommands)
                    .append(" commands (user level + hardware level). ").append(receivedCommands)
                    .append(" of them are arrived to destination (non arrived commands have reached TTL, lost on the bus network or have wrong receiver address).\n "
                            + "Commands execution takes an average time of ").append(averageExecutionTime)
                    .append("ms and a max of ").append(maxExecutionTime).append("ms. Commands average latency is ")
                    .append(averageCommandsLatency).append("ms. Max commands latency is ").append(maxCommandsLatency)
                    .append("ms.\n");
            buff.append("Actuators and BehaviorManager have sent ").append(sentReplies)
                    .append(" commands execution replies of which ").append(receivedReplies)
                    .append(" are received by Freedomotic. ").append(timeoutedReplies)
                    .append(" replies have timed out for a non responding actuator.\n");
            buff.append("Average Bus troughtput (messages/sec): ").append(computeBusTroughtput()).append(". \n")
                    .append("Enqueued Reactions: ").append(enqueuedReactions).append(" Dequeued Reactions: ")
                    .append(dequeuedReactions).append(" of which ").append(missedDeadlines)
                    .append(" missed its deadline (").append((missedDeadlines * 100) / dequeuedReactions)
                    .append("%).\n");
            buff.append("Average reactions executed per second ")
                    .append(dequeuedReactions / ((System.currentTimeMillis() - STARTUP_TIME) / 1000))
                    .append(" (an average of ").append(receivedCommands / dequeuedReactions)
                    .append(" commands in every executed reaction).\n");
            buff.append("System runs for (ms): ").append(System.currentTimeMillis() - STARTUP_TIME).append("\n");

            return buff.toString();
        } catch (Exception e) {
        }

        return "";
    }

    /**
     *
     */
    public static void saveToFile() {
//        StringBuilder buffer = new StringBuilder();
//        //save latency to file
//        File f = new File(Info.getApplicationPath() + "/log/latency.csv");
//        //then make a writer object
//        FileWriter fw = null;
//        try {
//            fw = new FileWriter(f);
//            //then write into the file
//            for (int i = 0; i < scores.length; i++) {
//                buffer.append(scores[i][0]).append(",").append(scores[i][1]).append(",").append(scores[i][2]).append(",").append(scores[i][3]).append("\n");
//            }
//            fw.write(buffer.toString());
//            fw.close();
//        } catch (IOException ex) {
//            System.out.println(ex.getMessage());
//        }
    }

    private Profiler() {
    }
}
