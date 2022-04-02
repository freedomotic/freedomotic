/**
 *
 * Copyright (c) 2009-2022 Freedomotic Team http://www.freedomotic-platform.com
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
package com.freedomotic.i18n;

import com.freedomotic.app.Freedomotic;
import com.freedomotic.settings.AppConfig;
import com.freedomotic.settings.Info;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matteo Mazzoni
 */
class I18nImpl implements I18n {

    private static final Logger LOG = LoggerFactory.getLogger(I18n.class.getName());
    private static final CustomSecurityManager customSecurityManager = new CustomSecurityManager();
    private Locale currentLocale;
    private final HashMap<String, ResourceBundle> messages;
    private final UTF8control rbControl;
    private static final String FREEDOMOTIC_PACKAGE = "com.freedomotic";
    private static final List<Locale> locales = new ArrayList<>();
    private final AppConfig config;
    private final HashMap<String, File> packageBundleDir;
    private static final Locale fallBackLocale = Locale.ENGLISH;

    private static class CustomSecurityManager extends SecurityManager {

        public Class<?> getCallerClass() {
            return getClassContext()[2];
        }
    }

    @Inject
    public I18nImpl(AppConfig config) {
        this.config = config;
        currentLocale = Locale.getDefault();
        messages = new HashMap<>();
        rbControl = new UTF8control();
        packageBundleDir = new HashMap<>();
        // add mapping for base strings
        packageBundleDir.put(FREEDOMOTIC_PACKAGE, new File(Info.PATHS.PATH_WORKDIR + "/i18n"));
    }

    /*
     * For Freedomotic core: translations are inside /i18n/Freedomotic.properties
     * For Plugin: translations are inside plugins/_plugin_type_/_plugin_package_/i18n/_package_last_part_.properties
     */
    protected String msg(String packageName, String key, Object[] fields) {
        // scorri packageNamefino a trovarne uno presente in packageBundleDir

        File folder = null;
        String workingPackage = packageName;
        while (workingPackage.lastIndexOf('.') != -1) {
            folder = packageBundleDir.get(workingPackage);
            if (folder == null) {
                workingPackage = workingPackage.substring(0, workingPackage.lastIndexOf('.'));
            } else {
                // try and get string for current package
                String value = msg(folder, workingPackage, key, fields, true, currentLocale);
                if (value != null) {
                    return value;
                }
                break;
            }
        }
        folder = packageBundleDir.get(FREEDOMOTIC_PACKAGE);
        return msg(folder, FREEDOMOTIC_PACKAGE, key, fields, false, currentLocale);
    }

    private String msg(File folder, String packageName, String key, Object[] fields, boolean reThrow, Locale locale) {
        // estrai locale corrente

        if (locale == null) {
            locale = Locale.ENGLISH;
        }
        String loc = locale.toString();

        // cerca in messages (ed eventualmente aggiungi) la risorsa per il package e la lingua
        final String bundleKey = packageName + ":" + loc;
        loadResourceBundleIfNeeded(folder, packageName, loc, bundleKey);

        // estrai stringa
        if (messages.containsKey(bundleKey)) {
            try {
                // try and extract strig for current locale...
                if (messages.get(bundleKey).containsKey(key)) {
                    return java.text.MessageFormat.format(messages.get(bundleKey).getString(key), fields) + " ";
                } else if (locale != fallBackLocale) {
                    // ... otherwise search string translation in FALLBACKLOCALE
                    String searchDefaultTranslation = msg(folder, packageName, key, fields, reThrow, fallBackLocale);
                    if (searchDefaultTranslation != null) {
                        return searchDefaultTranslation;
                    } else if (reThrow) {
                        return null;
                    }
                    //instead of showing nothing we use the key
                    return key;
                }
            } catch (MissingResourceException ex) {
                LOG.error("Cannot find resourceBundle files inside folder \"{}\" for package \"{}\"", new Object[]{folder.getAbsolutePath(), packageName}, Freedomotic.getStackTraceInfo(ex));
            }
        }
        return null;
    }

    private void loadResourceBundleIfNeeded(File folder, String packageName, String loc, final String bundleKey) {
        if (!messages.containsKey(bundleKey)) {
            try (URLClassLoader loader = new URLClassLoader(new URL[] {folder.toURI().toURL()})) {
                int lastSize = packageName.split("\\.").length;
                String baseName = packageName.split("\\.")[lastSize - 1];
                messages.put(bundleKey, ResourceBundle.getBundle(baseName, currentLocale, loader, rbControl));
                LOG.info("Adding resourceBundle: package={}, locale={} pointing at \"{}\"", packageName, loc, folder.getAbsolutePath());
            } catch (MalformedURLException ex) {
                LOG.error("Cannot find folder while loading resourceBundle for package \"{}\"", packageName);
            } catch (MissingResourceException ex) {
                LOG.error("Cannot find resourceBundle files inside folder \"{}\" for package \"{}\"", new Object[]{packageName, folder.getAbsolutePath()}, Freedomotic.getStackTraceInfo(ex));
            } catch (IOException ex) {
                LOG.error("Error closing URLClassLoader: {}", Freedomotic.getStackTraceInfo(ex));
            }
        }
    }

    @Override
    public String msg(String key) {
        String caller = customSecurityManager.getCallerClass().getPackage().getName();
        return msg(caller, key, null);
    }

    @Override
    public String msg(String key, Object[] fields) {
        String caller = customSecurityManager.getCallerClass().getPackage().getName();
        return msg(caller, key, fields);
    }

    @Override
    public void registerBundleTranslations(String packageName, File i18nFolder) {
        if (!packageBundleDir.containsKey(packageName)) {
            packageBundleDir.put(packageName, i18nFolder);
        }
    }

    /**
     *
     */
    protected class UTF8control extends ResourceBundle.Control {

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
                try (InputStream temp = stream) {
                    bundle = new PropertyResourceBundle(new InputStreamReader(temp, "UTF-8"));
                }
            }

            return bundle;
        }
    }

    @Override
    public void setDefaultLocale(String locale) {
        currentLocale = null;
        if (locale == null || locale.isEmpty() || "auto".equals(locale) || "yes".equals(locale) || "true".equals(locale)) {
            currentLocale = Locale.getDefault();
            config.put("KEY_ENABLE_I18N", "auto");
        } else if (locale.length() >= 5) {
            currentLocale = new Locale(locale.substring(0, 2), locale.substring(3, 5));
            config.put("KEY_ENABLE_I18N", currentLocale.toString());
        }
    }

    // should be replaced by user specific Locale
    /**
    * @deprecated
    */
    @Deprecated
    @Override
    public Locale getDefaultLocale() {
        return currentLocale;
    }

    @Override
    public List<Locale> getAvailableLocales() {
        final String bundlename = "freedomotic";
        locales.clear();
        File root = new File(Info.PATHS.PATH_WORKDIR + "/i18n");
        File[] files = root.listFiles((File dir, String name) -> {
            return name.matches("^" + bundlename + "(_\\w{2}(_\\w{2})?)?\\.properties$");
        });

        for (File file : files) {
            String value = file.getName().replaceAll("^" + bundlename + "(_)?|\\.properties$", "");

            if (!value.isEmpty()) {
                Locale locale = new Locale(value.substring(0, 2), value.substring(3, 5));
                locales.add(locale);

            }
        }
        return locales;
    }
}
