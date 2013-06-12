package it.freedomotic.service;

import it.freedomotic.util.JarFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.io.File;
import java.io.IOException;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Allows programs to modify the classpath during runtime.
 */
public class ClassPathUpdater {

    /**
     * Used to find the method signature.
     */
    private static final Class[] PARAMETERS = new Class[]{URL.class};
    /**
     * Class containing the private addURL method.
     */
    private static final Class<?> CLASS_LOADER = URLClassLoader.class;

    /**
     * Adds a new path to the classloader. If the given string points to a file,
     * then that file's parent file (i.e., directory) is used as the directory
     * to add to the classpath. If the given string represents a directory, then
     * the directory is directly added to the classpath.
     *
     * @param s The directory to add to the classpath (or a file, which will
     * relegate to its directory).
     */
    public static void add(String s)
            throws IOException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        add(new File(s));
    }

    /**
     * Adds a new path to the classloader. If the given file object is a file,
     * then its parent file (i.e., directory) is used as the directory to add to
     * the classpath. If the given string represents a directory, then the
     * directory it represents is added.
     *
     * @param f The directory (or enclosing directory if a file) to add to the
     * classpath.
     */
    public static void add(File f)
            throws IOException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        // f = f.isDirectory() ? f : f.getParentFile();
        if (f.isDirectory()) {
            File[] jarList = f.listFiles(new JarFilter());
            //the list of files in the jar
            for (File jar : jarList) {
                if (jar.isFile()) {
                    add(jar.toURI().toURL());
                }
            }
        } else {
            add(f.toURI().toURL());
        }
    }

    /**
     * Adds a new path to the classloader. The class must point to a directory,
     * not a file.
     *
     * @param url The path to include when searching the classpath.
     */
    public static void add(URL url)
            throws IOException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        Method method = CLASS_LOADER.getDeclaredMethod("addURL", PARAMETERS);
        method.setAccessible(true);
        method.invoke(getClassLoader(), new Object[]{url});
    }

    private static URLClassLoader getClassLoader() {
        return (URLClassLoader) ClassLoader.getSystemClassLoader();
    }

    private ClassPathUpdater() {
    }
}