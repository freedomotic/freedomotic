package it.freedomotic.model.object;


public class BooleanBehavior extends Behavior{

    private boolean value;

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
