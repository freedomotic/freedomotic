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
package com.freedomotic.plugins.devices.restapiv3.utils;

import java.io.InputStream;
import java.util.Scanner;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matteo Mazzoni
 */
@Provider
public class ThrowableExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = LoggerFactory.getLogger(ThrowableExceptionMapper.class.getName());
    @Context
    HttpServletRequest request;

    @Override
    public Response toResponse(Throwable t) {
        if (t instanceof WebApplicationException) {
            return ((WebApplicationException) t).getResponse();
        } else {
            String errorMessage = buildErrorMessage(request);
            LOG.error(errorMessage, t);
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
