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
      private ArrayList<MarketPlaceFile> field_icon;
      private ArrayList<MarketPlaceValue> field_os;
      private ArrayList<MarketPlaceFile> field_file;
      private String uri;  
      private String type;
      
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
}
