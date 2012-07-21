package com.furiousbob.jms.client;
/**
 * 
 * @author Vinicius Carvalho
 *
 */
public interface ConnectionCallback {
	/**
	 * Called on connection to the JMS broker
	 */
	void onConnect();
	/**
	 * Called on an error or disconnection from the broker
	 * @param cause - the reason behind the error
	 */
	void onError(String cause);
	
	/**
	 * Called on a disconnection initiated from the client
	 */
	void onDisconnect();
}
