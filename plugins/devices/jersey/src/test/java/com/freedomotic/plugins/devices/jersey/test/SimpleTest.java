package com.freedomotic.plugins.devices.jersey.test;

import com.freedomotic.plugins.devices.japi.RestJersey;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import static org.junit.Assert.assertEquals;
import org.junit.Test;



public class SimpleTest extends JerseyTest {
    @Override
    protected Application configure() {
        return new ResourceConfig().packages(RestJersey.RESOURCE_PKG);
    }
 
    @Test
    public void test() {
     //   final String hello = target("/v3/environments").request().get(String.class);
     //   assertEquals("Hello World!", hello);
    }
}