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
import com.freedomotic.environment.EnvironmentRepository;
import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.model.geometry.FreedomPoint;
import com.freedomotic.model.geometry.FreedomPolygon;
import com.freedomotic.plugins.fromfile.WorkerThread;
import com.freedomotic.reactions.Command;
import com.freedomotic.settings.Info;
import com.google.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
public class TrackingReadFile extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(TrackingReadFile.class.getName());
    private OutputStream out;
    private boolean connected = false;
    private final int SLEEP_TIME = 1000;
    private final String DATA_TYPE = configuration.getStringProperty("data-type", "rooms");
    private final int ITERATIONS = configuration.getIntProperty("iterations", 1);
    private ArrayList<WorkerThread> workers = null;

    @Inject
    EnvironmentRepository environmentRepository;

    /**
     * Simulates a motes WSN. Positions are read from a text file.
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
            LOG.info("Getting all .mote files in {}", dir.getCanonicalPath());
            List<File> motes = (List<File>) FileUtils.listFiles(dir, extensions, true);
            if (!motes.isEmpty()) {
                for (File file : motes) {
                    switch (DATA_TYPE) {
                        case "coordinates":
                            readMoteFileCoordinates(file);
                            break;
                        case "rooms":
                            readMoteFileRooms(file);
                            break;
                        default:
                            throw new PluginStartupException("<data-type> property wrong in manifest file");
                    }
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

/**
 * Reads coordinates from file.
 * 
 * @param f file of coordinates 
 */
    private void readMoteFileCoordinates(File f) {
        FileReader fr = null;
        ArrayList<Coordinate> coord = new ArrayList<Coordinate>();
        String userId = FilenameUtils.removeExtension(f.getName());

        try {
            LOG.info("Reading coordinates from file {}", f.getAbsolutePath());
            fr = new FileReader(f);

            BufferedReader br = new BufferedReader(fr);
            String line;

            while ((line = br.readLine()) != null) {
                //tokenize string
                StringTokenizer st = new StringTokenizer(line, ",");
                LOG.info("Mote {} coordinate added {}", userId, line);
                Coordinate c = new Coordinate();
                c.setUserId(userId);
                c.setX(new Integer(st.nextToken()));
                c.setY(new Integer(st.nextToken()));
                c.setTime(new Integer(st.nextToken()));
                coord.add(c);
            }
            fr.close();
            WorkerThread wt = new WorkerThread(this, coord, ITERATIONS);
            workers.add(wt);
        } catch (FileNotFoundException ex) {
            LOG.error("Coordinates file not found for mote " + userId);
        } catch (IOException ex) {
            LOG.error("IOException: ", ex);
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                LOG.error("IOException: ", ex);
            }
        }
    }

    /**
     * Reads room names from file.
     * 
     * @param f file of room names
     */
    private void readMoteFileRooms(File f) {
        FileReader fr = null;
        ArrayList<Coordinate> coord = new ArrayList<Coordinate>();
        String userId = FilenameUtils.removeExtension(f.getName());

        try {
            LOG.info("Reading coordinates from file {}", f.getAbsolutePath());
            fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                //tokenize string
                StringTokenizer st = new StringTokenizer(line, ",");
                String roomName = st.nextToken();
                LOG.info("Mote {} coordinate added {}", userId, line);
                ZoneLogic zone = environmentRepository.findAll().get(0).getZone(roomName);
                if (!(zone == null)) {
                    FreedomPoint roomCenterCoordinate = getPolygonCenter(zone.getPojo().getShape());
                    Coordinate c = new Coordinate();
                    c.setUserId(userId);
                    c.setX(roomCenterCoordinate.getX());
                    c.setY(roomCenterCoordinate.getY());
                    c.setTime(new Integer(st.nextToken()));
                    coord.add(c);
                } else {
                    LOG.warn("Room '{}' not found.", roomName);
                }
            }
            fr.close();
            WorkerThread wt = new WorkerThread(this, coord, ITERATIONS);
            workers.add(wt);
        } catch (FileNotFoundException ex) {
            LOG.error("Coordinates file not found for mote " + userId);
        } catch (IOException ex) {
            LOG.error("IOException: ", ex);
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                LOG.error("IOException: ", ex);
            }
        }
    }

    /**
     * Returns the coordinates of the center of the polygon.
     * 
     * @param input the polygon
     * @return the coordinates of the center
     */
    private static FreedomPoint getPolygonCenter(FreedomPolygon input) {
        double x = 0, y = 0;
        for (FreedomPoint v : input.getPoints()) {
            x += v.getX();
            y += v.getY();
        }
        x /= input.getPoints().size();
        y /= input.getPoints().size();
        return new FreedomPoint((int) x, (int) y);
    }

    /**
     * Returns the plugin logger.
     * 
     * @return the logger
     */
    public Logger getLog() {
        return LOG;
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
