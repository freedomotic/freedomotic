/*
 * Copyright (c) 2009-2014 Freedomotic team
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

package com.freedomotic.plugins.devices.restapiv3.filters;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

public class CorsBasicHttpAuthenticationFilter extends BasicHttpAuthenticationFilter {
  @Override
  protected boolean isAccessAllowed(ServletRequest request, ServletResponse
response, Object mappedValue) {
    HttpServletRequest httpRequest = WebUtils.toHttp(request);
    String httpMethod = httpRequest.getMethod();
    if ("OPTIONS".equalsIgnoreCase(httpMethod)) {
      return true;
    } 
    else if (httpRequest.getRequestURI().equalsIgnoreCase("/v3/users/_/login")){
      return true;
    }
    else {
      return super.isAccessAllowed(request, response, mappedValue);
    }
  }
 
}
