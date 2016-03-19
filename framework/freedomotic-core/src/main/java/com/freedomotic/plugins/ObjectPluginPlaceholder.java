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
package com.freedomotic.plugins;

import com.freedomotic.api.Client;
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.model.ds.Config;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.ThingRepository;
import java.io.File;

/**
 *
 * @author Enrico Nicoletti
 */
public class ObjectPluginPlaceholder implements Client {

    private final File example;
    private final EnvObjectLogic template;
    private Config config;
    private final ThingRepository thingsRepository;

    /**
     *
     * @param thingsRepository
     * @param example
     * @throws RepositoryException
     */
    public ObjectPluginPlaceholder(ThingRepository thingsRepository, File example) throws RepositoryException {
        this.example = example;
        this.thingsRepository = thingsRepository;
        template = thingsRepository.load(example);
        if (template == null) {
            throw new IllegalStateException("Cannot build an object placeholder plugin from a null object");
        }
        config = new Config();
    }

    /**
     *
     * @return
     */
    public EnvObjectLogic getObject() {
        return template;
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
        return template.getPojo().getDescription();
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
        return template.getPojo().getName();
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
        thingsRepository.copy(template);
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
        //is running if there is already an template of this kind in the map
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
        if (env == null) {
            throw new IllegalArgumentException("Cannot place an object on a null environment");
        }
        EnvObjectLogic obj = thingsRepository.copy(template);
        obj.setEnvironment(env);
    }

    @Override
    public void destroy() {
        // There should be no need to destroy a placeholder
    }
}
