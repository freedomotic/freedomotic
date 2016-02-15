/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
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

package com.freedomotic.plugins.devices.isy99i;

import com.universaldevices.common.UDClientStatus;
import com.universaldevices.resources.errormessages.ErrorEventListener;
import com.universaldevices.resources.errormessages.Errors;
import com.universaldevices.upnp.UDControlPoint;
import com.universaldevices.upnp.UDProxyDevice;

public class MyISYErrorHandler implements ErrorEventListener {

    private int socket_open_failed_count = 0;

    private void offLine(int status, Object device) {
        String err = Errors.getErrorMessage(status);
        System.out.println("Warning: " + status + (err == null ? " " : err));
        if (UDClientStatus.isBusy()) {
            return;
        }
        if (status == 1050) {
            socket_open_failed_count++;
        }
        if (socket_open_failed_count < 3) {
            return;
        }
        socket_open_failed_count = 0;

        if (device == null) {
            device = UDControlPoint.firstDevice;
        }
        if (device != null && device instanceof UDProxyDevice) {
            UDControlPoint.getInstance().offLine((UDProxyDevice) device);
        }

    }

    private void humanInterventionRequired(int status, String msg) {
        String err = Errors.getErrorMessage(status);
        System.out.println("Human Intervention Required: " + status + (err == null ? " " : err));
        if (msg != null) {
            System.out.println(msg);
        }
    }

    private void warning(int status, String msg) {
        String err = Errors.getErrorMessage(status);
        System.out.println("Warning: " + status + (err == null ? " " : err));
        if (msg != null) {
            System.out.println(msg);
        }
    }

    private void fatalError(int status, String msg) {
        String err = Errors.getErrorMessage(status);
        System.out.println("Fatal Error: " + status + (err == null ? " " : err));
        if (msg != null) {
            System.out.println(msg);
        }
    }

    /**
     * This method is invoked when ISY encounters an error
     *
     * @param status - the error code
     * @param msg - any generated messages by <code>ISYClient</code>
     * @param device - the <code>UDProxyDevice</code> initiating this event
     * @return - whether or not the client attempt displaying the error on a UI.
     * if a <code>GUIErrorHandler</code> is installed, that object is invoked
     * (showError)
     */
    public boolean errorOccured(int status, String msg, Object device) {

        switch (status) {
            case -1:
            case 803/*discovering nodes:retry*/:
            case 902/*Node Is in Error; Check connections!*/:
                warning(status, null);
                break;

            case 801/*maximum secure sessions*/:
            case 805/*internal error: reboot*/:
            case 815/*Maximum Subscribers Reached*/:
            case 903/*System Not Initialized; Restart!*/:
            case 905/*Subscription Failed; The device might need reboot!*/:
            case 1020/*Couldn't create the event handler socket*/:
                fatalError(status, "Exit the applciation; might have to reboot ISY");
                break;

            case 781/*no such session*/:
            case 802/*device in error*/:
            case 813/*subscription id not found*/:
            case 904/*Subscription Failed!*/:
            case 906/*Event received for a different subscription; Restart!*/:
            case 907/*Bad Event Received*/:
            case 1021/*Subscription failed: socket*/:
            case 1022/*Unsubscription failed: socket*/:
            case 1023/*Interrupted I/O: ProxyDevice*/:
            case 1024/*I/O error: ProxyDevice*/:
            case 1025/*Server socket close failed*/:
            case 1026/*Server socket close failed-2*/:
            case 1050/*Socket open failed*/:
            case 1051/*Socket close failed*/:
            case 1100/*Couldn't open the stream*/:
            case 1200/*Couldn't resolve localhost*/:
            case 1301/*Device not responding*/:
                offLine(status, device);
                break;

            case 604/*human intervention required*/:
            case 701/*authorization failed*/:
            case 1000/*XML parse error*/:
            case 4000 /*No < or > in the name*/:
            case 5000 /*Invalid userid/pwd */:
            case 5001 /*Invalid length*/:
            case 9000 /*Invalid SSL Certificate*/:
                humanInterventionRequired(status, null);
                offLine(status, device);
                break;

        }

        return false;
    }
}
