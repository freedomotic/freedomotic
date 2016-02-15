/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.plugins.devices.freedomchat;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 *
 * @author enrico
 */
public class GuiceJUnitRunner extends BlockJUnit4ClassRunner {

    private Injector injector;

    /**
     *
     * @param klass
     * @throws InitializationError
     */
    public GuiceJUnitRunner(Class<?> klass) throws InitializationError {
        super(klass);
        Class<?>[] classes = getModulesFor(klass);
        injector = createInjectorFor(classes);
    }

    /**
     *
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    public @interface GuiceInjectors {

        /**
         *
         * @return
         */
        Class<?>[] value();
    }

    /**
     *
     * @return @throws Exception
     */
    @Override
    public Object createTest() throws Exception {
        Object obj = super.createTest();
        injector.injectMembers(obj);
        return obj;
    }

    private Injector createInjectorFor(Class<?>[] classes) throws InitializationError {
        Module[] modules = new Module[classes.length];
        for (int i = 0; i < classes.length; i++) {
            try {
                modules[i] = (Module) (classes[i]).newInstance();
            } catch (InstantiationException e) {
                throw new InitializationError(e);
            } catch (IllegalAccessException e) {
                throw new InitializationError(e);
            }
        }
        return Guice.createInjector(modules);
    }

    private Class<?>[] getModulesFor(Class<?> klass) throws InitializationError {
        GuiceInjectors annotation = klass.getAnnotation(GuiceInjectors.class);
        if (annotation == null) {
            throw new InitializationError(
                    "Missing @GuiceModules annotation for unit test '" + klass.getName()
                    + "'");
        }
        return annotation.value();
    }
}
