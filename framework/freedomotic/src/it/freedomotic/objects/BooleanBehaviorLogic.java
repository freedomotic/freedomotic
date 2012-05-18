package it.freedomotic.objects;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.model.ds.Config;
import it.freedomotic.model.object.BooleanBehavior;

/**
 *
 * @author Enrico
 */
public class BooleanBehaviorLogic implements BehaviorLogic {

    private BooleanBehavior data;
    protected BooleanBehaviorListener listener;

    public BooleanBehaviorLogic(BooleanBehavior pojo) {
        this.data = pojo;
    }

    @Override
    public synchronized final void filterParams(Config params, boolean fireCommand) {
        //filter accepted values
        String parsed = params.getProperty("value").trim();
        if (parsed.equalsIgnoreCase("false") || parsed.equals("0")) {
            if (this.getValue() != false) { //if is really changed
                listener.onFalse(params, fireCommand);
            }
        }
        if (parsed.equalsIgnoreCase("true") || parsed.equals("1")) {
            if (this.getValue() != true) { //if is really changed
                listener.onTrue(params, fireCommand);
            }
        }

        if (parsed.equalsIgnoreCase("opposite")) {
            opposite(params, fireCommand);
        }
        if (parsed.equalsIgnoreCase("next")) {
            opposite(params, fireCommand);
        }
        if (parsed.equalsIgnoreCase("previous")) {
            opposite(params, fireCommand);
        }
    }

    private void opposite(Config params, boolean fireCommand) {
        if (data.getValue() == true) {
            if (this.getValue() != false) { //if is really changed
                listener.onFalse(params, fireCommand);
            }
        } else {
            if (this.getValue() != true) { //if is really changed
                listener.onTrue(params, fireCommand);
            }
        }
    }

    public void addListener(BooleanBehaviorListener booleanBehaviorListener) {
        listener = booleanBehaviorListener;
    }

    @Override
    public String getName() {
        return data.getName();
    }

    public void setValue(boolean b) {
        data.setValue(b);
    }

    public boolean getValue() {
        return data.getValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BooleanBehaviorLogic other = (BooleanBehaviorLogic) obj;
        if (this.data != other.data && (this.data == null || !this.data.equals(other.data))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (this.data != null ? this.data.hashCode() : 0);
        return hash;
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
