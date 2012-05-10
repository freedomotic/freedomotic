 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.api.Sensor;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.exceptions.UnableToExecuteException;
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
public class TrackingReadSocket extends Sensor {

    OutputStream out;
    boolean connected = false;
    final int SLEEP_TIME = 1000;
    final int NUM_MOTE = new Integer(Freedomotic.config.getIntProperty("KEY_SIMULATED_PERSON_COUNT",3));
    private static int PORT = 1111, maxConnections = -1; //illimited

    public TrackingReadSocket() {
        super("Tracking Simulator (Read Socket)", "/it.nicoletti.test/tracking-simulator-read-socket.xml");
        setDescription("It simulates a motes WSN that send information about movable sensors position, read from a socket (port:" + PORT + ")");
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
        int id = 0, x = -1, y = -1;
        StringTokenizer tokenizer = null;

        try {
            tokenizer = new StringTokenizer(in);
            id = new Integer(tokenizer.nextToken()).intValue();
            x = new Integer(tokenizer.nextToken()).intValue();
            y = new Integer(tokenizer.nextToken()).intValue();
        } catch (Exception ex) {
            System.out.println("Error while parsing client input." + "\n" + in + "\ntoken count: " + tokenizer.countTokens());
        }
//          MUST BE REIMPLEMENTED
//        for (EnvObjectLogic object : EnvObjectPersistence.getObjectList()) {
//            if (object instanceof it.freedomotic.objects.impl.Person){
//                Person person = (Person)object;
//                Point position = inventPosition();
//                person.getPojo().setCurrentRepresentation(0);
//                person.getPojo().getCurrentRepresentation().setOffset((int)position.getX(), (int)position.getY());
//                person.setChanged(true);
//            }
//        }
    }

    @Override
    protected void onInformationRequest() throws IOException, UnableToExecuteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onRun() {
        //do nothing
    }

    private class ClientInputReader implements Runnable {

        private Socket server;
        private String line, input;

        ClientInputReader(Socket server) {
            this.server = server;
            System.out.println("New client connected to server on " + server.getInetAddress());
        }

        public void run() {
            try {
                // Get input from the client
                DataInputStream in = new DataInputStream(server.getInputStream());
                PrintStream out = new PrintStream(server.getOutputStream());

                while ((line = in.readLine()) != null && !line.equals(".") && isRunning) {
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
