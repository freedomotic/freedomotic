package it.freedomotic.model.object;

/**
 *
 * @author Enrico
 */

public class RangedIntBehavior extends Behavior {

    private static final long serialVersionUID = 6390384029652176632L;
	
	private int value;
    private int max;
    private int min;
    private int scale;
    private int step;

    @Override
    public String toString() { 
        if (scale == 1)
            return new Integer(value).toString();
        return new Double((double)value/(double)getScale()).toString();        
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
    
    public int getScale() {
        if (scale<=0)
            scale = 1;
        return scale;
    }        
 
    public void setValue(int inputValue) {
        //activate this behavior if it was unactivated
        this.setActive(true);
        value = inputValue;
    }
}
