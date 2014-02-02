/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.marketplace.postplugin;

import java.util.ArrayList;

/**
 * POJO class that represents all information available from the Drupal
 * MarketPlace that is retrieved using the Drupal Rest server. At this moment
 * only a few fields are parsed, just the necesary to retrieve the plugin zip
 *
 * @author GGPT
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
//    private String nid;
//    private String type;
//    private String language;
//    private String uid;
//    private String status;
//    private String created;
//    private String changed;
//    private String comment;
//    private String promote;
//    private String moderate;
//    private String sticky;
//    private String tnid;
//    private String translate;    
//
//    private String revision_uid;
    private String title;
    private String body; //XML
////    private String teaser; //XML
////    private String log;
//////    private String revision_timestamp;
//////    private String format;
////    private String name;
////    private String picture;
//    private String data;
    private String path;
    private String field_developer; //TODO check for correct developername
    private String field_status;//list
////    private String field_forum;
    private String field_description;
    private PluginCategoryEnum field_category;
////    private String field_icon;
    private ArrayList<String> field_os;
////    private String filename;
////    private String field_requires;    
    private MarketPlacePluginFileField field_file; //*** 
    private int field_file_position = 0;
    //    private String field_screenshot;
//    private String field_hardware;
//    private String last_comment_timestamp;
//    private String last_comment_name;
//    private String comment_count;
    private ArrayList<String> taxonomy;
//    private String files; 
//    private String nodewords; 
//    private String copyright;
//    private String dc_contributor;
//    private String dc_creator;
//    private String dc_date;
//    private String dc_title;
//    private String description;
//    private String keywords;
//    private String location;
//    private String pics_label;
//    private String revisit_after;
//    private String robots;
//    private String uri;

    //private String vid;
    public MarketPlacePlugin() {
    }

    //Returns a Drupal style json of the plugin to be used with the webservice
    public String toJson() {
        String pluginData = formatBaseData() + ",";
        pluginData += "\"title\":\"" + getTitle() + "\",";
        pluginData += "\"field_category\":[{\"value\":\"" + getField_category() + "\"}],"
                + "\"field_developer\":{\"0\":{\"uid\":{\"uid\":\"" + getField_developer() + "\"}}},";
        pluginData += "\"field_status\":[{\"value\":\"" + getField_status() + "\"}],";
        if (formatFieldOS() != "") {
            pluginData += formatFieldOS() + ",";
        }
        if (formatTaxonomy() != "") {
            pluginData += formatTaxonomy() + ",";
        }
        pluginData += "\"field_description\":[{\"value\":\"" + getField_description() + "\"}],";
        if (formatFieldFile() != "") {
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
        for (String s : field_os) {
            list += "\"" + s + "\":\"" + s + "\",";
        }
        //remove the last ,
        if (list != "") {
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
        if (field_file != null) {
            return "\"field_file\":{\"" + field_file_position + "\":{" + field_file.formatFile() + "}}";
        } else {
            return "";
        }
    }
    //At this moment we only mantain one file on the plugin

    public String formatFieldCategory() {
        if (field_category != null) {
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
        return field_developer;
    }

    /**
     * @param field_developer the field_developer to set
     */
    public void setField_developer(String field_developer) {
        this.field_developer = field_developer;
    }

    /**
     * @return the field_status
     */
    public String getField_status() {
        return field_status;
    }

    /**
     * @return the field_description
     */
    public String getField_description() {
        return field_description;
    }

    /**
     * @return the field_category
     */
    public PluginCategoryEnum getField_category() {
        return field_category;
    }

    /**
     * @param field_status the field_status to set
     */
    public void setField_status(String field_status) {
        this.field_status = field_status;
    }

    /**
     * @param field_description the field_description to set
     */
    public void setField_description(String field_description) {
        this.field_description = field_description;
    }

    /**
     * @param field_category the field_category to set
     */
    public void setField_category(PluginCategoryEnum field_category) {
        this.field_category = field_category;
    }

    public void addField_os(String os) {
        if (field_os == null) {
            field_os = new ArrayList<String>();
        }
        if (!field_os.contains(os)) {
            field_os.add(os);
        }
    }

    public void removeField_os(String os) {
        if (field_os.contains(os)) {
            field_os.remove(os);
        }
    }

    public void setField_file(MarketPlacePluginFileField field_file) {
        setField_file(field_file, 0);
    }

    /**
     * @param field_file the field_file to set
     */
    public void setField_file(MarketPlacePluginFileField field_file, int position) {
        this.field_file = field_file;
        this.field_file_position = position;
    }

    public void addTaxonomyWord(String word) {
        if (taxonomy == null) {
            taxonomy = new ArrayList<String>();
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
