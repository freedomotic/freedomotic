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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.freedomotic.marketplace.util.MarketPlaceFile;
import com.freedomotic.marketplace.util.MarketPlacePlugin2;
import java.io.*;
import java.lang.reflect.Type;
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
import org.slf4j.LoggerFactory;

/**
 *
 * @author Gabriel Pulido de Torres
 */
public class JavaUploader {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(JavaUploader.class.getName());
    public static final String DRUPALPATH = "http://www.freedomotic.com/";

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
            while (!"user".equalsIgnoreCase(name)) {
                reader.nextString();
                name = reader.nextName();
            }
            reader.beginObject();
            String value = reader.nextString();
            reader.close();
            return value;
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return null;

    }

    /**
     *
     */
    public static CookieSetting parseCookie(String loginResponse) {
        try {
            String sessionId = "";
            String sessionName = "";
            JsonReader reader = new JsonReader(new StringReader(loginResponse));
            reader.beginObject();
            String name = reader.nextName();
            if ("sessid".equalsIgnoreCase(name)) {
                sessionId = reader.nextString();
            }
            name = reader.nextName();
            if ("session_name".equalsIgnoreCase(name)) {
                sessionName = reader.nextString();
            }
            reader.close();
            return new CookieSetting(0, sessionName, sessionId);
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
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
        cr.setMethod(Method.POST);
        Response resp = cr.getResponse();
        if (resp.getStatus().isSuccess()) {
            try {
                return resp.getEntity().getText();
            } catch (IOException e) {
                LOG.error("IOException: ", e.getMessage());
            }
        } else {
            LOG.info(resp.getStatus().getName());
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
        Response resp = cr2.getResponse();
        String jsonResponse = "";
        String nid = "";
        if (resp.getStatus().isSuccess()) {
            try {
                jsonResponse = resp.getEntity().getText();
                System.out.println(jsonResponse);
                return jsonResponse;
            } catch (IOException e) {
                LOG.error("IOException: ", e.getMessage());
            }
        } else {
            LOG.info(resp.getStatus().getName());
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

        cr2.setMethod(Method.POST);

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
        pluginData += plugin.formatFieldFile();
        pluginData += "}";
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

        Response resp = testFileResource.getResponse();

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
                LOG.error("IOException: ", e.getMessage());
            }
        } else {
            LOG.info(resp.getStatus().getName());
        }
        return null;
    }

    //Helper method to transform a File to a byte[]
    public static byte[] fileToByteArray(File file) throws FileNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            for (int readNum; (readNum = fis.read(buf)) != -1;) {
                bos.write(buf, 0, readNum); //no doubt here is 0
                //Writes len bytes from the specified byte array starting at offset off to this byte array output stream.
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            }
        }
        return bos.toByteArray();
    }
}
