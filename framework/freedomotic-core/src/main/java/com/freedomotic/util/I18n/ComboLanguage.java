/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.util.I18n;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
public  class ComboLanguage {

        private String descr;
        private String value;

        ComboLanguage(String descr, String value) {
            this.descr = descr;
            this.value = value;
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
}
