/*Copyright 2009 Enrico Nicoletti
 eMail: enrico.nicoletti84@gmail.com

 This file is part of Freedomotic.

 Freedomotic is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 any later version.

 Freedomotic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with EventEngine; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.freedomotic.bus;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.core.Profiler;
import it.freedomotic.reactions.Command;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;

public class CommandChannel extends AbstractBusConnector implements MessageListener {

    private BusConsumer handler;
    private ExecutorService executor;
    private String channelName;

    public CommandChannel() {
        super();
        executor = Executors.newCachedThreadPool();
    }

    public void consumeFrom(String channelName) {
        if (handler != null) {
            try {
                Destination queue = getBusSharedSession().createQueue(channelName /*
                         * + "?consumer.exclusive=true"
                         */);
                this.channelName = channelName;
                receiver = getBusSharedSession().createConsumer(queue);
                receiver.setMessageListener(this);
                Freedomotic.logger.info(getHandler().getClass().getSimpleName() + " listen on " + queue.toString());
            } catch (javax.jms.JMSException jmse) {
                Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(jmse));
            }
        } else {
            Freedomotic.logger.severe("No handler for command channel " + channelName);
        }
    }

    public void reply(Command command, Destination channel, String correlationID) {
        try {
            ObjectMessage msg = getBusSharedSession().createObjectMessage();
            msg.setObject(command);
            msg.setJMSCorrelationID(correlationID);
            getBusSharedWriter().setTimeToLive(Freedomotic.config.getIntProperty("KEY_MESSAGES_TTL", 1000));
            Profiler.incrementSentReplies();
            //Freedomotic.logger.severe("Sending reply " + correlationID + " to " + channel + " and expecting reply on " + msg.getJMSReplyTo());
            getBusSharedWriter().send(channel, msg);
        } catch (JMSException jmse) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(jmse));
        }
    }

    public Command send(final Command command) {
        //Creiamo un task asincrono
        Callable task = new Callable() {

            @Override
            public Command call() throws Exception {
                Session session = getBusUnlistenedSession();
                try {
                    ObjectMessage msg = session.createObjectMessage();
                    msg.setObject(command);
                    Queue receiver = new ActiveMQQueue(command.getReceiver());
                    getBusSharedWriter().setTimeToLive(Freedomotic.config.getIntProperty("KEY_MESSAGES_TTL", 1000));
                    if (command.getReplyTimeout() > 0) {
                        return sendAndWait(receiver, msg);
                    } else {
                        //send the message immediately without creating temporary queues and consumers on it
                        //this increments perfornances if no reply is expected
                        getBusSharedWriter().send(receiver, msg);
                        Profiler.incrementSentCommands();
                        command.setExecuted(true);
                        return command; //always say it is executed (it's not sure but the caller is not interested: best effort)
                    }
                } catch (JMSException ex) {
                    Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(ex));
                    command.setExecuted(false);
                    return command;
                }
            }
        };
        Future<Command> result = executor.submit(task);
        try {
            return result.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(CommandChannel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(CommandChannel.class.getName()).log(Level.SEVERE, null, ex);
        }
        throw new RuntimeException("An exception is occurred while sending a command on the messaging bus.");
    }

    private Command sendAndWait(Queue receiver, ObjectMessage msg) throws JMSException {
        Command command = (Command) msg.getObject();
        //we have to wait an execution reply for an hardware device or an external client
        ActiveMQDestination temporaryQueue = (ActiveMQDestination) getBusUnlistenedSession().createTemporaryQueue();
        msg.setJMSReplyTo(temporaryQueue);
        MessageConsumer responseConsumer = getBusUnlistenedSession().createConsumer(temporaryQueue); //a temporary consumer on a temporary queue
        getBusSharedWriter().send(receiver, msg);
        Profiler.incrementSentCommands();
        //the receive() call is blocking so we execute it in a thread
        Freedomotic.logger.config("Send and await reply to command '" + command.getName() + "' for " + command.getReplyTimeout() + "ms");
        Message jmsResponse = responseConsumer.receive(command.getReplyTimeout());
        if (jmsResponse != null) {
            ObjectMessage objMessage = (ObjectMessage) jmsResponse;
            //a command is sent, we expect a command as reply
            Command reply = (Command) objMessage.getObject();
            Freedomotic.logger.config("Reply to command '" + command.getName() + "' received. Result is " + reply.getProperty("result"));
            Profiler.incrementReceivedReplies();
            //here do someting with the reply
            return reply;
        } else {
            Freedomotic.logger.config("Command '" + command.getName() + "' timed out after " + command.getReplyTimeout() + "ms");
            Profiler.incrementTimeoutedReplies();
        }
        command.setExecuted(false); //suppose it is executes even with no reply from destination
        return command; //returns back the original inaltered command
    }

    private BusConsumer getHandler() {
        return handler;
    }

    public void setHandler(BusConsumer handler) {
        this.handler = handler;
    }

    /**
     * Freedomotic receives something from the bus. It can be an event or a
     * command. This is the bus hook for any java class that use the freedomotic
     * bus. Classes register itself as handlers on this class.
     *
     * @param aMessage
     */
    @Override
    public void onMessage(Message aMessage) {
        Profiler.incrementReceivedCommands();
        if (getHandler() != null) {
            if (aMessage instanceof ObjectMessage) {
                getHandler().onMessage((ObjectMessage) aMessage);
            } else {
                Freedomotic.logger.severe("A message is received by " + channelName + 
                        " but it is not an object message is a " + aMessage.getClass().getCanonicalName());
                TextMessage text = (TextMessage)aMessage;
                try {
                    System.out.println(text.getText());
                } catch (JMSException ex) {
                    Logger.getLogger(CommandChannel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            Freedomotic.logger.warning("No handler specified for this command channel");
        }
    }

    private String createRandomString() {
        Random random = new Random(System.currentTimeMillis());
        long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }
}
