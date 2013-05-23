package it.freedomotic.plugins;

import it.freedomotic.api.Sensor;

import it.freedomotic.app.Freedomotic;

import it.freedomotic.exceptions.UnableToExecuteException;

import it.freedomotic.plugins.fromfile.WorkerThread;

import it.freedomotic.util.Info;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class TrackingReadFile
        extends Sensor {

    OutputStream out;
    boolean connected = false;
    final int SLEEP_TIME = 1000;
    int NUM_MOTE = 3;
    ArrayList<WorkerThread> workers = new ArrayList<WorkerThread>();

    public TrackingReadFile() {
        super("Tracking Simulator (Read file)", "/it.nicoletti.test/tracking-simulator-read-file.xml");
        setDescription("It simulates a motes WSN that send information about movable sensors position. Positions are read from a text file");
    }

    @Override
    public void onStart() {
        NUM_MOTE = new Integer(Freedomotic.config.getIntProperty("KEY_SIMULATED_PERSON_COUNT", 3));

        for (int i = 0; i < NUM_MOTE; i++) {
            readMoteFile(i);
        }

        for (WorkerThread workerThread : workers) {
            workerThread.start();
        }
    }

    private void readMoteFile(int n) {
        FileReader fr;
        ArrayList<Coordinate> coord = new ArrayList<Coordinate>();

        try {
            File f = new File(Info.getApplicationPath() + "/plugins/mote-" + n + ".txt");
            System.out.println("\nReading coordinates from file " + f.getAbsolutePath());
            fr = new FileReader(f);

            BufferedReader br = new BufferedReader(fr);
            String line;

            while ((line = br.readLine()) != null) {
                //tokenize string
                StringTokenizer st = new StringTokenizer(line);
                System.out.println("   Mote " + n + " coordinate added " + line);

                Coordinate c = new Coordinate();
                c.setId(n);
                c.setX(new Integer(st.nextToken()));
                c.setY(new Integer(st.nextToken()));
                c.setTime(new Integer(st.nextToken()));
                coord.add(c);
            }

            fr.close();

            WorkerThread wt = new WorkerThread(this, coord);
            workers.add(wt);
        } catch (FileNotFoundException ex) {
            System.out.println("Coordinates file not found for mote " + n);
        } catch (IOException ex) {
            Logger.getLogger(TrackingReadFile.class.getName()).log(Level.SEVERE, null, ex);
        }
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
}
