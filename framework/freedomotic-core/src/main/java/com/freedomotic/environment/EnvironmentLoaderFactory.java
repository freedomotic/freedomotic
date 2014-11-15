/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.environment;

import java.io.File;

/**
 *
 * @author enrico
 */
public interface EnvironmentLoaderFactory {
        public EnvironmentLoader create(File directory);
}
