/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.persistence.converters;

import it.freedomotic.persistence.PayloadConverter;
import com.thoughtworks.xstream.XStream;
import it.freedomotic.reactions.Payload;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Enrico
 */
public class PayloadConverterTest {

    private static String xml;
    private static XStream xstream;

    public PayloadConverterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        xml = "<payload><payload><it.freedomotic.reactions.Statement>"
                + "<logical>AND</logical><attribute>protocol</attribute>"
                + "<operand>EQUALS</operand><value>SNT084Eth8R8I</value>"
                + "</it.freedomotic.reactions.Statement>"
                + "<it.freedomotic.reactions.Statement><logical>SET</logical>"
                + "<attribute>behaviorValue</attribute><operand>EQUALS</operand>"
                + "<value>@event.isOn</value></it.freedomotic.reactions.Statement>"
                + "</payload></payload>";

        xstream = new XStream();
        xstream.registerConverter(new PayloadConverter());
        xstream.alias("payload", Payload.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testMarshal() {
        System.out.println("Testing Payload to XML");
        Payload payload = new Payload();
        payload.addStatement("protocol", "SNT084Eth8R8I");
        payload.addStatement("SET", "behaviorValue", "EQUALS", "@event.isOn");
        String marshal = xstream.toXML(payload);
        marshal = marshal.replace("\n", "");
        marshal = marshal.replace(" ", "");
        assertEquals(marshal, xml);
    }

    @Test
    public void testUnmarshal() {
        System.out.println("Testing XML to Payload");
        Payload payload = (Payload) xstream.fromXML(xml);
        assertEquals("protocol", payload.getStatements("protocol").get(0).getAttribute());
        assertEquals("SNT084Eth8R8I", payload.getStatements("protocol").get(0).getValue());
        assertEquals("behaviorValue", payload.getStatements("behaviorValue").get(0).getAttribute());
        assertEquals("@event.isOn", payload.getStatements("behaviorValue").get(0).getValue());                  
    }

    @Test
    public void testCanConvert() {
    }
}
