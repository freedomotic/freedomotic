/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.util.I18n;

import java.util.Locale;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
public  class ComboLanguage implements Comparable{

        private String descr;
        private String value;
        private Locale loc;

        public ComboLanguage(String descr, String value, Locale loc) {
            this.descr = descr;
            this.value = value;
            this.loc = loc;
        }

    /**
     *
     * @return
     */
    @Override
        public String toString() {
            return descr;
        }

    /**
     *
     * @return
     */
    public String getValue() {
            return value;
        }

    @Override
    public int compareTo(Object o) {
        return this.descr.compareTo(o.toString());
    }
}
