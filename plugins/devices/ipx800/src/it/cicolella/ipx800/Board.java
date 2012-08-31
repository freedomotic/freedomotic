/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cicolella.ipx800;

/**
 *
 * @author windows
 */
public class Board {

    private String ipAddress = null;
    private String ledTag;
    private String analogInputTag;
    private String digitalInputTag;
    private int port;
    private int relayNumber;
    private int analogInputNumber;
    private int digitalInputNumber;
    private int startingRelay;

    public Board(String ipAddress, int port, int relayNumber, int analogInputNumber,
            int digitalInputNumber, int startingRelay, String ledTag, String digitalInputTag, String analogInputTag) {
        setIpAddress(ipAddress);
        setPort(port);
        setRelayNumber(relayNumber);
        setAnalogInputNumber(analogInputNumber);
        setDigitalInputNumber(digitalInputNumber);
        setStartingRelay(startingRelay);
        setLedTag(ledTag);
        setDigitalInputTag(digitalInputTag);
        setAnalogInputTag(analogInputTag);
    }

    public int getAnalogInputNumber() {
        return analogInputNumber;
    }

    public void setAnalogInputNumber(int analogInputNumber) {
        this.analogInputNumber = analogInputNumber;
    }

    public int getDigitalInputNumber() {
        return digitalInputNumber;
    }

    public void setDigitalInputNumber(int digitalInputNumber) {
        this.digitalInputNumber = digitalInputNumber;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getLedTag() {
        return ledTag;
    }

    public void setLedTag(String ledTag) {
        this.ledTag = ledTag;
    }
    
    public String getAnalogInputTag() {
        return analogInputTag;
    }

    public void setAnalogInputTag(String analogInputTag) {
        this.analogInputTag = analogInputTag;
    }
    
    public String getDigitalInputTag() {
        return digitalInputTag;
    }

    public void setDigitalInputTag(String digitalInputTag) {
        this.digitalInputTag = digitalInputTag;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getRelayNumber() {
        return relayNumber;
    }

    public void setRelayNumber(int relayNumber) {
        this.relayNumber = relayNumber;
    }

    public int getStartingRelay() {
        return startingRelay;
    }

    public void setStartingRelay(int startingRelay) {
        this.startingRelay = startingRelay;
    }
}