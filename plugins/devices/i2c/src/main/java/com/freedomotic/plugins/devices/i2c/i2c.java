package com.freedomotic.plugins.devices.i2c;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.GpioFactory;

import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class i2c extends Protocol {

    final int POLLING_WAIT;
    private I2CDevice dev;
    private I2CBus bus;
    private GpioPinDigitalInput i2c_int;
    private ArrayList<Board> boards;
    private int DEV_NUMBER;
    // address = dev_i2c_address: line_number 
    private String[] address = null;

    public i2c() {
        //every plugin needs a name and a manifest XML file
        super("i2c", "/i2c/i2c-manifest.xml");
        //read a property from the manifest file below which is in
        //FREEDOMOTIC_FOLDER/plugins/devices/com.freedomotic.hello/hello-world.xml
        POLLING_WAIT = configuration.getIntProperty("time-between-reads", 2000);
        //POLLING_WAIT is the value of the property "time-between-reads" or 2000 millisecs,
        //default value if the property does not exist in the manifest
        setPollingWait(POLLING_WAIT); //millisecs interval between hardware device status reads

    }

    @Override
    protected void onShowGui() {
        /**
         * uncomment the line below to add a GUI to this plugin the GUI can be
         * started with a right-click on plugin list on the desktop frontend
         * (com.freedomotic.jfrontend plugin)
         */
        //bindGuiToPlugin(new HelloWorldGui(this));
    }

    @Override
    protected void onHideGui() {
        //implement here what to do when the this plugin GUI is closed
        //for example you can change the plugin description
        setDescription("My GUI is now hidden");
    }

    @Override
    protected void onRun() {
        Freedomotic.logger.info("I2C onRun() logs this message every "
                + "POLLINGWAIT=" + POLLING_WAIT + "milliseconds");
        //at the end of this method the system waits POLLINGTIME 
        //before calling it again. The result is this log message is printed
        //every 2 seconds (2000 millisecs)

    }

    @Override
    protected void onStart() {
        Freedomotic.logger.info("I2C plugin is started");


        try {
            bus = I2CFactory.getInstance(I2CBus.BUS_0);
        } catch (IOException ex) {
            Logger.getLogger(i2c.class.getName()).log(Level.SEVERE, null, ex);
        }

        // provision gpio pin #02 as an input pin with its internal pull down resistor enabled
        // (configure pin edge to both rising and falling to get notified for HIGH and LOW state
        // changes)
        i2c_int = GpioFactory.getInstance().provisionDigitalInputPin(RaspiPin.GPIO_02, // PIN NUMBER
                "MyButton", // PIN FRIENDLY NAME (optional)
                PinPullResistance.PULL_DOWN); // PIN RESISTANCE (optional)   
        // create and register gpio pin listener
        i2c_int.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                // display pin state on console
                System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
                // scan i2c Bus for changes
                for (Board board : boards) {
                    try {
                        dev = bus.getDevice(board.getAddress());
                        int globVal = dev.read();
                        int val = 0;
                        for (int i = 0; i < 8; i++) {
                            val = (globVal >> i) & 0x0001; //extract i-pos bit of byte
                            if (board.setLineStatus(i, val)) {
                                String EVaddress = board.getAddress() + ":" + i;
                                notifyChangeEvent(EVaddress, val);
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(i2c.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
        });

        DEV_NUMBER = configuration.getTuples().size();
        int devAddr;
        int devNum;
        String alias;
        for (int i = 0; i < DEV_NUMBER; i++) {
            devAddr = configuration.getTuples().getIntProperty(i, "address", 0);
            devNum = configuration.getTuples().getIntProperty(i, "line-number", 0);
            alias = configuration.getTuples().getStringProperty(i, "alias", "");
            boards.add(new Board(devAddr, devNum, alias));
        }


    }

    @Override
    protected void onStop() {
        Freedomotic.logger.info("I2C plugin is stopped ");


        try {
            bus.close();
        } catch (IOException ex) {
            Logger.getLogger(i2c.class.getName()).log(Level.SEVERE, null, ex);
        }

        i2c_int.unexport();
        boards.clear();
        boards = null;
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        Freedomotic.logger.info("I2C plugin receives a command called " + c.getName()
                + " with parameters " + c.getProperties().toString());
        String delimiter = configuration.getProperty("address-delimiter");
        address = c.getProperty("address").split(delimiter);
        int line = Integer.parseInt(address[1]);
        int value = 0;
        if (c.getProperty("command").equals("TURN-ON")) {
            value = 1;
        } else if (c.getProperty("command").equals("TURN-OFF")) {
            value = 0;
        } else if (c.getProperty("command").equals("SWITCH")) {
            value = -1;
        }

        for (Board board : boards) {
            if ((board.getAddress() == Integer.parseInt(address[0])) || (board.getAlias().equals(address[0]))) {
                short lineID = Short.parseShort(address[1]);
                dev = bus.getDevice(board.getAddress());
                dev.write(board.toBeWritten(line, value));
            }
        }


    }

    private void notifyChangeEvent(String device_address, int val) {

        Freedomotic.logger.log(Level.INFO, "Sending I2C protocol read event for object address ''{0}''. It''s readed status is {1}", new Object[]{device_address, val});
        //building the event
        ProtocolRead event = new ProtocolRead(this, "i2c", device_address); //IP:PORT:RELAYLINE

        if (val == 1) {
            event.addProperty("isOn", "true");
        } else {
            event.addProperty("isOn", "false");
        }

        //adding some optional information to the event
        //event.addProperty("boardIP", board.getIpAddress());
        //event.addProperty("boardPort", new Integer(board.getPort()).toString());
        //event.addProperty("relayLine", new Integer(relayLine).toString());
        //publish the event on the messaging bus
        this.notifyEvent(event);
    }

    @Override
    protected boolean canExecute(Command c) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }
}