package com.furiousbob.jms.client;

import com.google.gwt.core.client.JavaScriptObject;
/**
 * 
 * @author Vinicius Carvalho
 * 
 * An overlay on top of the StompJS message object. Messages can be created by using the
 * static method
 *
 */
public class Message extends JavaScriptObject {
	protected Message(){}
	
	
	public final native String getBody()/*-{  
		return this.body;
	}-*/;
	
	public final native Header getHeaders()/*-{
		return this.headers;
	}-*/;
	
	public static final native Message create(String json)/*-{
		return eval('(' + json + ')');
	}-*/;
	
}
