/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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
package com.freedomotic.plugins;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.plugins.fromfile.WorkerThread;
import com.freedomotic.reactions.Command;
import com.freedomotic.settings.Info;
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
public class TrackingReadFile extends Protocol {

    OutputStream out;
    boolean connected = false;
    final int SLEEP_TIME = 1000;
    int NUM_MOTE = 3;
    ArrayList<WorkerThread> workers = new ArrayList<WorkerThread>();

    /**
     *
     */
    public TrackingReadFile() {
        super("Tracking Simulator (Read file)", "/simulation/tracking-simulator-read-file.xml");
        setDescription("It simulates a motes WSN that send information about movable sensors position. Positions are read from a text file");
    }

    @Override
    public void onStart() {
        NUM_MOTE = Integer.valueOf(getApi().getConfig().getIntProperty("KEY_SIMULATED_PERSON_COUNT", 3));

        for (int i = 0; i < NUM_MOTE; i++) {
            readMoteFile(i);
        }

        for (WorkerThread workerThread : workers) {
            workerThread.start();
        }
    }

    private void readMoteFile(int n) {
        FileReader fr = null;
        ArrayList<Coordinate> coord = new ArrayList<Coordinate>();

        try {
            File f = new File(Info.PATHS.PATH_PLUGINS_FOLDER + "/mote-" + n + ".txt");
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
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                Logger.getLogger(TrackingReadFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    protected void onRun() {
        //do nothing
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        //do nothing
    }

    @Override
    protected boolean canExecute(Command c) {
        //do nothing
        return true;
    }

    @Override
    protected void onEvent(EventTemplate event) {
        //do nothing
    }
}
