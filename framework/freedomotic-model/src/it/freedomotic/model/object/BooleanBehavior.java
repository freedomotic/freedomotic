package it.freedomotic.model.object;


public class BooleanBehavior extends Behavior{

    private boolean value;
    public final static String VALUE_TRUE = "true";
    public final static String VALUE_FALSE = "false";

    public final boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return new Boolean(value).toString();
    }

    public void setValue(boolean inputValue) {
        //activate this behavior if it was unactivated
        setActive(true);
        value = inputValue;
    }


}
