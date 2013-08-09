/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.environment;

import it.freedomotic.exceptions.DaoLayerException;

import it.freedomotic.model.environment.Environment;

import java.io.File;

/**
 *
 * @author enrico
 */
public interface EnvironmentDAO {

    void save(Environment environment) throws DaoLayerException;

    void delete(Environment environment) throws DaoLayerException;

    Environment load() throws DaoLayerException;
}
