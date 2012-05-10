/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package es.gpulido.twitter;

import es.gpulido.twitter.gateways.TwitterGateway;
import it.freedomotic.api.Actuator;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 *
 * @author gpt
 */
public class TwitterActuator extends Actuator {

  // private ModbusMaster master;
    private Twitter twitter;
    public TwitterActuator(){
        super("TwitterActuator", "/es.gpulido.twitter/twitter-actuator.xml");
        twitter = TwitterGateway.getInstance(configuration);
        start(); //or set the property startup-time at value "on load" in the config file
    }
  
    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        try {
            //Maybe we can use the async api
            //First implementation. We can extend sending a mes to an specific user (for example)
            String statusmess = c.getProperty("status");
            Status status = twitter.updateStatus(statusmess);
            System.out.println("Successfully updated the status to [" + status.getText() + "].");
        } catch (TwitterException ex) {
            Logger.getLogger(TwitterActuator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
