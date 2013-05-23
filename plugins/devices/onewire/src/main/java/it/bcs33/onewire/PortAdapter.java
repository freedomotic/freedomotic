/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bcs33.onewire;

import com.dalsemi.onewire.OneWireAccessProvider;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.TemperatureContainer;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.ProtocolRead;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author windows
 */
public class PortAdapter {

    private static String adapterName = null;
    private static String portName = null;
    //private int indexDevice=0;
    private static double value;
    public static ArrayList<DeviceOneWire> devicesOneWire = null;
    private double readValue[];
    private static DSPortAdapter dsDevice;
    private TemperatureContainer tc;

    public PortAdapter(String adapterName, String portName, double value) {
        setAdapterName(adapterName);
        setPortName(portName);
        setValue(value);

        try {
            connect();
        } catch (Exception e) {
            Logger.getLogger(onewire.class.getName()).log(Level.SEVERE, null, e);

        }
    }

    // this function check if exist a container. If don't exist insert new container in ArrayList.
    // if exist a container but with different value return true else return false
    public boolean checkOneWireContainer(OneWireContainer oneWireContainer) throws OneWireIOException, OneWireException {
        boolean found = false;
        boolean changed = false;
        boolean isTempContainer = false;
        double temperature = 0;
        DeviceOneWire temp;
        String valueToMonitorize = null;

        String strFind = oneWireContainer.getAddressAsString();
        String strCheck = null;

        try {
            tc = (TemperatureContainer) oneWireContainer;
            isTempContainer = true;
        } catch (Exception e) {
            tc = null;
            isTempContainer = false;   //just to reiterate
        }
        if (isTempContainer) {
            //Freedomotic.logger.info("= This device is a " + owc.getName());
            //Freedomotic.logger.info("= Also known as a "  + owc.getAlternateNames());
            //Freedomotic.logger.info("= It is a Temperature Container");
            byte[] state = tc.readDevice();
            tc.doTemperatureConvert(state);
            temperature = tc.getTemperature(state);
            //Freedomotic.logger.info("= Reported temperature: " + temperature);
        }
        /*          
         if (owc.hashCode()!=oneWireContainer.hashCode()){
         Freedomotic.logger.info("Object  is changed ");
         changed=true;
         }
         else{
         Freedomotic.logger.info("Object  is not changed ");
         }
         */

        if (devicesOneWire == null) {
            devicesOneWire = new ArrayList<DeviceOneWire>();
        }
        for (DeviceOneWire device : devicesOneWire) {

            //Freedomotic.logger.info("--->List elements owc " + device.getAddress() + " Element present " + oneWireContainer.getAddressAsString());
            strCheck = device.getAddress();
            if (strCheck.compareTo(strFind) == 0) {
                //Freedomotic.logger.info("find " + oneWireContainer.getAddressAsString() + " in ArrayList");
                // check different
                if (isTempContainer) {
                    if (temperature != device.getValue()) {
                        //Freedomotic.logger.warning("Device Changed old value:" + device.getValue() + " new value:" + temperature);
                        device.setValue(temperature);
                        device.setChanged(true);

                        changed = true;
                    }

                }
                found = true;
            }
        }

        if (found == false) {

            if (isTempContainer) {
                valueToMonitorize = "temperature";

                //temp.setValue(temperature);
                //temp.setValueToMonitorize();
            }
            temp = new DeviceOneWire(strFind, valueToMonitorize, temperature);
            //temp.setAddress(strFind);

            devicesOneWire.add(temp);
            //Freedomotic.logger.warning("Object " + temp.getAddress() + " is add ");
            changed = true;
        }

        return changed;
    }

    public boolean connect() throws OneWireIOException, OneWireException {

        Freedomotic.logger.info("Trying to connect to OneWire on address name " + adapterName + " : Port name " + portName);

        try {
            dsDevice = OneWireAccessProvider.getAdapter(adapterName, portName);
        } catch (Exception e) {
            Freedomotic.logger.severe("That is not a valid adapter/port combination.");
            Enumeration en = OneWireAccessProvider.enumerateAllAdapters();

            while (en.hasMoreElements()) {
                DSPortAdapter temp = (DSPortAdapter) en.nextElement();

                //System.out.println("Adapter: " + temp.getAdapterName());
                Freedomotic.logger.info("Adapter: " + temp.getAdapterName());
                Enumeration f = temp.getPortNames();

                while (f.hasMoreElements()) {
                    //System.out.println("   Port name : "                 + (( String ) f.nextElement()));
                    Freedomotic.logger.info("   Port name : " + ((String) f.nextElement()));
                }
            }

            return false;
        }

        Freedomotic.logger.info(" OK connect to OneWire on address name " + adapterName + " : Port name " + portName);

        return true;
    }

    public boolean checkDeviceListAndEvaluateDiffs() throws OneWireIOException, OneWireException {
        boolean next;
        boolean isTempContainer;
        try {
            next = dsDevice.findFirstDevice();
        } catch (Exception e) {
            Freedomotic.logger.severe("= Could not find First Device...");
            return false;
        }

        if (!next) {
            Freedomotic.logger.severe("Could not find any iButtons!");
            return false;
        }

        while (next) {

            OneWireContainer owc = dsDevice.getDeviceContainer();


            if (checkOneWireContainer(owc)) {
                Freedomotic.logger.info("= This container " + owc.getAddressAsString() + " is changed ");
            } else {
                //Freedomotic.logger.info("= This container isn't changed ");
            }
            next = dsDevice.findNextDevice();

        }

        return true;

    }

    public void getListDevice() {
        for (DeviceOneWire device : devicesOneWire) {
            Freedomotic.logger.info("-List elements owc " + device.getAddress());

        }
    }

    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }

    public DSPortAdapter getDSPortAdapter() {
        return dsDevice;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
    /*public int getIndexDevice() {
     return indexDevice;
     }*/

    /* public void setIndexDevice(int indexDevice) {
     this.indexDevice = indexDevice;
     }*/
    /*
     //System.out.println(
     //   "====================================================");
     //System.out.println("= Found One Wire DeviceOneWire: "
     //                   + owc.getAddressAsString() + "          =");
     //System.out.println(
     //   "====================================================");
     //System.out.println("=");
     Freedomotic.logger.info("Found One Wire DeviceOneWire: " + owc.getAddressAsString());
        
     boolean              isTempContainer = false;
     TemperatureContainer tc              = null;

     DSPortAdapter

     if (isTempContainer)
     {
     Freedomotic.logger.info("= This device is a " + owc.getName());
     Freedomotic.logger.info("= Also known as a " + owc.getAlternateNames());
     Freedomotic.logger.info("= It is a Temperature Container");

     double  max       = tc.getMaxTemperature();
     double  min       = tc.getMinTemperature();
     boolean hasAlarms = tc.hasTemperatureAlarms();

     Freedomotic.logger.info("= This device " + (hasAlarms ? "has"
     : "does not have") + " alarms");
     Freedomotic.logger.info("= Maximum temperature: " + max);
     Freedomotic.logger.info("= Minimum temperature: " + min);

            
     double temp= 0.0;
     try
     {
     byte[] state = tc.readDevice();
     temp = tc.getTemperature(state);
     //device.setValue(temp);
                
     }
     catch (Exception e)
     {
     Freedomotic.logger.severe("= Could not read DeviceOneWire...");
     }

     Freedomotic.logger.info("= Reported temperature: " + temp);
     }
     else
     {
     Freedomotic.logger.info("= This device is not a temperature device.");
            
     }
        
     */
}