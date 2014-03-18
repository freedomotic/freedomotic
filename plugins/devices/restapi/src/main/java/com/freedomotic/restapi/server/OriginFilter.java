/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.restapi.server;

import java.util.Map;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.engine.header.Header;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.routing.Filter;
import org.restlet.util.Series;

/**
 *
 * @author matteo
 */
public class OriginFilter extends Filter {

    public OriginFilter(Context context) {
        super(context);
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        if (Method.OPTIONS.equals(request.getMethod())) {
            final Map<String, Object> responseAttributes = response.getAttributes();
            @SuppressWarnings("unchecked")
            Series<Header> responseHeaders = (Series<Header>) responseAttributes.get("org.restlet.http.headers");
            
            // if(MyConfig.getAllow​edOrigins().contains​(origin)) {
            if (responseHeaders == null) {
                responseHeaders = new Series<Header>(Header.class);
                response.getAttributes().put("org.restlet.http.headers", responseHeaders);
            }
            responseHeaders.add("Access-Control-Allow-Origin", "*");
            // responseHeaders.add("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS");
            responseHeaders.add("Access-Control-Allow-Headers", "Accept,Accept-Version,Content-Type,Api-Version,Authorization");
            responseHeaders.add("Access-Control-Allow-Credentials", "true");
            // responseHeaders.add("Access-Control-Max-Age", "60");
            response.setEntity(new EmptyRepresentation());
            return SKIP;
            //   }
        }

        return super.beforeHandle(request, response);
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        if (!Method.OPTIONS.equals(request.getMethod())) {
            final Map<String, Object> responseAttributes = response.getAttributes();
            @SuppressWarnings("unchecked")
            Series<Header> responseHeaders = (Series<Header>) responseAttributes.get("org.restlet.http.headers");
            if (responseHeaders == null) {
                responseHeaders = new Series<Header>(Header.class);
                responseAttributes.put("org.restlet.http.headers", responseHeaders);
            }
            responseHeaders.add("Access-Control-Allow-Origin", "*");
           // responseHeaders.add("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS");
            responseHeaders.add("Access-Control-Allow-Headers", "Accept,Accept-Version,Content-Type,Api-Version,Authorization");
            responseHeaders.add("Access-Control-Allow-Credentials", "true");
            // responseHeaders.add("Access-Control-Max-Age", "60");
        }
        // }
        super.afterHandle(request, response);
    }
}
