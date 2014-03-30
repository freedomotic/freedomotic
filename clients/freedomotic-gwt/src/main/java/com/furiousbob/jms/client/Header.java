package com.furiousbob.jms.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 *
 * @author Vinicius Carvalho
 *
 *
 */
public class Header extends JavaScriptObject {

    protected Header() {
    }

    public final native String getDestination()/*-{
     return this.destination;
     }-*/;

    public final native Integer getExpires()/*-{
     return this.expires;
     }-*/;

    public final native String getSubscription()/*-{
     return this.subscription;
     }-*/;

    public final native String getId()/*-{
     return this["message-id"];
     }-*/;

    public final native Integer getPriority()/*-{
     return this.priority;
     }-*/;

    public final native Long getTimestamp()/*-{
     return this.timestamp;
     }-*/;
}
