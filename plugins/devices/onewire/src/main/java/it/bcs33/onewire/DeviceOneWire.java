/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bcs33.onewire;

/**
 *
 * @author windows
 */
public class DeviceOneWire {

    private String address = null;
    private String valueToMonitorize;
    private double value;
    private boolean changed;
    
    public DeviceOneWire(String address, String valueToMonitorize, double value) {
            setAddress(address);
            setValueToMonitorize(valueToMonitorize);
            setValue(value);
            setChanged(true);
                        
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getValueToMonitorize() {
        return valueToMonitorize;
    }

    public void setValueToMonitorize(String valueToMonitorize) {
        this.valueToMonitorize = valueToMonitorize;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public boolean getChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }
   
}