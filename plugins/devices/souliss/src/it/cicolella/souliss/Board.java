/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cicolella.souliss;

/**
 *
 * @author mauro
 */
public class Board {

    private String ipAddress = null;
    private int port;

    public Board(String ipAddress, int port) {
            setIpAddress(ipAddress);
            setPort(port);
                      
    }

   
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

   
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    
}