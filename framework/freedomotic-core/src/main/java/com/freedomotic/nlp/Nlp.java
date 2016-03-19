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
package com.freedomotic.nlp;

import java.util.Comparator;
import java.util.List;
import com.freedomotic.exceptions.NoResultsException;

/**
 * Takes in input some text, analyzes it to compute a similarity value related
 * to a set of predefined object avaliable to the system (eg: automation
 * commands)
 *
 * @author Enrico Nicoletti
 * @param <T>
 */
public interface Nlp<T> {

    /**
     * Analyzes the text and ranks object of type T according to their
     * similarity value. This objects are typically {@link Commands}, for
     * example a speech recognition algorithm returns a text and you want to
     * identity the most similar predefined command to execute it.
     *
     * Beware that computing similarity may be CPU intensive. Similarity may be
     * computed with different algorithms by the implementing classes.
     *
     * @param inputText The text to analyze
     * @param maxResults The maximum number of result elements
     * @return
     * @throws com.freedomotic.exceptions.NoResultsException
     */
    List<Rank<T>> computeSimilarity(String inputText, int maxResults) throws NoResultsException;

    /**
     *
     * @param <T>
     */
    public class Rank<T> {

        int similarity;
        T cmd;

        /**
         *
         * @param similarity
         * @param cmd
         */
        public Rank(int similarity, T cmd) {
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
        public T getElement() {
            return cmd;
        }
    }

    public class DescendingRankComparator implements Comparator<Rank> {

        @Override
        public int compare(Rank ob1, Rank ob2) {
            return ob2.getSimilarity() - ob1.getSimilarity(); //descending order of similarity
        }
    }

}
