/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.freedomotic.plugins;

import com.freedomotic.api.Sensor;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.UnableToExecuteException;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 *
 * @author Enrico
 */
public class TrackingReadSocket
        extends Sensor {

    OutputStream out;
    boolean connected = false;
    final int SLEEP_TIME = 1000;
    final int NUM_MOTE = 3;
    private static int PORT = 1111; //illimited
    private static int maxConnections = -1; //illimited

    public TrackingReadSocket() {
        super("Tracking Simulator (Read Socket)", "/test/tracking-simulator-read-socket.xml");
        setDescription("It simulates a motes WSN that send information about movable sensors position, read from a socket (port:"
                + PORT + ")");
    }

    @Override
    public void onStart() {
        createServerSocket();
    }

    private void createServerSocket() {
        int i = 0;

        try {
            ServerSocket listener = new ServerSocket(PORT);
            Socket server;
            System.out.println("\nStart listening on server socket " + listener.getInetAddress());

            while (((i++ < maxConnections) || (maxConnections == -1)) && (isRunning)) {
                ClientInputReader connection;
                server = listener.accept();
                connection = new ClientInputReader(server);

                Thread t = new Thread(connection);
                t.start();
            }
        } catch (IOException ioe) {
            System.out.println("IOException on socket listen: " + ioe);
            ioe.printStackTrace();
        }
    }

    private void parseInput(String in) {
        int id = 0;
        int x = -1;
        int y = -1;
        StringTokenizer tokenizer = null;

        try {
            tokenizer = new StringTokenizer(in);
            id = new Integer(tokenizer.nextToken()).intValue();
            x = new Integer(tokenizer.nextToken()).intValue();
            y = new Integer(tokenizer.nextToken()).intValue();
        } catch (Exception ex) {
            System.out.println("Error while parsing client input." + "\n" + in + "\ntoken count: "
                    + tokenizer.countTokens());
        }

//          MUST BE REIMPLEMENTED
//        for (EnvObjectLogic object : EnvObjectPersistence.getObjectList()) {
//            if (object instanceof com.freedomotic.objects.impl.Person){
//                Person person = (Person)object;
//                Point position = inventPosition();
//                person.getPojo().setCurrentRepresentation(0);
//                person.getPojo().getCurrentRepresentation().setOffset((int)position.getX(), (int)position.getY());
//                person.setChanged(true);
//            }
//        }
    }

    @Override
    protected void onInformationRequest()
            throws IOException, UnableToExecuteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onRun() {
        //do nothing
    }

    private class ClientInputReader
            implements Runnable {

        private Socket server;
        private String line;
        private String input;

        ClientInputReader(Socket server) {
            this.server = server;
            System.out.println("New client connected to server on " + server.getInetAddress());
        }

        public void run() {
            try {
                // Get input from the client
                DataInputStream in = new DataInputStream(server.getInputStream());
                PrintStream out = new PrintStream(server.getOutputStream());

                while (((line = in.readLine()) != null) && !line.equals(".") && isRunning) {
                    System.out.println("Readed from socket: " + line);
                    parseInput(line);
                }

                System.out.println("Closing socket connection " + server.getInetAddress());
                server.close();
            } catch (IOException ioe) {
                System.out.println("IOException on socket listen: " + ioe);
                ioe.printStackTrace();
            }
        }
    }
}
