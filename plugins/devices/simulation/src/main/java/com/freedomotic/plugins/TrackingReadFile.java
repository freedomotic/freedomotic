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
package com.freedomotic.plugins;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.PluginStartupException;
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
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Enrico
 */
public class TrackingReadFile extends Protocol {

    private static final Logger LOG = Logger.getLogger(TrackingReadFile.class.getName());
    private OutputStream out;
    private boolean connected = false;
    private final int SLEEP_TIME = 1000;
    private ArrayList<WorkerThread> workers = null;

    /**
     *
     */
    public TrackingReadFile() {
        super("Tracking Simulator (Read file)", "/simulation/tracking-simulator-read-file.xml");
        setDescription("Simulates a motes WSN. Positions are read from a text file");
    }

    @Override
    public void onStart() throws PluginStartupException {
        try {
            workers = new ArrayList<WorkerThread>();

            File dir = new File(Info.PATHS.PATH_DEVICES_FOLDER + "/simulation/data/motes");
            String[] extensions = new String[]{"mote"};
            System.out.println("Getting all .mote files in " + dir.getCanonicalPath());
            List<File> motes = (List<File>) FileUtils.listFiles(dir, extensions, true);
            if (!motes.isEmpty()) {
                for (File file : motes) {
                    readMoteFile(file);
                }
                for (WorkerThread workerThread : workers) {
                    workerThread.start();
                }
            } else {
                throw new PluginStartupException("No .mote files found.");
            }
        } catch (IOException ex) {
            throw new PluginStartupException("Error during file .motes loading." + ex.getMessage(), ex);
        }
    }

    private void readMoteFile(File f) {
        FileReader fr = null;
        ArrayList<Coordinate> coord = new ArrayList<Coordinate>();
        String userId = FilenameUtils.removeExtension(f.getName());

        try {
            //File f = new File(Info.PATHS.PATH_DEVICES_FOLDER + "/simulation/mote-" + n + ".txt");
            LOG.log(Level.INFO, "Reading coordinates from file " + f.getAbsolutePath());
            fr = new FileReader(f);

            BufferedReader br = new BufferedReader(fr);
            String line;

            while ((line = br.readLine()) != null) {
                //tokenize string
                StringTokenizer st = new StringTokenizer(line);
                LOG.log(Level.INFO, "Mote " + userId + " coordinate added " + line);

                Coordinate c = new Coordinate();
                c.setUserId(userId);
                c.setX(new Integer(st.nextToken()));
                c.setY(new Integer(st.nextToken()));
                c.setTime(new Integer(st.nextToken()));
                coord.add(c);
            }

            fr.close();

            WorkerThread wt = new WorkerThread(this, coord);
            workers.add(wt);
        } catch (FileNotFoundException ex) {
            LOG.log(Level.SEVERE, "Coordinates file not found for mote " + userId);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "IOException: " + ex);
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "IOException: " + ex);
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
