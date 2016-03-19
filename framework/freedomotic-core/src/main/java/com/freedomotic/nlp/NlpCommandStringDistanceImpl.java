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
package com.freedomotic.nlp;

import com.freedomotic.exceptions.NoResultsException;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import org.slf4j.Logger;

/**
 * Computes Commands similarity ranking usin Damerau-Levenstrin string distance
 * algorithm
 *
 * @see Command
 * @author Enrico Nicoletti
 */
public class NlpCommandStringDistanceImpl implements NlpCommand {

    private static final Logger LOG = LoggerFactory.getLogger(NlpCommandStringDistanceImpl.class.getName());
    private List<Rank<Command>> ranking;
    private final CommandRepository commandsRepository;

    @Inject
    public NlpCommandStringDistanceImpl(CommandRepository commandsRepository) {
        this.commandsRepository = commandsRepository;
    }

    /**
     * {@inheritDoc} May return also elements with similarity equals to zero.
     *
     * @param inputText
     * @return
     */
    @Override
    public List<Rank<Command>> computeSimilarity(String inputText, int maxResults) throws NoResultsException {
        ranking = new ArrayList<>();
        buildRanking(inputText);
        Collections.sort(ranking, new DescendingRankComparator());

//        for (Rank<Command> r : ranking) {
//            if (r.getSimilarity() > 0) {
//                System.out.println(r.getElement().getName() + " points " + r.getSimilarity());
//            }
//        }
        if (ranking.isEmpty()) {
            return Collections.unmodifiableList(ranking);
        } else {
            return Collections.unmodifiableList(ranking.subList(0, Math.min(maxResults - 1, ranking.size())));
        }
    }

    /**
     * Tokenize the string and compute string distance using Damerau-Levensthein
     * algorithm. May return also elements with similarity equals to zero.
     *
     * @param input
     */
    private void buildRanking(String input) {
        if (input == null || input.isEmpty()) {
            return;
        }
        DamerauLevenshtein algorithm = new DamerauLevenshtein();
        for (Command command : commandsRepository.findAll()) {
            int similarity = 0;
            // Compare every word of the input to any word in the command name
            for (String inputWord : Arrays.asList(input.split(" "))) {
                // Use the command name not the command tags for this
                for (String commandWord : Arrays.asList(command.getName().split(" "))) {
                    algorithm.setWordsToCompare(inputWord.trim().toLowerCase(), commandWord.trim().toLowerCase());
                    int distance = algorithm.getSimilarity();
                    //convert word distance into a percent value
                    double distancePercent = ((double) (distance * 100)) / ((double) inputWord.length());

                    if (distancePercent == 0) {
                        similarity += 30; //3 points for any idenitical word
                    } else {
                        if (distancePercent <= 30) { //30% of error, not too much, it can be a typo, give it some points
                            similarity += 15;
                        }
                    }
                }
            }
            ranking.add(new Rank(similarity, command));
        }
    }

}
