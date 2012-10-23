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
        String activeMqIp = "192.168.1.3";
        //create an XML command to send
        String commandDestination = "/queue/app.data.request";
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
        //create a event to send (simulate object click)
        String eventDestination = "/topic/VirtualTopic.app.event.sensor.object.behavior.clicked";
        String event =
                "<it.freedomotic.events.ObjectReceiveClick>"
                + "<eventName>ObjectReceiveClick</eventName>"
                + "<sender>AndroidFrontend</sender>"
                + "<payload>"
                + "  <payload>"
                + "    <it.freedomotic.reactions.Statement>"
                + "      <logical>AND</logical>"
                + "      <attribute>click</attribute>"
                + "      <operand>EQUALS</operand>"
                + "      <value>SINGLE_CLICK</value>"
                + "    </it.freedomotic.reactions.Statement>"
                + "    <it.freedomotic.reactions.Statement>"
                + "      <logical>AND</logical>"
                + "      <attribute>object.type</attribute>"
                + "      <operand>EQUALS</operand>"
                + "      <value>EnvObject.ElectricDevice.Light</value>"
                + "    </it.freedomotic.reactions.Statement>"
                + "    <it.freedomotic.reactions.Statement>"
                + "      <logical>AND</logical>"
                + "      <attribute>object.name</attribute>"
                + "      <operand>EQUALS</operand>"
                + "      <value>Livingroom Light</value>"
                + "    </it.freedomotic.reactions.Statement>"
                + "  </payload>"
                + "</payload>"
                + "</it.freedomotic.events.ObjectReceiveClick>";

        String manifest = 
                "  <it.freedomotic.model.ds.Config>\n"
                + "  <properties>\n"
                + "    <property name=\"startup-time\" value=\"on load\"/>\n"
                + "    <property name=\"name\" value=\"Remote Plugin\"/>\n"
                + "    <property name=\"category\" value=\"category\"/>\n"
                + "    <property name=\"description\" value=\"Plugin added with join plugin\"/>\n"
                + "    <property name=\"short-name\" value=\"shortname\"/>\n"
                + "  </properties>\n"
                + "  <xmlFile/>\n"
                + "</it.freedomotic.model.ds.Config>";
                

          
        String manifestDestination = "/queue/app.plugin.create";

        Client c;
        try {
            c = new Client(activeMqIp, 61666, "", "");
            System.out.println("Connected to broker");
            Map header = new HashMap();
            header.put("transformation", "jms-object-xml");
            header.put("reply-to", "/queue/app.data.response");
            System.out.println("Subscribe for replies to command");
            c.subscribe("/queue/app.data.response", new Listener() {
                @Override
                public void message(Map map, String string) {
                    System.out.println("STOMP client receives something...");
                    System.out.println(string);
                }
            });
            System.out.println("Sending XML manifest...");
            c.send(manifestDestination, manifest, header);
            System.out.println("Sending XML command...");
            c.send(commandDestination, command, header);
            System.out.println("Sending XML event...");
            c.send(eventDestination, event, header);
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
