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
package com.freedomotic.things.impl;

import com.freedomotic.events.ObjectReceiveClick;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.reactions.Trigger;

/**
 *
 * @author enrico
 */
public class Clock
        extends EnvObjectLogic {

    @Override
    public void init() {
        super.init();
    }

    @Override
    protected void createCommands() {
        //no commands for this kind of objects
        super.createCommands();
    }

    @Override
    protected void createTriggers() {
        super.createTriggers();

        Trigger everySecond = new Trigger();
        everySecond.setName("Every one second");
        everySecond.setDescription("schedule actions to be executed at a fixed interval of 1 second");
        everySecond.setChannel("app.event.sensor.calendar.event.schedule");
        everySecond.setSuspensionTime(1000);

        Trigger everyMinute = new Trigger();
        everyMinute.setName("Every one minute");
        everyMinute.setDescription("schedule actions to be executed at a fixed interval of 60 second");
        everyMinute.setChannel("app.event.sensor.calendar.event.schedule");
        everyMinute.setSuspensionTime(60000);

        Trigger morning = new Trigger();
        morning.setName("Every minute while is morning");
        morning.setDescription("execute a command every 60 second from 8:00 to 12:00");
        morning.setChannel("app.event.sensor.calendar.event.schedule");
        morning.getPayload().addStatement("AND", "time.hour", "GREATER_THAN", "7");
        morning.getPayload().addStatement("AND", "time.hour", "LESS_THAN", "13");
        morning.setSuspensionTime(60000);

        Trigger clicked = new Trigger();
        clicked.setName("When " + this.getPojo().getName() + " is clicked");
        clicked.setChannel("app.event.sensor.object.behavior.clicked");
        clicked.getPayload().addStatement("object.name", this.getPojo().getName());
        clicked.getPayload().addStatement("click", ObjectReceiveClick.SINGLE_CLICK);
        clicked.setPersistence(false);

        Trigger eight = new Trigger();
        eight.setName("At 8:00AM");
        eight.setDescription("at 8:00 in the morning");
        eight.setChannel("app.event.sensor.calendar.event.schedule");
        eight.getPayload().addStatement("AND", "time.hour", "EQUALS", "8");
        eight.getPayload().addStatement("AND", "time.minute", "EQUALS", "0");
        eight.getPayload().addStatement("AND", "time.second", "EQUALS", "0");

        triggerRepository.create(clicked);
        triggerRepository.create(everySecond);
        triggerRepository.create(everyMinute);
        triggerRepository.create(morning);
        triggerRepository.create(eight);
    }
}
