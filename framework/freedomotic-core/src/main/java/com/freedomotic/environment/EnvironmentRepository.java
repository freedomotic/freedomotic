/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.environment;

import com.freedomotic.exceptions.DaoLayerException;
import com.freedomotic.objects.EnvObjectLogic;
import com.freedomotic.persistence.Repository;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author enrico
 */
public interface EnvironmentRepository extends Repository<EnvironmentLogic> {
    
    //TODO: remove it it's just temporary to ease the refactoring
    public void saveEnvironmentsToFolder(File folder) throws DaoLayerException;
    public EnvObjectLogic join(String clazz, String name, String protocol, String address);
    public boolean loadEnvironmentsFromDir(File folder, boolean makeUnique) throws DaoLayerException;
    public void saveAs(EnvironmentLogic env, File folder) throws IOException;
    
}
