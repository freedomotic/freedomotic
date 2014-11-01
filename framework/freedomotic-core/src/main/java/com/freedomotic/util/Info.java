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
package com.freedomotic.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author nicoletti
 */
@XmlRootElement
public class Info {

    private static final Logger LOG = Logger.getLogger(Info.class.getName());
    public static FrameworkSettings FRAMEWORK = new FrameworkSettings();
    public static PathSettings PATHS = new PathSettings();
    public static MessagingSettings MESSAGING = new MessagingSettings();

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class FrameworkSettings {

        //framework versioning
        public final Integer FRAMEWORK_MAJOR = 5;
        public final Integer FRAMEWORK_MINOR = 6;
        public final Integer FRAMEWORK_BUILD = 0;
        public final String FRAMEWORK_VERSION_CODENAME = "Commander";
        public final String FRAMEWORK_RELEASE_DATE = "In Development";
        public final String FRAMEWORK_LICENSE = "GNU GPL v2";
        public final String FRAMEWORK_RELEASE_TYPE = "beta";
        public final String FRAMEWORK_AUTHOR = "Freedomotic Development Team";
        public final String PROJECT_MAIL = "info@freedomotic.com";
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PathSettings {

        //framework base paths
        public final File PATH_WORKDIR = Info.getWorkdir();

        public final File PATH_CONFIG_FOLDER = new File(PATH_WORKDIR + "/config/");

        public File PATH_DATA_FOLDER = new File(PATH_WORKDIR + "/data/");
        public File PATH_RESOURCES_FOLDER = new File(PATH_DATA_FOLDER + "/resources/");

        public final File PATH_PLUGINS_FOLDER = new File(PATH_WORKDIR + "/plugins/");
        public final File PATH_DEVICES_FOLDER = new File(PATH_PLUGINS_FOLDER + "/devices/");
        public final File PATH_OBJECTS_FOLDER = new File(PATH_PLUGINS_FOLDER + "/objects/");
        public final File PATH_EVENTS_FOLDER = new File(PATH_PLUGINS_FOLDER + "/events/");

        public final File PATH_PROVIDERS_FOLDER = new File(PATH_PLUGINS_FOLDER + "/providers/");
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MessagingSettings {
        //framework API and messaging

        public final String BROKER_IP = Info.getLocalHost();

        /* http://activemq.apache.org/activemq-3-transport-configurations.html
         * http://marcelojabali.blogspot.it/2011/10/apache-activemq-enhancements-to-jms.html
         * public static final String BROKER_DEFAULT = "vm://freedomotic";
         * info about peer brokers http://fusesource.com/docs/broker/5.2/connectivity_guide/N04F73598.04EE2290.html
         */
        public final String BROKER_DEFAULT = "vm://localhost";//"peer://localhost/broker"+UUID.randomUUID();

        public final int BROKER_PORT = 61616;

        public final String BROKER_STOMP = "stomp://0.0.0.0:61666";

        public final String BROKER_WEBSOCKET = "ws://0.0.0.0:61614";
        //default queues

        public final String CHANNEL_OBJECT_UPDATE = "app.event.object.behavior.change";

        public final String CHANNEL_ZONE_OCCUPIERS = "app.event.person.zone";

        public final String CHANNEL_PEOPLE_LOCATION = "app.event.sensor.person.movement.*";

        //behavior proprities
        public final int BEHAVIOR_MAX_PRIORITY = 9;

        public final int BEHAVIOR_MIN_PRIORITY = 0;
    }

    private static String getLocalHost() {
        String address = "";

        try {
            address = InetAddress.getLocalHost().toString();

        } catch (UnknownHostException ex) {
            Logger.getLogger(Info.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return address;

    }

    /**
     * This is a little workaround used to retrieve the right freedomotic-core
     * working directory also if it is run from maven. This is needed as we use
     * the git+maven as a SDK for freedomotic.
     *
     * @return a String containing the fixed absolute path to freedomotic-core
     * folder
     */
    private static File getWorkdir() {
        //This returns an URL like String, not a File like, be aware of the consequences
        String jarFolder = Info.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (jarFolder.endsWith("/target/freedomotic-core/freedomotic.jar")) { //run from commandline
            jarFolder = jarFolder.substring(0, jarFolder.indexOf("/target/freedomotic-core/freedomotic.jar"));
        } else if (jarFolder.endsWith("/target/classes/")) { //run from IDE
            jarFolder = jarFolder.substring(0, jarFolder.indexOf("/target/classes/"));
        } else if (jarFolder.endsWith("freedomotic.jar")) { //run from release package
            jarFolder = jarFolder.substring(0, jarFolder.indexOf("freedomotic.jar"));
        }

        try {
            //decode the URL to translate it into a File
            //this hack can work only if freedomotic starts on local filesystem
            File workdir = new File(URLDecoder.decode(jarFolder, "UTF-8"));
            LOG.info(workdir.getAbsolutePath());
            return workdir;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Info.class.getName()).log(Level.SEVERE, null, ex);
        }

        LOG.severe("Something went wrong when figuring out which is the current workdir. "
                + "Cannot start freedmotic as a consequence");
        return null;
    }

    public static void relocateDataPath(File path) {
        if (path == null) {
            throw new IllegalArgumentException("Base data path should point to a not null file");
        }
        if (!path.isDirectory() || !path.canRead()) {
            throw new IllegalArgumentException("Base data path should point to an existent readable directory and '" + path.getAbsolutePath() + "' it's not.");
        }
        Info.PATHS.PATH_DATA_FOLDER = path;
        Info.PATHS.PATH_RESOURCES_FOLDER = new File(path + "/resources/");
    }

    /**
     *
     * @return
     */
    public static Integer getMajor() {
        return FRAMEWORK.FRAMEWORK_MAJOR;
    }

    /**
     *
     * @return
     */
    public static Integer getMinor() {
        return FRAMEWORK.FRAMEWORK_MINOR;
    }

    /**
     *
     * @return
     */
    public static Integer getRevision() {
        return FRAMEWORK.FRAMEWORK_BUILD;
    }

    /**
     *
     * @return
     */
    public static String getVersion() {
        return FRAMEWORK.FRAMEWORK_MAJOR.toString() + "." + FRAMEWORK.FRAMEWORK_MINOR.toString() + "." + FRAMEWORK.FRAMEWORK_BUILD.toString();
    }

    /**
     *
     * @return
     */
    public static String getVersionCodeName() {
        return FRAMEWORK.FRAMEWORK_VERSION_CODENAME;
    }

    /**
     *
     * @return
     */
    public static String getReleaseDate() {
        return FRAMEWORK.FRAMEWORK_RELEASE_DATE;
    }

    /**
     *
     * @return
     */
    public static String getReleaseType() {
        return FRAMEWORK.FRAMEWORK_RELEASE_TYPE;
    }

    /**
     *
     * @return
     */
    public static String getLicense() {
        return FRAMEWORK.FRAMEWORK_LICENSE;
    }

    /**
     *
     * @return
     */
    @XmlElement
    public static String getAuthor() {
        return FRAMEWORK.FRAMEWORK_AUTHOR;
    }

    /**
     *
     * @return
     */
    public static String getAuthorMail() {
        return FRAMEWORK.PROJECT_MAIL;
    }

    /**
     *
     * @return
     */
    public static String getAClasspathView() {
        String str = ("Operative System: " + System.getProperty("os.name") + "\n"
                + "Architecture: " + System.getProperty("os.arch") + "\n"
                + "Version: " + System.getProperty("os.version") + "\n"
                + "User: " + System.getProperty("user.name") + "\n"
                + "Program path: " + System.getProperty("user.dir") + "\n"
                + "Classpath: " + splitPathString(System.getProperty("java.class.path")) + "\n"
                + "Ext directories included on classpath: " + splitPathString(System.getProperty("java.ext.dirs")) + "\n"
                + "Low Level classpath: " + splitPathString(System.getProperty("java.library.path")) + "\n");

        return str;
    }

    @Deprecated
    private static String splitPathString(String str) {
        StringBuilder buff = new StringBuilder();
        StringTokenizer token = new StringTokenizer(str,
                System.getProperty("path.separator"));

        while (token.hasMoreElements()) {
            buff.append("\n    ").append(token.nextToken());
        }

        return buff.toString();

    }

    private Info() {
    }
}
