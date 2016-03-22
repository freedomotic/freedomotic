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

import com.freedomotic.i18n.I18n;
import com.freedomotic.util.LogFormatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author Enrico Nicoletti
 */
public class LogWindowHandler extends Handler {

    /**
     *
     */
    public LogWindow window = null;
    private static LogWindowHandler handler = null;

    private LogWindowHandler(I18n i18n) {
        setLevel(Level.ALL);
        if (window == null) {
            window = new LogWindow(i18n, this);
        }
    }

    /**
     *
     * @param i18n
     * @return
     */
    public static LogWindowHandler getInstance(I18n i18n) {
        if (handler == null) {
            handler = new LogWindowHandler(i18n);
            handler.setFormatter(new LogFormatter());
        }

        return handler;
    }

    /**
     *
     * @param record
     */
    @Override
    public void publish(LogRecord record) {

        if (!isLoggable(record)) {
            return;
        }

        window.append(new Object[]{record.getLevel(),
            handler.getFormatter().formatMessage(record)});
    }

    /**
     *
     */
    @Override
    public void close() {
    }

    /**
     *
     */
    @Override
    public void flush() {
    }
}
