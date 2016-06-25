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
package com.freedomotic.plugins.devices.restapiv3.filters;

/**
 * @author Matteo Mazzoni
 */

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

public class CorsBasicHttpAuthenticationFilter extends BasicHttpAuthenticationFilter {

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String httpMethod = httpRequest.getMethod();
        if ("OPTIONS".equalsIgnoreCase(httpMethod)) {
            return true;
        } else if (httpRequest.getRequestURI().equalsIgnoreCase("/v3/users/_/login")) {
            return true;
        } else {
            return super.isAccessAllowed(request, response, mappedValue);
        }
    }

    @Override
    protected boolean sendChallenge(ServletRequest request, ServletResponse response) {

        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        // During Cross Origin requests, no info about auth schema is given
        if (httpRequest.getHeader("Origin") != null && !httpRequest.getHeader("Origin").isEmpty()) {
            HttpServletResponse httpResponse = WebUtils.toHttp(response);
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        return super.sendChallenge(request, response); //To change body of generated methods, choose Tools | Templates.
    }

}
