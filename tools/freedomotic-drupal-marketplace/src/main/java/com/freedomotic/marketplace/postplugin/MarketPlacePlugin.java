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
package com.freedomotic.marketplace.postplugin;

import java.util.ArrayList;

/**
 * POJO class that represents all information available from the Drupal
 * MarketPlace that is retrieved using the Drupal Rest server. At this moment
 * only a few fields are parsed, just the necesary to retrieve the plugin zip
 *
 * @author Gabriel Pulido de Torres
 */
public class MarketPlacePlugin {

    public static final String STATUS_PROOF_OF_CONCEPT = "Proof of Concept";
    public static final String STATUS_PROTOTYPE = "Prototype";
    public static final String STATUS_BETA_VERSION = "Beta Version";
    public static final String STATUS_STABLE_RELEASE = "Stable Release";
    public static final String OS_ALL = "All supported";
    public static final String OS_LINUX = "Linux";
    public static final String OS_WINDOWS = "Windows";
    public static final String OS_MAC = "Mac";
    public static final String OS_SOLARIS = "Solaris";
    private String title;
    private String body; //XML
    private String path;
    private String fieldDeveloper; //TODO check for correct developername
    private String fieldStatus;//list
    private String fieldDescription;
    private PluginCategoryEnum fieldCategory;
    private ArrayList<String> fieldOs;
    private MarketPlacePluginFileField fieldFile; //*** 
    private int fieldFilePosition = 0;
    private ArrayList<String> taxonomy;

    public MarketPlacePlugin() {
    }

    //Returns a Drupal style json of the plugin to be used with the webservice
    public String toJson() {
        String pluginData = formatBaseData() + ",";
        pluginData += "\"title\":\"" + getTitle() + "\",";
        pluginData += "\"field_category\":[{\"value\":\"" + getField_category() + "\"}],"
                + "\"field_developer\":{\"0\":{\"uid\":{\"uid\":\"" + getField_developer() + "\"}}},";
        pluginData += "\"field_status\":[{\"value\":\"" + getField_status() + "\"}],";
        if (!"".equalsIgnoreCase(formatFieldOS())) {
            pluginData += formatFieldOS() + ",";
        }
        if (!"".equalsIgnoreCase(formatTaxonomy())) {
            pluginData += formatTaxonomy() + ",";
        }
        pluginData += "\"field_description\":[{\"value\":\"" + getField_description() + "\"}],";
        if (!"".equalsIgnoreCase(formatFieldFile())) {
            pluginData += formatFieldFile() + ",";
        }
        pluginData += "\"body\":{\"und\":{\"0\":{\"value\":\"" + getBody() + "\"}}}"
                // + "}"
                + "}";
        return pluginData;
    }

    public String formatBaseData() {
        return "{\"type\":\"plugin\","
                + "\"language\":\"und\"";
//        return "{\"node\":"
//                + "{\"type\":\"plugin\","
//                + "\"language\":\"und\"";    
    }

    public String formatFieldOS() {
        String list = "";
        for (String s : fieldOs) {
            list += "\"" + s + "\":\"" + s + "\",";
        }
        //remove the last ,
        if (!"".equalsIgnoreCase(list)) {
            list = list.substring(0, list.length() - 1);
        } else {
            return "";
        }
        return "\"field_os\":{\"value\":{" + list + "}}";

    }

    public String formatTaxonomy() {
        String list = "";
        for (String s : taxonomy) {
            list += s + " ";
        }
        //remove the last space
        if (list != "") {
            list = list.substring(0, list.length() - 1);
        } else {
            return "";
        }
        return "\"taxonomy\":{\"tags\":{\"" + taxonomy.size() + "\":\"" + list + "\"}}";
    }

    //At this moment we only mantain one file on the plugin
    public String formatFieldFile() {
        if (fieldFile != null) {
            return "\"field_file\":{\"" + fieldFilePosition + "\":{" + fieldFile.formatFile() + "}}";
        } else {
            return "";
        }
    }
    //At this moment we only mantain one file on the plugin

    public String formatFieldCategory() {
        if (fieldCategory != null) {
            return "\"field_category\":[{\"value\":\"" + getField_category() + "\"}]";
        } else {
            return "";
        }
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * @param body the body to set
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the field_developer
     */
    public String getField_developer() {
        return fieldDeveloper;
    }

    /**
     * @param field_developer the field_developer to set
     */
    public void setField_developer(String field_developer) {
        this.fieldDeveloper = field_developer;
    }

    /**
     * @return the field_status
     */
    public String getField_status() {
        return fieldStatus;
    }

    /**
     * @return the field_description
     */
    public String getField_description() {
        return fieldDescription;
    }

    /**
     * @return the field_category
     */
    public PluginCategoryEnum getField_category() {
        return fieldCategory;
    }

    /**
     * @param field_status the field_status to set
     */
    public void setField_status(String field_status) {
        this.fieldStatus = field_status;
    }

    /**
     * @param field_description the field_description to set
     */
    public void setField_description(String field_description) {
        this.fieldDescription = field_description;
    }

    /**
     * @param field_category the field_category to set
     */
    public void setField_category(PluginCategoryEnum field_category) {
        this.fieldCategory = field_category;
    }

    public void addField_os(String os) {
        if (fieldOs == null) {
            fieldOs = new ArrayList<>();
        }
        if (!fieldOs.contains(os)) {
            fieldOs.add(os);
        }
    }

    public void removeField_os(String os) {
        if (fieldOs.contains(os)) {
            fieldOs.remove(os);
        }
    }

    public void setField_file(MarketPlacePluginFileField field_file) {
        setField_file(field_file, 0);
    }

    /**
     * @param fieldFile the field_file to set
     * @param position
     */
    public void setField_file(MarketPlacePluginFileField fieldFile, int position) {
        this.fieldFile = fieldFile;
        this.fieldFilePosition = position;
    }

    public void addTaxonomyWord(String word) {
        if (taxonomy == null) {
            taxonomy = new ArrayList<>();
        }
        if (!taxonomy.contains(word)) {
            taxonomy.add(word);
        }
    }

    public void removeTaxonomyWord(String word) {
        if (taxonomy.contains(word)) {
            taxonomy.remove(word);
        }
    }
}
