/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.marketplace;

import it.freedomotic.marketplace.postplugin.JavaUploader;
import it.freedomotic.marketplace.postplugin.MarketPlacePlugin;
import it.freedomotic.marketplace.postplugin.MarketPlacePluginFileField;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.restlet.data.CookieSetting;

/**
 *
 * @author Enrico
 */
public class AntUploader extends Task {

    String username;
    String password;
    String nodeid;
    String attachment;

    public void execute() {
        JavaUploader drupal = new JavaUploader();

        MarketPlacePlugin plugin = new MarketPlacePlugin();
        plugin.addField_os(MarketPlacePlugin.OS_LINUX);
        plugin.addField_os(MarketPlacePlugin.OS_WINDOWS);
        //change this for real username and password
        String loginJson = drupal.login(username, password);
        CookieSetting cS = drupal.parseCookie(loginJson);
        String userid = drupal.parseUid(loginJson);
        if (cS != null) {
            try {
                File marketDirectory = new File(attachment);
                File[] files = null;
                System.out.println("Uploading to drupal nodeid " + nodeid + " the file in " + marketDirectory.getAbsolutePath());
                if (marketDirectory.isDirectory()) {

                    // This filter only returns object files
                    FileFilter objectFileFileter = new FileFilter() {

                        @Override
                        public boolean accept(File file) {
                            if (file.isFile()
                                    && (file.getName().endsWith(".device")
                                    || file.getName().endsWith(".object")
                                    || file.getName().endsWith(".event"))) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                    };
                    files = marketDirectory.listFiles(objectFileFileter);
                } else {
                    throw new BuildException(marketDirectory + " is not a folder");
                }
                File fileToUpload = files[0]; //get first file in folder according to filtering rules
                if (fileToUpload != null) {
                    String fileid = drupal.postFile(cS, userid, marketDirectory.getAbsolutePath(), fileToUpload.getName());
                    MarketPlacePluginFileField fileField = new MarketPlacePluginFileField(fileid, extractVersion(fileToUpload.getName()));
                    plugin.setField_file(fileField);
                    //update the nodeid with fields in plugin
                    drupal.putPlugin(cS, nodeid, plugin);
                } else {
                    throw new BuildException("No marketplace files in folder " + marketDirectory);
                }
            } catch (IOException ex) {
                throw new BuildException("Cannot find attachment file. " + ex.getMessage());
            }
        }
    }

    private String extractVersion(String filename) {
        //suppose filename is something like it.nicoletti.test-5.2.x-1.212.device
        //only 5.2.x-1.212 is needed
        //remove extension
        filename = filename.substring(0, filename.lastIndexOf("."));
        String[] tokens = filename.split("-");
        //3 tokens expected
        if (tokens.length == 3) {
            return tokens[1] + "-" + tokens[2];
        } else {
            return filename;
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
