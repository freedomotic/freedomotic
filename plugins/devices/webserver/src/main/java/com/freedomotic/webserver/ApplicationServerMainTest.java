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
package com.freedomotic.webserver;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 *
 * @author Gabriel Pulido de Torres
 */
public class ApplicationServerMainTest {

    public static void main(String[] args) throws Exception {

        Server server = new Server(8080);
        String dir = "/home/gpt/Desarrollo/freedomotic/framework/freedomotic/plugins/devices/es.gpulido.webserver/data/webapps/gwt_client";
        WebAppContext context = new WebAppContext();

        context.setDescriptor(dir + "/WEB-INF/web.xml");
        context.setResourceBase("/home/gpt/Desarrollo/freedomotic/framework/freedomotic/plugins/devices/es.gpulido.webserver/data/webapps/gwt_client");
        context.setContextPath("/");
        context.setParentLoaderPriority(true);

        server.setHandler(context);
        server.start();


    }
}
