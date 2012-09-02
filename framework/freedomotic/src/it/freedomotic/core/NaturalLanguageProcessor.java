/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.core;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.reactions.CommandPersistence;
import it.freedomotic.reactions.Command;
import it.freedomotic.util.DamerauLevenshtein;
import java.util.*;

/**
 *
 * @author Enrico
 */
public class NaturalLanguageProcessor {

    List<Rank> ranking;

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

    public class Rank {

        int similarity;
        Command cmd;

        public Rank(int similarity, Command cmd) {
            this.similarity = similarity;
            this.cmd = cmd;
        }

        public int getSimilarity() {
            return similarity;
        }

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
