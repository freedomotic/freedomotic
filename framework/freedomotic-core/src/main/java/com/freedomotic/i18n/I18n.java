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
package com.freedomotic.i18n;

import com.freedomotic.api.Client;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

/**
 *
 * @author Matteo Mazzoni
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
     * @return @deprecated
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
     * @param packageName
     * @param i18nFolder
     */
    void registerBundleTranslations(String packageName, File i18nFolder);

}
