/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
public class Delayer extends Protocol {

    public Delayer() {
        super("Delayer", "/it.nicoletti.test/delayer.xml");
        setDescription("Delayed commands in automations");
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        System.out.println("Delayer START sleeping for " + Integer.parseInt(c.getProperty("delay")));
        try {
            //reminder(c, Long.parseLong(c.getProperty("delay")));
            Thread.sleep(5000);
            System.out.println("Delayer ENDS  sleeping for " + Integer.parseInt(c.getProperty("delay")));
        } catch (InterruptedException ex) {
            Logger.getLogger(Delayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    public void reminder(Command c, long ms) {
//        Timer timer = new Timer();
//        timer.schedule(new RemindTask(c, timer), ms);
//    }
//
//    class RemindTask extends TimerTask {
//
//        Command c;
//        Timer t;
//
//        private RemindTask(Command c, Timer t) {
//            this.c = c;
//            this.t = t;
//        }
//
//        public void run() {
//            System.out.println("Delayer ENDS  sleeping for " + Integer.parseInt(c.getProperty("delay")));
//            c.setExecuted(true);
//            reply(c);
//            t.cancel(); //Terminate the timer thread
//        }
//    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onRun() {
        // throw new UnsupportedOperationException("Not supported yet.");
    }
}
