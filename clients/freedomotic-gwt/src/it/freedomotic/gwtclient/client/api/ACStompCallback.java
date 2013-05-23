package it.freedomotic.gwtclient.client.api;

import com.furiousbob.jms.client.ConnectionCallback;
import com.furiousbob.jms.client.StompClient;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class ACStompCallback implements ConnectionCallback {

    private StompClient sc;
    private boolean connected = false;

    @Override
    public void onConnect() {
        GWT.log("STOMP: onConnect");
        //Window.alert("STOMP: onConnect");
        connected = true;
        subscribe();
    }

    public boolean isConnected() {
        return connected;

    }

    @Override
    public void onError(String cause) {
        GWT.log("ERROR STOMP: " + cause, new Throwable(cause));
        //Window.alert("ERROR STOMP: " + cause);
        if (sc != null) {
            Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
                @Override
                public boolean execute() {
                    GWT.log("STOMP retry...");
                    //Window.alert("STOMP retry...");
                    sc.connect();
                    return false;
                }
            }, 30000);
        }
    }

    @Override
    public void onDisconnect() {
        connected = false;
        GWT.log("STOMP: onDisconnect, will use " + sc + " to reconnect...");
        //Window.alert("STOMP: onDisconnect, will use " + sc + " to reconnect...");
    }

    public void setClient(StompClient sc) {
        // store client for reconnect attempt
        this.sc = sc;
    }

    public void subscribe() {
        String q = "/topic/VirtualTopic.app.event.sensor.object.behavior.change";
        //String q= "/queue/test";		
        if (q != null) {
            JSONObject header = new JSONObject();
            header.put("transformation", new JSONString("jms-object-xml"));
            sc.subscribe(q, new ACStompListener(sc), header.getJavaScriptObject());
            //sc.subscribe(q, new ACStompListener(sc));
            GWT.log("subscribed to queue " + q);
            //Window.alert("subscribed to queue " + q);

        }
    }
}
