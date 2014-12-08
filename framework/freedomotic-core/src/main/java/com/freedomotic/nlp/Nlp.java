/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.nlp;

import java.util.Comparator;
import java.util.List;
import com.freedomotic.exceptions.NoResultsException;

/**
 * Takes in input some text, analyzes it to compute a similarity value related
 * to a set of predefined object avaliable to the system (eg: automation commands)
 * 
 * @author enrico
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
