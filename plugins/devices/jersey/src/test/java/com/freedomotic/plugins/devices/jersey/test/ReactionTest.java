/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.freedomotic.plugins.devices.jersey.test;

import com.freedomotic.plugins.devices.japi.resources.ReactionResource;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.Reaction;
import com.freedomotic.reactions.Trigger;
import java.util.List;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilderException;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author matteo
 */
public class ReactionTest extends AbstractTest<Reaction>{

    @Override
    public void test() {
     // skip test (need some more work to be ready for this)
    }

    
    @Override
    public void init() throws UriBuilderException, IllegalArgumentException {
        container = api.reactions();
        item = new Reaction();
        item.setUuid(uuid);
        Command com = new Command();
        com.setName("Reaction Command");
        com.setHardwareLevel(false);
        Trigger t = new Trigger();
        t.setName("Reaction trigger");
        api.triggers().create(t);
        api.commands().create(com);
        item.setTrigger(t);
        
        item.addCommand(com);
        initPath(ReactionResource.class);
        listType = new GenericType<List<Reaction>>(){};
        singleType = new GenericType<Reaction>(){};
    }

    @Override
    protected void putModifications(Reaction orig) {
        orig.getTrigger().setChannel("pippo");
    }

    @Override
    protected void putAssertions(Reaction pre, Reaction post) {
        assertEquals("PUT - trigger channel check", pre.getTrigger().getChannel(), post.getTrigger().getChannel());
    }

    @Override
    protected void getAssertions(Reaction obj) {
       assertEquals("Single test - UUID", item.getUuid(), obj.getUuid());
    }

    @Override
    protected void listAssertions(List<Reaction> list) {
        assertEquals("Single test - UUID", item.getUuid(), list.get(0).getUuid());
    }

    @Override
    protected String getUuid(Reaction obj) {
        return obj.getUuid();
    }
    
}
