package it.freedomotic.gwtclient.client.api;


import com.furiousbob.jms.client.Message;
import com.furiousbob.jms.client.MessageListener;
import com.furiousbob.jms.client.StompClient;
import com.google.gwt.core.client.GWT;

public class ACStompListener implements MessageListener {

	private StompClient sc;

	public ACStompListener(StompClient sc) {
		this.sc = sc;
		GWT.log("messageListener sc: " + this.sc);
		//Window.alert("messageListener sc: " + this.sc);
	}

	@Override
	public void onMessage(Message message) {
		GWT.log("Received message: " + message);		
		EnvironmentsController.message(message.getBody());
	}

}
