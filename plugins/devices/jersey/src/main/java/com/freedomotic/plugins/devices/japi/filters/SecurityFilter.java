/*
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
*
* Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
*
* The contents of this file are subject to the terms of either the GNU
* General Public License Version 2 only ("GPL") or the Common Development
* and Distribution License("CDDL") (collectively, the "License"). You
* may not use this file except in compliance with the License. You can
* obtain a copy of the License at
* http://glassfish.java.net/public/CDDL+GPL_1_1.html
* or packager/legal/LICENSE.txt. See the License for the specific
* language governing permissions and limitations under the License.
*
* When distributing the software, include this License Header Notice in each
* file and include the License file at packager/legal/LICENSE.txt.
*
* GPL Classpath Exception:
* Oracle designates this particular file as subject to the "Classpath"
* exception as provided by Oracle in the GPL Version 2 section of the License
* file that accompanied this code.
*
* Modifications:
* If applicable, add the following below the License Header, with the fields
* enclosed by brackets [] replaced by your own identifying information:
* "Portions Copyright [year] [name of copyright owner]"
*
* Contributor(s):
* If you wish your version of this file to be governed by only the CDDL or
* only the GPL Version 2, indicate your decision by adding "[Contributor]
* elects to include this software in this distribution under the [CDDL or GPL
* Version 2] license." If you don't indicate a single choice of license, a
* recipient has the option to distribute your version of this file under
* either the CDDL, the GPL Version 2 or to extend the choice of license to
* its licensees as provided above. However, if you add GPL Version 2 code
* and therefore, elected the GPL Version 2 license, then the option applies
* only if the new code is made subject to such option by the copyright
* holder.
*/
package com.freedomotic.plugins.devices.japi.filters;

import com.freedomotic.api.API;
import java.io.IOException;
import java.security.Principal;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import javax.inject.Inject;
import org.apache.shiro.subject.Subject;

import org.glassfish.jersey.internal.util.Base64;
import org.glassfish.jersey.server.ContainerRequest;

/**
* Simple authentication filter.
*
* Returns response with http status 401 when proper authentication is not provided in incoming request.
*
* @author Pavel Bucek (pavel.bucek at oracle.com)
* @see ContainerRequestFilter
*/
@Provider
@PreMatching
public class SecurityFilter implements ContainerRequestFilter {

    
    @Inject
    javax.inject.Provider<UriInfo> uriInfo;
    private static final String REALM = "HTTPS Example authentication";
    
    private API api;
    
    @Override
    public void filter(ContainerRequestContext filterContext) throws IOException {
        Subject user = authenticate(filterContext.getRequest());
        filterContext.setSecurityContext(new ShiroAuthorizer(user));
    }
  
    @Inject
    public SecurityFilter(API api){
        this.api= api;
    }

    private Subject authenticate(Request request) {
        // Extract authentication credentials
        String authentication = ((ContainerRequest) request).getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authentication == null) {
            throw new AuthenticationException("Authentication credentials are required", REALM);
        }
        if (!authentication.startsWith("Basic ")) {
            return null;
            // additional checks should be done here
            // "Only HTTP Basic authentication is supported"
        }
        authentication = authentication.substring("Basic ".length());
        String[] values = Base64.decodeAsString(authentication).split(":");
        if (values.length < 2) {
            throw new WebApplicationException(400);
            // "Invalid syntax for username and password"
        }
        String username = values[0];
        String password = values[1];
        if ((username == null) || (password == null)) {
            throw new WebApplicationException(400);
            // "Missing username or password"
        }

        Subject user;
        // Validate the extracted credentials
        if (api.getAuth().login(username, password)) {
            
            user = api.getAuth().getSubject();
            Logger.getLogger(SecurityFilter.class.getName()).info("User: "+user);
        } else {
            System.out.println("USER NOT AUTHENTICATED");
            throw new AuthenticationException("Invalid username or password\r\n", REALM);
        }
        return user;
    }

    public class ShiroAuthorizer implements SecurityContext {

        private final Subject user;
       
        public ShiroAuthorizer(final Subject user) {
            this.user = user;
        }

        @Override
        public Principal getUserPrincipal() {
            return (Principal) user.getPrincipal();
        }

        @Override
        public boolean isUserInRole(String role) {
            return user.hasRole(role);
        }

        @Override
        public boolean isSecure() {
            return "https".equals(uriInfo.get().getRequestUri().getScheme());
        }

        @Override
        public String getAuthenticationScheme() {
            return SecurityContext.BASIC_AUTH;
        }
    }

    
}