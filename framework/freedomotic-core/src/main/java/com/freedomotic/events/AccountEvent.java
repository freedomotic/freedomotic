/**
 *
 * Copyright (c) 2009-2015 Freedomotic team http://freedomotic.com
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
package com.freedomotic.events;

import com.freedomotic.api.EventTemplate;

/**
 * Channel <b>app.event.sensor.account.change"</b>. Informs about a change in an
 * account status.
 *
 * @author enrico
 */
public class AccountEvent extends EventTemplate {

    public enum AccountActions {
        CREATED, DELETED, BANNED, LOGIN, LOGOUT
    };

    public AccountEvent(Object source, String accountSubject, AccountActions action) {
        payload.addStatement("account.subject", accountSubject);
        payload.addStatement("account.action", action.toString());
    }

    /**
     *
     */
    @Override
    protected void generateEventPayload() {
    }

    /**
     *
     * @return
     */
    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.account.change";
    }

}
