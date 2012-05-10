
package it.freedomotic.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author Enrico
 */
public class XmlFilter implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
        return (name.endsWith(".xml"));
    }
}
