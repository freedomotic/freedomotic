/**
 *
 * Copyright (c) 2009-2020 Freedomotic Team http://freedomotic.com
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

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * For Freedomotic core: translations are inside /i18n/Freedomotic.properties
 * For Plugin: translations are inside plugins/_plugin_type_/_plugin_package_/i18n/_package_last_part_.properties
 * 
 * @author Matteo Mazzoni
 */
public interface I18n {

    /**
     *
     * @return a list of available locales
     */
    List<Locale> getAvailableLocales();

    // should be replaced by user specific Locale
    /**
     * @deprecated
     * @return a default locale
     */
    @Deprecated
    Locale getDefaultLocale();

    /**
     * Get a translated message by specified key
     * @param key
     * @return a translated message by key
     */
    String msg(String key);

    /**
     * Get a translated message by specified key and uses it to format the given fields
     * @param key
     * @param fields object(s) to format
     * @return a translated and formatted message by key
     */
    String msg(String key, Object[] fields);

    /**
     *
     * @param locale
     */
    void setDefaultLocale(String locale);

    /**
     *
     * @param packageName
     * @param i18nFolder
     */
    void registerBundleTranslations(String packageName, File i18nFolder);

}
