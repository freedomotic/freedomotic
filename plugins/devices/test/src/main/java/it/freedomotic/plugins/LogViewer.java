/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
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

package it.freedomotic.plugins;

import it.freedomotic.api.Actuator;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.plugins.gui.LogWindowHandler;
import it.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class LogViewer
        extends Actuator {

    private LogWindowHandler handler = null;
    //get root logger
    private static final Logger logger = Logger.getLogger("it.freedomotic");

    public LogViewer() {
        super("Log Viewer", "/test/logviewer-manifest.xml");
    }

    @Override
    protected void onShowGui() {
        //nothing special to do here
    }

    @Override
    protected void onStart() {
        handler = LogWindowHandler.getInstance(getApi().getI18n());
        handler.setFilter(new Filter() {
            @Override
            public boolean isLoggable(LogRecord record) {
                //logs every message
                return true;
            }
        });
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        //IMPORTANT!!!!
        logger.setLevel(Level.ALL);
        bindGuiToPlugin(handler.window);
//        showGui();
    }

    @Override
    protected void onStop() {
        //free memory
        hideGui();
        gui = null;
        logger.removeHandler(handler);
        handler = null;
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
