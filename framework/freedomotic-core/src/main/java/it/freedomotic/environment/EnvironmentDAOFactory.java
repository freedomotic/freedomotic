/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.environment;

import com.google.inject.assistedinject.Assisted;

import java.io.File;

/**
 *
 * @author enrico
 */
public interface EnvironmentDAOFactory {

    public EnvironmentDAO create(@Assisted File directory);
}
