/**
 *
 * Copyright (c) 2009-2014 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins;

import com.freedomotic.api.Client;
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.model.ds.Config;
import com.freedomotic.objects.EnvObjectLogic;
import com.freedomotic.objects.EnvObjectPersistence;
import java.io.File;

/**
 *
 * @author enrico
 */
public class ObjectPluginPlaceholder
        implements Client {

    private File example;
    private EnvObjectLogic object;
    private Config config;

    /**
     *
     * @param example
     * @throws RepositoryException
     */
    public ObjectPluginPlaceholder(File example)
            throws RepositoryException {
        this.example = example;
        object = EnvObjectPersistence.loadObject(example);
        config = new Config();
    }

    /**
     *
     * @return
     */
    public EnvObjectLogic getObject() {
        return object;
    }

    /**
     *
     * @param name
     */
    @Override
    public void setName(String name) {
        //no name change allowed. do nothing
    }

    /**
     *
     * @return
     */
    @Override
    public String getDescription() {
        return object.getPojo().getDescription();
    }

    /**
     *
     * @param description
     */
    @Override
    public void setDescription(String description) {
        //no change allowed
    }

    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return object.getPojo().getName();
    }

    /**
     *
     * @return
     */
    @Override
    public String getType() {
        return "Object";
    }

    /**
     *
     */
    @Override
    public void start() {
        EnvObjectPersistence.add(object, EnvObjectPersistence.MAKE_UNIQUE);
    }

    /**
     *
     */
    @Override
    public void stop() {
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isRunning() {
        //is running if there is already an object of this kind in the map
//        boolean found = false;
//        for (EnvObjectLogic obj : EnvObjectPersistence.getObjectList()) {
//            if (obj.getClass().getCanonicalName().equals(clazz.getCanonicalName())) {
//                found = true;
//            }
//        }
//        return found;
        return true;
    }

    /**
     *
     * @return
     */
    public File getTemplate() {
        return example;
    }

    /**
     *
     */
    @Override
    public void showGui() {
    }

    /**
     *
     */
    @Override
    public void hideGui() {
    }

    /**
     *
     * @return
     */
    @Override
    public Config getConfiguration() {
        return config;
    }

    /**
     *
     * @param env
     */
    public void startOnEnv(EnvironmentLogic env) {
        if (env==null){
            throw new IllegalArgumentException("Cannot place an object on a null environment");
        }
        EnvObjectLogic obj = EnvObjectPersistence.add(object, EnvObjectPersistence.MAKE_UNIQUE);
        obj.setEnvironment(env);
    }
}
