/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cicolella.daenetip2;

/**
 *
 * @author windows
 */
public class Board {

    private String ipAddress = null;
    private String alias = null;
    private String readOnlyCommunity = null;
    private String readWriteCommunity = null;
    private int snmpPort;
    private int P3Status = 2000;
    private int P5Status = 2000;
    private int[] P6Status = {0, 0, 0, 0, 0, 0, 0, 0};

    public Board(String alias, String ipAddress, int snmpPort, String readOnlyCommunity, String readWriteCommunity) {
        setAlias(alias);
        setIpAddress(ipAddress);
        setSnmpPort(snmpPort);
        setP3Status(-1);
        setP5Status(-1);
        setReadOnlyCommunity(readOnlyCommunity);
        setReadWriteCommunity(readWriteCommunity);
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getSnmpPort() {
        return snmpPort;
    }

    public void setSnmpPort(int port) {
        this.snmpPort = port;
    }

    public int getP3Status() {
        return P3Status;
    }

    public void setP3Status(int P3Status) {
        this.P3Status = P3Status;
    }

    public int getP5Status() {
        return P5Status;
    }

    public void setP5Status(int P5Status) {
        this.P5Status = P5Status;
    }
    
    public int getP6Status(int i) {
        return P6Status[i];
    }

    public void setP6Status(int pin, int value) {
        this.P6Status[pin] = value;
    }

    public String getReadOnlyCommunity() {
        return readOnlyCommunity;
    }

    public void setReadOnlyCommunity(String readOnlyCommunity) {
        this.readOnlyCommunity = readOnlyCommunity;
    }

    public String getReadWriteCommunity() {
        return readWriteCommunity;
    }

    public void setReadWriteCommunity(String readWriteCommunity) {
        this.readWriteCommunity = readWriteCommunity;
    }
}
