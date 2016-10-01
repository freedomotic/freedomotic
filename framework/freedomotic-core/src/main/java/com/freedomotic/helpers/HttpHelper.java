/**
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
 * <p>
 * This file is part of Freedomotic
 * <p>
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 * <p>
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.helpers;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrico Nicoletti
 */
public class HttpHelper {

    private static final Logger LOG = LoggerFactory.getLogger(HttpHelper.class.getName());
    private static final int DEFAULT_TIMEOUT = 30_000; //30seconds

    private DocumentBuilder documentBuilder;
    private XPath xPath;
    private int connectionTimeout = DEFAULT_TIMEOUT;

    public HttpHelper() {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            LOG.error(ex.getMessage());
        }
        XPathFactory xpathFactory = XPathFactory.newInstance();
        xPath = xpathFactory.newXPath();
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
                    LOG.warn("XPath query {} produced no results on content: \n{}", new String[]{xpathQuery, xmlContent});
                    result = "";
                }
                results.add(result);
            }

        } catch (XPathExpressionException | SAXException | IOException ex) {
            throw new IOException("Cannot perform the given xpath query", ex);
        }
        return results;
    }

    /**
     * Determines the timeout in milliseconds until a connection is established.
     * A timeout value of zero is interpreted as an infinite timeout.
     * <br>
     * See also {@link RequestConfig#getConnectTimeout()}
     *
     * @param connectionTimeout timeout in milliseconds
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * @return @throws IOException if the URL format is wrong or if cannot read
     * from source
     */
    private String doGet(String url, String username, String password) throws IOException {

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(credentialsProvider)
                .setDefaultRequestConfig(RequestConfig.copy(RequestConfig.DEFAULT)
                        .setConnectTimeout(connectionTimeout)
                        .build())
                .build();

        HttpGet httpGet = new HttpGet(asUri(url));
        HttpResponse response = client.execute(httpGet);

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

    private URI asUri(String url) throws IOException {
        URI uri;
        try {
            String decodedUrl = URLDecoder.decode(url, "UTF-8");
            uri = new URL(decodedUrl).toURI();
        } catch (URISyntaxException | MalformedURLException ex) {
            throw new IOException("The URL " + url + "' is not properly formatted: " + ex.getMessage(), ex);
        }
        return uri;
    }

}
