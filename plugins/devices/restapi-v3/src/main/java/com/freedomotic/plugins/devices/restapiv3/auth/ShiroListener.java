/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.freedomotic.plugins.devices.restapiv3.auth;

/**
 *
 * @author matteo
 */
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.shiro.web.env.EnvironmentLoaderListener;

@WebListener
public class ShiroListener extends EnvironmentLoaderListener implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    sce.getServletContext().setInitParameter(ENVIRONMENT_CLASS_PARAM, FDWebEnvironment.class.getName());
    super.contextInitialized(sce);
  }

}
