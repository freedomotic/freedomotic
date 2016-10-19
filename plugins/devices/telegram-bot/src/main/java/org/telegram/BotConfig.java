package org.telegram;

import com.freedomotic.api.API;
import com.google.inject.Inject;
 

public class BotConfig {
    
    @Inject
    private API api;
    
    public final String BOT_TOKEN = api.getConfig().getStringProperty("time-between-reads", "<token>");
    public final String BOT_USERNAME = "weatherbot";
    
}
