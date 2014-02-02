/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.marketplace.postplugin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.freedomotic.marketplace.util.DrupalRestHelper;
import com.freedomotic.marketplace.util.MarketPlaceFile;
import com.freedomotic.marketplace.util.MarketPlacePlugin2;
import java.io.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.CookieSetting;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.util.Base64;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 *
 * @author gpt
 */
public class JavaUploader {

    public static final String DRUPALPATH = "http://www.freedomotic.com/";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        //change this for real username and password        
        //String loginJson = login("username", "password");
        //CookieSetting cS = parseCookie(loginJson);
        //String uid = parseUid(loginJson);

        //String test = postTaxonomySelectNodes("151",0);        
        //List<MarketPlacePlugin2> testlist = DrupalRestHelper.retrievePluginsByCategory("151");
        MarketPlacePlugin2 pluginTest = (MarketPlacePlugin2) DrupalRestHelper.retrievePluginPackage("http://www.freedomotic.com/rest/node/1196");
//        MarketPlacePlugin2 pluginTest = (MarketPlacePlugin2) DrupalRestHelper.retrievePluginPackage("http://www.freedomotic.com/rest/node/394");
//        putPlugin(cS, "394", pluginTest); 
//        String nid = "394";
        //if (cS != null) {
        //String nid = postPlugin(cS, plugin);
        //String fid = postFile(cS, uid, "/home/gpt/Desarrollo/", "testfile1.zip");            
        //MarketPlacePluginFileField fileField = new MarketPlacePluginFileField(fid, "file asociated by code");
        //MarketPlaceFile fileField = postFile(cS, uid, "/home/gpt/Desarrollo/", "testfile1.zip", true);
        //plugin.setField_file(fileField);
        //putPlugin(cS, nid, plugin);
        //}        
    }

    /**
     * Obtains the user id from the login Json response
     *
     * @param loginResponse Drupal Json response to a login
     * @return String The uid of the user that matches the login
     */
    public static String parseUid(String loginResponse) {
        try {
            JsonReader reader = new JsonReader(new StringReader(loginResponse));
            reader.beginObject();
            String name = reader.nextName();
            while (!name.equals("user")) {
                reader.nextString();
                name = reader.nextName();
            }
            reader.beginObject();
            name = reader.nextName();
            String value = reader.nextString();
            reader.close();
            return value;
        } catch (IOException ex) {
            Logger.getLogger(JavaUploader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    /**
     *
     */
    public static CookieSetting parseCookie(String loginResponse) {
        try {
            String session_id = "";
            String session_name = "";
            JsonReader reader = new JsonReader(new StringReader(loginResponse));
            reader.beginObject();
            String name = reader.nextName();
            if (name.equals("sessid")) {
                session_id = reader.nextString();
            }
            name = reader.nextName();
            if (name.equals("session_name")) {
                session_name = reader.nextString();
            }
            reader.close();
            return new CookieSetting(0, session_name, session_id);
        } catch (IOException ex) {
            Logger.getLogger(JavaUploader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Login to the drupal web service.
     *
     * @param username Drupal valid username
     * @param password Drupal Valid password for the previous username
     * @return String the drupal Json response
     */
    public static String login(String username, String password) {

        ClientResource cr = new ClientResource(DRUPALPATH + "/rest/user/login");
        String jsonData = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        JsonRepresentation jsonRep = new JsonRepresentation(jsonData);
        cr.setMethod(Method.POST);
        Representation rep = cr.post(jsonRep);
        Response resp = cr.getResponse();
        if (resp.getStatus().isSuccess()) {
            try {
                return resp.getEntity().getText();

            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        } else {
            System.out.println(resp.getStatus().getName());
        }
        return null;
    }

    /**
     *
     *
     * @return the nid of the created plugin, "" if it has not been created
     */
    public static String postTaxonomyGetTree(String vocabularyNumber) {
        ClientResource cr2 = new ClientResource(DRUPALPATH + "/rest/taxonomy_vocabulary/getTree");
        String text = "{\"vid\":\"" + vocabularyNumber + "\"}";

        cr2.setMethod(Method.POST);
        Representation rep2 = cr2.post(new JsonRepresentation(text));
        Response resp = cr2.getResponse();
        String jsonResponse = "";
        String nid = "";
        if (resp.getStatus().isSuccess()) {
            try {
                jsonResponse = resp.getEntity().getText();
                System.out.println(jsonResponse);
                return jsonResponse;
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        } else {
            System.out.println(resp.getStatus().getName());
        }
        return "";
    }

    /**
     *
     *
     * @return A string with the selected nodes information as JSON
     * representation
     */
    public static String postTaxonomySelectNodes(String taxonomyTreeNumber, int page) {
        //plugin post                 
        try {
            ClientResource cr2 = new ClientResource(DRUPALPATH + "/rest/taxonomy_term/selectNodes?page=" + page);
            //String text = "{\"tids\":\""+taxonomyTreeNumber+"\",\"pager\":false}";        
            String text = "{\"tids\":\"" + taxonomyTreeNumber + "\"}";
            cr2.setMethod(Method.POST);
            JsonRepresentation jsonRepresentation = new JsonRepresentation(text);
            if (jsonRepresentation != null) {
                Representation rep2 = cr2.post(jsonRepresentation);
                Response resp = cr2.getResponse();
                String jsonResponse = "";
                String nid = "";
                if (resp.getStatus().isSuccess()) {
                    try {
                        jsonResponse = resp.getEntity().getText();
                        System.out.println(jsonResponse);
                        return jsonResponse;
                    } catch (IOException e) {
                        System.out.println("IOException: " + e.getMessage());
                    }
                } else {
                    System.out.println(resp.getStatus().getName());
                }
            }
        } catch (ResourceException resourceException) {
           // Freedomotic.logger.warning(resourceException.toString());
        }
        return "";
    }

    /**
     * Creates a new plugin info on the drupal site
     *
     * @param cS CookieSetting retrieved from the login method
     * @param plugin Plugin to be posted on the Drupal site
     * @return the nid of the created plugin, "" if it has not been created
     */
    public static String postPlugin(CookieSetting cS, MarketPlacePlugin plugin) {
        //plugin post                 
        ClientResource cr2 = new ClientResource(DRUPALPATH + "/rest/node");
        cr2.getRequest().getCookies().add(cS);
        String pluginData = plugin.toJson();
//        String pluginData = "{\"node\":"
//                + "{\"type\":\"plugin\","
//                + "\"title\":\""+plugin.getTitle()+"\","
//                + "\"language\":\"und\","
//                //+ "\"field_category\":[{\"value\":\""+plugin.getField_category()+"\"}],"
//                + "\"field_category\":{\"0\":{\"value\":\""+plugin.getField_category()+"\"}},"
//                + "\"field_plugin_category\":{\"0\":{\"value\":\"151\"}},"
//                + "\"field_developer\":{\"0\":{\"uid\":{\"uid\":\"gpulido\"}}},"
//                + "\"field_status\":[{\"value\":\""+plugin.getField_status()+"\"}],"
//                + "\"field_os\":{\"value\":{\"Linux\":\"Linux\",\"Windows\":\"Windows\"}},"
//                + "\"taxonomy\":{\"tags\":{\"2\":\"modbus gabriel\"}},"
//                + "\"field_description\":[{\"value\":\"Test of autocreated plugin.\"}],"
//                + "\"field_file\":{\"0\":{\"fid\":\"318\",\"data\":{\"description\":\"FileNameDescription\"}}},"
//                + "\"body\":{\"und\":{\"0\":{\"value\":\"This is the body of my node\"}}}"
//                + "}"
//                + "}";

        cr2.setMethod(Method.POST);

        Representation rep2 = cr2.post(new JsonRepresentation(pluginData));
        Response resp = cr2.getResponse();
        String jsonResponse = "";
        String nid = "";
        if (resp.getStatus().isSuccess()) {
            try {
                jsonResponse = resp.getEntity().getText();
                System.out.println(jsonResponse);
                //extract the fid field
                JsonReader reader = new JsonReader(new StringReader(jsonResponse));
                reader.beginObject();
                String jsonName = reader.nextName();
                if (jsonName.equals("nid")) {
                    nid = reader.nextString();
                }
                reader.close();

            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        } else {
            System.out.println(resp.getStatus().getName());
        }
        return nid;

    }

    /**
     * Update the plugin information of the drupal site with the plugin data
     *
     * @param cS CookieSetting retrieved from the login method
     * @param nodeId The nodeId of the node on the drupal site
     * @param plugin The plugin data that is going to be used on the update
     */
    @Deprecated
    public static void putPlugin(CookieSetting cS, String nodeId, MarketPlacePlugin plugin) {

        Client client = new Client(new Context(), Protocol.HTTP);
        client.getContext().getParameters().add("use ForwardedForHeader", "false");
        ClientResource pluginResource = new ClientResource(DRUPALPATH + "/rest/node/" + nodeId);
        pluginResource.setNext(client);
        pluginResource.getRequest().getCookies().add(cS);
        //the only data needed to update a plugin is the node, type, and field_os
        String pluginData = plugin.formatBaseData() + ",";
        pluginData += plugin.formatFieldOS() + ",";
        pluginData += "\"field_category\":{\"0\":{\"value\":\"" + plugin.getField_category() + "\"}},";
        pluginData += "\"field_plugin_category\":{\"0\":{\"value\":\"151\"}}";


        //pluginData += "\"field_plugin_category\":[{\"value\":\"151\"},{\"value\":null},{\"value\":null},{\"value\":null},{\"value\":null}]" +",";        
        //pluginData += plugin.formatFieldCategory();
        pluginData += plugin.formatFieldFile();
        pluginData += "}";
        //+ "}";
        Representation rep = pluginResource.put(new JsonRepresentation(pluginData));
        Response resp2 = pluginResource.getResponse();
        if (resp2.getStatus().isSuccess()) {
            try {
                System.out.println(resp2.getEntity().getText());
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        } else {
            System.out.println(resp2.getStatus().getName());
        }

    }

    /**
     * Update the plugin information of the drupal site with the plugin data
     *
     * @param cS CookieSetting retrieved from the login method
     * @param nodeId The nodeId of the node on the drupal site
     * @param plugin The plugin data that is going to be used on the update
     */
    public static void putPlugin(CookieSetting cS, String nodeId, MarketPlacePlugin2 plugin) {

        Client client = new Client(new Context(), Protocol.HTTP);
        client.getContext().getParameters().add("use ForwardedForHeader", "false");
        ClientResource pluginResource = new ClientResource(DRUPALPATH + "/rest/node/" + nodeId);
        pluginResource.setNext(client);
        pluginResource.getRequest().getCookies().add(cS);
        //the only data needed to update a plugin is the node, type, field_os and plugin_category
        String pluginData = "{"
                + plugin.formatBaseData() + ","
                + plugin.formatFieldCategory() + ","
                + plugin.formatFieldPluginCategory() + ","
                + plugin.formatFieldOS() + ","
                + plugin.formatFieldFile()
                + "}";
        System.out.println("PluginData " + pluginData);
        Representation rep = pluginResource.put(new JsonRepresentation(pluginData));
        Response resp2 = pluginResource.getResponse();
        if (resp2.getStatus().isSuccess()) {
            try {
                System.out.println(resp2.getEntity().getText());
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        } else {
            System.out.println(resp2.getStatus().getName());
        }

    }

    /**
     * Uploads a new file on the drupal site
     *
     * @param cS CookieSetting retrieved from the login method
     * @param uid User id. Must be the same as the login
     * @param pathName Local path to the file to be uploaded (without the name)
     * @param name name of the file to be uploaded
     * @return The MarketPlaceFile of the new posted file
     * @throws IOException
     */
    public static MarketPlaceFile postFile(CookieSetting cS, String uid, String pathName, String name) throws IOException {
        //TODO: check what happens when the file already exists
        // Instantiate the client connector, and configure it.
        Client client = new Client(new Context(), Protocol.HTTP);
        client.getContext().getParameters().add("use ForwardedForHeader", "false");

        ClientResource testFileResource = new ClientResource(DRUPALPATH + "/rest/file");

        testFileResource.setNext(client);
        testFileResource.getRequest().getCookies().add(cS);

        //   File zipFile = new File("/home/gpt/Desarrollo/testfile1.zip");
        File zipFile = new File(pathName + "/" + name);
        int size = (int) zipFile.length();
        String base64String = Base64.encode(fileToByteArray(zipFile), false);
        //we post the file as uid 0
        String fileData = "{\"uid\":\"" + uid + "\","
                + "\"filename\":\"" + name + "\","
                + "\"filesize\":\"" + size + "\","
                + "\"file\":\"" + base64String + "\""
                + "}";
        testFileResource.setMethod(Method.POST);

        Representation rep2 = testFileResource.post(new JsonRepresentation(fileData));
        Response resp = testFileResource.getResponse();
        // {"fid":"320","uri":"http://freedomotic.com/rest/file/320"}     
        String jsonResponse = "";
        String fid = "";
        if (resp.getStatus().isSuccess()) {
            try {
                jsonResponse = resp.getEntity().getText();
                System.out.println(jsonResponse);
                Gson gson = new Gson();
                Type collectionType = new TypeToken<MarketPlaceFile>() {
                }.getType();
                MarketPlaceFile pluginFile = gson.fromJson(jsonResponse, collectionType);
                pluginFile.setFilename(name);
                return pluginFile;
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        } else {
            System.out.println(resp.getStatus().getName());
        }
        return null;
    }

    //Helper method to transform a File to a byte[]
    public static byte[] fileToByteArray(File file) throws FileNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        //System.out.println(file.exists() + "!!");
        //InputStream in = resource.openStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            for (int readNum; (readNum = fis.read(buf)) != -1;) {
                bos.write(buf, 0, readNum); //no doubt here is 0
                //Writes len bytes from the specified byte array starting at offset off to this byte array output stream.
                //System.out.println("read " + readNum + " bytes,");
            }
        } catch (IOException ex) {
            //Logger.getLogger(genJpeg.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bos.toByteArray();

    }
}
