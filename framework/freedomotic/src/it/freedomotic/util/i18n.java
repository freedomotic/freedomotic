/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.util;

import it.freedomotic.api.Plugin;
import it.freedomotic.app.Freedomotic;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public static String msg(Object obj, String key) {
        String bundleName = "Freedomotic";
        if(obj != null){
           bundleName = obj.getClass().getPackage().getName();
        }
        if (!messages.containsKey(bundleName)) {
            try {
                File folder = null;
                if (obj != null) {
                    String packageName = bundleName;
                    int lastSize = bundleName.split("\\.").length;
                    bundleName =  bundleName.split("\\.")[lastSize-1];
                    //Plugin plug = (Plugin) obj;
                    folder = new File(Info.getApplicationPath() + File.separator + "plugins" + File.separator + "devices" + File.separator + packageName + File.separator + "i18n");
                } else {
                    folder = new File(Info.getApplicationPath() + File.separator + "i18n");
                }
                ClassLoader loader;

                URL[] urls = {folder.toURI().toURL()};
                loader = new URLClassLoader(urls);
                messages.put(bundleName, ResourceBundle.getBundle(bundleName, Locale.getDefault(), loader));
            } catch (MalformedURLException ex) {
                Freedomotic.logger.severe("Cannot load resourceBundle for package" + bundleName);
            }

        }
        if (messages.containsKey(bundleName)) {
            return messages.get(bundleName).getString(key);
        }
        return "";
    }
    public static String  msg(String key){
        return msg(null,key);
    }
}
