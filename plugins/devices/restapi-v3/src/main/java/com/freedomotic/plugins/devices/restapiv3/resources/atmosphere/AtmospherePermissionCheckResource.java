/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.restapiv3.resources.atmosphere;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.plugins.devices.restapiv3.RestAPIv3;
import com.wordnik.swagger.annotations.Api;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import org.atmosphere.config.service.AtmosphereService;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;

/**
 *
 * @author matteo
 */
@Path(AtmospherePermissionCheckResource.PATH)
//@Api(value = "ws_permissionCheck", description = "WS for checking current user permissions", position = 10)
@AtmosphereService(
        dispatch = false,
        interceptors = {AtmosphereResourceLifecycleInterceptor.class},
        path = "/" + RestAPIv3.API_VERSION + "/ws/" + AtmospherePermissionCheckResource.PATH,
        servlet = "org.glassfish.jersey.servlet.ServletContainer")
public class AtmospherePermissionCheckResource extends AbstractWSResource {

    public final static String PATH = "ispermitted";

    @Context
    private HttpServletRequest request;

    @POST
    public void query(String permission) {
        AtmosphereResource r = (AtmosphereResource) request.getAttribute(ApplicationConfig.ATMOSPHERE_RESOURCE);
        if (r != null) {
            Boolean permOK = api.getAuth().isPermitted(permission);
            r.getResponse().write("{'" + permission + "':" + permOK.toString() + "}");
        } else {
            throw new IllegalStateException();
        }

    }

    @Override
    public void broadcast(EventTemplate message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
