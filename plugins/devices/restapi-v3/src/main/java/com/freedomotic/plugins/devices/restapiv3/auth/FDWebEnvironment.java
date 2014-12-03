/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.restapiv3.auth;

import com.freedomotic.api.API;
import com.freedomotic.plugins.devices.restapiv3.filters.CorsBasicHttpAuthenticationFilter;
import javax.inject.Inject;
import org.apache.shiro.web.env.DefaultWebEnvironment;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

/**
 *
 * @author matteo
 */
public class FDWebEnvironment extends DefaultWebEnvironment {
    
    @Inject
    private API api;

    public FDWebEnvironment() {
        //API api = Freedomotic.INJECTOR.getInstance(API.class);
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
        setWebSecurityManager(new DefaultWebSecurityManager(api.getAuth().getUserRealm()));
    }

}
