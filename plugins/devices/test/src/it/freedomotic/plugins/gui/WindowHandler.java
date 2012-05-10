/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins.gui;

import it.freedomotic.util.LogFormatter;
import java.util.logging.*;

/**
 *
 * @author Enrico
 */
public class WindowHandler extends Handler {

    public LogWindow window = null;
    private Level level = null;
    private static WindowHandler handler = null;

    private WindowHandler() {
        LogManager manager = LogManager.getLogManager();
        String className = this.getClass().getName();
        String level = manager.getProperty(className + ".level");
        String filter = manager.getProperty(className + ".filter");
        setLevel(level != null ? Level.parse(level) : Level.INFO);
        if (window == null) {
            window = new LogWindow(this);
        }
    }

    public static synchronized WindowHandler getInstance() {
        if (handler == null) {
            handler = new WindowHandler();
        }
        return handler;
    }

    public synchronized void publish(LogRecord record) {
        String message;
        if (!isLoggable(record)) {
            return;
        }
        //message = getFormatter().format(record);
//        message = record.getLevel() + "\t "
//                + record.getSourceClassName() + "\t"
//                + record.getSourceMethodName() + "\t "
//                + record.getMessage();
        window.append(new Object[]{
                    record.getLevel(),
                    record.getSourceClassName(),
                    record.getSourceMethodName(),
                    record.getMessage()
                });
    }

    public void close() {
    }

    public void flush() {
    }
}
