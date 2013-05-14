/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wilsonkong888.wireless_powermeter;
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
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.EnvObjectPersistence;

/**
 *
 * @author Mauro Cicolella
 */
public class Wireless_powermeter extends Protocol implements SerialPortEventListener {
    
    SerialPort serialPort;
    boolean set_address=false; 
    /** The output stream to the serial port */
    private OutputStream output;
    private InputStream instream;
    int no_of_object=0,object_counter=0;
    /** Milliseconds to block while waiting for port open */
    private static final int TIME_OUT = 2000;
    /** Default bits per second for COM port. */
    private static final int DATA_RATE = 9600;
    // Serial Receive Buffer Size
    public static final int BUFFER_SIZE = 100;
    SerialConnectionProvider serial;
    byte[] receive1 = new byte[BUFFER_SIZE];
    // Hold the address of power meter
    byte[] address_all = new byte[15];
    // Pointer of the receiver buffer
    int buffer_ptr;
    ProtocolRead  event;
    // error_serial turns "true" when error occur
    private Boolean error_serial=false;


    public Wireless_powermeter() {
        super("Wireless_powermeter", "/com.wilsonkong888.wireless_powermeter/wireless_powermeter-manifest.xml");
        setPollingWait((configuration.getIntProperty("polling_rate", 2000))/2); 
    }

