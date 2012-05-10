/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.gpulido.twitter.gateways;

import it.freedomotic.model.ds.Config;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author gpt
 */
public class TwitterGateway {

    //class attributes
    private static Twitter twitter = null;  //Singleton reference

    //private constructor so this object can be instanced only by using the getInstance method
    private TwitterGateway() {
    }

    public static Twitter getInstance() {
        return getInstance(new Config());
    }

    public static synchronized Twitter getInstance(Config configuration) {
        if (twitter == null) {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            //TODO: create a default twitter account for freedom project. If the user do not customize the oauth parameters in
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
