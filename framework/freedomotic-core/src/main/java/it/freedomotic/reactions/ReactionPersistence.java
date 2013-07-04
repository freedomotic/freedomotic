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
package it.freedomotic.reactions;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.persistence.FreedomXStream;
import it.freedomotic.util.DOMValidateDTD;
import it.freedomotic.util.Info;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.thoughtworks.xstream.XStream;

/**
 *
 * @author Enrico
 */
public class ReactionPersistence {

    private static List<Reaction> list = new ArrayList<Reaction>();        //for persistence purposes. ELEMENTS CANNOT BE MODIFIED OUTSIDE THIS CLASS

    private ReactionPersistence() {
        //avoid instance creation
    }

    public static void saveReactions(File folder) {
        if (list.isEmpty()) {
            Freedomotic.logger.warning("There are no reactions to persist, " + folder.getAbsolutePath() + " will not be altered.");
            return;
        }
        if (!folder.isDirectory()) {
            Freedomotic.logger.warning(folder.getAbsoluteFile() + " is not a valid reaction folder. Skipped");
            return;
        }
        XStream xstream = FreedomXStream.getXstream();
        deleteReactionFiles(folder);
        try {
            Freedomotic.logger.info("Saving reactions to file in " + folder.getAbsolutePath());
            for (Reaction reaction : list) {
                String uuid = reaction.getUUID();
                if (uuid == null || uuid.isEmpty()) {
                    reaction.setUUID(UUID.randomUUID().toString());
                }
                String fileName = reaction.getUUID() + ".xrea";
                FileWriter fstream = new FileWriter(folder + "/" + fileName);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(xstream.toXML(reaction)); //persist only the data not the logic
                //Close the output stream
                out.close();
                fstream.close();
            }
        } catch (Exception e) {
            Freedomotic.logger.info(e.getLocalizedMessage());
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
        }
    }

    private static void deleteReactionFiles(File folder) {
        File[] files = folder.listFiles();
        // This filter only returns object files
        FileFilter objectFileFileter = new FileFilter() {
            public boolean accept(File file) {
                if (file.isFile() && file.getName().endsWith(".xrea")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        files = folder.listFiles(objectFileFileter);
        for (File file : files) {
            file.delete();
        }
    }

    public synchronized static void loadReactions(File folder) {
        XStream xstream = FreedomXStream.getXstream();
        // This filter only returns object files
        FileFilter objectFileFileter = new FileFilter() {
            public boolean accept(File file) {
                if (file.isFile() && file.getName().endsWith(".xrea")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        File[] files = folder.listFiles(objectFileFileter);
        try {
            StringBuilder summary = new StringBuilder();
            //print an header for the index.txt file
            summary.append("#Filename \t\t #Reaction \t\t\t #Description").append("\n");
            if (files != null) {
                for (File file : files) {
                    //validate the object against a predefined DTD
                    String xml = DOMValidateDTD.validate(file, Info.getApplicationPath() + "/config/validator/reaction.dtd");
                    Reaction reaction = null;
                    try {
                        reaction = (Reaction) xstream.fromXML(xml);
                    } catch (Exception e) {
                        Freedomotic.logger.severe("Reaction file is not well formatted");
                        continue;
                    }
                    if (reaction.getCommands().size() == 0) {
                        Freedomotic.logger.severe("Reaction " + reaction.toString() + " has no valid commands. Maybe related objects are missing or not configured properly.");
                    }
                    add(reaction);
                    summary.append(reaction.getUUID()).append("\t\t\t").append(reaction.toString()).append("\t\t\t").append(reaction.getDescription()).append("\n");
                }
                //writing a summary .txt file with the list of commands in this folder
                FileWriter fstream = new FileWriter(folder + "/index.txt");
                BufferedWriter indexfile = new BufferedWriter(fstream);
                indexfile.write(summary.toString());
                //Close the output stream
                indexfile.close();
            } else {
                Freedomotic.logger.info("No reactions to load from this folder " + folder.toString());
            }
        } catch (Exception e) {
            Freedomotic.logger.severe("Exception while loading reaction in " + folder.getAbsolutePath() + ".\n" + Freedomotic.getStackTraceInfo(e));
        }
    }

    public static void add(Reaction r) {
        if (r!=null && !exists(r)) { //if not already loaded
            r.getTrigger().register(); //trigger starts to listen on its channel
            list.add(r);
            r.setChanged();
            Freedomotic.logger.info("Added new reaction " + r.getDescription());
        } else {
            Freedomotic.logger.info("The reaction '" + r.getDescription() + "' is already loaded so its skipped.");
        }
    }

    public static void remove(Reaction input) {
        if (input != null) {
            boolean removed = list.remove(input);
            Freedomotic.logger.info("Removed reaction " + input.getDescription());
            input.getTrigger().unregister();
            if ((!removed) && (list.contains(input))) {
                Freedomotic.logger.warning("Error while removing Reaction " + input.getDescription() + " from the list");
            }
        }
    }

    public static Iterator<Reaction> iterator() {
        return list.iterator();
    }

    public static List<Reaction> getReactions() {
        return Collections.unmodifiableList(list);
    }

    public static int size() {
        return list.size();
    }

    public static boolean exists(Reaction input) {
        if (input != null) {
            for (Iterator<Reaction> it = list.iterator(); it.hasNext();) {
                Reaction reaction = it.next();
                if (input.equals(reaction)) {
                    return true;
                }
            }
        }
        return false;
    }
}
