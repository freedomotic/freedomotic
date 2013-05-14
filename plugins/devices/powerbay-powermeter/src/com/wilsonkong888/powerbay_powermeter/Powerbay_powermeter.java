/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wilsonkong888.powerbay_powermeter;
import gnu.io.*;
import java.util.Date;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.*;
import it.freedomotic.events.ProtocolRead;
import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import it.freedomotic.serial.SerialConnectionProvider;
import java.io.IOException;


/**
 *
 * @author Mauro Cicolella
 */
public class Powerbay_powermeter extends Protocol implements SerialPortEventListener {
    
    SerialPort serialPort;

    /** The output stream to the serial port */
    private OutputStream output;
    private InputStream instream;
    
    /** Milliseconds to block while waiting for port open */
    private static final int TIME_OUT = 2000;
    /** Default bits per second for COM port. */
    private static final int DATA_RATE = 1200;
    // Serial Receive Buffer Size
    public static final int BUFFER_SIZE = 100;
    SerialConnectionProvider serial;
    byte[] receive1 = new byte[BUFFER_SIZE];
    // Pointer of the receiver buffer
    int buffer_ptr;
    ProtocolRead  event;
    // error_serial turns "true" when error occur
    private Boolean error_serial=false;
    // TX Packet for setting the power meter's address to 173
    private static final byte[] set_address = new byte[] {(byte)0xFE, (byte)0xFE, (byte)0xFE, (byte)0x68, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x68, (byte)0x0A, (byte)0x06,(byte) 0xA6, (byte)0x34, (byte)0x33,(byte)0x33,(byte)0x33,(byte)0x33,(byte)0x1C,(byte)0x16};
    // TX Packet for reading the power information   
    private static final byte[] send = new byte[] {(byte)0xFE, (byte)0xFE, (byte)0xFE, (byte)0x68, (byte)0x73, (byte)0x1, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x68, (byte)0x1, (byte)0x2,(byte) 0x49, (byte)0xC3, (byte)0x53, (byte)0x16};
    
    public Powerbay_powermeter() {
        super("Powerbay_powermeter", "/com.wilsonkong888.powerbay_powermeter/powerbay_powermeter-manifest.xml");
        setPollingWait(configuration.getIntProperty("polling_rate", 2000)); 
    }

    public void initialize() {
        try {
            int i;
            error_serial=false;
            CommPortIdentifier portId =  CommPortIdentifier.getPortIdentifier(configuration.getStringProperty("serial.port", "COM4"));
            if (portId == null) {
                    //System.out.println("Could not find COM port.");
                    error_serial=true;
                    //Freedomotic.logger.config("Could not find COM port.");
                    return;
            }

            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(),
                            TIME_OUT);

            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                            SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);

