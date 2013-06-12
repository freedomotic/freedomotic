/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.core;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.util.Info;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.imageio.ImageIO;

/**
 *
 * @author Enrico
 */
public final class ResourcesManager {

    private static final HashMap<String, BufferedImage> CACHE = new HashMap<String, BufferedImage>();

    public static BufferedImage getResource(String imageName, int width, int height) {
        String resizedImageName = imageName + "_" + width + "x" + height;
        if (!(width > 0) || !(height > 0)) { //not needs resizeing
            return getResource(imageName);
        }
        BufferedImage img = CACHE.get(resizedImageName.toLowerCase());
        if (img == null) { //img not in cache
            try {
                //loads image from disk searching it recursively in folder
                img = fetchFromHDD(new File(Info.getResourcesPath()), imageName);
            } catch (IOException e) {
                Freedomotic.logger.warning("No image " + imageName + " found recursively in " + new File(Info.getResourcesPath()).getPath());
            }
            if (img != null) {
                //img loaded from disk. Now it is cached resized
                img = resizeImage(img, width, height);
                CACHE.put(resizedImageName.toLowerCase(), img);
                return img;
            }
        } else {
            return img; //return the already cached image
        }
        return null; //an error
    }

    public static synchronized BufferedImage getResource(String imageName) {
        BufferedImage img = CACHE.get(imageName.toLowerCase());
        if (img == null) { //img not in cache
            try {
                //loads image from disk searching it recursively in folder
                img = fetchFromHDD(new File(Info.getResourcesPath()), imageName);
            } catch (IOException e) {
                Freedomotic.logger.warning("No image " + imageName + " found recursively in " + new File(Info.getResourcesPath()).getPath());
            }
            if (img != null) {
                //img succesfully loaded from disk. Now it is cached
                CACHE.put(imageName.toLowerCase(), img);
                return img;
            }
        } else {
            return img; //return the already cached image
        }
        return null; //not cached and not loaded from hdd
    }

    public static File getFile(File folder, String fileName) {
        DirectoryReader dirReader = new DirectoryReader();
        dirReader.find(folder, fileName);
        return dirReader.getFile();
    }

    public static synchronized void addResource(String imageName, BufferedImage image) {
        CACHE.put(imageName, image);
    }

    private static BufferedImage resizeImage(BufferedImage image, int width, int height) {
        int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType();
        BufferedImage resizedImage = new BufferedImage(width, height, type);
        Graphics2D tmp = resizedImage.createGraphics();
        tmp.setComposite(AlphaComposite.Src);
        tmp.drawImage(image, 0, 0, width, height, null);
        return resizedImage;
    }

    private static BufferedImage fetchFromHDD(File folder, String imageName) throws IOException {
        BufferedImage img = null;
        try {
            DirectoryReader dirReader = new DirectoryReader();
            dirReader.find(folder, imageName);
            File file = dirReader.getFile();
            if (file != null) {
                img = ImageIO.read(file);
            }
        } catch (IOException ex) {
            throw new IOException();
        }
        return img;
    }

    public static void clear() {
        CACHE.clear();
    }

    private static class DirectoryReader {

        File output = null;

        void find(File folder, String fileName) {
            if (folder.isFile()) {
                if (folder.getName().equalsIgnoreCase(fileName)) {
                    output = folder; //the fileName is found
                }
            } else if (folder.isDirectory()) { //a subdirectory
                File[] listOfFiles = folder.listFiles();
                if (listOfFiles != null) {
                    for (int i = 0; i < listOfFiles.length; i++) {
                        find(listOfFiles[i], fileName);
                    }
                }
            }
        }

        public File getFile() {
            return output;
        }
    }

    private ResourcesManager() {
    }
}
