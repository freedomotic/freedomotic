/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.bus;

import com.freedomotic.api.AbstractConsumer;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import com.freedomotic.testutils.GuiceJUnitRunner;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author nicoletti
 */
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceInjectors({FreedomoticInjector.class})
public class BusServiceTest {

    @Inject
    private BusService busService;

    /**
     *
     */
    public BusServiceTest() {
        //do not init bus in contructior, it is not yet injected
        //do it in class setUp()
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
        busService.init();
    }

    /**
     *
     */
    @After
    public void tearDown() {
        busService.destroy();
    }

    /**
     * Test of send method, of class BusService.
     */
    @Test
    public void testSendCommand() {
        LOG.info("Test bus send and forget");
        Command command = new Command();
        command.setName("Send and forget");
        command.setReceiver("unlistened.test.channel");
        assertFalse("Unsent command should be marked with executed=false flag", command.isExecuted());
        Command result = busService.send(command);
        assertTrue("Succesfully sent command should be marked as executed", result.isExecuted());
    }

    /**
     * Test of reply method, of class BusService.
     */
    @Test
    public void testSendCommandAndWaitTimeout() {
        LOG.info("Test send and see what happens when reply timeout is reached");
        Command command = new Command();
        command.setName("Unlistened command test");
        //send it on unlistened channel so it will not receive reply within timeout
        command.setReceiver("unlistened.test.channel");
        command.setReplyTimeout(100); //wait reply for two seconds
        Command result = busService.send(command);
        LOG.log(Level.INFO, "Reply timeout for command ''{0}'' is reached, executed={1}", new Object[]{result, result.isExecuted()});
        assertEquals("Timeout reply command is the original command", result, command);
        assertFalse("When timeout is reached the original command is marked as not executed", result.isExecuted());
    }

    /**
     * Test of reply method, of class BusService.
     */
    @Test
    public void testSendCommandAndWaitReply() {
        LOG.info("Test send and wait for reply within timeout");

        //create a listener for this command
        AbstractConsumer listener = new AbstractConsumer(busService) {

            @Override
            protected void onCommand(Command c) throws IOException, UnableToExecuteException {
                c.setProperty("receiver-reply", "OK");
            }

            @Override
            protected void onEvent(EventTemplate event) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            protected String getMessagingChannel() {
                return "wait.for.reply.here";
            }
        };

        //prepare and send the command
        Command command = new Command();
        command.setName("Command with expected reply");
        command.setReceiver("wait.for.reply.here");
        command.setReplyTimeout(10000); //wait reply for ten seconds
        Command result = busService.send(command);
        LOG.log(Level.INFO, "Received reply command is ''{0}'' executed={1}", new Object[]{result, result.isExecuted()});
        //the reply should have the property addedd by the listener
        assertTrue("Command reply was received", result.getProperty("receiver-reply").equals("OK"));
    }

    private static final Logger LOG = Logger.getLogger(BusServiceTest.class.getName());
}
