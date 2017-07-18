package com.freedomotic.plugins.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.freedomotic.api.Client;
import com.freedomotic.mocks.MockBusService;
import com.freedomotic.mocks.MockClient;
import com.freedomotic.mocks.MockFreedomoticInstance;
import com.freedomotic.mocks.MockInjector;
import com.freedomotic.mocks.MockThingRepository;
import com.freedomotic.mocks.Mockable;
import com.freedomotic.model.ds.Config;
import com.freedomotic.settings.Info;

public class ClientStorageInMemoryTest extends MockFreedomoticInstance {
	
	private ClientStorageInMemory csim;
	private static final Mockable[] mockClasses = {
			new MockBusService(),
			new MockThingRepository()
		};
	
	@BeforeClass
	public static void setUpClass() throws Exception {}

	@AfterClass
	public static void tearDownClass() throws Exception {}

	@Before
	public void setUp() {
		csim = MockInjector.builder().injectProperties(new ClientStorageInMemory(), mockClasses);
	}

	@Test
	public void addAndRemoveValidClient() {
		MockClient mockClient = new MockClient();
		Config c = new Config();
		c.setProperty("framework.required.major", Info.getMajor() + "");
		c.setProperty("framework.required.minor", Info.getMinor() + "");
		c.setProperty("framework.required.build", Info.FRAMEWORK.FRAMEWORK_BUILD.toString());
		mockClient.setMockConfiguration(c);
		csim.add(mockClient);
		assertEquals(1, csim.getClients().size());
		assertEquals(MockClient.class, csim.getClients().get(0).getClass());
		csim.remove(mockClient);
		assertEquals(0, csim.getClients().size());
	}
	
	@Test
	public void addAndRemovePluginPlaceholder() {
		MockClient mockClient = new MockClient();
		Config c = new Config();
		c.setProperty("framework.required.major", "1");
		c.setProperty("framework.required.minor", "0");
		c.setProperty("framework.required.build", Info.FRAMEWORK.FRAMEWORK_BUILD.toString());
		mockClient.setMockConfiguration(c);
		csim.add(mockClient);
		assertEquals(1, csim.getClients().size());
		assertNotEquals(MockClient.class, csim.getClients().get(0).getClass());
		csim.remove(mockClient);
		assertEquals(1, csim.getClients().size());
		csim.remove(csim.get("Cannot start \"" + mockClient.getName() + "\""));
		assertEquals(0, csim.getClients().size());
	}
	
	@Test
	public void clientsComparators() {
		Config c = new Config();
		c.setProperty("framework.required.major", Info.getMajor() + "");
		c.setProperty("framework.required.minor", Info.getMinor() + "");
		c.setProperty("framework.required.build", Info.FRAMEWORK.FRAMEWORK_BUILD.toString());
		
		MockClient b = new MockClient();
		b.setName("BBB");
		MockClient a = new MockClient();
		a.setName("AAA");
		MockClient zero = new MockClient();
		zero.setName("000");
		
		a.setMockConfiguration(c);
		b.setMockConfiguration(c);
		zero.setMockConfiguration(c);
		
		csim.add(b);
		csim.add(a);
		csim.add(zero);
		
		List<Client> sortList = csim.getClients();
		
		assertEquals("000", sortList.get(0).getName());
		assertEquals("AAA", sortList.get(1).getName());
		assertEquals("BBB", sortList.get(2).getName());
		
		csim.remove(a);
		csim.remove(b);
		csim.remove(zero);
	}
	
	@After
	public void tearDown() {
	}

}
