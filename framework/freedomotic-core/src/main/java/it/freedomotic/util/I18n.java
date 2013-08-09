/**
 *
 * Copyright (c) 2009-2013 Freedomotic team http://freedomotic.com
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
package it.freedomotic.util;

import com.google.inject.Inject;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.app.AppConfig;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Logger;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
public class I18n {

    private static Locale currentLocale = Locale.getDefault();
    private static HashMap<String, ResourceBundle> messages = new HashMap<String, ResourceBundle>();
    private static UTF8control RB_Control = new UTF8control();
    private static Vector<ComboLanguage> languages = new Vector<ComboLanguage>();
    @Inject
    static AppConfig config;

    private I18n() {
    }

    /*
     * For Freedomotic core: translations are inside /i18n/Freedomotic.properties
     * For Plugin: translations are inside plugins/_plugin_type_/_plugin_package_/i18n/_package_last_part_.properties
     */
    public static String msg(Object obj, String key, Object[] fields) {
        String bundleName = "Freedomotic";
        File folder = null;
        if (currentLocale == null) {
            return key;
        }
        String loc = currentLocale.toString(); //currentLocale.getLanguage() + "_" + currentLocale.getCountry();
        if (obj != null) {
            bundleName = obj.getClass().getPackage().getName();
        }
        if (!messages.containsKey(bundleName + ":" + loc)) {
            String superBundleName = bundleName.lastIndexOf('.') == -1 ? bundleName : bundleName.substring(0, bundleName.lastIndexOf('.'));
            if (!messages.containsKey(superBundleName + ":" + loc)) {
                try {
                    if (obj != null) {
                        folder = new File(Info.getApplicationPath() + File.separator + "plugins" + File.separator + "devices" + File.separator + bundleName + File.separator + "data" + File.separator + "i18n");
                    } else {
                        folder = new File(Info.getApplicationPath() + File.separator + "i18n");
                    }

                    ClassLoader loader;
                    URL[] urls = {folder.toURI().toURL()};
                    loader = new URLClassLoader(urls);

                    String fileName = bundleName;
                    int lastSize = bundleName.split("\\.").length;
                    fileName = bundleName.split("\\.")[lastSize - 1];

                    messages.put(bundleName + ":" + loc, ResourceBundle.getBundle(fileName, currentLocale, loader, RB_Control));
                    LOG.info("Adding resourceBundle: package=" + bundleName + ", locale=" + loc + ". pointing at " + folder.getAbsolutePath());
                } catch (MalformedURLException ex) {
                    LOG.severe("Cannot find folderwhile loading resourceBundle for package" + bundleName);
                } catch (MissingResourceException ex) {
                    LOG.severe("Cannot find resourceBundle files inside folder for package" + bundleName);
                }
            } else {
                bundleName = superBundleName;
            }
        }
        if (messages.containsKey(bundleName + ":" + loc)) {
            try {
                if (messages.get(bundleName + ":" + loc).containsKey(key)) {
                    return java.text.MessageFormat.format(messages.get(bundleName + ":" + loc).getString(key), fields) + " ";
                }
            } catch (MissingResourceException ex) {
                LOG.severe("Cannot find resourceBundle files inside folder for package" + bundleName);
            }
        }

        return key;
    }

    public static String msg(String key) {
        return msg(null, key, null);
    }

    public static String msg(String key, Object[] fields) {
        return msg(null, key, fields);
    }

    public static String msg(String key, String field) {
        return msg(null,
                key,
                new Object[]{field});
    }

    public static String msg(Object obj, String key) {
        if (obj instanceof String) {
            return msg((String) obj, key);
        } else {
            return msg(obj, key, null);
        }
    }

    /**
     *
     */
    protected static class UTF8control
            extends ResourceBundle.Control {

        protected static final String BUNDLE_EXTENSION = "properties";

        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader,
                boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            // The below code is copied from default Control#newBundle() implementation.
            // Only the PropertyResourceBundle line is changed to read the file as UTF-8.
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, BUNDLE_EXTENSION);
            ResourceBundle bundle = null;
            InputStream stream = null;

            if (reload) {
                URL url = loader.getResource(resourceName);

                if (url != null) {
                    URLConnection connection = url.openConnection();

                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }

            if (stream != null) {
                try {
                    bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
                } finally {
                    stream.close();
                }
            }

            return bundle;
        }
    }

    public static void setDefaultLocale(String loc) {
        //if (loc.equals("no") || loc.equals("false")){
        currentLocale = null;
        if (loc == null || loc.isEmpty() || loc.equals("auto") || loc.equals("yes") || loc.equals("true")) {
            currentLocale = Locale.getDefault();
            config.put("KEY_ENABLE_I18N", "auto");
        } else if (loc.length() >= 5) {
            currentLocale = new Locale(loc.substring(0, 2), loc.substring(3, 5));
            config.put("KEY_ENABLE_I18N", currentLocale.toString());
        }
    }

    // should be replaced by user specific Locale
    @Deprecated
    public static String getDefaultLocale() {
        return currentLocale.toString();
    }

    public static Vector<ComboLanguage> getAvailableLocales() {
        final String bundlename = "Freedomotic";
        languages.clear();
        File root = new File(Info.getApplicationPath() + File.separator + "i18n");
        File[] files = root.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches("^" + bundlename + "(_\\w{2}(_\\w{2})?)?\\.properties$");
            }
        });


        for (File file : files) {
            String value = file.getName().replaceAll("^" + bundlename + "(_)?|\\.properties$", "");

            if (!value.isEmpty()) {
                Locale loc = new Locale(value.substring(0, 2), value.substring(3, 5));
                languages.add(new ComboLanguage(loc.getDisplayLanguage(loc), value));
            }
        }
        languages.add(new ComboLanguage("Automatic", "auto"));
        return languages;
    }

    public static class ComboLanguage {

        private String descr;
        private String value;

        ComboLanguage(String descr, String value) {
            this.descr = descr;
            this.value = value;
        }

        @Override
        public String toString() {
            return descr;
        }

        public String getValue() {
            return value;
        }
    }
    private static final Logger LOG = Logger.getLogger(I18n.class.getName());
}
