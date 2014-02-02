/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.util.I18n;

import com.freedomotic.api.Client;
import java.util.Vector;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
public interface I18n {

    Vector<ComboLanguage> getAvailableLocales();

    // should be replaced by user specific Locale
    @Deprecated
    String getDefaultLocale();

    /*
     * For Freedomotic core: translations are inside /i18n/Freedomotic.properties
     * For Plugin: translations are inside plugins/_plugin_type_/_plugin_package_/i18n/_package_last_part_.properties
     */
    //String msg(String packageName, String key, Object[] fields);

    String msg(String key);

    String msg(String key, Object[] fields);

    void setDefaultLocale(String loc);
    
    void registerPluginBundleDir(Client client);
    
}
