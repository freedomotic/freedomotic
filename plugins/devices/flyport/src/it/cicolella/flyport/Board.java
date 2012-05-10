/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cicolella.flyport;

/**
 *
 * @author windows
 */
public class Board {

    private String ipAddress = null;
    private String lineToMonitorize;
    private int port;
    private int potNumber;
    private int ledNumber;
    private int btnNumber;
    private int startingValue;

    public Board(String ipAddress, int port, int potNumber, int ledNumber,
            int btnNumber, String lineToMonitorize, int startingValue) {
            setIpAddress(ipAddress);
            setPort(port);
            setPotNumber(potNumber);
            setLedNumber(ledNumber);
            setBtnNumber(btnNumber);
            setLineToMonitorize(lineToMonitorize);
            setStartingValue(startingValue);
            
    }

    public int getPotNumber() {
        return potNumber;
    }

    public void setPotNumber(int potNumber) {
        this.potNumber = potNumber;
    }

    public int getLedNumber() {
        return ledNumber;
    }

    public void setLedNumber(int ledNumber) {
        this.ledNumber = ledNumber;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getLineToMonitorize() {
        return lineToMonitorize;
    }

    public void setLineToMonitorize(String lineToMonitorize) {
        this.lineToMonitorize = lineToMonitorize;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getBtnNumber() {
        return btnNumber;
    }

    public void setBtnNumber(int btnNumber) {
        this.btnNumber = btnNumber;
    }

    public int getStartingValue() {
        return startingValue;
    }

    public void setStartingValue(int startingValue) {
        this.startingValue = startingValue;
    }
}