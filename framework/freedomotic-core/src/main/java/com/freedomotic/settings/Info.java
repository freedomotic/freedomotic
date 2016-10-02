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
package com.freedomotic.settings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
@XmlRootElement
public class Info {

    private static final Logger LOG = LoggerFactory.getLogger(Info.class.getName());
    public static final FrameworkSettings FRAMEWORK = new FrameworkSettings();
    public static final PathSettings PATHS = new PathSettings();
    public static final MessagingSettings MESSAGING = new MessagingSettings();

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
        public File PATH_WORKDIR = Info.getWorkdir();

        public final File PATH_CONFIG_FOLDER = new File(PATH_WORKDIR + "/config/");
        public File PATH_DATA_FOLDER = new File(PATH_WORKDIR + "/data/");
        public File PATH_RESOURCES_FOLDER = new File(PATH_DATA_FOLDER + "/resources/");
        public File PATH_ENVIRONMENTS_FOLDER = new File(PATH_DATA_FOLDER + "/furn/");

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
        public final String BROKER_DEFAULT_PROTOCOL = "peer";
        public final String BROKER_DEFAULT_CLUSTER_NAME = "freedomotic";
        public final String BROKER_DEFAULT_UUID = UUID.randomUUID().toString();

        public final int BROKER_PORT = 61616;

        //port zero mean the first available port
        public final String BROKER_STOMP = "stomp://0.0.0.0:0";
        //port zero mean the first available port
        public final String BROKER_WEBSOCKET = "ws://0.0.0.0:0";
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
            LOG.error(ex.getMessage());
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
        } else if (jarFolder.endsWith(".jar")) {
            String tmp = ClassLoader.getSystemClassLoader().getResource(".").getPath();
            jarFolder = tmp.substring(0, tmp.indexOf("/plugins/")) + "/framework/freedomotic-core/"; //run from integration test in a plugin
        }

        try {
            //decode the URL to translate it into a File
            //this hack can work only if freedomotic starts on local filesystem
            File workdir = new File(URLDecoder.decode(jarFolder, "UTF-8"));
            LOG.info(workdir.getAbsolutePath());
            return workdir;
        } catch (UnsupportedEncodingException ex) {
            LOG.error(ex.getMessage());
        }

        LOG.error("Something went wrong when figuring out which is the current workdir. "
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
        Info.PATHS.PATH_ENVIRONMENTS_FOLDER = new File(path + "/furn/");
    }

    public static void relocateWorkdir(File file) {
        Info.PATHS.PATH_WORKDIR = file;
    }

    /**
     * Gets the framework major version.
     *
     * @return the framework major version
     */
    public static Integer getMajor() {
        return FRAMEWORK.FRAMEWORK_MAJOR;
    }

    /**
     * Gets the framework minor version.
     *
     * @return the framework minor version
     */
    public static Integer getMinor() {
        return FRAMEWORK.FRAMEWORK_MINOR;
    }

    /**
     * Gets the framework build revision.
     *
     * @return the framework build revision
     */
    public static Integer getRevision() {
        return FRAMEWORK.FRAMEWORK_BUILD;
    }

    /**
     * Gets the framework version.
     *
     * @return the framework version
     */
    public static String getVersion() {
        return FRAMEWORK.FRAMEWORK_MAJOR.toString() + "." + FRAMEWORK.FRAMEWORK_MINOR.toString() + "." + FRAMEWORK.FRAMEWORK_BUILD.toString();
    }

    /**
     * Gets the framework version codename.
     *
     * @return the framework major version
     */
    public static String getVersionCodeName() {
        return FRAMEWORK.FRAMEWORK_VERSION_CODENAME;
    }

    /**
     * Gets the framework release date.
     *
     * @return the framework release date
     */
    public static String getReleaseDate() {
        return FRAMEWORK.FRAMEWORK_RELEASE_DATE;
    }

    /**
     * Gets the framework release type.
     *
     * @return the framework release type
     */
    public static String getReleaseType() {
        return FRAMEWORK.FRAMEWORK_RELEASE_TYPE;
    }

    /**
     * Gets the framework license.
     *
     * @return the framework license
     */
    public static String getLicense() {
        return FRAMEWORK.FRAMEWORK_LICENSE;
    }

    /**
     * Gets the framework author.
     *
     * @return the framework author
     */
    @XmlElement
    public static String getAuthor() {
        return FRAMEWORK.FRAMEWORK_AUTHOR;
    }

    /**
     * Gets the framework author's mail.
     *
     * @return the framework author's mail
     */
    public static String getAuthorMail() {
        return FRAMEWORK.PROJECT_MAIL;
    }

    /**
     * Gets the classpath view.
     *
     * @return the classpath view
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
