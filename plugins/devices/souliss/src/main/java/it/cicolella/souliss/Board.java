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
    private String statusToQuery = null;
    private int port;

    public Board(String ipAddress, int port, String statusToQuery) {
        setIpAddress(ipAddress);
        setPort(port);
        setStatusToQuery(statusToQuery);
    }

    public String getStatusToQuery() {
        return statusToQuery;
    }

    public void setStatusToQuery(String statusToQuery) {
        this.statusToQuery = statusToQuery;
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