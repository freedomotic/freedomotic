package com.freedomotic.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MessageEventTest {

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void messageEventCreation() {
		MessageEvent evt = new MessageEvent(null, "hello there");
		assertNotNull(evt);
		assertEquals("Default message type should be 'callout'", "callout", evt.getPayload().getStatementValue("message.type"));
		assertEquals("Default message level should be 'info'", "info", evt.getPayload().getStatementValue("message.level"));
	}
	
	@Test
	public void getterAndSetterMethodsVerification() {
		MessageEvent evt = new MessageEvent(null, "hello there");
		evt.setFrom("testFrom");
		evt.setTo("testTo");
		evt.setAttachmentPath("path");
		assertEquals("getFrom should retrieve the setFrom property", "testFrom", evt.getFrom());
		assertEquals("getTo should retrieve the setFrom property'", "testTo", evt.getTo());
		assertEquals("Text property should be the one set in the constructor", "hello there", evt.getText());
		assertEquals("getAttachmentPath should retrieve the setAttachmentPath property", "path", evt.getAttachmentPath());
	}
	
	@Test
	public void setAttachmentPathWithFile() {
		MessageEvent evt = new MessageEvent(null, "hello there");
		File fileTest = new File("test.xml");
		evt.setAttachmentPath(fileTest);
		assertEquals("getAttachmentPath should retrieve the setAttachmentPath property", fileTest.getAbsolutePath(), evt.getAttachmentPath());
	}
	
	@Test
	public void testDefaultDestination() {
		MessageEvent evt = new MessageEvent(null, "hello there");
		assertEquals("Default destination should be 'app.event.sensor.messages.callout'", "app.event.sensor.messages.callout", evt.getDefaultDestination());
	}
	
	@Test
	public void testDefaultDestinationWithNull() {
		MessageEvent evt = new MessageEvent(null, "hello there");
		evt.getPayload().clear();
		assertEquals("Custom destination should work also with null", "app.event.sensor.messages", evt.getDefaultDestination());
	}
}
