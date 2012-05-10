package it.freedomotic.util;

import java.nio.channels.*;
import java.io.*;

/*
 * All creatits to kingv
 *  this code is taken from
 * http://www.hwupgrade.it/forum/archive/index.php/t-992627.html
 */
public class CopyFile {

    public static void copy(File sfile, File dfile) throws Exception {
        FileChannel source = new FileInputStream(sfile).getChannel();
        FileChannel dest = new FileOutputStream(dfile).getChannel();
        source.transferTo(0, source.size(), dest);
        source.close();
        dest.close();
    }


    private CopyFile() {
    }
}
