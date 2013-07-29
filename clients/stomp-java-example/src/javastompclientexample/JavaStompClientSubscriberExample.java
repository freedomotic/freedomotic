/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javastompclientexample;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import net.ser1.stomp.Client;
import net.ser1.stomp.Listener;

/**
 *
 * @author gpt
 */
public class JavaStompClientSubscriberExample implements Listener {

    public static void main(String[] args) {
//        if (args.length < 2) {
//            System.out.println("parameters needed: ActiveMqIP queue");
//            return;
//        }
        String activeMqIp = "localhost"; //i.e "192.168.1.13
        int port = 61666;
        String channel = "/queue/command.jfrontend.user.callout";
        Client c;
        try {
            c = new Client(activeMqIp, port, "", "");
            System.out.println("Client connected on " + activeMqIp + ":" + port);
            new JavaStompClientSubscriberExample(c, channel);
        } catch (IOException ex) {
            Logger.getLogger(JavaStompClientSubscriberExample.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LoginException ex) {
            Logger.getLogger(JavaStompClientSubscriberExample.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JavaStompClientSubscriberExample(Client c, String channel) {
        Map header = new HashMap();
        header.put("transformation", "jms-object-xml");
        c.subscribe(channel, this, header);
        System.out.println("Subscribed to " + channel);
    }

    @Override
    public void message(Map map, String message) {
        System.out.println("message received by STOMP listener\n" + message);
    }
}
