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

import com.google.common.collect.ImmutableMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.io.IOUtils.toByteArray;

/**
 * @author Enrico Nicoletti
 */
public class HttpHelper {

    private static final Logger LOG = LoggerFactory.getLogger(HttpHelper.class.getName());
    private static final int DEFAULT_TIMEOUT = 30_000; //30seconds
    private static final Charset DEFAULT_UTF8 = Charset.forName("UTF-8");

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
        return doGetAsString(url, null, null);
    }

    /**
     * Post the content to given URL and returns result as byte[]. Post content type should be defined beforehand.
     *
     * @param url      of the service
     * @param content  in byte format that is going to be post
     * @param username for basic authentication
     * @param password for basic authentication
     * @return post result as byte array
     * @throws IOException
     */
    public byte[] post(String url, byte[] content, String username, String password) throws IOException {
        return post(url, content, username, password, ImmutableMap.<String, String>of());
    }

    /**
     * @param url      of the service
     * @param content  in byte format that is going to be post
     * @param username for basic authentication
     * @param password for basic authentication
     * @param headers  http headers
     * @return post result as byte array
     */
    public byte[] post(String url, byte[] content, String username, String password,
                       Map<String, String> headers) throws IOException {
        return doPost(url, content, username, password, headers);
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
        return doGetAsString(url, username, password);
    }

    /**
     * Perform an XPath query on the XML content retrieved from the given URL
     *
     * @param url          The url from wich retrieve the XML content
     * @param username     username if authentication is required. Can be null
     * @param password     password if authentication is required. Can be null
     * @param xpathQueries any valid xpath query
     * @return
     * @throws IOException
     */
    public List<String> queryXml(String url, String username, String password, String... xpathQueries) throws IOException {
        byte[] xmlContent = doGet(url, username, password);
        List<String> results = new ArrayList<>();
        try {
            InputSource is = new InputSource(new ByteArrayInputStream(xmlContent));
            Document xmlDocument = documentBuilder.parse(is);
            xmlDocument.getDocumentElement().normalize();

            //xpathQuery  contains the xpath expression to be applied on the retrieved content
            for (String xpathQuery : xpathQueries) {
                String result = xPath.compile(xpathQuery).evaluate(xmlDocument);
                // Notify an enpy result to the user
                if (result == null || result.isEmpty()) {
                    LOG.warn("XPath query {} produced no results on content: \n{}", new String[]{xpathQuery, new String(xmlContent)});
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

    private byte[] doPost(String url, byte[] content, String username, String password,
                          Map<String, String> headers) throws IOException {

        final HttpPost httpPost = new HttpPost(asUri(url));
        final HttpEntity httpEntity = new ByteArrayEntity(content);
        httpPost.setEntity(httpEntity);

        for (Map.Entry<String, String> header : headers.entrySet()) {
            httpPost.setHeader(header.getKey(), header.getValue());
        }

        final HttpResponse httpResponse = fireHttpRequest(httpPost, username, password);
        try (InputStream inputStream = httpResponse.getEntity().getContent()) {
            return toByteArray(inputStream);
        }
    }

    private byte[] doGet(String url, String username, String password) throws IOException {
        HttpResponse httpResponse = fireGetRequest(url, username, password);
        try (InputStream inputStream = httpResponse.getEntity().getContent()) {
            return toByteArray(inputStream);
        }
    }

    private String doGetAsString(String url, String username, String password) throws IOException {
        HttpResponse httpResponse = fireGetRequest(url, username, password);
        try (InputStream inputStream = httpResponse.getEntity().getContent()) {
            byte[] content = toByteArray(inputStream);
            return new String(content, determineCharsetName(httpResponse));
        }
    }

    private HttpResponse fireGetRequest(String url, String username, String password) throws IOException {
        return fireHttpRequest(new HttpGet(asUri(url)), username, password);
    }

    private HttpResponse fireHttpRequest(HttpRequestBase httpRequest, String username, String password) throws IOException {
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        final CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(credentialsProvider)
                .setDefaultRequestConfig(RequestConfig.copy(RequestConfig.DEFAULT)
                        .setConnectTimeout(connectionTimeout)
                        .build())
                .build();
        return client.execute(httpRequest);
    }

    private Charset determineCharsetName(HttpResponse response) {
        ContentType contentType = ContentType.getLenient(response.getEntity());
        if (contentType != null && contentType.getCharset() != null) {
            return contentType.getCharset();
        }
        return DEFAULT_UTF8;
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
