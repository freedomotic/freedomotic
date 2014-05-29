/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.freedomotic.plugins.devices.japi.utils;

import java.io.InputStream;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author MazzoniM
 */
@Provider
public class ThrowableExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger log = Logger.getLogger(ThrowableExceptionMapper.class.getName());
    @Context
    HttpServletRequest request;

    @Override
    public Response toResponse(Throwable t) {
        if (t instanceof WebApplicationException) {
            return ((WebApplicationException) t).getResponse();
        } else {
            String errorMessage = buildErrorMessage(request);
            log.log(Level.SEVERE,errorMessage, t);
            return Response.serverError().entity("").build();
        }
    }

    private String buildErrorMessage(HttpServletRequest req) {
        StringBuilder message = new StringBuilder();
        String entity = "(empty)";

        try {
            // How to cache getInputStream: http://stackoverflow.com/a/17129256/356408
            InputStream is = req.getInputStream();
            // Read an InputStream elegantly: http://stackoverflow.com/a/5445161/356408
            Scanner s = new Scanner(is, "UTF-8").useDelimiter("\\A");
            entity = s.hasNext() ? s.next() : entity;
        } catch (Exception ex) {
            // Ignore exceptions around getting the entity
        }

        message.append("Uncaught REST API exception:\n");
        message.append("URL: ").append(getOriginalURL(req)).append("\n");
        message.append("Method: ").append(req.getMethod()).append("\n");
        message.append("Entity: ").append(entity).append("\n");

        return message.toString();
    }

    private String getOriginalURL(HttpServletRequest req) {
        // Rebuild the original request URL: http://stackoverflow.com/a/5212336/356408
        String scheme = req.getScheme();             // http
        String serverName = req.getServerName();     // hostname.com
        int serverPort = req.getServerPort();        // 80
        String contextPath = req.getContextPath();   // /mywebapp
        String servletPath = req.getServletPath();   // /servlet/MyServlet
        String pathInfo = req.getPathInfo();         // /a/b;c=123
        String queryString = req.getQueryString();   // d=789

        // Reconstruct original requesting URL
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        if (serverPort != 80 && serverPort != 443) {
            url.append(":").append(serverPort);
        }

        url.append(contextPath).append(servletPath);

        if (pathInfo != null) {
            url.append(pathInfo);
        }

        if (queryString != null) {
            url.append("?").append(queryString);
        }

        return url.toString();
    }
}