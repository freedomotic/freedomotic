/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.marketplace.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.freedomotic.marketplace.IPluginPackage;
import com.freedomotic.marketplace.postplugin.JavaUploader;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author GPT
 */
public class DrupalRestHelper {

    public static final String DRUPALSCHEMA = "http";
    public static final String DRUPALPATH = "www.freedomotic.com";
    public static final String DRUPALSERVICE = "rest";
    public static final String DEFAULTIMAGEPATH = "/sites/default/files/imagefield_default_images/Addons-64_0.png";
    public static ImageIcon defaultIconImage;

    public static List<IPluginPackage> retrieveAllPlugins() {

        List<IPluginPackage> pluginPackageList = new ArrayList<IPluginPackage>();
        ArrayList<MarketPlacePluginResume> resumes = retrieveResumes();
        for (MarketPlacePluginResume mpr : resumes) {
            if (mpr.getUri() != null) {
                pluginPackageList.add(mpr.getPlugin());
            }
        }
        return pluginPackageList;

    }

    protected static ImageIcon retrieveImageIcon(String drupalRelativePath) {

        ImageIcon imageIcon = null;
        URL url;
        try {
            URI uri = new URI(DRUPALSCHEMA, DRUPALPATH, drupalRelativePath, "", "");
            url = uri.toURL();
            ImageIcon imgIcon = new ImageIcon(url);
            Image img = imgIcon.getImage();
            //TODO: scale the same from both dimensions
            Image newimg = img.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH);
            imageIcon = new ImageIcon(newimg);
        } catch (URISyntaxException ex) {
            Logger.getLogger(DrupalRestHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DrupalRestHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return imageIcon;
    }

    protected static ArrayList<MarketPlacePluginResume> retrieveResumes() {
        ArrayList<MarketPlacePluginResume> pluginList = new ArrayList<MarketPlacePluginResume>();
        try {
            ClientResource cr = new ClientResource(DRUPALSCHEMA + "://" + DRUPALPATH + "/rest/node?parameters[type]=plugin");
            Representation test2 = cr.get();
            String jsonData2;
            jsonData2 = test2.getText();
            Gson gson = new Gson();
            Type collectionType = new TypeToken<ArrayList<MarketPlacePluginResume>>() {
            }.getType();
            pluginList = gson.fromJson(jsonData2, collectionType);
            defaultIconImage = retrieveImageIcon(DEFAULTIMAGEPATH);
        } catch (IOException ex) {
        }
        return pluginList;
    }

    public static ArrayList<MarketPlacePluginCategory> retrieveCategories() {
        ArrayList<MarketPlacePluginCategory> categoryList = new ArrayList<MarketPlacePluginCategory>();
        String jsonData = JavaUploader.postTaxonomyGetTree("5");
        Gson gson = new Gson();
        Type collectionType = new TypeToken<ArrayList<MarketPlacePluginCategory>>() {
        }.getType();
        categoryList = gson.fromJson(jsonData, collectionType);
        return categoryList;
    }

    public static ArrayList<MarketPlacePlugin2> retrievePluginsByCategory(String categoryId) {
        ArrayList<MarketPlacePlugin2> pluginPackageList = new ArrayList<MarketPlacePlugin2>();
        int page = 0;
        boolean newData = true;
        String previousJsonData = "EMPTY";
        ArrayList<MarketPlacePlugin2> pagePluginPackageList = new ArrayList<MarketPlacePlugin2>();
        while (newData) {
            String jsonData = "";
            try {
                jsonData = JavaUploader.postTaxonomySelectNodes(categoryId, page);
                if (!jsonData.isEmpty()) {
                    if (page == 0 || !jsonData.equals(previousJsonData)) {
                        Gson gson = new Gson();
                        Type collectionType = new TypeToken<ArrayList<MarketPlacePlugin2>>() {
                        }.getType();
                        pagePluginPackageList = gson.fromJson(jsonData, collectionType);
                        pluginPackageList.addAll(pagePluginPackageList);
                        page++;
                        previousJsonData = jsonData;
                    } else {
                        newData = false;
                    }
                } else {
                    previousJsonData = "EMPTY";
                    newData = false;
                }
            } catch (Exception e) {
                //Freedomotic.logger.severe(e.getMessage());
            }
        }
        return pluginPackageList;

    }

    public static IPluginPackage retrievePluginPackage(String uri) {
        ClientResource cr = new ClientResource(uri);
        Gson gson;
        try {
            String jsonData = cr.get().getText();
            gson = new Gson();
            Type collectionType = new TypeToken<MarketPlacePlugin2>() {
            }.getType();
            return gson.fromJson(jsonData, collectionType);
        } catch (IOException ex) {
            //         Logger.getLogger(MarketPlacePluginResume.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    //Very quick parse, should be done better
    //This is a monster refactor!!!
//    protected static void fillPluginPackage(PluginPackage pp, String json) {
//        try {
//            JsonReader reader = new JsonReader(new StringReader(json));
//            reader.beginObject();
//            while (reader.hasNext()) {
//                String name = reader.nextName();
//                if (name.equals("title")) {
//                    pp.setTitle(reader.nextString());
//                } else if (name.equals("uri")) {
//                    pp.setUri(reader.nextString());
//                } else if (name.equals("field_file")) {
//                    reader.beginArray();
//                    if (reader.peek() != JsonToken.NULL) {
//                        reader.beginObject();
//                        while (reader.hasNext()) {
//                            String name2 = reader.nextName();
//                            if (name2.equals("filepath")) {
//                                pp.setFilePath(DRUPALSCHEMA +"://"+DRUPALPATH +"/"+ reader.nextString());
//                            } else {
//                                reader.skipValue();
//                            }
//                        }
//                        reader.endObject();
//                    } else {
//                        reader.nextNull();
//                    }
//                    reader.endArray();
//                } else if (name.equals("field_description")) {
//                    reader.beginArray();
//                    if (reader.peek() != JsonToken.NULL) {
//                        reader.beginObject();
//                        while (reader.hasNext()) {
//                            String name2 = reader.nextName();
//                            if (name2.equals("value")) {
//                                pp.setDescription(reader.nextString());
//                            } else {
//                                reader.skipValue();
//                            }
//                        }
//                        reader.endObject();
//                    } else {
//                        reader.nextNull();
//                    }
//                    reader.endArray();
//
//                } else if (name.equals("field_icon")) {
//                    reader.beginArray();
//                    if (reader.peek() != JsonToken.NULL) {
//                        reader.beginObject();
//                        while (reader.hasNext()) {
//                            String name2 = reader.nextName();
//                            if (name2.equals("filepath")) {
//                                String imagepath = "/"+reader.nextString();                                
//                                pp.setIcon(retrieveImageIcon(imagepath));
//                            } else {
//                                reader.skipValue();
//                            }
//                        }
//                        reader.endObject();
//                    } else {
//                        reader.nextNull();
//                    }
//                    reader.endArray();
//
//                } else {
//                    reader.skipValue(); //avoid some unhandle events
//                }
//            }
//            reader.endObject();
//            reader.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (java.lang.IllegalStateException e) {
//            e.printStackTrace();
//        }
//
//    }
}
