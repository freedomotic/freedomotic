/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.environment.impl;

import java.io.File;

/**
 * A Guice factory to bind the {@link EnvironmentPersistence} class to its implementation
 * while allowing to pass a parameter to the implementation constructior (see Guice assisted injection)
 * @author enrico
 */
interface EnvironmentPersistenceFactory {
        public EnvironmentPersistence create(File directory);
}
