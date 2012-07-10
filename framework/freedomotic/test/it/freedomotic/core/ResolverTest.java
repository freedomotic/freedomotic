package it.freedomotic.core;

import it.freedomotic.events.GenericEvent;
import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.Trigger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;




import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Enrico
 */
public class ResolverTest {

    public ResolverTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testResolve_Command() {
        System.out.println("Commands resolving a set of references mixed with text like 'temperature is @event.temperature'");
        Command c = new Command();
        c.setName("say something using TTS");
        c.setProperty("zero", "@event.temperature");
        c.setProperty("one", "temperature is @event.temperature.");
        c.setProperty("two", "temperature is @event.temperature#celsius degree.");
        c.setProperty("three", "temperature in @event.zone is @event.temperature.");
        c.setProperty("four", "temperature in @event.zone is @event.temperature celsius degree.");
        c.setProperty("five", "temperature in @event.zone is @event.temperature celsius degree. @event.zone# is hot because temperature is +@event.temperature°C.");
        c.setProperty("six", "temperature in @event.zone is managed by object @event.object.name#.");
        //testing scripting
        c.setProperty("seven", "= seven=\"Current temperature is @event.temperature celsius degrees. In fahrenheit is \" + Math.floor(((@event.temperature+40)*1.8)-40) + \" degrees.\";");
        c.setProperty("eight", "= eight=10+5;"); //this always returns a double
        c.setProperty("nine", "= nine=Math.floor(10+5).toString();"); //print the number as is to avoid conversion to double
        c.setProperty("ten", "= if (@event.temperature<= 20) ten=2; else what=@event.temperature/10;");
        GenericEvent event = new GenericEvent(this);
        event.addProperty("zone", "Kitchen");
        event.addProperty("temperature", "25");
        event.addProperty("object.name", "Indoor Thermometer");
        Resolver resolver = new Resolver();
        resolver.addContext("event.", event.getPayload());
        Command result = new Command();
        try {
            result = resolver.resolve(c);
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(ResolverTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertEquals("25", result.getProperty("zero"));
        assertEquals("temperature is 25.", result.getProperty("one"));
        assertEquals("temperature is 25celsius degree.", result.getProperty("two"));
        assertEquals("temperature in Kitchen is 25.", result.getProperty("three"));
        assertEquals("temperature in Kitchen is 25 celsius degree.", result.getProperty("four"));
        assertEquals("temperature in Kitchen is 25 celsius degree. Kitchen is hot because temperature is +25°C.", result.getProperty("five"));
        assertEquals("temperature in Kitchen is managed by object Indoor Thermometer.", result.getProperty("six"));
        assertEquals("Current temperature is 25 celsius degrees. In fahrenheit is 77 degrees.", result.getProperty("seven"));
        assertEquals("15.0", result.getProperty("eight"));
        assertEquals("15", result.getProperty("nine"));        
        //assertEquals("15", result.getProperty("ten"));
    }

    @Test
    public void testResolve_Trigger() {
        System.out.println("Triggers resolving a set of references mixed with text like 'temperature is @event.temperature'");
        Trigger c = new Trigger();
        c.setName("say something using TTS");
        c.getPayload().addStatement("zero", "@event.temperature");
        c.getPayload().addStatement("one", "temperature is @event.temperature.");
        c.getPayload().addStatement("two", "temperature is @event.temperature#celsius degree.");
        c.getPayload().addStatement("three", "temperature in @event.zone is @event.temperature.");
        c.getPayload().addStatement("four", "temperature in @event.zone is @event.temperature celsius degree.");
        c.getPayload().addStatement("five", "temperature in @event.zone is @event.temperature celsius degree. @event.zone# is hot because temperature is +@event.temperature°C.");
        c.getPayload().addStatement("six", "temperature in @event.zone is managed by object @event.object.name#.");
        c.getPayload().addStatement("seven", "= seven=\"Current temperature is @event.temperature celsius degrees. In fahrenheit is \" + Math.floor(((@event.temperature+40)*1.8)-40) + \" degrees.\";");
        c.getPayload().addStatement("eight", "= eight=10+5;"); //this always returns a double
        c.getPayload().addStatement("nine", "= nine=Math.floor(10+5).toString();"); //print the number as is to avoid conversion to double
        GenericEvent event = new GenericEvent(this);
        event.addProperty("zone", "Kitchen");
        event.addProperty("temperature", "25");
        event.addProperty("object.name", "Indoor Thermometer");
        Resolver resolver = new Resolver();
        resolver.addContext("event.", event.getPayload());
        Trigger result = resolver.resolve(c);
        assertEquals("25", result.getPayload().getStatements("zero").get(0).getValue());
        assertEquals("temperature is 25.", result.getPayload().getStatements("one").get(0).getValue());
        assertEquals("temperature is 25celsius degree.", result.getPayload().getStatements("two").get(0).getValue());
        assertEquals("temperature in Kitchen is 25.", result.getPayload().getStatements("three").get(0).getValue());
        assertEquals("temperature in Kitchen is 25 celsius degree.", result.getPayload().getStatements("four").get(0).getValue());
        assertEquals("temperature in Kitchen is 25 celsius degree. Kitchen is hot because temperature is +25°C.", result.getPayload().getStatements("five").get(0).getValue());
        assertEquals("temperature in Kitchen is managed by object Indoor Thermometer.", result.getPayload().getStatements("six").get(0).getValue());
        //testing scripting
        assertEquals("Current temperature is 25 celsius degrees. In fahrenheit is 77 degrees.", result.getPayload().getStatements("seven").get(0).getValue());
        assertEquals("15.0", result.getPayload().getStatements("eight").get(0).getValue());
        assertEquals("15", result.getPayload().getStatements("nine").get(0).getValue());
    }
}