    public void initialize() {
        try {
            
            no_of_object=0;
            object_counter=0;
            // Search for object with protocol "wireless_powermeter"
            for (EnvObjectLogic object : EnvObjectPersistence.getObjectByProtocol("wireless_powermeter")){
                String address = object.getPojo().getPhisicalAddress();
                String name = object.getPojo().getName();
                //Freedomotic.logger.info(address);
                //Freedomotic.logger.info(name);
                address_all[no_of_object]=Byte.valueOf(address);
                if(no_of_object<14)
                    no_of_object++;
            }
            
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
        byte[] set_channel = new byte[] {(byte)0x64, (byte)0x34, (byte)0x32, (byte)0x30, (byte)0x39, (byte)0x65};
        byte[] read_command = new byte[] {(byte)0x64, (byte)0x63};
        //called in a loop while this plugin is running
        //loops waittime is specified using setPollingWait()
        try{
            int i=0;  
            byte address;

            if(error_serial==false){  
                
                if(set_address==true){
                    set_address=false;
                    // Clear the serial input buffer
                    buffer_ptr=0;
                    for(i=0;i<BUFFER_SIZE;i++)
                        receive1[i]=0;            

                    // Send Read Packet
                    for(i=0;i<read_command.length;i++ )
                    {
                        output.write(read_command[i]);
                    }
                    //Freedomotic.logger.info("Send read command");
                    
                }else{
                    buffer_ptr=0;
                    set_address=true;
                    // Set the channel
                    address=(byte) (address_all[object_counter]%10);
                    set_channel[4]=(byte) ((byte)address + 0x30);
                    address=(byte) (address_all[object_counter]%100/10);
                    set_channel[3]=(byte) ((byte)address + 0x30);   

                    // Send "Set Channel" Packet
                    for(i=0;i<set_channel.length;i++ )
                    {
                        output.write(set_channel[i]);
                    }                  
                    //Freedomotic.logger.info("Send to Address: "+object_counter+" , "+(int)send[5]);
                    //Freedomotic.logger.info("Send set address command");
                    object_counter++;
                    if(object_counter==no_of_object){
                        object_counter=0;
                    }
                    
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
            String address;
            double power,voltage,current,pf,kwh;
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
                                //Freedomotic.logger.info("Serial Event read "+buffer_ptr+":"+receive1[buffer_ptr]);
                                if(buffer_ptr!=BUFFER_SIZE-1)
                                    buffer_ptr++;   

                            }


                    }

                    if(buffer_ptr>=27&&error_serial==false&&set_address==false){


                        //Freedomotic.logger.config("Receive successfully!");
                        for(i=0;i<BUFFER_SIZE;i++){
                            temp=receive1[i] & (byte)0xF;
                            receive1[i] = (byte)temp;
                        }
                        address=Byte.toString(receive1[26]);
                        buffer_ptr=0;
                        // If receive packet beginning with A:
                        if(receive1[0]==0xA){            
                            power = receive1[1] * 1000 + receive1[2] * 100 + receive1[3] * 10 + receive1[4] + receive1[5] * 0.1;
                            voltage = receive1[6] * 100 + receive1[7] * 10 + receive1[8] + receive1[9] * 0.1;
                            pf = receive1[23] * 10 + receive1[24] + receive1[25] * 0.1;                    
                            current = receive1[18] * 10 + receive1[19] + receive1[20] * 0.1 + receive1[21] * 0.01 + receive1[22] * 0.001;
                            current=current*1000;
                            kwh = receive1[10] * 1000 + receive1[11] * 100 + receive1[12] * 10 + receive1[13] + receive1[14] * 0.1 + receive1[15] * 0.01 + receive1[16] * 0.001 + receive1[17] * 0.0001;
                            
                            SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd 'at' HH:mm:ss");

                            Freedomotic.logger.info(ft.format(date)+": Wireless_powermeter "+(int)receive1[26]+" Reads Power=" + String.format("%.6g%n",power)+"W, Voltage="+String.format("%.6g%n",voltage)+"V, Current="+String.format("%.6g%n",current)+"mA, Power Factor="+String.format("%.6g%n",pf)+", Energy="+String.format("%.6g%n",kwh)+"kwh"); 

                            // Send data read event
                            event = new ProtocolRead(this, "wireless_powermeter",address );
                            if (voltage>1){
                                event.addProperty("wireless_powermeter.voltage", String.format("%.6g%n",voltage));
                            }else{
                                event.addProperty("wireless_powermeter,voltage", "1");
                            }

                            if (pf>1){
                                event.addProperty("wireless_powermeter.power_factor", String.format("%.6g%n",pf));
                            }else{
                                event.addProperty("wireless_powermeter,power_factor", "1");
                            }
                            if (power>1){
                                event.addProperty("wireless_powermeter.power", String.format("%.6g%n",power));
                            }else{
                                event.addProperty("wireless_powermeter.power", "1");
                            }
                            if (current>1){
                                event.addProperty("wireless_powermeter.current", String.format("%.6g%n",current));
                            }else{
                                event.addProperty("wireless_powermeter.current", "1");
                            }
                            if (kwh>1){
                                event.addProperty("wireless_powermeter.energy", String.format("%.6g%n",kwh));
                            }else{
                                event.addProperty("wireless_powermeter.energy", "1");
                            }
                            event.addProperty("wireless_powermeter.powered", "true");
                            // Both work:
                            Freedomotic.sendEvent(event);
                            //this.notifyEvent(event);
                        
                        }
                        // If receive packet beginning with B:
                        if(receive1[0]==0xB){            
                            power = receive1[1] * 1000 + receive1[2] * 100 + receive1[3] * 10 + receive1[4] + receive1[5] * 0.1;
                            voltage = receive1[6] * 100 + receive1[7] * 10 + receive1[8] + receive1[9] * 0.1;
                            pf = receive1[23] * 10 + receive1[24] + receive1[25] * 0.1;                    
                            current = receive1[18] * 10 + receive1[19] + receive1[20] * 0.1 + receive1[21] * 0.01 + receive1[22] * 0.001;
                            current=current*1000;
                            
                            SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd 'at' HH:mm:ss");

                            Freedomotic.logger.info(ft.format(date)+": Wireless_powermeter Reads Power=" + String.format("%.6g%n",power)+"W, Voltage="+String.format("%.6g%n",voltage)+"V, Current="+String.format("%.6g%n",current)+"mA, Power Factor="+String.format("%.6g%n",pf)+", Energy=No reading"); 

                            // Send data read event
                            event = new ProtocolRead(this, "wireless_powermeter", address);
                            if (voltage>1){
                                event.addProperty("wireless_powermeter.voltage", String.format("%.6g%n",voltage));
                            }else{
                                event.addProperty("wireless_powermeter,voltage", "1");
                            }

                            if (pf>1){
                                event.addProperty("wireless_powermeter.power_factor", String.format("%.6g%n",pf));
                            }else{
                                event.addProperty("wireless_powermeter,power_factor", "1");
                            }
                            if (power>1){
                                event.addProperty("wireless_powermeter.power", String.format("%.6g%n",power));
                            }else{
                                event.addProperty("wireless_powermeter.power", "1");
                            }
                            if (current>1){
                                event.addProperty("wireless_powermeter.current", String.format("%.6g%n",current));
                            }else{
                                event.addProperty("wireless_powermeter.current", "1");
                            }

                            event.addProperty("wireless_powermeter.powered", "true");
                            // Both work:
                            Freedomotic.sendEvent(event);
                            //this.notifyEvent(event);
                        
                        }
                        // If receive packet beginning with C:
                        if(receive1[0]==0xC){            
                            power = receive1[1] * 1000 + receive1[2] * 100 + receive1[3] * 10 + receive1[4] + receive1[5] * 0.1;
                            voltage = receive1[6] * 100 + receive1[7] * 10 + receive1[8] + receive1[9] * 0.1;
                 
                            current = receive1[18] * 10 + receive1[19] + receive1[20] * 0.1 + receive1[21] * 0.01 + receive1[22] * 0.001;
                            current=current*1000;
                            
                            SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd 'at' HH:mm:ss");

                            Freedomotic.logger.info(ft.format(date)+": Wireless_powermeter Reads Power=" + String.format("%.6g%n",power)+"W, Voltage="+String.format("%.6g%n",voltage)+"V, Current="+String.format("%.6g%n",current)+"mA, Power Factor=No reading, Energy=No reading"); 

                            // Send data read event
                            event = new ProtocolRead(this, "wireless_powermeter", address);
                            if (voltage>1){
                                event.addProperty("wireless_powermeter.voltage", String.format("%.6g%n",voltage));
                            }else{
                                event.addProperty("wireless_powermeter,voltage", "1");
                            }

                            if (power>1){
                                event.addProperty("wireless_powermeter.power", String.format("%.6g%n",power));
                            }else{
                                event.addProperty("wireless_powermeter.power", "1");
                            }
                            if (current>1){
                                event.addProperty("wireless_powermeter.current", String.format("%.6g%n",current));
                            }else{
                                event.addProperty("wireless_powermeter.current", "1");
                            }

                            event.addProperty("wireless_powermeter.powered", "true");
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
