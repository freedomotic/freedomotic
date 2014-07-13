/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.testutils;

import com.freedomotic.app.FreedomoticInjector;
import com.google.inject.AbstractModule;
import org.junit.Ignore;

/**
 *
 * @author enrico
 */
@Ignore
public class FreedomoticTestsInjector
        extends AbstractModule {

    /**
     *
     */
    @Override
    protected void configure() {
        install(new FreedomoticInjector());
    }
}
