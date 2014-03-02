/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.bus;

import com.freedomotic.app.Freedomotic;
import com.freedomotic.reactions.Command;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author nicoletti
 */
public class BusServiceTest {

    private BusService bus = Freedomotic.INJECTOR.getInstance(BusService.class);

    /**
     *
     */
    public BusServiceTest() {
        bus.init();
    }

    /**
     *
     */
    @BeforeClass
    public static void setUpClass() {
    }

    /**
     *
     */
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     *
     */
    @Before
    public void setUp() {
    }

    /**
     *
     */
    @After
    public void tearDown() {
    }

    /**
     * Test of send method, of class BusService.
     */
    @Test
    public void testSendCommand() {
        LOG.info("Test bus send command asynch");
        Command command = new Command();
        command.setReceiver("unlistened.test.channel");
        assertFalse("Unsent command should be marked with executed=false flag", command.isExecuted());
        Command result = bus.send(command);
        assertTrue("Succesfully sent command should be marked as executed", result.isExecuted());
    }

    /**
     * Test of reply method, of class BusService.
     */
    @Test
    public void testSendCommandAndWaitTimeout() {
        LOG.info("Test send command and do not reply to test timeout");
        Command command = new Command();
        //send it on unlistened channel so it will not receive reply within timeout
        command.setReceiver("unlistened.test.channel");
        command.setReplyTimeout(2000); //wait reply for two seconds
        Command result = bus.send(command);
        assertEquals("Timeout reply command is the original command", result, command);
        assertFalse("When timeout is reached the original command is marked as not executed", result.isExecuted());
    }

    /**
     * Test of reply method, of class BusService.
     */
    @Test
    public void testSendCommandAndWaitReply() {
        LOG.info("Test send command and wait for reply");
        
        //create a listener for this command
        BusMessagesListener listener = new BusMessagesListener(new BusConsumer() {

            @Override
            public void onMessage(ObjectMessage message) {
                assertNotNull("Received message should be not null", message);
                try {
                    Command c = (Command) message.getObject();
                    c.setProperty("receiver-reply", "OK");
                } catch (JMSException ex) {
                    Logger.getLogger(BusServiceTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        });
        listener.consumeCommandFrom("wait.for.reply.here");
        
        //prepare and send the command
        Command command = new Command();
        command.setReceiver("wait.for.reply.here");
        command.setReplyTimeout(2000); //wait reply for two seconds
        Command result = bus.send(command);
        
        //the reply should have the property addedd by the listener
        assertTrue("Command reply was received", result.getProperty("receiver-reply").equals("OK"));
    }

    private static final Logger LOG = Logger.getLogger(BusServiceTest.class.getName());
}
