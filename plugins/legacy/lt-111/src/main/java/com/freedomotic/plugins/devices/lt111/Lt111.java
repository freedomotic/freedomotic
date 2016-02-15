/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.plugins.devices.lt111;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.logging.Logger;
import com.freedomotic.events.ProtocolRead;

import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.EnvObjectPersistence;
import java.io.IOException;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
        
public class Lt111
        extends Protocol {

    List<String> address_list = new ArrayList<String>();
    ServerSocket serverSocket;
    Socket connectionSocket;
    Client client;
    int port;
    private static final Logger LOG = Logger.getLogger(Lt111.class.getName());
    final int POLLING_WAIT;

    public Lt111() {
        //every plugin needs a name and a manifest XML file
        super("Lt111", "/lt111/lt111-manifest.xml");
        //read a property from the manifest file below which is in
        //FREEDOMOTIC_FOLDER/plugins/devices/com.freedomotic.hello/hello-world.xml
        POLLING_WAIT = configuration.getIntProperty("polling_rate", 2000);
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
        LOG.info("Lt111 onRun() logs this message every " + "POLLINGWAIT=" + POLLING_WAIT
                + "milliseconds");
        for (short i = 0; i < address_list.size(); i++) {
            try{
                System.out.println("Wilson debug: Start class with address "+address_list.get(i));
                client = new Client(address_list.get(i),POLLING_WAIT);
                client.start();
            }catch(Exception e){
                System.out.println("LT-111 OnStart Error: " + e);
            } 
        }
        //at the end of this method the system waits POLLINGTIME 
        //before calling it again. The result is this log message is printed
        //every 2 seconds (2000 millisecs)
    }

    @Override
    protected void onStart() {
        LOG.info("Lt111 plugin is started");
        Socket clientSocket = null;
        address_list.clear();
       for (EnvObjectLogic object : EnvObjectPersistence.getObjectByProtocol("LT111")){
            String address = object.getPojo().getPhisicalAddress();
            String name = object.getPojo().getName();
            address_list.add(address);
            System.out.println("Wilson debug: Modbus Address"+address);
        }
    }

    @Override
    protected void onStop() {
        LOG.info("Lt111 plugin is stopped ");

    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        LOG.info("Lt111 plugin receives a command called " + c.getName() + " with parameters "
                + c.getProperties().toString());
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


class Client extends Thread
{

    private DataOutputStream outToClient;
    private byte[] read_bytes;
    int logging_interval;
    int polling;
    String address;
    DataOutputStream outToServer;
    DataInputStream inFromServer;
    public Client(String ip_address,int POLLING_WAIT)
    {
        address=ip_address;
        polling=POLLING_WAIT;
    }
    public void run() 
    {

        try{
            
            String[] ip_address = address.split(":");
            System.out.println("Wilson debug: IP:"+ip_address[0]+", "+ip_address[1]);
            
            Socket clientSocket = new Socket(ip_address[0], Integer.valueOf(ip_address[1]));
            
            clientSocket.setSoTimeout(2000); // 2 seconds timeout
            System.out.println("Wilson debug: Initialze this address:"+address);
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            
            inFromServer = new DataInputStream(clientSocket.getInputStream());
            
            /*
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

            Runnable periodicTask = new Runnable() {
                public void run() {
                        // Invoke method(s) to do the work
                        read_realpower();
                }
            };

            executor.scheduleAtFixedRate(periodicTask, 0, polling, TimeUnit.MILLISECONDS);
            */
            
            read_realpower();
            outToServer.close();
            inFromServer.close();
            clientSocket.close();
        }catch(Exception e){
            System.out.println("LT-111 Client Error: " + e);
        }        
        //System.out.println("Runnning");    
    }
    public void read_realpower() 
    {
       byte[] read_command = new byte[] {(byte)0x01, (byte)0x03, (byte)0x00, (byte)0x48, (byte)0x00, (byte)0x06, (byte)0x45, (byte)0xDE};

       try{
            
            //int length = inFromServer.readInt(); 
            short CRC;
            
            byte[] message = new byte[17];
            //String modifiedSentence = inFromServer.readLine();
            
            // read length of incoming message
            CRC=crc16(read_command);
            read_command[6]=(byte)CRC;
            read_command[7]=(byte)(CRC>>>8);
            
            //System.out.println("Send bytes: "+getHex(read_command));

            outToServer.write(read_command);

            inFromServer.readFully(message, 0, message.length); // read the message
            CRC=crc16(message);
            //System.out.println("Receive bytes: "+getHex(message));
            //System.out.println("CRC="+Integer.toHexString(CRC&0xFF));

            if(((message[15]&0xFF)==(CRC&0xFF))&&(message[16]&0xFF)==((CRC>>>8)&0xFF)){
                //System.out.println("Correct");
                float voltage=(float) (((message[4]&0xFF)+((message[3]&0xFF)*256))/100);
                short current=(short) (((message[6]&0xFF)+((message[5]&0xFF)*256))/1000);
                short power=(short) ((message[8]&0xFF)+((message[7]&0xFF)*256));
                int energy=(((message[9]&0xFF)*256*256*256)+((message[10]&0xFF)*256*256)+((message[11]&0xFF)*256)+(message[12]&0xFF))/3200;
                short pf= (short) (((message[14]&0xFF)+(message[13]&0xFF)*256)/10);
                Date date = new Date();
                SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd 'at' HH:mm:ss");
                //System.out.println(ft.format(date)+" "+address+" read: Voltage:"+voltage+"V, Current:"+current+"mA, Power:"+power+"W, Energy:"+energy+"kwh, Power Factor:"+pf);
                // Send data read event
                ProtocolRead event = new ProtocolRead(this, "LT111",address );
                event.addProperty("lt111.current", Short.toString(current));
                event.addProperty("lt111.power", Short.toString(power));
                event.addProperty("lt111.voltage",  String.format("%.6g%n",voltage));
                event.addProperty("lt111.energy", Integer.toString(energy));
                event.addProperty("lt111.power_factor", Short.toString(pf));
                Freedomotic.sendEvent(event);                
            }
            
            //System.out.println(getHex(message));
            

        }catch(Exception e){
            System.out.println("LT-111 send Error: " + e);
        } 
    } 

     public String getHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b&0xff)+",");
        }
        return result.toString();

    }
        public short crc16(byte[] data)
     {
         short reg_crc=(short)0xFFFF;
         for (int i=0; i<(data.length-2); i++) {
             reg_crc ^= (data[i]&0xFF);
             for (int j=0; j<8; j++) {
                    if((reg_crc & 0x0001)==0x01){ /* LSB(b0)=1 */ 
                            reg_crc=(short)((reg_crc&0xFFFF)>>>1); 
                            reg_crc ^= 0xA001;
                    }else{ 
                            reg_crc=(short)((reg_crc&0xFFFF) >>> 1); 
                    }
             }
         }      
         return reg_crc;
     } 
    // ignores the higher 16 bits
    public float toFloat( int hbits )
    {
        int mant = hbits & 0x03ff;            // 10 bits mantissa
        int exp =  hbits & 0x7c00;            // 5 bits exponent
        if( exp == 0x7c00 )                   // NaN/Inf
            exp = 0x3fc00;                    // -> NaN/Inf
        else if( exp != 0 )                   // normalized value
        {
            exp += 0x1c000;                   // exp - 15 + 127
            if( mant == 0 && exp > 0x1c400 )  // smooth transition
                return Float.intBitsToFloat( ( hbits & 0x8000 ) << 16
                                                | exp << 13 | 0x3ff );
        }
        else if( mant != 0 )                  // && exp==0 -> subnormal
        {
            exp = 0x1c400;                    // make it normal
            do {
                mant <<= 1;                   // mantissa * 2
                exp -= 0x400;                 // decrease exp by 1
            } while( ( mant & 0x400 ) == 0 ); // while not normal
            mant &= 0x3ff;                    // discard subnormal bit
        }                                     // else +/-0 -> +/-0
        return Float.intBitsToFloat(          // combine all parts
            ( hbits & 0x8000 ) << 16          // sign  << ( 31 - 15 )
            | ( exp | mant ) << 13 );         // value << ( 23 - 10 )
    }
}

}


