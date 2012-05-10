package it.freedomotic.model.object;

/**
 *
 * @author Enrico
 */

public class RangedIntBehavior extends Behavior {

    private int value;
    private int max;
    private int min;
    private int step;

    @Override
    public String toString() {
        return new Integer(value).toString();
    }

    public int getValue() {
        return value;
    }

    public int getStep() {
        return step;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

 
    public void setValue(int inputValue) {
        //activate this behavior if it was unactivated
        this.setActive(true);
        value = inputValue;
    }
}
