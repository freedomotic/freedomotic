package com.freedomotic.mocks;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freedomotic.app.FreedomoticInjector;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * It is a test utility to perform Mock Guice Dependency Injection
 * 
 * @author P3trur0 https://flatmap.it
 *
 */
public final class MockInjector {

	private static final Logger LOG = LoggerFactory.getLogger(MockInjector.class);

	private MockInjector() {
	}

	public static MockInjector builder() {
		return new MockInjector();
	}

	@SuppressWarnings("rawtypes")
	/**
	 * This can be used to force a GUICE injection of Mock classes used for unit
	 * tests
	 * 
	 * @param objectToInjectIn
	 *            this is the class that you are going to test
	 * @param propsToInject
	 *            this are the mockable objects to be injected in the class
	 * @return
	 */
	public <T> T injectProperties(T objectToInjectIn, Mockable[] propsToInject) {
		List<Mockable> entitiesToInject = Arrays.asList(propsToInject);
		Injector injector = Guice.createInjector(new AbstractModule() {
			@SuppressWarnings("unchecked")
			@Override
			protected void configure() {
				for (Mockable mockClass : entitiesToInject) {
					Class interfaceToInject = mockClass.getClass().getInterfaces()[0];
					LOG.info("Injecting mockable named {} it implements the following interface {}",
							mockClass.getClass(), interfaceToInject);
					bind(interfaceToInject).toInstance(mockClass.getInstance());
				}
			}
		});
		injector.injectMembers(objectToInjectIn);
		return objectToInjectIn;
	}

}
