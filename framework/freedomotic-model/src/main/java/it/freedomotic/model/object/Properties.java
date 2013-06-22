/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.model.object;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author gpt
 */
public class Properties implements Serializable {
    
	private static final long serialVersionUID = 1L;
	
	HashMap<String,String> propertyList;
            
    public Properties()
    {
        propertyList = new HashMap<String,String>();    
    }
    public Properties(HashMap<String,String> prop)
    {
        propertyList = prop;    
    }
    
    public Set<String> stringPropertyNames()
    {              
        return propertyList.keySet();
    }
    
    public String getProperty(String name)
    {
        return propertyList.get(name);
    }
    public void setProperty(String name, String value)
    {
        propertyList.put(name, value);    
    }
    
    public Set<Entry<String,String>> entrySet()
    {
        return propertyList.entrySet();    
    }
    public int size()
    {
        return propertyList.size();            
    }
}
