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
package com.freedomotic.plugins.impl;

import com.freedomotic.app.Freedomotic;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
class FetchHttpFiles {

    private static final Logger LOG = LoggerFactory.getLogger(FetchHttpFiles.class.getName());

//    public static void downloadAll(URL httpUrl, File destFolder) {
//        try {
//            //File destFolder = new File(destFolder);
//            ApacheURLLister lister = new ApacheURLLister();
//            // this list of URLs objects
//            List files = lister.listAll(httpUrl);
//            LOG.info("list file is complete.." + files);
//            for (Iterator iter = files.iterator(); iter.hasNext();) {
//                URL fileUrl = (URL) iter.next();
//                LOG.info("file: " + fileUrl);
//                download(fileUrl, destFolder);
//            }
//            LOG.info("download is complete..");
//        } catch (Exception e) {
//            LOG.error(Freedomotic.getStackTraceInfo(e));
//        }
//    }
    /**
     *
     * @param url
     * @param destFolder
     * @param filename
     * @return
     * @throws Exception
     */
    public static boolean download(URL url, File destFolder, String filename)
            throws Exception {
        //File sourceFile = new File(url.getPath());
        //File destinationFile = new File(destFolder.getPath() + "/" + sourceFile.getName());
        File destinationFile = new File(destFolder.getPath() + "/" + filename);
        LOG.info("  Download started");
        LOG.info("    Source folder:      " + url);
        LOG.info("    Destination folder: " + destinationFile);

        //destination.getParentFile().mkdirs();
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            URLConnection urlc = url.openConnection();

            bis = new BufferedInputStream(urlc.getInputStream());
            bos = new BufferedOutputStream(new FileOutputStream(destinationFile.getPath()));

            int i;

            while ((i = bis.read()) != -1) {
                bos.write(i);
            }
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ioe) {
                    LOG.error(Freedomotic.getStackTraceInfo(ioe));

                    return false;
                }
            }

            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException ioe) {
                    LOG.error(Freedomotic.getStackTraceInfo(ioe));

                    return false;
                }
            }
        }

        LOG.info("  Download completed");

        return true;
    }

//    public static boolean checkVersion(Plugin plugin, Config server) {
//        boolean updated = false;
//        String name = Plugin.normalizeName(plugin.getName());
//        int version = plugin.getVersion();
//        int required = plugin.getRequiredVersion();
//
//        int remoteVersion = server.getIntProperty(name + "-last-version", -1);
//
//        if ((version < remoteVersion) && (remoteVersion > 0)) {
//            LOG.info("Checking plugin " + name + " for update\n"
//                    + "  local version: " + version);
//            LOG.info("  server version: " + remoteVersion);
//            //plugin needs update
//            if (Info.getIntVersion() >= server.getIntProperty(name + "-required", 0)) {
//                LOG.info("  freedomotic required: " + server.getIntProperty(name + "-required", 0));
//                LOG.info("  freedomotic version: " + Info.getIntVersion());
//                //can be updated
//                ArrayList<String> urls = server.getPathListProperty(name + "-download-url");
//                ArrayList<String> file = server.getPathListProperty(name + "-destination-folder");
//                int i = 0;
//
//                for (String url : urls) {
//                    try {
//                        File path = null;
//                        try {
//                            path = new File(Info.getPluginsPath() + "/" + file.get(i));
//                            try {
//                                download(new URL(Info.getRemoteRepository() + url.toString()), path);
//                                updated = true;
//                            } catch (Exception exception) {
//                                LOG.warn("Download error missing file on server or server unreachable");
//                            }
//                        } catch (Exception e) {
//                            LOG.warn("Missing destination folder for updated plugins.");
//                        }
//                    } catch (Exception ex) {
//                        LoggerFactory.getLogger(FetchHttpFiles.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                    i++;
//                }
//            }
//        }
//        return updated;
//    }
//    public class Downloader extends Thread {
//
//        String urls;
//
//        public Downloader(String urls) {
//            this.urls = urls;
//            start();
//        }
//        boolean done = false;
//
//        @Override
//        public void run() {
//            try {
//                done = FetchHttpFiles.download(new URL(urls), new File(Info.getPluginsPath()));
//            } catch (Exception ex) {
//                done = false;
//            } finally {
//                if (!done) {
//                    JOptionPane.showMessageDialog(null,
//                            "Unable to download the requested plugin. Check your internet connection and the provided URL.",
//                            "Download Error", JOptionPane.INFORMATION_MESSAGE);
//                }
//            }
//        }
//    }
    private FetchHttpFiles() {
    }
}
