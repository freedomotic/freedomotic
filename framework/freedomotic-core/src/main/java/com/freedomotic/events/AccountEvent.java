/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
