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
package com.freedomotic.marketplace;

import com.freedomotic.marketplace.postplugin.JavaUploader;
import com.freedomotic.marketplace.util.DrupalRestHelper;
import com.freedomotic.marketplace.util.MarketPlaceFile;
import com.freedomotic.marketplace.util.MarketPlacePlugin2;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.restlet.data.CookieSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
public class AntUploader extends Task {

    private static final Logger LOG = LoggerFactory.getLogger(AntUploader.class.getName());
    String username;
    String password;
    String nodeid;
    String attachment;

    @Override
    public void execute() {
        JavaUploader drupal = new JavaUploader();
        //change this for real username and password
        String loginJson = JavaUploader.login(username, password);
        CookieSetting cS = JavaUploader.parseCookie(loginJson);
        String userid = JavaUploader.parseUid(loginJson);
        if (cS != null) {
            //first try to retrieve the plugin from the drupal site
            LOG.info("Retrieving node {} from the marketplace", nodeid);
            MarketPlacePlugin2 plugin = (MarketPlacePlugin2) DrupalRestHelper.retrievePluginPackage("http://www.freedomotic.com/rest/node/" + nodeid);
            if (plugin != null) {
                try {
                    File marketDirectory = new File(attachment);
                    File fileToUpload = findFileToUpload(marketDirectory);
                    if (fileToUpload != null) {
                        MarketPlaceFile pluginFile = JavaUploader.postFile(cS, userid, marketDirectory.getAbsolutePath(), fileToUpload.getName());
                        plugin.addFile(pluginFile);
                        JavaUploader.putPlugin(cS, nodeid, plugin);
                    } else {
                        throw new BuildException("No marketplace files in folder \"" + marketDirectory + "\"");
                    }
                } catch (IOException ex) {
                    throw new BuildException("Cannot find attachment file. " + ex.getMessage());
                }
            } else {
                throw new BuildException("There isn't a plugin with id: " + nodeid);
            }
        }

    }

    /**
     * Search the directory to the correct "marketplace like" file to be
     * uploaded
     *
     * @param marketDirectory
     * @return
     */
    public File findFileToUpload(File marketDirectory) {
        File[] files = null;
       
        LOG.info("Uploading to drupal nodeid {} the file in \"{}\"", nodeid, marketDirectory.getAbsolutePath());
        if (marketDirectory.isDirectory()) {
            // This filter only returns object files
            FileFilter objectFileFilter = (File file) -> {
                return (file.isFile()
                        && (file.getName().endsWith(".device")
                        || file.getName().endsWith(".object")
                        || file.getName().endsWith(".event")));
            };
            files = marketDirectory.listFiles(objectFileFilter);
        } else {
            throw new BuildException("\"" + marketDirectory + "\" is not a folder");
        }
        return files[0]; //get first file in folder according to filtering rules
    }

    public String extractVersion(String filename) {
        //suppose filename is something like it.nicoletti.test-5.2.x-1.212.device
        //only 5.2.x-1.212 is needed
        //remove extension
        String pluginFilename = filename.substring(0, filename.lastIndexOf('.'));
        String[] tokens = pluginFilename.split("-");
        //3 tokens expected
        if (tokens.length == 3) {
            return tokens[1] + "-" + tokens[2];
        } else {
            return pluginFilename;
        }
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getNodeid() {
        return nodeid;
    }

    public void setNodeid(String nodeid) {
        this.nodeid = nodeid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
