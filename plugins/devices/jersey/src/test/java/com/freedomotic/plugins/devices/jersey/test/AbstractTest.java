/**
 *
 * Copyright (c) 2009-2014 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.plugins.devices.jersey.test;

/**
 *
 * @author matteo
 */
import com.freedomotic.api.API;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.persistence.ContainerInterface;
import com.freedomotic.plugins.devices.japi.RestJersey;
import com.freedomotic.plugins.devices.japi.utils.ThrowableExceptionMapper;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public abstract class AbstractTest<Z> extends JerseyTest {

    private String PATH;
    Z item;
    GenericType<Z> singleType;
    GenericType<List<Z>> listType;
    String uuid;
    MediaType representation;
    API api;
    ContainerInterface container;


    @Override
    protected Application configure() {
        api = Freedomotic.INJECTOR.getInstance(API.class);
        uuid = UUID.randomUUID().toString();
        init();
        representation = MediaType.APPLICATION_JSON_TYPE;
        
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        
        ResourceConfig rc = new ResourceConfig().packages(RestJersey.RESOURCE_PKG);
        rc.registerClasses(JacksonFeature.class);
      //  rc.registerClasses(MoxyXmlFeature.class);
        rc.register(ThrowableExceptionMapper.class);
        return rc;
    }

    abstract public void init() throws UriBuilderException, IllegalArgumentException;

    @Test
    public void test() {
        Entity<Z> cmdEntity = Entity.entity(item, representation);

        // POST
        final Response resPOST = target(PATH).request().post(cmdEntity);
        assertEquals("POST test", Status.CREATED.getStatusCode(), resPOST.getStatus());

        //GET list
        List<Z> cl = target(PATH).request(representation).get(listType);
        assertEquals("List size", 1, cl.size());
        listAssertions(cl);

        //GET single
        Z objPre = target(PATH).path(getUuid(item)).request(representation).get(singleType);
        getAssertions(objPre);

        // PUT
        putModifications(objPre);
        Entity<Z> envEntityPut = Entity.entity(objPre, representation);
        Response resPUT = target(PATH).path(getUuid(item)).request().put(envEntityPut);
        assertEquals("PUT test", Status.OK.getStatusCode(), resPUT.getStatus());
        Z objPost = target(PATH).path(getUuid(item)).request(representation).get(singleType);
        putAssertions(objPre, objPost);

        //COPY
        final Response resCOPY = target(PATH).path(getUuid(item)).path("/copy").request(representation).post(null);
        assertEquals("COPY test", Status.CREATED.getStatusCode(), resCOPY.getStatus());
        cl = target(PATH).request().get(listType);
        assertEquals("COPY - Size test", 2, cl.size());

        //DELETE
        Response resDELETE = target(PATH).path(getUuid(item)).request(representation).delete();
        assertEquals("DELETE test", Status.OK.getStatusCode(), resDELETE.getStatus());
        cl = target(PATH).request().get(listType);
        assertEquals("DELETE - Size test", 1, cl.size());
        Response postDELETE = target(PATH).path(getUuid(item)).request().get();
        assertEquals("DELETE - error searching deleted item", Status.NOT_FOUND.getStatusCode(), postDELETE.getStatus());

    }

    protected void initPath(Class res) {
        PATH = UriBuilder.fromResource(res).build().toString();
    }

    protected void initPath(String path) {
        PATH = path;
        System.out.print("PATH: " + PATH);
    }

    abstract protected void putModifications(Z orig);

    abstract protected void putAssertions(Z pre, Z post);

    abstract protected void getAssertions(Z obj);

    abstract protected void listAssertions(List<Z> list);

    abstract protected String getUuid(Z obj);

    protected void cleanUp(){
        if (container != null){
            container.clear();
        }
    }
    
    @Override
    @After
    public void tearDown() throws Exception {
        api.commands().clear();
        api.environments().clear();
        api.triggers().clear();
        api.reactions().clear();
        api.objects().clear();
                
        cleanUp();
        super.tearDown(); //To change body of generated methods, choose Tools | Templates.
    }

}
