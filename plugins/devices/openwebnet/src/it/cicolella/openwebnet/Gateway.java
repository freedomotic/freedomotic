/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cicolella.openwebnet;

/**
 *
 * @author windows
 */
public class Gateway {

    private String ipAddress = null;
    private String alias = null;
    private String gatewayType = null;
    private int portNumber;
    private int passwordOpen;
    boolean connected;
    BTicinoSocketReadManager gestSocketMonitor;
    BTicinoSocketWriteManager gestSocketCommands;

    public Gateway(String alias, String ipAddress, int portNumber, String gatewayType) {
        setIpAddress(ipAddress);
        setPortNumber(portNumber);
        setAlias(alias);
        setGatewayType(gatewayType);
        setConnected(false);
        setPasswordOpen(0);
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }
    
    public int getPasswordOpen() {
        return passwordOpen;
    }

    public void setPasswordOpen(int passwordOpen) {
        this.passwordOpen = passwordOpen;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public void setGatewayType(String gatewayType) {
        this.gatewayType = gatewayType;
    }

    public String getGatewayType() {
        return gatewayType;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean getConnected() {
        return connected;
    }

    public BTicinoSocketReadManager getGestSocketMonitor() {
        return gestSocketMonitor;
    }

    // inserire riferimento ad alias
    public void SetGestSocketMonitor(OpenWebNet freedomoticSensor, String alias) {
       // this.gestSocketMonitor = new BTicinoSocketReadManager(freedomoticSensor, alias);
    }
}
