/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.resttestclient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import com.google.gson.stream.JsonReader;

/**
 * POJO class that represents all information available from the Drupal
 * arketPlace that is retrieved using the Drupal Rest server. At this moment
 * only a few fields are parsed, just the necesary to retrieve the plugin zip
 *
 * @author GGPT
 */
public class MarketPlacePlugin {

    public static final String STATUS_PROOF_OF_CONCEPT = "Proof of Concept";
    public static final String STATUS_PROTOTYPE = "Prototype";
    public static final String STATUS_BETA_VERSION = "Beta Version";
    public static final String STATUS_STABLE_RELEASE = "Stable Release";
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
    private String filename;
////    private String field_requires;    
    private MarketPlacePluginFileField field_file; //***
    private String filepath;
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

    public String toJson() {
        String pluginData = formatBaseData() + ",";
        pluginData += "\"title\":\"" + title + "\",";
        pluginData += "\"field_category\":[{\"value\":\"" + field_category + "\"}],"
                + "\"field_developer\":{\"0\":{\"uid\":{\"uid\":\"" + field_developer + "\"}}},";
        pluginData += "\"field_status\":[{\"value\":\"" + field_status + "\"}],";
        if (formatFieldOS() != "") {
            pluginData += formatFieldOS() + ",";
        }
        if (formatTaxonomy() != "") {
            pluginData += formatTaxonomy() + ",";
        }
        pluginData += "\"field_description\":[{\"value\":\"" + field_description + "\"}],";
        if (formatFieldFile() != "") {
            pluginData += formatFieldFile() + ",";
        }
        pluginData += "\"body\":{\"und\":{\"0\":{\"value\":\"" + body + "\"}}}"
                + "}"
                + "}";
        return pluginData;
    }

    public String formatBaseData() {
        return "{\"node\":"
                + "{\"type\":\"plugin\","
                + "\"language\":\"und\"";

    }

    public String formatFieldOS() {
        String list = "";
        for (String s : field_os) {
            list += "\"" + s + "\":\"" + s + "\",";
        }
        //remove the last ,
        if (list != "") {
            list = list.substring(0, list.length() - 2);
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
            list = list.substring(0, list.length() - 2);
        } else {
            return "";
        }
        return "\"taxonomy\":{\"tags\":{\"" + list.length() + "\":\"" + list + "\"}}";


    }

    //At this moment we only mantain one file on the plugin
    public String formatFieldFile() {
        if (field_file != null) {
            return "\"field_file\":{\"0\":{" + field_file.formatFile() + "}}";
        } else {
            return "";
        }
    }

    //Very quick parse, should be done better
    public void parseJson(String json) {
        try {
            JsonReader reader = new JsonReader(new StringReader(json));
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("title")) {
                    title = reader.nextString();
                } else if (name.equals("path")) {
                    path = reader.nextString();
                } else if (name.equals("field_file")) {
                    reader.beginArray();
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String name2 = reader.nextName();
                        if (name2.equals("filename")) {
                            filename = reader.nextString();
                        } else if (name2.equals("filepath")) {
                            filepath = reader.nextString();
                        } else {
                            reader.skipValue();
                        }
                    }
                    reader.endObject();
                    reader.endArray();
                } else {
                    reader.skipValue(); //avoid some unhandle events
                }
            }
            reader.endObject();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
