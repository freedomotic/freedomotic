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
package com.freedomotic.plugins.devices.restapiv3.test;

/**
 *
 * @author matteo
 */
import com.freedomotic.api.API;
import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.plugins.devices.restapiv3.RestAPIv3;
import com.freedomotic.plugins.devices.restapiv3.utils.ThrowableExceptionMapper;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
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
import org.junit.runner.RunWith;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceInjectors({FreedomoticInjector.class})
public abstract class AbstractTest<Z> extends JerseyTest {

    private String path;
    private Z item;
    private GenericType<Z> singleType;
    private GenericType<List<Z>> listType;
    private String uuid;
    private MediaType representation;
    protected boolean testPOST = true;
    protected boolean testPUT = true;
    protected boolean testCOPY = true;
    protected boolean testGET = true;
    protected boolean testDELETE = true;

    @Inject
    private API api;

    // Expect that subclasses implement this methods
    abstract void init() throws UriBuilderException, IllegalArgumentException;

    abstract void putModifications(Z orig);

    abstract void putAssertions(Z pre, Z post);

    abstract void getAssertions(Z obj);

    abstract void listAssertions(List<Z> list);

    abstract String getUuid(Z obj);

    @Override
    protected Application configure() {
        uuid = UUID.randomUUID().toString();
        representation = MediaType.APPLICATION_JSON_TYPE;

        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        ResourceConfig rc = new ResourceConfig().packages(RestAPIv3.JERSEY_RESOURCE_PKG);
        rc.registerClasses(JacksonFeature.class);
        //  rc.registerClasses(MoxyXmlFeature.class);
        rc.register(ThrowableExceptionMapper.class);
        return rc;
    }

    @After
    @Override
    public void tearDown() throws Exception {
        getApi().commands().deleteAll();
        getApi().environments().deleteAll();
        getApi().triggers().deleteAll();
        getApi().reactions().deleteAll();
        getApi().things().deleteAll();
        for (String roleName : getApi().getAuth().getRoles().keySet()){
            getApi().getAuth().deleteRole(roleName);
        }
        super.tearDown(); //To change body of generated methods, choose Tools | Templates.
    }

    @Test
    public void test() {
        //Init should be in test() because the @GuiceInjector injects this
        //class members just before executing the test
        init();
        //JUST FOR DEBUG PURPOSES
        if (getApi() == null) {
            throw new IllegalStateException("At this point the api reference should be injected!");
        }
        Entity<Z> cmdEntity = Entity.entity(getItem(), getRepresentation());

        // POST
        if (testPOST) {
            final Response response = target(getPATH()).request().post(cmdEntity);
            assertEquals("POST response HTTP status code not as expected", Status.CREATED.getStatusCode(), response.getStatus());
        }
        //GET list
        List<Z> cl = target(getPATH()).request(getRepresentation()).get(getListType());
        if (testGET) {
            assertEquals("Assertion failed while testing GET. List size", 1, cl.size());
            listAssertions(cl);
        }
        //GET single
        Z objPre = target(getPATH()).path(getUuid(getItem())).request(getRepresentation()).get(getSingleType());
        if (testGET) {
            getAssertions(objPre);
        }
        // PUT
        if (testPUT) {
            putModifications(objPre);
            Entity<Z> envEntityPut = Entity.entity(objPre, getRepresentation());
            Response resPUT = target(getPATH()).path(getUuid(getItem())).request().put(envEntityPut);
            assertEquals("PUT test", Status.OK.getStatusCode(), resPUT.getStatus());
            Z objPost = target(getPATH()).path(getUuid(getItem())).request(getRepresentation()).get(getSingleType());
            putAssertions(objPre, objPost);
        }
        //COPY
        if (testCOPY) {
            final Response resCOPY = target(getPATH()).path(getUuid(getItem())).path("/copy").request(getRepresentation()).post(null);
            assertEquals("COPY test", Status.CREATED.getStatusCode(), resCOPY.getStatus());
            cl = target(getPATH()).request().get(getListType());
            assertEquals("COPY - Size test", 2, cl.size());
        }
        //DELETE
        if (testDELETE) {
            Response resDELETE = target(getPATH()).path(getUuid(getItem())).request(getRepresentation()).delete();
            assertEquals("DELETE test", Status.OK.getStatusCode(), resDELETE.getStatus());
            cl = target(getPATH()).request().get(getListType());
            if (testCOPY) {
                assertEquals("DELETE - Size test", 1, cl.size());
            } else {
                assertEquals("DELETE - Size test", 0, cl.size());
            }
            Response postDELETE = target(getPATH()).path(getUuid(getItem())).request().get();
            assertEquals("DELETE - error searching deleted item", Status.NOT_FOUND.getStatusCode(), postDELETE.getStatus());
        }
    }

    protected void initPath(Class res) {
        setPATH(UriBuilder.fromResource(res).build().toString());
    }

    protected void initPath(String path) {
        setPATH(path);
        //System.out.print("PATH: " + getPATH());
    }

    /**
     * @return the path
     */
    public String getPATH() {
        return path;
    }

    /**
     * @param PATH the path to set
     */
    public void setPATH(String PATH) {
        this.path = PATH;
    }

    /**
     * @return the item
     */
    public Z getItem() {
        return item;
    }

    /**
     * @param item the item to set
     */
    public void setItem(Z item) {
        this.item = item;
    }

    /**
     * @return the singleType
     */
    public GenericType<Z> getSingleType() {
        return singleType;
    }

    /**
     * @param singleType the singleType to set
     */
    public void setSingleType(GenericType<Z> singleType) {
        this.singleType = singleType;
    }

    /**
     * @return the listType
     */
    public GenericType<List<Z>> getListType() {
        return listType;
    }

    /**
     * @param listType the listType to set
     */
    public void setListType(GenericType<List<Z>> listType) {
        this.listType = listType;
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the representation
     */
    public MediaType getRepresentation() {
        return representation;
    }

    /**
     * @param representation the representation to set
     */
    public void setRepresentation(MediaType representation) {
        this.representation = representation;
    }

    /**
     * @return the api
     */
    public API getApi() {
        return api;
    }

    /**
     * @param api the api to set
     */
    public void setApi(API api) {
        this.api = api;
    }

}
