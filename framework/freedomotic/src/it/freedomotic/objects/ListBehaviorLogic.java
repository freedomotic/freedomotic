package it.freedomotic.objects;

import it.freedomotic.model.ds.Config;
import it.freedomotic.model.object.ListBehavior;
import java.util.ArrayList;

/**
 * This behavior accepts a string which is an element of the list or "next" or
 * "previous" as input params. The selectedChanged is called only if the request
 * is valid (new selection is a value in the list) and if is not the current
 * selected value.
 *
 * @author Enrico
 */
public class ListBehaviorLogic implements BehaviorLogic {

    private ListBehavior data;
    private Listener listener;

    public interface Listener {

        public void selectedChanged(final Config params, boolean fireCommand);
    }

    public ListBehaviorLogic(ListBehavior pojo) {
        this.data = pojo;
    }

    @Override
    public synchronized final void filterParams(Config params, boolean fireCommand) {
        //value contains the sting used in user level commands like object="tv" behavior="inputs" value="hdmi1"
        //we have to check if value is a suitable choice according to a list of possibilities (check if it exists)
        String parsed = params.getProperty("value").trim();

        if (parsed.equalsIgnoreCase("next")) {
            next(params, fireCommand);
        }
        if (parsed.equalsIgnoreCase("previous")) {
            previous(params, fireCommand);
        }

        if (!parsed.equalsIgnoreCase(data.getSelected())) {
            if (data.contains(parsed)) {
                //notify the user wants to use another value from the list
                listener.selectedChanged(params, fireCommand);
            }
        }
    }

    private void next(Config params, boolean fireCommand) {
        int index = (data.indexOfSelection() + 1) % data.getItemsNumber();
        params.setProperty("value", data.get(index));
        listener.selectedChanged(params, fireCommand);
    }

    private void previous(Config params, boolean fireCommand) {
        int index = data.indexOfSelection() - 1;
        if (index < 0) {
            //index is negative for sure so we have to add it not substract
            index = data.getItemsNumber() + index;
        }
        params.setProperty("value", data.get(index));
        listener.selectedChanged(params, fireCommand);
    }

    public void addListener(Listener listBehaviorListener) {
        listener = listBehaviorListener;
    }

    @Override
    public String getName() {
        return data.getName();
    }

    public String getSelected() {
        return data.getSelected();
    }

    public boolean setSelected(String key) {
        return data.setSelected(key);
    }

    public ArrayList<String> getValuesList() {
        return data.getList();
    }

    @Override
    public boolean isActive() {
        return data.isActive();
    }

    @Override
    public String getValueAsString() {
        return data.toString();
    }
}
