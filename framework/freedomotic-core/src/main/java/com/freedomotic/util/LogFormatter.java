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
package com.freedomotic.util;

import com.freedomotic.settings.Info;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import org.slf4j.Logger;
import java.util.logging.LogRecord;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
public class LogFormatter
        extends Formatter {

    private static final Logger LOG = LoggerFactory.getLogger(LogFormatter.class.getName());

    SimpleDateFormat date = new SimpleDateFormat("HH:mm  ss,S");

    /**
     *
     */
    public LogFormatter() {
        super();
    }

    /**
     *
     * @param record
     * @return
     */
    @Override
    public String format(LogRecord record) {
        return ("<tr bgcolor=\"" + getColor(record.getLevel().intValue()) + "\"><td>" + date.format(record.getMillis()) + " "
                + getShortClassName(record.getSourceClassName()) + "</font></td><td>"
                + formatTextToHTML(formatMessage(record)) + "</font></td></tr>\n");
    }

    private String getColor(int level) {
        String htmlColor = "#EAEAE1";
        if (level == Level.SEVERE.intValue()) {
            htmlColor = "#FFDDDD";
        } else if (level == Level.WARNING.intValue()) {
            htmlColor = "#FFFFDD";
        } else if (level == Level.INFO.intValue()) {
            htmlColor = "#DDFFDD";
        }
        return htmlColor;
    }

    private String getShortClassName(String name) {
        return name.substring(name.lastIndexOf('.') + 1,
                name.length());
    }

    private static String convertSpaces(String input) {
        return input.replace("\n", "<br>");
    }

    /**
     *
     * @param input
     * @return
     */
    public static String formatTextToHTML(String input) {
        if (input.startsWith("---- ") && (input.endsWith(" ----"))) {
            //it's a title
            input = input.replace("----", "").trim();
            input = "<br><h4>" + input + "</h4>";
        }

        if (input.startsWith("--- ") && (input.endsWith(" ---"))) {
            //it's a title
            input = input.replace("---", "").trim();
            input = "<br><h3>" + input + "</h3>";
        }

        if (input.startsWith("-- ") && (input.endsWith(" --"))) {
            //it's a title
            input = input.replace("--", "").trim();
            input = "<br><h2>" + input + "</h2>";
        }

        input = input.replace("{{; ", "<ul><li>"); //to overcome format error
        input = input.replace("{{", "<ul><li>");
        input = input.replace("; ", "</li><li>");
        input = input.replace("}}", "</li></ul>");

        return convertSpaces(input);
    }

    /**
     *
     * @param h
     * @return
     */
    @Override
    public String getHead(Handler h) {
        return ("<html>\n  " + "<body>\n" + "<h1>Freedomotic Developers Log - " + new Date().toString() + "</h1>"
                + "<h2>Press F5 to update the page while Freedomotic is running</h2>"
                + "<div>Here is the logger of Freedomotic. It is mainly usefull for developers. We are currently in beta so it is enabled by default.</div>"
                + "<div>You can get a more user/configurator perspective on the project website at <a href=\"http://www.freedomotic.com\">http://www.freedomotic.com/</a>.</div>"
                + "<div>If you don't know where to start take a look at the <a href=\"http://freedomotic.com/content/use-it\">getting started tutorial</a></div><br>"
                + "<div>Scroll this page till the end to read the latest log records. Information records have green background, warnings and exceptions are highlighted with red background.</div><br>"
                + "<div>If the pop-up of this logger annoys you you can disable setting the parameter 'KEY_LOGGER_POPUP = false' in "
                + Info.PATHS.PATH_CONFIG_FOLDER + "/config.xml</div><br>"
                + "<div>This file can still be opened from " + Info.PATHS.PATH_WORKDIR
                + "/log/freedomlog.html</div><br>." + "<font face='Verdana' size='5'>\n"
                + "<table border=0 cellpadding=0 cellspacing=0 width=\"100%\" style='border: 0pt solid #000000; width: 100%; border-Collapse: collapse'>\n"
                + "<tr>" + "    <td>Time</td>" + "    <td>Log Message</td>" + "</tr>\n");
    }

    /**
     *
     * @param h
     * @return
     */
    @Override
    public String getTail(Handler h) {
        return ("</table>\n</font>\n</body>\n</html>");
    }
}
