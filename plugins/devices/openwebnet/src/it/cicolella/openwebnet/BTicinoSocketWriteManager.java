package it.cicolella.openwebnet;

/*****************************************************************
 * BTicinoSocketWriteManager.java                                *
 * Original code:			          -              *
 * date          : Sep 8, 2004                                   *
 * copyright     : (C) 2005 by Bticino S.p.A. Erba (CO) - Italy  *
 *                     Embedded Software Development Laboratory  *
 * license       : GPL                                           *
 * email         : 		             		         *
 * web site      : www.bticino.it; www.myhome-bticino.it         *
 *                                                               *
 * Modified and adapted for Freedomotic project by:              *
 * Mauro Cicolella - Enrico Nicoletti                            *
 * date          : 24/11/2011                                    *
 * web site      : www.freedomotic.com                           *
 *****************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
import it.freedomotic.app.Freedomotic;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Description:
 * Handle of socket commands, open/close connection, command sending
 *
 */
public class BTicinoSocketWriteManager {

    /*
     * state 0 = not connected
     * state 1 = request sent, waiting for reply
     * state 2 = waiting for ack or nack. If ack chenge to state 3
     * state 3 = connected
     */
    static BTicinoReadWebserverThread readTh = null; //thread for receiving chars from gateway
    static SocketTimeoutThread timeoutThread = null; // timeout thread
    static private int socketCommandState = 0;  //socket command state
    static String responseLine = null; // receiving string from gateway
    static final String SOCKET_COMMAND = "*99*0##";
    static final String SOCKET_SUPER_COMMAND = "*99*9##";
    Socket socket = null;
    BufferedReader input = null;
    PrintWriter output = null;

    /**
     * Costructor
     *
     */
    public BTicinoSocketWriteManager() {
    }

    public static void setSocketCommandState(int socketCommandStateValue) {
        socketCommandState = socketCommandStateValue;
    }

    public static int getSocketCommandState() {
        return (socketCommandState);
    }

