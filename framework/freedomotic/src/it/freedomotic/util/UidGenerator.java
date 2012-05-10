/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.freedomotic.util;

/**
 * Generates an unique ID as a progressive int.
 * Used to mark events and command with a numeric unique value
 * @author enrico
 */
public class UidGenerator {

    private static int lastId=0;

    public static int getNextUid(){
        lastId++;
        return lastId;
    }

    public static String getNextStringUid(){
        lastId++;
        return Integer.valueOf(lastId).toString();
    }

}
