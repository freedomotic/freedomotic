package it.freedomotic.model.object;

import java.util.ArrayList;

/**
 *
 * @author Enrico
 */
public class ListBehavior extends Behavior {

    private int selected;
    private ArrayList<String> list = new ArrayList<String>();

    public void add(String key) {
        list.add(key);
    }

    public void remove(String key) {
        list.remove(key);
    }

    public boolean contains(String key) {
        return list.contains(key);
    }
    
    public String getSelected() {
        return (String) list.get(selected);
    }

    public ArrayList<String> getList() {
        return list;
    }

    public boolean setSelected(String key) {
        if (list.contains(key)) {
            selected = list.indexOf(key);
            return true;
        }
        return false;
    }

    public int getItemsNumber(){
        return list.size();
    }

    public int indexOf(String key){
        return list.indexOf(key);
    }

    public int indexOfSelection(){
        return selected;
    }

    public String get(int index){
        return list.get(index);
    }

    @Override
    public String toString() {
        return list.get(selected).toString();
    }
}
