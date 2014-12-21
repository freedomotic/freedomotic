/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.helpers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author nicoletti
 */
public class HttpHelper {

    private DocumentBuilder documentBuilder;
    private XPath xPath;
    private final HttpParams httpParams = new BasicHttpParams();
    private static final int DEFAULT_TIMEOUT = 30000; //30seconds

    private static final Logger LOG = Logger.getLogger(HttpHelper.class.getName());

    public HttpHelper() {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(HttpHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        XPathFactory xpathFactory = XPathFactory.newInstance();
        xPath = xpathFactory.newXPath();
        setConnectionTimeout(DEFAULT_TIMEOUT);
    }

    /**
     * Get the content of an URL (eg: a webpage) as a string. Content can be
     * HTML, XML or JSON
     *
     * @param url
     * @return
     * @throws IOException
     */
    public String retrieveContent(String url) throws IOException {
        return doGet(url, null, null);
    }

    /**
     * Get the content of an URL (eg: a webpage) as a string. Content can be
     * HTML, XML or JSON
     *
     * @param url
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    public String retrieveContent(String url, String username, String password) throws IOException {
        return doGet(url, username, password);
    }

    /**
     * Perform an XPath query on the XML content retrieved from the given URL
     *
     * @param url The url from wich retrieve the XML content
     * @param username username if authentication is required. Can be null
     * @param password password if authentication is required. Can be null
     * @param xpathQueries any valid xpath query
     * @return
     * @throws IOException
     */
    public List<String> queryXml(String url, String username, String password, String... xpathQueries) throws IOException {
        String xmlContent = doGet(url, username, password);
        List<String> results = new ArrayList<>();
        try {
            InputSource is = new InputSource(new StringReader(xmlContent));
            Document xmlDocument = documentBuilder.parse(is);
            xmlDocument.getDocumentElement().normalize();

            //xpathQuery  contains the xpath expression to be applied on the retrieved content
            for (String xpathQuery : xpathQueries) {
                String result = xPath.compile(xpathQuery).evaluate(xmlDocument);
                // Notify an enpy result to the user
                if (result == null || result.isEmpty()) {
                    LOG.log(Level.WARNING, "XPath query {0} produced no results on content: \n{1}", new String[]{xpathQuery, xmlContent});
                    result = "";
                }
                results.add(result);
            }

        } catch (XPathExpressionException | SAXException | IOException ex) {
            throw new IOException("Cannot perform the given xpath query", ex);
        }
        return results;
    }

    public void setConnectionTimeout(int timeout) {
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
    }

    /**
     *
     * @return @throws IOException if the URL format is wrong or if cannot read
     * from source
     */
    private String doGet(String url, String username, String password) throws IOException {

        Authenticator.setDefault(new MyAuthenticator(username, password));

        DefaultHttpClient client = new DefaultHttpClient(httpParams);
        String decodedUrl;
        HttpGet request = null;
        try {
            decodedUrl = URLDecoder.decode(url, "UTF-8");
            request = new HttpGet(new URL(decodedUrl).toURI());
        } catch (URISyntaxException | MalformedURLException ex) {
            throw new IOException("The URL " + url + "' is not properly formatted: " + ex.getMessage(), ex);
        }
        HttpResponse response = client.execute(request);

        Reader reader = null;
        try {
            reader = new InputStreamReader(response.getEntity().getContent());

            StringBuilder buffer = new StringBuilder();
            int read;
            char[] cbuf = new char[1024];
            while ((read = reader.read(cbuf)) != -1) {
                buffer.append(cbuf, 0, read);
            }
            return buffer.toString();

        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    private class MyAuthenticator extends Authenticator {

        String username;
        String password;

        public MyAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            return (new PasswordAuthentication(username, password.toCharArray()));
        }
    }

    /*
     HttpHelper http = new HttpHelper();
     try {
     long start= System.currentTimeMillis();
     List<String> results = http.queryXml("http://api.openweathermap.org/data/2.5/weather?q=Trento&mode=xml", null, null,
     "//current/temperature/@value",
     "//current/temperature/@unit");
     long end= System.currentTimeMillis();
     System.out.println("Temperature in Trento: " + results.get(0) + " " + results.get(1) +  " -> " + (end-start));
     } catch (IOException ex) {
     System.out.println(ex.getMessage());
     }
     */
}
