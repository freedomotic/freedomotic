/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.environment;

import com.freedomotic.model.environment.Environment;
import com.freedomotic.util.Info;
import com.google.inject.Provider;
import java.io.File;
import javax.inject.Inject;

/**
 * @deprecated EXPERIMENTAL DO NOT USE IT
 * 
 * Creates a new {@link com.freedomotic.environment.EnvironmentLogic} using an 
 * {@link com.freedomotic.model.environment.Environment} pojo as data container.
 * The returned object is fully injected.
 * After the object is created it should be inserted in the repository using 
 * {@link com.freedomotic.environment.EnvironmentRepository#create(com.freedomotic.environment.EnvironmentLogic)}
 * @author enrico
 */
public class EnvironmentLogicFactory implements Provider<EnvironmentLogic> {

    private final Environment environment;

    @Inject
    public EnvironmentLogicFactory(Environment environment) {
        System.out.println("DEBUG: environment pojo is " + environment.getName());
        this.environment = environment;
    }

    @Override
    public EnvironmentLogic get() {
        EnvironmentLogic envLogic = new EnvironmentLogic();
        envLogic.setPojo(environment);
        envLogic.setSource(new File(Info.PATHS.PATH_ENVIRONMENTS_FOLDER + "/" + environment.getUUID() + "/" + environment.getUUID() + ".xenv"));
        return envLogic;
    }
}
