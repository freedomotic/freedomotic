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
public class JavaStompClientExample {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
//        if (args.length <4)
//        {
//            System.out.println("parameters needed: ActiveMqIP objectName behavior value");
//            return;            
//        }
        String activeMqIp = "192.168.1.2";
//        String object = "Luce Camera";
//        String behavior = "powered";
//        String value = "opposite";
        String queue = "/queue/app.data.request";
        String command =
                "<it.freedomotic.reactions.Command>"
                + "    <name>A message from STOMP</name>"
                + "    <receiver>app.actuators.frontend.javadesktop.in</receiver>"
                + "    <description>STOMP is used to ask a question</description>"
                + "    <delay>0</delay>"
                + "    <timeout>10000</timeout>"
                + "    <properties>"
                + "        <properties>"
                + "        		<property name=\"question\" value=\"" + "Do you like freedomotic?" + "\"/>"
                + "		        <property name=\"options\" value=\"Yes;No;I don't know\"/>"
                + "        </properties>"
                + "    </properties>"
                + "</it.freedomotic.reactions.Command>";

        Client c;
        try {
            c = new Client(activeMqIp, 61666, "", "");
            //TODO: A sendw could be used, using a header with the queue
            System.out.println("connected");
            Map header = new HashMap();
            header.put("transformation", "jms-object-xml");
            header.put("reply-to", "/queue/app.data.response");
            c.subscribe("/queue/app.data.response", new Listener() {

                @Override
                public void message(Map map, String string) {
                    System.out.println("stomp receives something");
                    System.out.println(string);
                }
            });
            c.send(queue, command, header);
            System.out.println("sent");
        } catch (IOException ex) {
            Logger.getLogger(JavaStompClientExample.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LoginException ex) {
            Logger.getLogger(JavaStompClientExample.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(JavaStompClientExample.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
