/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.freedomotic.plugins.devices.restapiv3.test;

import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.plugins.devices.restapiv3.representations.ReactionRepresentation;
import com.freedomotic.plugins.devices.restapiv3.resources.jersey.ReactionResource;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.Reaction;
import com.freedomotic.reactions.Trigger;
import com.google.inject.Inject;
import java.util.List;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilderException;
import static org.junit.Assert.assertEquals;
import org.junit.runner.RunWith;

/**
 *
 * @author matteo
 */
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceInjectors({FreedomoticInjector.class})
public class ReactionTest extends AbstractTest<ReactionRepresentation>{

    @Inject
    ReactionRepresentation rea;
    
    @Override
    public void init() throws UriBuilderException, IllegalArgumentException {
        testCOPY = false;
        Command com = new Command();
        com.setName("Reaction Command");
        com.setHardwareLevel(false);
        
        Trigger t = new Trigger();
        t.setName("Reaction trigger");
        Trigger t2 = new Trigger();
        t2.setName("Second Trigger");
        
        getApi().triggers().create(t);
        getApi().triggers().create(t2);
        
        getApi().commands().create(com);
        
        Reaction r = new Reaction(t, com);
        setItem(new ReactionRepresentation(r));
        
        initPath(ReactionResource.class);
        setListType(new GenericType<List<ReactionRepresentation>>(){});
        setSingleType(new GenericType<ReactionRepresentation>(){});
    }

    @Override
    protected void putModifications(ReactionRepresentation orig) {
        orig.setTriggerUuid(getApi().triggers().findByName("Second Trigger").get(0).getUUID());
    }

    @Override
    protected void putAssertions(ReactionRepresentation pre, ReactionRepresentation post) {
        assertEquals("PUT - trigger UUID check", pre.getTriggerUuid(), post.getTriggerUuid());
    }

    @Override
    protected void getAssertions(ReactionRepresentation obj) {
       assertEquals("Single test - UUID", getItem().getUuid(), obj.getUuid());
    }

    @Override
    protected void listAssertions(List<ReactionRepresentation> list) {
        assertEquals("Single test - UUID", getItem().getUuid(), list.get(0).getUuid());
    }

    @Override
    protected String getUuid(ReactionRepresentation obj) {
        return obj.getUuid();
    }
    
}
