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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.nlp;

import com.freedomotic.exceptions.NoResultsException;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.inject.Inject;

/**
 * Computes Commands similarity ranking usin Damerau-Levenstrin string distance
 * algorithm
 *
 * @see Command
 * @author Enrico
 */
public class NlpCommandStringDistanceImpl implements NlpCommand {

    private static final Logger LOG = Logger.getLogger(NlpCommandStringDistanceImpl.class.getName());
    private List<Rank<Command>> ranking;
    private final CommandRepository commandsRepository;

    @Inject
    public NlpCommandStringDistanceImpl(CommandRepository commandsRepository) {
        this.commandsRepository = commandsRepository;
    }

    /**
     * {@inheritDoc}
     * May return also elements with similarity equals to zero.
     *
     * @param inputText
     * @return
     */
    @Override
    public List<Rank<Command>> computeSimilarity(String inputText, int maxResults) throws NoResultsException {
        ranking = new ArrayList<>();
        buildRanking(inputText);
        Collections.sort(ranking, new DescendingRankComparator());
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
        for (Command command : commandsRepository.findAll()) {
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

}
