package it.freedomotic.gwtclient.client.widgets;

import sun.security.jca.GetInstance;
import it.freedomotic.gwtclient.client.api.EnvironmentsController;
import it.freedomotic.model.environment.Environment;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.ListBox;

public class EnvListBox extends ListBox {

    private FloorPlanWidget parent;
    private boolean dataInitialized = false;

    public EnvListBox(final FloorPlanWidget parent) {
        this.parent = parent;
        // setup timer
        final Timer timer = new Timer() {
            @Override
            public void run() {
                if (!dataInitialized) {
                    init();
                }
            }
        };
        timer.scheduleRepeating(1000);
    }

    public void init() {
        if (EnvironmentsController.getInstance().HasData()) {
            for (Environment env : EnvironmentsController.getInstance().getEnvironments()) {
                addItem(env.getName(), env.getUUID());
            }
            addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    // TODO Auto-generated method stub
                    if (getSelectedIndex() != -1) {
                        String val = getValue(getSelectedIndex());
                        parent.setEnvironment(val);
                    }
                }
            });
            dataInitialized = true;
        }
    }
}