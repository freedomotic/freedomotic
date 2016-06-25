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
package com.freedomotic.plugins.devices.restapiv3.auth;

import com.freedomotic.plugins.devices.restapiv3.RestAPIv3;
import com.freedomotic.plugins.devices.restapiv3.filters.CorsBasicHttpAuthenticationFilter;
import org.apache.shiro.web.env.DefaultWebEnvironment;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;

/**
 *
 * @author Matteo Mazzoni
 */
public class FDWebEnvironment extends DefaultWebEnvironment {

    public FDWebEnvironment() {
        BasicHttpAuthenticationFilter authc = new CorsBasicHttpAuthenticationFilter();
        LogoutFilter logout = new LogoutFilter();
        logout.setRedirectUrl("http://www.freedomotic.com/");
        
        FilterChainManager fcMan = new DefaultFilterChainManager();
        fcMan.addFilter("authc", authc);
        fcMan.addFilter("logout", logout);
        fcMan.createChain("/auth/logout", "logout");
        fcMan.createChain("/v3/**", "authc");

        PathMatchingFilterChainResolver resolver = new PathMatchingFilterChainResolver();
        resolver.setFilterChainManager(fcMan);

        setFilterChainResolver(resolver);
        setWebSecurityManager(RestAPIv3.defaultWebSecurityManager);
    }

}
