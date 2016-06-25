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
package com.freedomotic.plugins.devices.restapiv3.resources.atmosphere;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.freedomotic.api.API;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.plugins.devices.restapiv3.RestAPIv3;
import com.freedomotic.plugins.devices.restapiv3.representations.PermissionCheckRepresentation;
import com.freedomotic.security.User;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.wordnik.swagger.annotations.Api;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import org.apache.shiro.subject.Subject;
import org.atmosphere.config.service.AtmosphereService;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.FrameworkConfig;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.ShiroInterceptor;

/**
 *
 * @author Matteo Mazzoni
 */
@Path(AtmospherePermissionCheckResource.PATH)
@Api(value = "ws_permissionCheck", description = "WS for checking current user permissions", position = 10)
@AtmosphereService(
        dispatch = false,
        interceptors = {AtmosphereResourceLifecycleInterceptor.class, ShiroInterceptor.class},
        path = "/" + RestAPIv3.API_VERSION + "/ws/" + AtmospherePermissionCheckResource.PATH,
        servlet = "org.glassfish.jersey.servlet.ServletContainer")
public class AtmospherePermissionCheckResource {

    public final static String PATH = "ispermitted";

    private final static Injector INJECTOR = Guice.createInjector(new FreedomoticInjector());
    private final static API api = INJECTOR.getInstance(API.class);
    protected ObjectMapper om;

    public AtmospherePermissionCheckResource() {
        om = new ObjectMapper();
        // JAXB annotation
        AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
        om.setAnnotationIntrospector(new AnnotationIntrospectorPair(jaxbIntrospector, jacksonIntrospector));
    }

    @Context
    private HttpServletRequest request;

    @POST
    public void query(String permission) {
        if (api != null) {
            AtmosphereResource r = (AtmosphereResource) request.getAttribute(ApplicationConfig.ATMOSPHERE_RESOURCE);
            if (r != null) {
                Subject sub = (Subject) r.getRequest().getAttribute(FrameworkConfig.SECURITY_SUBJECT);
                User u = api.getAuth().getUser(sub.getPrincipal().toString());
                Boolean permOK = u.isPermitted(permission);
                PermissionCheckRepresentation p = new PermissionCheckRepresentation(u.getName(), permission, permOK);
                try {
                    r.getResponse().write(om.writeValueAsString(p));
                } catch (JsonProcessingException ex) {
                }
            } else {
                throw new IllegalStateException();
            }
        }
    }

}
