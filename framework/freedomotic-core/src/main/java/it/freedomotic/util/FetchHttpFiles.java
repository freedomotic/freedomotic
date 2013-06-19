/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.util;

import it.freedomotic.app.Freedomotic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author Enrico
 */
public class FetchHttpFiles {

//    public static void downloadAll(URL httpUrl, File destFolder) {
//        try {
//            //File destFolder = new File(destFolder);
//            ApacheURLLister lister = new ApacheURLLister();
//            // this list of URLs objects
//            List files = lister.listAll(httpUrl);
//            Freedomotic.logger.info("list file is complete.." + files);
//            for (Iterator iter = files.iterator(); iter.hasNext();) {
//                URL fileUrl = (URL) iter.next();
//                Freedomotic.logger.info("file: " + fileUrl);
//                download(fileUrl, destFolder);
//            }
//            Freedomotic.logger.info("download is complete..");
//        } catch (Exception e) {
//            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
//        }
//    }

    public static boolean download(URL url, File destFolder, String filename) throws Exception {
        //File sourceFile = new File(url.getPath());
        //File destinationFile = new File(destFolder.getPath() + "/" + sourceFile.getName());
        File destinationFile = new File(destFolder.getPath() + "/" + filename);
        Freedomotic.logger.config("  Download started");
        Freedomotic.logger.config("    Source folder:      " + url);
        Freedomotic.logger.config("    Destination folder: " + destinationFile);
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
                    Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(ioe));
                    return false;
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException ioe) {
                    Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(ioe));
                    return false;
                }
            }
        }

        Freedomotic.logger.info("  Download completed");
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
//            Freedomotic.logger.info("Checking plugin " + name + " for update\n"
//                    + "  local version: " + version);
//            Freedomotic.logger.info("  server version: " + remoteVersion);
//            //plugin needs update
//            if (Info.getIntVersion() >= server.getIntProperty(name + "-required", 0)) {
//                Freedomotic.logger.info("  freedomotic required: " + server.getIntProperty(name + "-required", 0));
//                Freedomotic.logger.info("  freedomotic version: " + Info.getIntVersion());
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
//                                Freedomotic.logger.warning("Download error missing file on server or server unreachable");
//                            }
//                        } catch (Exception e) {
//                            Freedomotic.logger.warning("Missing destination folder for updated plugins.");
//                        }
//                    } catch (Exception ex) {
//                        Logger.getLogger(FetchHttpFiles.class.getName()).log(Level.SEVERE, null, ex);
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
