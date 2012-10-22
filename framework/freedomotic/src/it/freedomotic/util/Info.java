package it.freedomotic.util;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Info {
    //framework versioning
    public static final Integer FRAMEWORK_MAJOR = 5;
    public static final Integer FRAMEWORK_MINOR = 3;
    public static final Integer FRAMEWORK_BUILD = 1;
    public static final String  FRAMEWORK_RELEASE_DATE = "March 2012";
    public static final String  FRAMEWORK_LICENSE = "GNU Generic Public License v2";
    public static final String  FRAMEWORK_RELEASE_TYPE = "beta";
    //project info
    public static final String PROJECT_MAIL = "info@freedomotic.com";
    //framework base paths
    public static final String PATH_WORKDIR = System.getProperty("user.dir");
    public static final File PATH_DATA_FOLDER = new File(PATH_WORKDIR + "/data/");
    public static final File PATH_RESOURCES_FOLDER = new File(PATH_WORKDIR + "/data/resources/");
    public static final File PATH_PLUGINS_FOLDER = new File(PATH_WORKDIR + "/plugins/");
    public static final File PATH_DEVICES_FOLDER = new File(PATH_WORKDIR + "/plugins/devices/");
    public static final File PATH_OBJECTS_FOLDER = new File(PATH_WORKDIR + "/plugins/objects/");
    public static final File PATH_EVENTS_FOLDER = new File(PATH_WORKDIR + "/plugins/events/");
    public static final File PATH_PROVIDERS_FOLDER = new File(PATH_WORKDIR + "/plugins/providers/");
    //framework API and messaging
    public static final String BROKER_IP = getLocalHost();
    public static final String BROKER_DEFAULT = "vm://freedomotic";
    public static final int    BROKER_PORT = 61616;
    public static final String BROKER_STOMP = "stomp://0.0.0.0:61666";
    public static final String BROKER_WEBSOCKET = "ws://0.0.0.0:61614";
    //default queues
    public static final String CHANNEL_OBJECT_UPDATE = "app.event.object.behavior.change";
    public static final String CHANNEL_ZONE_OCCUPIERS = "app.event.person.zone";
    public static final String CHANNEL_PEOPLE_LOCATION = "app.event.sensor.person.movement.*";
    //behavior proprities
    public static final int BEHAVIOR_MAX_PRIORITY = 9;
    public static final int BEHAVIOR_MIN_PRIORITY = 0;
    
    private static String getLocalHost() {
        String address = "";
        try {
            address = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException ex) {
            Logger.getLogger(Info.class.getName()).log(Level.SEVERE, null, ex);
        }
        return address;
    }
    
    public static Integer getMajor() {
        return FRAMEWORK_MAJOR;
    }
    
    public static Integer getMinor() {
        return FRAMEWORK_MINOR;
    }
    
    public static Integer getRevision() {
        return FRAMEWORK_BUILD;
    }
    
    static public String getVersion() {
        return FRAMEWORK_MAJOR.toString() + "." + FRAMEWORK_MINOR.toString() + "." + FRAMEWORK_BUILD.toString();
    }
    
    static public String getReleaseDate() {
        return FRAMEWORK_RELEASE_DATE;
    }
    
    public static String getReleaseType() {
        return FRAMEWORK_RELEASE_TYPE;
    }
    
    static public String getLicense() {
        return FRAMEWORK_LICENSE;
    }
    
    static public String getAuthor() {
        return "Freedomotic Development Team";
    }
    
    static public String getAuthorMail() {
        return PROJECT_MAIL;
    }
    
    static public String getAClasspathView() {
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
    
    public static String getApplicationPath() {
        return PATH_WORKDIR;
    }
    
    @Deprecated
    public static String getDatafilePath() {
        return (new File(PATH_WORKDIR + "/data/").getAbsolutePath());
    }
    
    @Deprecated
    public static String getResourcesPath() {
        return (new File(PATH_WORKDIR + "/data/resources/").getAbsolutePath());
    }
    
    @Deprecated
    public static String getPluginsPath() {
        return (new File(PATH_WORKDIR + "/plugins/").getAbsolutePath());
    }
    
    @Deprecated    
    public static String getDevicesPath() {
        return (new File(PATH_WORKDIR + "/plugins/devices/").getAbsolutePath());
    }
    
    @Deprecated
    private static String splitPathString(String str) {
        StringBuilder buff = new StringBuilder();
        StringTokenizer token = new StringTokenizer(str, System.getProperty("path.separator"));
        while (token.hasMoreElements()) {
            buff.append("\n    ").append(token.nextToken());
        }
        return buff.toString();
        
    }

    private Info() {
    }
}
