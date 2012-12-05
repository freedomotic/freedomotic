/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.marketplace.util;

import it.freedomotic.service.IPluginPackage;
import java.util.ArrayList;
import javax.swing.ImageIcon;

/**
 *
 * @author gpt
 * New version of the POJO that represents a plugin
 */
public class MarketPlacePlugin2 implements IPluginPackage{
      private String title;
      private String description;
      private String body;
      private String teaser;            
      private ArrayList<MarketPlaceUser> field_developer;
      private ArrayList<MarketPlaceValue> field_status;
      private ArrayList<MarketPlaceValue> field_description;
      private ArrayList<MarketPlaceValue> field_category;
      private ArrayList<MarketPlaceValue> field_plugin_category;
      private ArrayList<MarketPlaceFile> field_icon;
      private ArrayList<MarketPlaceValue> field_os;
      private ArrayList<MarketPlaceFile> field_file;
      private String uri;  
      private String type;
      
    //private String body; //XML
    
    //private String path; 

    //private ArrayList<String> taxonomy;
       
    //Fields not parsed
    private transient ImageIcon icon;
      
    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getFilePath() {
        if (field_file!= null)            
            if (field_file.size()>0)
                if(field_file.get(0)!=null)
                    return field_file.get(0).getFilepath();        
        return "";
    }
        
    public String getFilePath(String version) {
        if (field_file!= null)
        {
            for (MarketPlaceFile marketPlaceFile: field_file)
            {            
                //TODO: change for a regular expresion to match the version
                if (marketPlaceFile.getFilename().contains(version))
                {
                    return marketPlaceFile.getFilepath();                    
                }
            }                        
        }
        return "";
    }
    
    public int getFileIndexByVersion(String version) {
         int i = -1;
        if (field_file!= null)
        {           
            for (MarketPlaceFile marketPlaceFile: field_file)
            {            
                i++;
                //TODO: change for a regular expresion to match the version
                if (marketPlaceFile.getFilename().contains(version))
                {
                    return i;                    
                }
            }                        
        }
        return i+1;
    }
    
    public int getFileCount()
    {
        return field_file.size();
    }
    

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ImageIcon getIcon() {
        if (icon == null)
        {
            if (field_icon!= null &&
                field_icon.size()>0 &&
                field_icon.get(0)!=null)
                        icon = DrupalRestHelper.retrieveImageIcon("/"+field_icon.get(0).getRelativeFilepath());
            else
                icon = DrupalRestHelper.defaultIconImage;
        }
        return icon;
    }
    
     /**
    *
    * Adds a file to the plugin, taking into account the core versions and name
    */
    public void addFile(MarketPlaceFile file)
    {
        String version = extractCorePluginVersion(file.getFilename());
        boolean found = false;
        //check if the plugin has a file with that version
        for(MarketPlaceFile pluginFile: field_file)
        {
            if (extractCorePluginVersion(pluginFile.getFilename()).equals(version))
            {                
                int index = field_file.indexOf(pluginFile);
                //Substitute the file
                //TODO: decide what to do with the old files
                field_file.set(index, file);
                found = true;
            }        
        }
        if (!found)
        {
            //Two posibilities. The name is not right or it is a new version.
            //We guess that is for a new version.
            //TODO: handle the case of not right.
            // We add the file at the end of the list
            field_file.add(file);                    
        }
    
    }
    
    
    public String formatBaseData()
    {
    return  "\"type\":\""+type+"\","
          + "\"language\":\"und\"";    
    }
                    
    public String formatFieldCategory()
    {
        String jsonString= "";
        for (int i = 0; i < field_category.size(); i++) {
            if(i==0)
            {
                jsonString += "\"field_category\":{";
            }
            jsonString+="\""+i+"\":"+field_category.get(i).formatValue();
            if (i!= field_category.size()-1)
            {
              jsonString+= ",";              
            }
            if(i==field_category.size()-1)
            {
                jsonString += "}";            
            }
        }
        return jsonString;
    }
    

    public String formatFieldPluginCategory()
    {               
        String jsonString= "";
        String s="";
        //we are assuming that the Plugin is well formed 
        //(ie, has at least one correct Plugin category)
        jsonString += "\"field_plugin_category\":{\"value\":{";
        boolean first = true;
        for (MarketPlaceValue value: field_plugin_category)
        {
           
            if (value.getValue()!= null)
            {
                if (!first)            
                    jsonString += ",";                        
                jsonString += value.formatValueAsListElement();
                first = false;
            }            
        }        
        
        jsonString += "}}";        
        return jsonString;        
    }
    
    
    public String formatFieldOS()
    {               
        String jsonString= "";
        String s="";
        for (int i = 0; i < field_os.size(); i++) {
            if(i==0)
            {
                jsonString += "\"field_os\":{\"value\":{";
            }            
            s = field_os.get(i).getValue();            
            //jsonString+="\""+i+"\":"+field_os.get(i).formatValue();
            jsonString+="\""+s+"\":\""+s+"\"";            
            if (i!= field_os.size()-1)
            {
              jsonString+= ",";              
            }
            if(i==field_os.size()-1)
            {
                jsonString += "}}";            
            }
        }
        return jsonString;
        
    }
    
    public String formatFieldFile()
    {
        String jsonString= "";
        for (int i = 0; i < field_file.size(); i++) {
            MarketPlaceFile pluginFile = field_file.get(i);
            pluginFile.setDescription(extractVersion(pluginFile.getFilename()));
            if(i==0)
            {
                jsonString += "\"field_file\":{";
            }            
            jsonString+= "\""+i+"\":{"+ pluginFile.formatFile()+"}";
            if (i!= field_file.size()-1)
            {
              jsonString+= ",";              
            }
            if(i==field_file.size()-1)
            {
                jsonString += "}";            
            }
        }        
        return jsonString;    
    }
    
    
    public static String extractCorePluginVersion(String filename) {
        //suppose filename is something like it.nicoletti.test-5.2.x-1.212.device
        //only 5.2.x-1.212 is needed
        //remove extension
        filename = filename.substring(0, filename.lastIndexOf("."));
        String[] tokens = filename.split("-");
        //3 tokens expected
        if (tokens.length == 3) {
            return tokens[1];
        } else {
            return filename;
        }
    }
    
    public static String extractVersion(String filename) {
        //suppose filename is something like it.nicoletti.test-5.2.x-1.212.device
        //only 5.2.x-1.212 is needed
        //remove extension
        filename = filename.substring(0, filename.lastIndexOf("."));
        String[] tokens = filename.split("-");
        //3 tokens expected
        if (tokens.length == 3) {
            return tokens[1] + "-" + tokens[2];
        } else {
            return filename;
        }
    }
    
}
