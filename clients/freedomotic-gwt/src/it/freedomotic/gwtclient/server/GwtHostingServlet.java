package it.freedomotic.gwtclient.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GwtHostingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        // Print a simple HTML page including a <script> tag referencing your GWT module as the response
        PrintWriter writer = resp.getWriter();
        writer.append("<html><head>")
                .append("<script type=\"text/javascript\" src=\"freedomotic/freedomotic.nocache.js\"></script>")
                .append("</head><body><p>Hello, world!</p></body></html>");
    }
}