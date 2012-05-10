package it.freedomotic.objects;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.model.ds.Config;
import it.freedomotic.model.object.RangedIntBehavior;

/**
 *
 * @author Enrico
 */
public class RangedIntBehaviorLogic implements BehaviorLogic {

    private RangedIntBehavior data;
    protected RangedIntBehaviorListener listener;

    public RangedIntBehaviorLogic(RangedIntBehavior pojo) {
        this.data = pojo;
    }

    public void addListener(RangedIntBehaviorListener listener) {
        this.listener = listener;
    }

    @Override
    public String toString() {
        return getName() + ": " + getValue();
    }

    public int getValue() {
        return data.getValue();
    }

    public int getStep() {
        return data.getStep();
    }

    public int getMax() {
        return data.getMax();
    }

    public int getMin() {
        return data.getMin();
    }

    @Override
    public void filterParams(Config params, boolean fireCommand) {
        //from dim to dim
        String input = params.getProperty("value").trim();
        int parsed = getMin();
        try {
            if (input.startsWith("+")) {
                parsed = getValue() + Integer.parseInt(input.replace("+", "")); //eliminate the + and sum the new value
            } else {
                if (input.startsWith("-")) {
                    parsed = getValue() - Integer.parseInt(input.replace("-", ""));  //eliminate the - and subtact the new value
                } else {
                    parsed = (int) Double.parseDouble(input); //takes doubles and integers. Doubles are truncated to standard int values
                }
            }
        } catch (NumberFormatException numberFormatException) {
            Freedomotic.logger.warning("Paramenter 'value = " + params.getProperty("value").trim() + "' in " + this.getName() + " behavior is not an integer.");
        }
        if (input.equalsIgnoreCase("next")) {
            parsed = getValue() + getStep();
        }
        if (input.equalsIgnoreCase("previous")) {
            parsed = getValue() - getStep();
        }
        if (input.equalsIgnoreCase("opposite")) {
            //opposite value not allowed for this behavior. Inform the user.
        }
        performValueChange(parsed, params, fireCommand);
    }

    private void performValueChange(int tmpValue, Config params, boolean fireCommand) {
        if (getValue() != tmpValue) {
            if (tmpValue <= getMin()) {
                params.setProperty("value", Integer.valueOf(getMin()).toString());
                listener.onLowerBoundValue(params, fireCommand);
            } else {
                if (tmpValue >= getMax()) {
                    params.setProperty("value", new Integer(getMax()).toString());
                    listener.onUpperBoundValue(params, fireCommand);
                } else {
                    listener.onRangeValue(tmpValue, params, fireCommand);
                }
            }
        }
    }

    public void setValue(int inputValue) {
        data.setValue(inputValue);
    }

    @Override
    public String getName() {
        return data.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RangedIntBehaviorLogic other = (RangedIntBehaviorLogic) obj;
        if (this.data != other.data && (this.data == null || !this.data.equals(other.data))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.data != null ? this.data.hashCode() : 0);
        return hash;
    }

    @Override
    public String getValueAsString() {
        return data.toString();
    }

    @Override
    public boolean isActive() {
        return data.isActive();
    }
}
