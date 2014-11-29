/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.objects;

import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.persistence.Repository;
import java.io.File;
import java.util.List;

/**
 *
 * @author enrico
 */
public interface ThingsRepository extends Repository<EnvObjectLogic> {
    
    public List<EnvObjectLogic> findByEnvironment(EnvironmentLogic env);
    public List<EnvObjectLogic> findByEnvironment(String uuid);
    public List<EnvObjectLogic> findByAddress(String protocol, String address);
    
    //TODO: temporary for refactoring, should be removed
    public EnvObjectLogic load(File file) throws RepositoryException;
    public List<EnvObjectLogic> loadAll(File folder) throws RepositoryException;   
}