    /**
     * state 0 = not connected
     * state 1 = request sent on socket command, waiting for reply
     * state 2 = waiting for ack or nack. If ack set state to 3
     * state 3 = connected
     *
     * @param ip gateway
     * @param port gateway
     * @param passwordOpen password open gateway set to
     * @return true if connection OK, false if connection failed
     */
    public boolean connect(String ip, int port, long passwordOpen) {
        try {
            //Freedomotic.logger.info("Trying to connect to ethernet gateway on address " + ip + ':' + port);
            OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Trying to connect to ethernet gateway on address " + ip + ':' + port);
            socket = new Socket(ip, port);
            setTimeout(0);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //Freedomotic.logger.info("Buffer reader created"); // FOR DEBUG USE
            OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Buffer reader created");
            output = new PrintWriter(socket.getOutputStream(), true);
            //Freedomotic.logger.info("Print Writer created"); // FOR DEBUG USE
            OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Print Writer created");
          } catch (IOException e) {
            Freedomotic.logger.severe("Connection impossible! " + e.toString());
            OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Connection impossibile!");
            this.close();
        }
        if (socket != null) {
            while (true) {
                readTh = null;
                readTh = new BTicinoReadWebserverThread(socket, input, 0);
                readTh.start();
                try {
                    readTh.join();
                } catch (InterruptedException e1) {
                    Freedomotic.logger.severe("----- ERROR readThread.join() during connection: " + e1.toString());
                    OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"----- ERROR readThread.join() during connection: " + e1.toString());
                 }

                if (responseLine != null) {
                    if (getSocketCommandState() == 0) { // sent request for connection
                        Freedomotic.logger.info("----- STATE 0 NOT CONNECTED ----- ");
                        OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"----- STATE 0 NOT CONNECTED ----- ");
                        Freedomotic.logger.info("Rx: " + responseLine);
                        OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Rx: " + responseLine);
                        if (responseLine.equals(OpenWebNet.MSG_OPEN_ACK)) {
                            Freedomotic.logger.info("Tx: " + SOCKET_COMMAND);
                            OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Tx: " + SOCKET_COMMAND);
                            output.write(SOCKET_COMMAND); //commands
                            output.flush();
                            setSocketCommandState(1); // waiting for reply
                            setTimeout(0);
                        } else {
                            //if no connection close the socket
                            Freedomotic.logger.info("Closing socket to server " + ip);
                            OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Closing socket to server " + ip);
                            this.close();
                            break;
                        }
                    } else if (getSocketCommandState() == 1) { //sent type service request
                        Freedomotic.logger.info("----- STATE 1 -----");
                        OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"----- STATE 1 -----");
                        Freedomotic.logger.info("Rx: " + responseLine);
                        OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Rx: " + responseLine);
                        if (responseLine.equals(OpenWebNet.MSG_OPEN_ACK)) {
                            Freedomotic.logger.info("Ack received, state = 3");
                            OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Ack received, state = 3");
                            setSocketCommandState(3);
                            break;
                        } else {
                            Freedomotic.logger.severe("Connection impossible!");
                            OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Connection impossibile!");
                            //if no connection close socket
                            Freedomotic.logger.severe("Closing server socket " + ip);
                            OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Closing server socket " + ip);
                            this.close();
                            break;
                        }
                        //	}
                    } else if (getSocketCommandState() == 2) {
                        Freedomotic.logger.info("----- STATE 2 -----");
                        OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"----- STATE 2 -----");
                        Freedomotic.logger.info("Rx: " + responseLine);
                        OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Rx: " + responseLine);
                        if (responseLine.equals(OpenWebNet.MSG_OPEN_ACK)) {
                            Freedomotic.logger.info("Connection OK");
                            OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Connection OK");
                            setSocketCommandState(3);
                            break;
                        } else {
                            Freedomotic.logger.severe("Connection impossible!");
                            OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Connection impossible!");
                            Freedomotic.logger.severe("Closing server socket " + ip);
                            OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Closing server socket " + ip);
                            this.close();
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    Freedomotic.logger.severe("--- NULL server response ---");
                    OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"--- NULL server response ---");
                    this.close();
                    break;// else of if(responseLine != null)
                }
            } // close while(true)
        } else {
        }
        if (getSocketCommandState() == 3) {
            return true;
        } else {
            return false;
        }
    }//close connect()

    /**
     * Close commands socket and set state = 0
     *
     */
    public void close() {
        if (socket != null) {
            try {
                socket.close();
                socket = null;
                //socketCommandState = 0;
                setSocketCommandState(0);
                Freedomotic.logger.info("-----Socket closed correctly-----"); //FOR DEBUG USE
                OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"-----Socket closed correctly-----");
              } catch (IOException e) {
                Freedomotic.logger.severe("Socket error: " + e.toString());
                OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Socket error: " + e.toString());
              }
        }
    }

    /**
     * Method for sending own command
     *
     * @param commandOpen
     * @return 0 command sent, 1 command not sent
     */
    public int send(String commandOpen) {
        Freedomotic.logger.info("Sending frame: " + commandOpen + " to the gateway");
        OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Sending frame: " + commandOpen + " to the gateway");
        output.write(commandOpen);
        output.flush();
        do {
            setTimeout(0);
            readTh = null;
            readTh = new BTicinoReadWebserverThread(socket, input, 0);
            readTh.start();
            try {
                readTh.join();
            } catch (InterruptedException e1) {
                Freedomotic.logger.severe("----- ERROR readThread.join() in sending command: " + e1.toString());
                OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"----- ERROR readThread.join() in sending command: " + e1.toString());
            }
            if (responseLine != null) {
                if (responseLine.equals(OpenWebNet.MSG_OPEN_ACK)) {
                    Freedomotic.logger.info("Rx: " + responseLine);
                    OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Rx: " + responseLine);
                    Freedomotic.logger.info("Command sent");// FOR DEBUG USE
                    OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Command sent");
                    this.close();
                    return 0;
                    //break;
                } else if (responseLine.equals(OpenWebNet.MSG_OPEN_NACK)) {
                    Freedomotic.logger.info("Rx: " + responseLine);
                    OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Rx: " + responseLine);
                    Freedomotic.logger.severe("Command NOT sent");
                    OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Command NOT sent");
                    this.close();
                    return 1;
                    //break;
                } else {
                    //STATE REQUEST
                    Freedomotic.logger.info("Rx: " + responseLine);
                    OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Rx: " + responseLine);
                    if (responseLine == OpenWebNet.MSG_OPEN_ACK) {
                        Freedomotic.logger.info("Command sent");// FOR DEBUG USE
                        OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Command sent");
                        this.close();
                        return 0;
                        //break;
                    } else if (responseLine == OpenWebNet.MSG_OPEN_NACK) {
                        Freedomotic.logger.severe("Command NOT sent");
                        OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Command NOT sent");
                        return 1;
                    }
                }
            } else {
                Freedomotic.logger.severe("Impossible sending command " + responseLine);
                OWNFrame.writeAreaLog(OWNUtilities.getDateTime()+" Act:"+"Impossible sending command " + responseLine);
                this.close();
                return 1;
            }
        } while (true);
    }//close send(...)

    /**
     * Timeour thread for webserver reply
     *
     * @param typeSocket: 0 = command socket, 1 = monitor socket
     */
    public void setTimeout(int typeSocket) {
        timeoutThread = null;
        timeoutThread = new SocketTimeoutThread("timeout", typeSocket);
        timeoutThread.start();
    }
}//close class

