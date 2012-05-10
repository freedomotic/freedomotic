/*Copyright 2009 Enrico Nicoletti
 * eMail: enrico.nicoletti84@gmail.com
 *
 * This file is part of Freedom.
 *
 * Freedom is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * Freedom is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedom; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.freedom.X10.gateways;

import it.freedom.X10.X10AbstractGateway;
import it.freedom.application.Config;
import it.nicoletti.serial.SerialConnectionProvider;
import java.util.Properties;

/**
 * An implementation of a reader/writer to an X10 transciever named Xannura Home PMIX35
 * You can fine the technical manual at http://freedomotic.googlecode.com/files/PMIX35.pdf
 * @author Enrico Nicoletti (enrico.nicoletti84@gmail.com)
 */
public class PMix35Gateway{

    private static SerialConnectionProvider usb = null;

    public PMix35Gateway() {
    }

    public static SerialConnectionProvider getInstance() {
        if (usb != null) {
            return usb;
        } else {
            Properties config = new Properties();
            config.setProperty("hello-message", "$>9000PXD3#"); //hello message defined by pmix15 comm protocol
            config.setProperty("hello-reply", "$<9000VP"); //expected reply to hello-message starts with this string
            config.setProperty("polling-message", "$>9000RQCE#"); //$>9000RQcs# forces read data using this string
            //The computer alway take the initiative to read, the PMIX35 is a passive gateway
            config.setProperty("polling-time", "600"); //millisecs between reads.
            usb = new SerialConnectionProvider(config); //instantiating a new serial connection with the previous parameters
            return usb;
        }
    }

//        public boolean sendMessage(String message) {
//        // Non è collegato
//        if (!isConnected()) {
//            errorSetMessage("Impossibile inviare messaggi in quanto il dispositivo PMix35 non è collegato.");
//            return false;
//        }
//
//        // Creo il messaggio (A01 -> $>9000LWA01**#)
//        message = completePMix35StandardMessage("LW"+message);
//
//        // Invio messaggio e ricevo ack
//        String ack = comunicateToPMix35(message);
//
//        // Se l'ack è corretto restituisco true
//        return (ack.compareToIgnoreCase(PMIX_ACK)==0);
//    }
    // Messaggi PMix35

    /**
     * Passato un messaggio aggiunge il carattere d'inizio del messaggio,
     * l'indirizzo del dispositivo PMix35 e alla fine il check sum e il carattere
     * di fine messaggio.<p>
     * Esempi di messaggi da completare:
     * <ul>
     * <li> A01A01 AONAON   ->  $>9000LW A01A01 AONAON**#
     * <li> TV3500             ->  $>9000TV3500**#
     * </ul>
     * ** corrisponde al CS.
     * LW means line write in pmix35 lingo
     * @param message messaggio da completare
     * @return il messaggio pronto per essere inviato
     */
    public static String composeMessage(String message) {
        // Completo la parte iniziale del messaggio
        message = "$>9000LW " + message;

        // Effettuo la somma esadecimale dei codici ascii di ogni lettera
        int sum = 0;
        for (int i = 0; i < message.length(); i++) {
            sum += message.charAt(i);
        }
        // Mantengo solo le ultime due cifre
        String cs = Integer.toHexString(sum);
        cs = cs.substring(cs.length() - 2);
        cs = cs.toUpperCase();

        // Completa la parte finale del messaggio
        return message + cs + "#";
    }
}

