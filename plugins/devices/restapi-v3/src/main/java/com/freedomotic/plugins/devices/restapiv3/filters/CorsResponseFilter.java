/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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
package com.freedomotic.plugins.devices.restapiv3.filters;

/**
 *
 * @author matteo
 */
import com.freedomotic.model.ds.Config;
import java.net.URI;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;

@Provider
@PreMatching
public class CorsResponseFilter implements ContainerResponseFilter {

    /**
     * Add the cross domain data to the output if needed
     *
     * @param creq The container request (input)
     * @param cres The container request (output)
     */
    Config config;

    public CorsResponseFilter(Config config) {
        this.config = config;
    }

    @Override
    public void filter(ContainerRequestContext creq, ContainerResponseContext cres) {
        /* Access-Control-Allow-Origin must contain one and only one occurrence of allowed origin 
        * moreover no wilcard is allowed for SSE,
        * so, we can define as many allowed origins we want, in config param - or even a wildcard '*'
        * if client origin is found in the allowed list 
        * its occurrence is inserted in the response header
        */
        String origin = creq.getHeaderString("Origin");
        String conf_allow_origin = config.getStringProperty("Access-Control-Allow-Origin", "*");
        if (conf_allow_origin.equals("*") || conf_allow_origin.contains(origin)) {
            cres.getHeaders().add("Access-Control-Allow-Origin", origin);
        }

        cres.getHeaders().add("Access-Control-Allow-Headers",
                config.getStringProperty("Access-Control-Allow-Headers",
                        "Accept,Accept-Version,Authorization,Content-Length,Content-MD5,Content-Type,Date,"
                        + "Origin,X-Access-Token,X-Api-Version,X-CSRF-Token,X-File-Name,X-Requested-With"));

        cres.getHeaders().add("Access-Control-Allow-Methods",
                config.getStringProperty("Access-Control-Allow-Methods", "GET,PUT,HEAD,POST,DELETE,OPTIONS"));

        cres.getHeaders().add("Access-Control-Max-Age",
                config.getStringProperty("Access-Control-Max-Age", "1209600"));

        cres.getHeaders().add("Access-Control-Allow-Credentials", "true");

    }
}
