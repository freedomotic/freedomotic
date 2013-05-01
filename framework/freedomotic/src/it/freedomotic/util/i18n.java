/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.util;

import it.freedomotic.app.Freedomotic;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
public class i18n {

    public static Locale currentLocale;
    public static HashMap<String, ResourceBundle> messages = new HashMap<String, ResourceBundle>();

    private i18n() {
    }
    /*
     * For Freedomotic core: translations are inside /i18n/Freedomotic.properties
     * For Plugin: translations are inside plugins/_plugin_type_/_plugin_package_/i18n/_package_last_part_.properties
     */

    public static String msg(Object obj, String key, Object[] fields) {
        String bundleName = "Freedomotic";
        File folder = null;

        String enableLocalization = Freedomotic.config.getStringProperty("KEY_ENABLE_I18N", "no");
        if (enableLocalization.equals("no") || enableLocalization.equals("false")) {
            return key;
        }
        
        if (obj != null) {
            bundleName = obj.getClass().getPackage().getName();
        }
                
        if (!messages.containsKey(bundleName)) {
            try {
                if (obj != null) {
                    String packageName = bundleName;
                    int lastSize = bundleName.split("\\.").length;
                    bundleName = bundleName.split("\\.")[lastSize - 1];
                    //Plugin plug = (Plugin) obj;
                    folder = new File(Info.getApplicationPath() + File.separator + "plugins" + File.separator + "devices" + File.separator + packageName + File.separator + "i18n");
                } else {
                    folder = new File(Info.getApplicationPath() + File.separator + "i18n");
                }
                ClassLoader loader;

                URL[] urls = {folder.toURI().toURL()};
                loader = new URLClassLoader(urls);
                Locale loc;
                if (enableLocalization.equals("auto") || enableLocalization.equals("yes") || enableLocalization.equals("true")) {
                    loc = Locale.getDefault();
                } else {
                    loc = new Locale(enableLocalization.substring(0,2),enableLocalization.substring(3,5));
                    
                    if (loc == null){
                        Freedomotic.logger.severe("Cannot set locale "+  enableLocalization + " falling back to default locale");
                        loc = Locale.getDefault();
                    }
                    
                }
                messages.put(bundleName, ResourceBundle.getBundle(bundleName, loc, loader));
                Freedomotic.logger.info("Adding resoulceBundle: package=" + bundleName + ", locale="+ loc.getLanguage() +"_" +loc.getCountry() + ". pointing at " + folder.getAbsolutePath());
            } catch (MalformedURLException ex) {
                Freedomotic.logger.severe("Cannot load resourceBundle for package" + bundleName);
            }

        }
        if (messages.containsKey(bundleName)) {
            try {
                if (messages.get(bundleName).containsKey(key)) {
                    return java.text.MessageFormat.format(messages.get(bundleName).getString(key), fields) + " ";
                }
            } catch (MissingResourceException ex) {
                Freedomotic.logger.severe("Cannot find resourceBundle for package " + bundleName);
            }
        }
        return "@@@";
    }

    public static String msg(String key) {
        return msg(null, key, null);
    }

    public static String msg(String key, Object[] fields) {
        return msg(null, key, fields);
    }

    public static String msg(String key, String field) {
        return msg(null, key, new Object[]{field});
    }

    public static String msg(Object obj, String key) {
        if (obj instanceof String) {
            return msg((String) obj, key);
        } else {
            return msg(obj, key, null);
        }
    }
}
