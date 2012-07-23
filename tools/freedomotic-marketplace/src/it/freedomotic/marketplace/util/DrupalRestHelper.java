/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.marketplace.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import it.freedomotic.service.PluginPackage;
import java.awt.Image;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 *
 * @author GGPT
 */
public class DrupalRestHelper {

    public static final String DRUPALSCHEMA = "http";
    public static final String DRUPALPATH = "www.freedomotic.com";
    public static final String DEFAULTIMAGEPATH = "/sites/default/files/imagefield_default_images/Addons-64_0.png";
    public static ImageIcon defaultIconImage;

    public static ArrayList<PluginPackage> retrievePackageList() {
        ArrayList<PluginPackage> pluginPackageList = new ArrayList<PluginPackage>();
        ArrayList<MarketPlacePluginResume> resumes = retrieveResumes();
        for (MarketPlacePluginResume mpr : resumes) {
            if (mpr.getUri() != null) {
                pluginPackageList.add(createPluginPackage(mpr));
            }
        }
        return pluginPackageList;
    }

    protected static ImageIcon retrieveImageIcon(String drupalRelativePath) {

        ImageIcon imageIcon = null;
        URL url;
        try {
            URI uri = new URI(DRUPALSCHEMA, DRUPALPATH, drupalRelativePath,"","");
            url = uri.toURL();
            Image img = ImageIO.read(url);
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
        
            ClientResource cr = new ClientResource(DRUPALSCHEMA+"://"+DRUPALPATH+"/rest/node?parameters[type]=plugin");
            Representation test2 = cr.get();
            String jsonData2;

            jsonData2 = test2.getText();
            Gson gson = new Gson();
            Type collectionType = new TypeToken<ArrayList<MarketPlacePluginResume>>() {
            }.getType();
            pluginList = gson.fromJson(jsonData2, collectionType);
            defaultIconImage = retrieveImageIcon(DEFAULTIMAGEPATH);
        } catch (IOException ex) 
        {
        
        }         
        return pluginList;
    }

    protected static PluginPackage createPluginPackage(MarketPlacePluginResume mpr) {
        PluginPackage pp = new PluginPackage();
        ClientResource cr = new ClientResource(mpr.getUri());
        Representation test2 = cr.get();
        String jsonData;
        try {
            jsonData = test2.getText();
            pp.setIcon(defaultIconImage);
            fillPluginPackage(pp, jsonData);
        } catch (IOException ex) {
            //         Logger.getLogger(MarketPlacePluginResume.class.getName()).log(Level.SEVERE, null, ex);
        }
        return pp;
    }

    //Very quick parse, should be done better
    //This is a monster refactor!!!
    protected static void fillPluginPackage(PluginPackage pp, String json) {
        try {
            JsonReader reader = new JsonReader(new StringReader(json));
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("title")) {
                    pp.setTitle(reader.nextString());
                } else if (name.equals("uri")) {
                    pp.setUri(reader.nextString());
                } else if (name.equals("field_file")) {
                    reader.beginArray();
                    if (reader.peek() != JsonToken.NULL) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String name2 = reader.nextName();
                            if (name2.equals("filepath")) {
                                pp.setFilePath(DRUPALSCHEMA +"://"+DRUPALPATH + reader.nextString());
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    } else {
                        reader.nextNull();
                    }
                    reader.endArray();
                } else if (name.equals("field_description")) {
                    reader.beginArray();
                    if (reader.peek() != JsonToken.NULL) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String name2 = reader.nextName();
                            if (name2.equals("value")) {
                                pp.setDescription(reader.nextString());
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    } else {
                        reader.nextNull();
                    }
                    reader.endArray();

                } else if (name.equals("field_icon")) {
                    reader.beginArray();
                    if (reader.peek() != JsonToken.NULL) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String name2 = reader.nextName();
                            if (name2.equals("filepath")) {
                                String imagepath = "/"+reader.nextString();                                
                                pp.setIcon(retrieveImageIcon(imagepath));
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    } else {
                        reader.nextNull();
                    }
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
        } catch (java.lang.IllegalStateException e) {
            e.printStackTrace();
        }

    }
}
