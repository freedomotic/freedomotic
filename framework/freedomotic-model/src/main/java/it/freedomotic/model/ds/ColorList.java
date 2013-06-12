package it.freedomotic.model.ds;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

public class ColorList {

    static ArrayList<Color> colors = new ArrayList<Color>();
    static int last = 0;

    public ColorList() {
        colors.add(Color.red);
        colors.add(Color.green);
        colors.add(Color.blue);
        colors.add(Color.magenta);
        colors.add(Color.orange);
        colors.add(Color.pink);
        colors.add(Color.yellow);
    }

    public static Color getRandom() {
        Random rand = new Random();
        return (new Color(rand.nextInt(256),
                rand.nextInt(256),
                rand.nextInt(256)));
    }

    public static Color getNext() {
            if (last >= colors.size()) {
                last = 0;
            }
            Color c = colors.get(last);
            last++;
            return c;
    }
}
