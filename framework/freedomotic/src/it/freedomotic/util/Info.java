package it.freedomotic.util;

import it.freedomotic.app.Freedomotic;
import java.io.File;
import java.util.StringTokenizer;

public class Info {

    static final private Integer major = 5;
    static final private Integer minor = 2;
    static final private Integer revision = 0;
    static final private String releaseDate = "March 2012";
    static final private String license = "GNU Generic Public License v2";
    static final private String authorMail = "info@freedomotic.com";
    static final private String releaseType = "beta";
    private static final String applicationPath = System.getProperty("user.dir");
    //default queues
    public static final String OBJECT_CHANGE_BEHAVIOR_QUEUE = "app.event.object.behavior.change";
    public static final String ZONE_EVENTS_QUEUE = "app.event.person.zone";
    public static final String PERSON_MOVEMENT_QUEUE = "app.event.sensor.person.movement.*";
    public static final String DEBUG_LOGGER_QUEUE = "app.actuators.debug.logger.*";

    public static final int MAX_BEHAVIOR_PRIORITY=9;
    public static final int MIN_BEHAVIOR_PRIORITY=0;

    public static Integer getMajor() {
        return major;
    }

    public static Integer getMinor() {
        return minor;
    }

    public static Integer getRevision() {
        return revision;
    }

    static public String getVersion() {
        return major.toString() + "." + minor.toString() + "." + revision.toString();
    }

    static public String getReleaseDate() {
        return releaseDate;
    }

    public static String getReleaseType() {
        return releaseType;
    }

    static public String getLicense() {
        return license;
    }

    static public String getAuthor() {
        return "Freedomotic Development Team";
    }

    static public String getAuthorMail() {
        return authorMail;
    }

    static public String getAClasspathView() {
        String str = ("Sistema operativo: " + System.getProperty("os.name") + "\n"
                + "Architettura: " + System.getProperty("os.arch") + "\n"
                + "Versione: " + System.getProperty("os.version") + "\n"
                + "Utente: " + System.getProperty("user.name") + "\n"
                + "Program path: " + System.getProperty("user.dir") + "\n"
                + "Classpath: " + splitPathString(System.getProperty("java.class.path")) + "\n"
                + "Ext directories included on classpath: " + splitPathString(System.getProperty("java.ext.dirs")) + "\n"
                + "Low Level classpath: " + splitPathString(System.getProperty("java.library.path")) + "\n");

        return str;
    }

    public static String getApplicationPath() {
        return applicationPath;
    }

    public static String getRemoteRepository() {
        return Freedomotic.config.getStringProperty("KEY_REPOSITORY_URL", "http://freedomotic.googlecode.com/files/");
    }

    public static String getDatafilePath() {
        return (new File(applicationPath + "/data/").getAbsolutePath());
    }

    public static String getPluginsPath() {
        return (new File(applicationPath + "/plugins/").getAbsolutePath());
    }

        public static String getDevicesPath() {
        return (new File(getPluginsPath() + "/devices/").getAbsolutePath());
    }

//    public static String getFuzzyRulesPath() {
//        return (new File(Info.getDatafilePath() + "/fcl/").getPath());
//    }

    public static String getResourcesPath() {
        return (new File(applicationPath + Freedomotic.config.getProperty("KEY_RESOURCES_PATH")).getPath());
    }

    private static String splitPathString(String str) {
        StringBuffer buff = new StringBuffer();
        StringTokenizer token = new StringTokenizer(str, System.getProperty("path.separator"));
        while (token.hasMoreElements()) {
            buff.append("\n    ").append(token.nextToken());
        }
        return buff.toString();

    }

    static int getIntVersion() {
        return Integer.parseInt(major.toString() + minor.toString() + revision.toString());
    }
}
