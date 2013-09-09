/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.util.I18n;

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

        @Override
        public String toString() {
            return descr;
        }

        public String getValue() {
            return value;
        }
}
