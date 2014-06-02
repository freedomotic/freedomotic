/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.util.I18n;

import com.freedomotic.api.Client;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
public interface I18n {

    /**
     *
     * @return
     */
    ArrayList<Locale> getAvailableLocales();

    // should be replaced by user specific Locale

    /**
     *
     * @return
     * @deprecated
     */
        @Deprecated
    Locale getDefaultLocale();

    /*
     * For Freedomotic core: translations are inside /i18n/Freedomotic.properties
     * For Plugin: translations are inside plugins/_plugin_type_/_plugin_package_/i18n/_package_last_part_.properties
     */
    //String msg(String packageName, String key, Object[] fields);

    /**
     *
     * @param key
     * @return
     */
    
    String msg(String key);

    /**
     *
     * @param key
     * @param fields
     * @return
     */
    String msg(String key, Object[] fields);

    /**
     *
     * @param loc
     */
    void setDefaultLocale(String loc);
    
    /**
     *
     * @param client
     */
    void registerPluginBundleDir(Client client);
    
}
