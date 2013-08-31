/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.api.Actuator;
import it.freedomotic.app.Freedomotic;
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
        handler = LogWindowHandler.getInstance();
        handler.setFilter(new Filter() {
            public boolean isLoggable(LogRecord record) {
                //logs every message
                return true;
            }
        });
        handler.setLevel(Level.ALL);
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
