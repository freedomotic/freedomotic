/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cicolella.grovesystem;

/**
 *
 * @author windows
 */
public class Board {

    private String ipAddress = null;
    private int udpPort;

    public Board(String ipAddress, int udpPort) {
        setIpAddress(ipAddress);
        setUdpPort(udpPort);

    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }
}