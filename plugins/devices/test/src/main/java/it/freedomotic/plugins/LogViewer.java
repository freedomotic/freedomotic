/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.api.Actuator;

import it.freedomotic.app.Freedomotic;

import it.freedomotic.exceptions.UnableToExecuteException;

import it.freedomotic.objects.EnvObjectLogic;

import it.freedomotic.plugins.gui.LogWindowHandler;

import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.CommandPersistence;

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
    private Logger logger = null;

    public LogViewer() {
        super("Log Viewer", "/test/logviewer-manifest.xml");
    }

    @Override
    protected void onShowGui() {
        //nothig special to do here
    }

    @Override
    protected void onStart() {
        handler = LogWindowHandler.getInstance();
        handler.setFilter(new Filter() {
            public boolean isLoggable(LogRecord record) {
                //logs every message
                return true;
            }
        });
        logger = Freedomotic.logger;
        logger.setLevel(Level.ALL);
        logger.addHandler(handler);
        bindGuiToPlugin(handler.window);
        showGui();
    }

    @Override
    protected void onStop() {
        //free memory
        hideGui();
        gui = null;
        logger.removeHandler(handler);
        handler = null;
        logger = null;
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
