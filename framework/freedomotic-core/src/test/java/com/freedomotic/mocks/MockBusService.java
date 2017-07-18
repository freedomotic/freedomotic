package com.freedomotic.mocks;

import static org.mockito.Mockito.when;

import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.mockito.Mockito;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.bus.BusService;
import com.freedomotic.reactions.Command;

/**
 * It is a mock version of the bus service injected with Guice
 * 
 * @author P3trur0 https://flatmap.it
 *
 */
public class MockBusService implements BusService, Mockable {
	
	@Override
	public Command send(Command command) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reply(Command command, Destination destination, String correlationID) {
		// TODO Auto-generated method stub
	}

	@Override
	public void send(EventTemplate ev) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send(EventTemplate ev, String toQueueName) {
		// TODO Auto-generated method stub
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
	}

	@Override
	public Session getReceiveSession() {
		return Mockito.mock(Session.class);
	}

	@Override
	public Session getSendSession() {
		return Mockito.mock(Session.class);
	}

	@Override
	public Session getUnlistenedSession() {
		return Mockito.mock(Session.class);
	}

	@Override
	public Session createSession() throws Exception {
		Session s = Mockito.mock(Session.class);
		when(s.createConsumer(null)).thenReturn(Mockito.mock(MessageConsumer.class));
		return s;
	}

	@Override
	public Object getInstance() {
		return new MockBusService();
	}

}
