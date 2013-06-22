package it.freedomotic.model.object;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Enrico
 */
public class MultiselectionListBehavior extends Behavior {

    private static final long serialVersionUID = -7839150128393354068L;
	
	private final ArrayList<String> list = new ArrayList<String>();
    private final List<String> selected = new ArrayList<String>();

    public void add(String key) {
        list.add(key);
    }

    public void remove(String key) {
        list.remove(key);
    }

    public boolean contains(String key) {
        return list.contains(key);
    }

    public boolean isSelected(String key) {
        return selected.contains(key);
    }

    public boolean isEnlisted(String key) {
        return list.contains(key);
    }

    public List<String> getSelected() {
        List<String> tmp = new ArrayList<String>();
        for (String item : list) {
            if (selected.contains(item)) {
                //is selected
                tmp.add(item);
            }
        }
        return tmp;
    }

    public void setSelected(String key) {
        if (list.contains(key) && !selected.contains(key)) {
            selected.add(key);
        }
    }

    public void setUnselected(String key) {
        if (list.contains(key) && selected.contains(key)) {
            selected.remove(key);
        }
    }

    public int indexOfSelection() {
        int selection = -1;
        if (selected.get(0) != null) {
            selection = list.indexOf(selected.get(0));
        }
        return Math.max(0, selection);
    }

    public int getItemsNumber() {
        return list.size();
    }

    public String get(int index) {
        return list.get(index);
    }

    @Override
    public String toString() {
        return list.size() + " items (" + selected.size() + " selected)";
    }

    public List<String> getList() {
        return list;
    }
}
