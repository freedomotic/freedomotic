/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cicolella.ethrly;

/**
 *
 * @Author Mauro Cicolella
 */
public class Board {

    private String ipAddress = null;
    private String autoConfiguration;
    private String objectClass;
    private String username;
    private String password;
    private String httpAuthentication;
    private String alias = null;
    private int port;
    private int relayNumber;
    private int[] relayStatus;

    public Board(String ipAddress, int port, String alias, int relayNumber, String autoConfiguration, String objectClass, String username, String password, String httpAuthentication) {
        setIpAddress(ipAddress);
        setPort(port);
        setAlias(alias);
        setUsername(username);
        setPassword(password);
        setHttpAuthentication(httpAuthentication);
        setRelayNumber(relayNumber);
        setAutoConfiguration(autoConfiguration);
        setObjectClass(objectClass);
        initializeRelayStatus(relayNumber);
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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getHttpAuthentication() {
        return httpAuthentication;
    }

    public void setHttpAuthentication(String httpAuthentication) {
        this.httpAuthentication = httpAuthentication;
    }

    public int getRelayNumber() {
        return relayNumber;
    }

    public void setRelayNumber(int relayNumber) {
        this.relayNumber = relayNumber;
    }

    public String getAutoConfiguration() {
        return autoConfiguration;
    }

    public void setAutoConfiguration(String autoConfiguration) {
        this.autoConfiguration = autoConfiguration;
    }

    public String getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(String objectClass) {
        this.objectClass = objectClass;
    }

    public int getRelayStatus(int relayNumber) {
        return relayStatus[relayNumber];
    }

    public void setRelayStatus(int relayNumber, int value) {
        relayStatus[relayNumber] = value;
    }

    private void initializeRelayStatus(int relayNumber) {
        relayStatus = new int[relayNumber];
        for (int i = 0; i < relayNumber; i++) {
            relayStatus[i] = -1;
        }
    }
}