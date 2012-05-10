/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.util;

import java.io.File;
import java.io.IOException;

/**
 * Builds a relative URL path from current directory to the base directory
 * @author Enrico
 */
public class PathFinder {

    /**
     * Builds a relative path to the given base path.
     * @param base - the path used as the base
     * @param path - the path to compute relative to the base path
     * @return A relative path from base to path
     * @throws IOException
     */
    public static File findRelativePath(String base, String path)
            throws IOException {
        String a = new File(base).getCanonicalFile().toURI().getPath();
        String b = new File(path).getCanonicalFile().toURI().getPath();
        String[] basePaths = a.split("/");
        String[] otherPaths = b.split("/");
        int n = 0;
        for (; n < basePaths.length && n < otherPaths.length; n++) {
            if (basePaths[n].equals(otherPaths[n]) == false) {
                break;
            }
        }
        //Freedomotic.logger.info("Common length: " + n);
        StringBuilder tmp = new StringBuilder("../");
        for (int m = n; m < basePaths.length - 1; m++) {
            tmp.append("../");
        }
        for (int m = n; m < otherPaths.length; m++) {
            tmp.append(otherPaths[m]);
            tmp.append("/");
        }

        return new File(tmp.toString());
    }
}
