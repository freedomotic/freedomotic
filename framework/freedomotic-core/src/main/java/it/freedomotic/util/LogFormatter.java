
package it.freedomotic.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author Enrico
 */
public class LogFormatter extends Formatter {
    SimpleDateFormat date = new SimpleDateFormat("HH:mm  ss,S");

    public LogFormatter() {
        super();
    }

    @Override
    public String format(LogRecord record) {
        if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
            return ("<tr bgcolor=\"#cf2d2d\"><td><font size='1'>" + date.format(record.getMillis()) + " " + getShortClassName(record.getSourceClassName()) + "</font></td><td><font size='1'>"
                    + formatTextToHTML(record.getMessage()) + "</font></td></tr>\n");
        } else {
            if (record.getLevel().intValue() == Level.INFO.intValue()) {
                return ("<tr bgcolor=\"#CC9999\"><td><font size='1'>" + date.format(record.getMillis()) + " " + getShortClassName(record.getSourceClassName()) +  "</font></td><td><font size='1'>"
                        + formatTextToHTML(record.getMessage()) + "</font></td></tr>\n");
            } else {
                return ("<tr bgcolor=\"#D6D6D6\"><td><font size='1'>" + date.format(record.getMillis()) + " " + getShortClassName(record.getSourceClassName()) +  "</font></td><td><font size='1'>"
                        + formatTextToHTML(record.getMessage()) + "</font></td></tr>\n");
            }
        }
    }

    private String getShortClassName(String name){
        return name.substring(name.lastIndexOf('.')+1, name.length());
    }

    private String convertSpaces(String input) {
        return input.replace("\n", "<br>");
    }

    private String formatTextToHTML(String input) {
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
        input = input.replace("{; ", "<ul><li>"); //to overcome format error
        input = input.replace("{", "<ul><li>");
        input = input.replace("; ", "</li><li>");
        input = input.replace("}", "</li></ul>");

        return convertSpaces(input);
    }

    @Override
    public String getHead(Handler h) {
        return ("<html>\n  "
                + "<body>\n"
                + "<h1>Freedomotic Developers Log - " + new Date().toString() + "</h1>"
                + "<h2>Press F5 to update the page while Freedomotic is running</h2>"
                + "<div>Here is the logger of Freedomotic. It is mainly usefull for developers. We are currently in beta so it is enabled by default.</div>"
                + "<div>You can get a more user/configurator perspective on the project website at <a href=\"http://www.freedomotic.com\">http://www.freedomotic.com/</a>.</div>"
                + "<div>If you don't know where to start take a look at the <a href=\"http://freedomotic.com/content/use-it\">getting started tutorial</a></div><br>"
                + "<div>Scroll this page till the end to read the latest log records. Information records have green background, warnings and exceptions are highlighted with red background.</div><br>"
                + "<div>If the pop-up of this logger annoys you you can disable setting the parameter 'KEY_LOGGER_POPUP = false' in " + Info.getApplicationPath() + "/config/config.xml</div><br>"
                + "<div>This file can still be opened from " + Info.getApplicationPath() + "/log/freedomlog.html</div><br>."
                + "<font face='Verdana' size='5'>\n"
                + "<table border=0 cellpadding=0 cellspacing=0 style='border: 0pt solid #000000; border-Collapse: collapse'>\n"
                + "<tr>"
                + "    <td>Time</td>"
                + "    <td>Log Message</td>"
                + "</tr>\n");
    }

    @Override
    public String getTail(Handler h) {
        return ("</table>\n</font>\n</body>\n</html>");
    }
}
