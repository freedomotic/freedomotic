/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.restapiv3.representations;

import com.freedomotic.security.User;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 *
 * @author matteo
 */
@XmlRootElement
public class UserRepresentation {
    private String name;
    private String password;
    private Set<String> roles = new HashSet<String>();
    @XmlElement(name="props")
    private Properties properties;

    public UserRepresentation() {
    }
    
    public UserRepresentation(User u){
        this.name=u.getName();
        this.password=u.getCredentials().toString();
        this.roles.addAll(u.getRoles());
        this.properties = u.getProperties();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the roles
     */
    public Set<String> getRoles() {
        return roles;
    }

    /**
     * @param roles the roles to set
     */
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    /**
     * @return the properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    
}
