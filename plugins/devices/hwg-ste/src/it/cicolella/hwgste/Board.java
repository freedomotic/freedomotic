/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cicolella.hwgste;

/**
 *
 * @author windows
 */
public class Board {

    private String ipAddress = null;
    private int snmpPort;
    private int port;
    private int sensorsNumber;
    

    public Board(String ipAddress, int port, int sensorsNumber) {
        setIpAddress(ipAddress);
        setPort(port);
        setSensorsNumber(sensorsNumber);

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
    
    public void setSnmpPort(int port) {
        this.snmpPort = port;
    }

    public int getSensorsNumber() {
        return sensorsNumber;
    }

    public void setSensorsNumber(int sensorsNumber) {

        this.sensorsNumber = sensorsNumber;
    }


}
