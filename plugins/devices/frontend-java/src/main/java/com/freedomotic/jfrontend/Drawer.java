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
package com.freedomotic.jfrontend;

import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.Room;
import com.freedomotic.environment.ZoneLogic;
import javax.swing.JPanel;

/**
 *
 * @author Enrico Nicoletti
 */
public abstract class Drawer extends JPanel {

    /**
     *
     * @param callout1
     */
    public abstract void createCallout(Callout callout1);

    /**
     *
     * @param b
     */
    public abstract void setNeedRepaint(boolean b);

    void setObjectEditMode(boolean b) {
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    void setRoomEditMode(boolean b) {
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    boolean getRoomEditMode() {
        //throw new UnsupportedOperationException("Not yet implemented");
        return false;
    }

    ZoneLogic getSelectedZone() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void createHandles(Room room) {
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    boolean getObjectEditMode() {
        //throw new UnsupportedOperationException("Not yet implemented");
        return false;
    }

    void setSelectedZone(ZoneLogic selectedZone) {
        //overwritted by subclasses
    }

    abstract EnvironmentLogic getCurrEnv();

    abstract void setCurrEnv(EnvironmentLogic env);
}
