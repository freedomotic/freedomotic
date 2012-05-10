/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cicolella.arduinows;

/**
 *
 * @author windows
 */
public class Board {

    private String ipAddress = null;
    private String delimiter = null;
    private int port;
    private int socketTimeout;
    private int pollingTime;

    public Board(String ipAddress, int port, int pollingTime, int socketTimeout, String delimiter) {
        setIpAddress(ipAddress);
        setPort(port);
        setPollingTime(pollingTime);
        setSocketTimeout(socketTimeout);
        setDelimiter(delimiter);
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

    public void setPollingTime(int pollingTime) {
        this.pollingTime = pollingTime;
    }

    public int getPollingTime() {
        return pollingTime;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }
}
