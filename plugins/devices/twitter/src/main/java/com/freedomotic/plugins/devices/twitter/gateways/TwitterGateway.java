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

package com.freedomotic.plugins.devices.twitter.gateways;

import com.freedomotic.model.ds.Config;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author Gabriel Pulido de Torres
 */

public class TwitterGateway {

    //class attributes
    private static Twitter twitter = null;  //Singleton reference

    //private constructor so this object can be instanced only by using the getInstance method
    private TwitterGateway() {
    }

    /**
     *
     * @return
     */
    public static Twitter getInstance() {
        return getInstance(new Config());
    }

    /**
     *
     * @param configuration
     * @return
     */
    public static synchronized Twitter getInstance(Config configuration) {
        if (twitter == null) {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            //TODO: create a default twitter account for freedomotic project. If the user do not customize the oauth parameters in
            //config file we can use by default the parameters of the freedom twitter account (the second argument in getStringProperty).
            cb.setDebugEnabled(true).setOAuthConsumerKey(configuration.getStringProperty("OAuthConsumerKey", null)) //"TLGtvoeABqf2tEG4itTUaw")
                    .setOAuthConsumerSecret(configuration.getStringProperty("OAuthConsumerSecret", null)) //"nUJPxYR1qJmhX9SnWTBT0MzO7dIqUtNyVPfhg10wf0")
                    .setOAuthAccessToken(configuration.getStringProperty("OAuthAccessToken", null))//"312792183-adnYVIv06spR4qsI3eKVv53CfrYHl3KqgtJtYm10")
                    .setOAuthAccessTokenSecret(configuration.getStringProperty("OAuthAccessTokenSecret", null));//("Br2O2wtZ2dsLMDN21qKdlCLsOuqXW8h3z3uButRk");
            TwitterFactory tf = new TwitterFactory(cb.build());
            twitter = tf.getInstance();
        }
        return twitter;
    }
}
