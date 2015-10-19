/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.freedomchat;

import com.freedomotic.api.API;
import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.Trigger;
import com.google.inject.Inject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 *
 * @author matteo
 */
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceInjectors({FreedomoticInjector.class})
public class MessageServiceTest {
    @Inject
    MessageService msg;
    
    @Inject
    API api;
    
    public MessageServiceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        Trigger t = new Trigger();
        t.setName("pluto");
        api.triggers().create(t);
        Command c = new Command();
        c.setName("pippo");
        c.setReceiver("a.b.c");
        api.commands().create(c);
    }
    
    @After
    public void tearDown() {
        api.commands().deleteAll();
        api.triggers().deleteAll();
    }

    /**
     * Test help 
     */
    @Test
    public void testHelp() {
        String mess = "help";
        String expResult = "Freedomotic CHAT help:";
        String result = msg.manageMessage(mess);
        assertTrue(result.startsWith(expResult));        
    }
    
    /**
     * Test list
     */
    @Test
    public void testList() {
        String mess = "list";
        String expResult = "Allowed";
        String result = msg.manageMessage(mess);
        assertTrue(result.startsWith(expResult));        
    }
    
    /**
     * Test asterisk
     */
    @Test
    public void testCommand() {
        String mess = "*";
        String expResult = "? pippo";
        String result = msg.manageMessage(mess);
        assertTrue(result.startsWith(expResult));        
       
        mess = "pippo";
        expResult = mess + "\n DONE.";
        result = msg.manageMessage(mess);
        assertEquals(expResult, result);     
        
        mess = "abcde";
        expResult = "No available commands similar to: "+ mess;
        result = msg.manageMessage(mess);
        assertEquals(expResult, result);     
    }    
    /**
     * Test asterisk
     */
    @Test
    public void testTrigger() {
        String mess = "list trigger";
        String expResult = "? \npluto\n";
        String result = msg.manageMessage(mess);
        assertEquals(expResult, result);        
    }
    
        /**
     * Test asterisk
     */
    @Test
    public void testIFTHEN() {
        String mess = "if pluto then pippo";
        String expResult = "DONE";
        String result = msg.manageMessage(mess);
        assertEquals(expResult, result);        
        
        mess = "if asdfakjsd then sdfasdas";
        expResult = "No available commands similar to: sdfasdas";
        result = msg.manageMessage(mess);
        assertEquals(expResult, result);        
        
    }
}
 
