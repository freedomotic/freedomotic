/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 * @author matteo
 */
public class FDWebEnvironment extends DefaultWebEnvironment {

    public FDWebEnvironment() {
        BasicHttpAuthenticationFilter authc = new CorsBasicHttpAuthenticationFilter();
        LogoutFilter logout = new LogoutFilter();

        FilterChainManager fcMan = new DefaultFilterChainManager();
        fcMan.addFilter("authc", authc);
        fcMan.addFilter("logout", logout);
        fcMan.createChain("/logout", "logout");
        fcMan.createChain("/**", "authc");

        PathMatchingFilterChainResolver resolver = new PathMatchingFilterChainResolver();
        resolver.setFilterChainManager(fcMan);

        setFilterChainResolver(resolver);
        setWebSecurityManager(RestAPIv3.defaultWebSecurityManager);
    }

}
