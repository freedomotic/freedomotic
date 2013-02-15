package it.freedomotic.gwtclient.client.widgets;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;


public class RoomDetailDialog extends DialogBox {
	private static int BORDER_X = 10; // the empty space around the map
	private static int BORDER_Y = 10; // the empty space around the map

	private static int CANVAS_WIDTH = 600 + (BORDER_X * 2);
	private static int CANVAS_HEIGHT = 400 + (BORDER_Y * 2);
	private VerticalPanel verticalPanel;
	private RoomDetailWidget roomDetail;
	public RoomDetailDialog(String roomID){
		
	setAutoHideEnabled(true);
	setText(roomID);
	setPopupPosition(200, 200);
	setWidth(CANVAS_WIDTH + "px");
	setHeight(CANVAS_HEIGHT + "px");
	verticalPanel = new VerticalPanel();
	setWidget(verticalPanel);
	roomDetail = new RoomDetailWidget(roomID);
	
	final Timer timer = new Timer() {
		@Override
		public void run() {
			verticalPanel.clear();
			verticalPanel.add(roomDetail.getCanvas());
		}
	};
	timer.scheduleRepeating(5000);
	
	}
}