            // open the streams
            instream = serialPort.getInputStream();
            output = serialPort.getOutputStream();

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);

            // Write address 173 to the power meter
            for(i=0;i<set_address.length;i++ )
            {
                output.write(set_address[i]);
            }  
        } catch (Exception e) {
            //System.err.println(e.toString());
            Freedomotic.logger.config("Initialize error: "+e.getMessage());
            error_serial=true;
            close();
        }
    }
    
    // Close connection
    public synchronized void close() {
        try{
            // Reset all data
            event = new ProtocolRead(this, "powerbay", "01");
            event.addProperty("powerbay.voltage", "999");
            event.addProperty("powerbay.power", "999");
            event.addProperty("powerbay.current", "999");
            event.addProperty("powerbay.energy", "999");
            event.addProperty("powerbay.powered", "false");
            Freedomotic.sendEvent(event);
            if (serialPort != null) {
                    serialPort.removeEventListener();
                    serialPort.close();
                    output.close();
                    // Below code will cause error message
                    //input.close();
                    Freedomotic.logger.config("Serial close()");
            }
        }catch(Exception e){
            Freedomotic.logger.config("Close error: "+e.getMessage());
        }
    }
    @Override
    public void onStart() {
        //called when the user starts the plugin from UI
            initialize();
    }

  
    @Override
    public void onStop() {
        //called when the user stops the plugin from UI
        close();
    }

    @Override
    protected void onRun() {
        //called in a loop while this plugin is running
        //loops waittime is specified using setPollingWait()
        try{
            int i;   
            //String charArray = new String(new byte[] {(byte)0xFE, (byte)0xFE, (byte)0xFE, (byte)0x68, (byte)0x73, (byte)0x1, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x68, (byte)0x1, (byte)0x2,(byte) 0x49, (byte)0xC3, (byte)0x53, (byte)0x16});
            if(error_serial==false){    
                // Clear the serial input buffer
                buffer_ptr=0;
                for(i=0;i<BUFFER_SIZE;i++)
                    receive1[i]=0;

                for(i=0;i<send.length;i++ )
                {
                    output.write(send[i]);
                }  
            }

        }catch (IOException ex) {
            Freedomotic.logger.config("onRun function error");
            error_serial=true;
            stop();
        }
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
	public synchronized void serialEvent(SerialPortEvent oEvent) {

            if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE && error_serial==false) {
            int i,j=0;
            byte[] readBuffer = new byte[400];
            byte[] decode1 = new byte[BUFFER_SIZE*2];  
            int temp;
            double power,voltage,hertz,current,pf,kwh,time,co2;
            Date date = new Date();
             try
             {
                    //receive1[buffer_ptr] =  input.read();
                    int availableBytes = instream.available();
                    if (availableBytes > 0) {
                            // Read the serial port
                            instream.read(readBuffer, 0, availableBytes);
                            for(i=0;i<availableBytes;i++){
                                receive1[buffer_ptr]=readBuffer[i];
                                //Freedomotic.logger.config("Serial Event read="+receive1[buffer_ptr]);
                                if(buffer_ptr!=BUFFER_SIZE-1)
                                    buffer_ptr++;   

                            }


                    }

                    if(buffer_ptr==35){

                        if((receive1[0] == (byte)0xFE) && (receive1[1] == (byte)0x68)&&(error_serial==false)){
                                //Freedomotic.logger.config("Receive successfully!");
                                for(i=0;i<BUFFER_SIZE;i++){
                                    temp=receive1[i];
                                    temp=temp>>4;
                                    temp=temp & 0xF;
                                    temp=temp-3;
                                    decode1[j]=(byte)temp;
                                    j++;

                                    temp=receive1[i];
                                    temp=temp & 0xF;
                                    temp=temp-3;
                                    decode1[j]=(byte)temp;                   
                                    j++;
                                }


                        power = decode1[0x42] * 10 + decode1[0x43] + decode1[0x44] * 1000 + decode1[0x45] * 100 + decode1[0x40] * 0.1 + decode1[0x41] * 0.01;
                        voltage = decode1[0x34] * 1000 + decode1[0x35] * 100 + decode1[0x32] * 10 + decode1[0x33] + decode1[0x30] * 0.1 + decode1[0x31] * 0.01;
                        hertz = decode1[0x2E] * 10 + decode1[0x2F] + decode1[0x2C] * 0.1 + decode1[0x2D] * 0.01;
                        current = decode1[0x3A] * 10000 + decode1[0x3B] * 1000 + decode1[0x38] * 100 + decode1[0x39] * 10 + decode1[0x36] + decode1[0x37] * 0.1;
                        pf = decode1[0x3E] + decode1[0x3F] * 0.1 + decode1[0x3C] * 0.01 + decode1[0x3D] * 0.001;
                        kwh = decode1[0x24] * 1000 + decode1[0x25] * 100 + decode1[0x22] * 10 + decode1[0x23] + decode1[0x20] * 0.1 + decode1[0x21] * 0.01;
                        time = decode1[0x2A] * 100000 + decode1[0x2B] * 10000 + decode1[0x28] * 1000 + decode1[0x29] * 100 + decode1[0x26] * 10 + decode1[0x27];
                        co2 = decode1[0x1E] * 1000 + decode1[0x1F] * 100 + decode1[0x1C] * 10 + decode1[0x1D] + decode1[0x1A] * 0.1 + decode1[0x1B] * 0.01;

                        SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd 'at' HH:mm:ss");

                        Freedomotic.logger.config(ft.format(date)+": Power=" + String.format("%.6g%n",power)+"W, Voltage="+String.format("%.6g%n",voltage)+"V, Freq="+String.format("%.6g%n",hertz)+"Hz, Current="+String.format("%.6g%n",current)+"mA, Power Factor="+String.format("%.6g%n",pf)+", Energy="+String.format("%.6g%n",kwh)+"kwh, Elapse time="+String.format("%.6g%n",time)+", CO2="+String.format("%.6g%n",co2)); 

                        // Send data read event
                        event = new ProtocolRead(this, "powerbay", "01");
                        if (voltage>1){
                            event.addProperty("powerbay.voltage", String.format("%.6g%n",voltage));
                        }else{
                            event.addProperty("powerbay,voltage", "1");
                        }
                        pf=pf*100;
                        if (pf>1){
                            event.addProperty("powerbay.power_factor", String.format("%.6g%n",pf));
                        }else{
                            event.addProperty("powerbay,power_factor", "1");
                        }
                        if (power>1){
                            event.addProperty("powerbay.power", String.format("%.6g%n",power));
                        }else{
                            event.addProperty("powerbay.power", "1");
                        }
                        if (current>1){
                            event.addProperty("powerbay.current", String.format("%.6g%n",current));
                        }else{
                            event.addProperty("powerbay.current", "1");
                        }
                        if (kwh>1){
                            event.addProperty("powerbay.energy", String.format("%.6g%n",kwh));
                        }else{
                            event.addProperty("powerbay.energy", "1");
                        }
                        event.addProperty("powerbay.powered", "true");
                        // Both work:
                        Freedomotic.sendEvent(event);
                        //this.notifyEvent(event);

                        }
                    }
                    //Freedomotic.logger.config("Serial Event no of bytes="+availableBytes);
                    //Freedomotic.logger.config("Serial Event read="+input.read());


                }catch ( IOException e ){
                    error_serial=true;
                    //e.printStackTrace();
                    //System.exit(-1);
                    Freedomotic.logger.config("Serial Event error"+e.getMessage());
                    close();
                }  
            }
            // Ignore all the other eventTypes, but you should consider the other ones.
        }

}
