package it.freedomotic.plugins;

import java.io.File;

/**
 *
 * @author Enrico
 */
public interface AddonLoaderInterface {
       abstract void load(AddonLoader manager, File path);
}
