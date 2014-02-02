/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.resttestclient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
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

import com.google.gson.stream.JsonReader;

/**
 *
 * @author gpt
 */
public class RestTestClient {

    public static final String DRUPALPATH = "http://freedomotic.com/";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {


        CookieSetting cS = login("gpulido", "senioranillos");
        if (cS != null) {
            //postPlugin(cS);
            //putPlugin(cS,831);
            //postFile(cS);
        }

    }

    public static CookieSetting login(String username, String password) {
        try {
            ClientResource cr = new ClientResource(DRUPALPATH + "/rest/user/login");
            String jsonData = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
            JsonRepresentation jsonRep = new JsonRepresentation(jsonData);
            cr.setMethod(Method.POST);
            Representation rep = cr.post(jsonRep);
            Response resp = cr.getResponse();
            String jsonResponse = "";
            if (resp.getStatus().isSuccess()) {
                try {
                    jsonResponse = resp.getEntity().getText();
                    System.out.println(jsonResponse);
                } catch (IOException e) {
                    System.out.println("IOException: " + e.getMessage());
                }
            } else {
                System.out.println(resp.getStatus().getName());
            }

            String session_id = "";
            String session_name = "";
            JsonReader reader = new JsonReader(new StringReader(jsonResponse));
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
            Logger.getLogger(RestTestClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    //creates a new plugin info on the drupal site
    public static String postPlugin(CookieSetting cS, MarketPlacePlugin plugin) {
        //plugin post                 
        ClientResource cr2 = new ClientResource(DRUPALPATH + "/rest/node");
        cr2.getRequest().getCookies().add(cS);
        String pluginData = plugin.toJson();
//        String pluginData = "{\"node\":"
//                + "{\"type\":\"plugin\","
//                + "\"title\":\""+plugin.getTitle()+"\","
//                + "\"language\":\"und\","
//                + "\"field_category\":[{\"value\":\""+plugin.getField_category()+"\"}],"
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
        Response resp2 = cr2.getResponse();
        if (resp2.getStatus().isSuccess()) {
            try {
                System.out.println(resp2.getEntity().getText());
                return (resp2.getEntity().getText());
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        } else {
            System.out.println(resp2.getStatus().getName());
        }
        return null;

    }

    //Update the plugin information
    public static void putPlugin(CookieSetting cS, int nodeId, MarketPlacePlugin plugin) {

        Client client = new Client(new Context(), Protocol.HTTP);
        client.getContext().getParameters().add("use ForwardedForHeader", "false");
        ClientResource pluginResource = new ClientResource(DRUPALPATH + "/rest/node/" + nodeId);
        pluginResource.setNext(client);
        pluginResource.getRequest().getCookies().add(cS);
        //the only data needed to update a plugin is the node, type, and field_os
        String pluginData = plugin.formatBaseData() + ",";
        pluginData += plugin.formatFieldOS() + ",";
        pluginData += plugin.formatFieldFile();
        pluginData += "}"
                + "}";
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

    //Uploads a new file on the drupal site
    public static String postFile(CookieSetting cS, String pathName, String name) throws IOException {
        //TODO: check what happens when the file already exists
        // Instantiate the client connector, and configure it.
        Client client = new Client(new Context(), Protocol.HTTP);
        client.getContext().getParameters().add("use ForwardedForHeader", "false");

        ClientResource testFileResource = new ClientResource(DRUPALPATH + "/rest/file");

        testFileResource.setNext(client);
        testFileResource.getRequest().getCookies().add(cS);


        //   File zipFile = new File("/home/gpt/Desarrollo/testfile1.zip");
        File zipFile = new File(pathName + name);
        int size = (int) zipFile.length();
        String base64String = Base64.encode(fileToByteArray(zipFile), false);
        //we post the file as uid 0
        String fileData = "{\"uid\":\"0\","
                + "\"filename\":\"" + name + "\","
                + "\"filesize\":\"" + size + "\","
                + "\"file\":\"" + base64String + "\""
                + "}";
        testFileResource.setMethod(Method.POST);

        Representation rep2 = testFileResource.post(new JsonRepresentation(fileData));
        Response resp2 = testFileResource.getResponse();
        if (resp2.getStatus().isSuccess()) {
            try {
                return resp2.getEntity().getText();
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        } else {
            System.out.println(resp2.getStatus().getName());
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
                System.out.println("read " + readNum + " bytes,");
            }
        } catch (IOException ex) {
            //Logger.getLogger(genJpeg.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bos.toByteArray();

    }
}
