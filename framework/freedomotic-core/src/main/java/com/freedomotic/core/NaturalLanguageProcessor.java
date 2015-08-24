/**
 *
 * Copyright (c) 2009-2015 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.core;

import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandPersistence;
import com.freedomotic.util.DamerauLevenshtein;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class NaturalLanguageProcessor {
    private static final Logger LOG = Logger.getLogger(NaturalLanguageProcessor.class.getName());
    private List<Rank> ranking;

    private void buildRanking(String input) {
        if (input == null || input.isEmpty()) {
            return;
        }
        for (Command command : CommandPersistence.getUserCommands()) {
            int similarity = 0;
            for (String inputTag : Arrays.asList(input.split(" "))) {
                for (String commandTag : command.getTags()) {
                    DamerauLevenshtein algorithm = new DamerauLevenshtein(inputTag.trim().toLowerCase(), commandTag.trim().toLowerCase());
                    int distance = algorithm.getSimilarity();
                    //the percent of similarity in in this tag related to input tag
                    double percent = ((double) (distance * 100)) / ((double) inputTag.length());

                    if (distance == 0) {
                        similarity += 3;
                    } else {
                        if (percent <= 30) { //30% of error, not too much, it can be a typo
                            similarity += 1;
                        }
                    }
                }
            }
            ranking.add(new Rank(similarity, command));
            //System.out.println(command.getName() + " points " + similarity);
        }
    }

    /**
     *
     * @param inputTags
     * @param resultSize
     * @return
     */
    public List<Rank> getMostSimilarCommand(String inputTags, int resultSize) {
        ranking = new ArrayList<Rank>();
        buildRanking(inputTags);
        Collections.sort(ranking, new DescendingRankComparator());
        if (ranking.size() == 0) {
            return Collections.unmodifiableList(ranking);
        } else {
            return ranking.subList(0, Math.min(resultSize - 1, ranking.size()));
        }
    }

    /**
     *
     */
    public class Rank {

        int similarity;
        Command cmd;

        /**
         *
         * @param similarity
         * @param cmd
         */
        public Rank(int similarity, Command cmd) {
            this.similarity = similarity;
            this.cmd = cmd;
        }

        /**
         *
         * @return
         */
        public int getSimilarity() {
            return similarity;
        }

        /**
         *
         * @return
         */
        public Command getCommand() {
            return cmd;
        }
    }

    private class DescendingRankComparator implements Comparator<Rank> {

        @Override
        public int compare(Rank ob1, Rank ob2) {
            return ob2.getSimilarity() - ob1.getSimilarity(); //descending order of similarity
        }
    }
}
