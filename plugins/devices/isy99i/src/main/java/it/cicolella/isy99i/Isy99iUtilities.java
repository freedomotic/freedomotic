/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cicolella.isy99i;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author mauro
 */
public class Isy99iUtilities {

    public static String getDateTime() {
        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return (sdf.format(calendar.getTime()));
    }
}
