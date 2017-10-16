/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.core;

import com.freedomotic.app.Freedomotic;
import com.freedomotic.settings.Info;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.imageio.ImageIO;

/**
 * Class responsible for adding and finding resources.
 *
 * @author Enrico Nicoletti
 */
public final class ResourcesManager {

    private static final Logger LOG = LoggerFactory.getLogger(ResourcesManager.class.getName());
    private static final LoadingCache<String, BufferedImage> imagesCache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .weakValues()
            .build(new ImageCacheLoader());

    /**
     * Gets image resource given by name and performs resizing to requested width and height.
     *
     * @param imageName the name of the image
     * @param width requested width of the image, if <= 0 then no resizing is performed
     * @param height requested height of the image, if <= 0 then no resizing is performed
     * @return image data or null when cannot find image by name of some error occurred
     */
    public static BufferedImage getResource(String imageName, int width, int height) {
        try {
            // Resizeing not needed, return standard image
            if ((width <= 0) || (height <= 0)) {
                return imagesCache.get(imageName);
            }
            // Compute the resized image key
            String resizedImageName = imageName + "_" + width + "x" + height;
            BufferedImage img = imagesCache.getIfPresent(resizedImageName.toLowerCase());
            // Resized image is in cache
            if (img != null) {
                return img;
            } else {
                img = getUnresizedImageFromCacheOrDisk(imageName);
                if (img == null) {
                    return null;
                }
                // Resize and cache
                if (img.getWidth() != width && img.getHeight() != height) {
                    img = resizeImage(img, width, height);
                }
                imagesCache.put(resizedImageName.toLowerCase(), img);
                return img;
            }
        } catch (ExecutionException ex) {
            LOG.error(ex.getMessage());
            return null;
        }
    }

    private static BufferedImage getUnresizedImageFromCacheOrDisk(final String imageName) {
        try {
            return imagesCache.get(imageName);
        } catch (ExecutionException e) {
            return getUnresizedImageFromDisk(imageName);
        }
    }

    private static BufferedImage getUnresizedImageFromDisk(final String imageName) {
        try {
            File imageFile = new File(imageName);
            if (imageFile.exists()) {
                BufferedImage img = ImageIO.read(imageFile);
                imagesCache.put(imageFile.getName(), img);
                return img;
            } else {
                return null;
            }
        } catch (IOException er) {
            LOG.error(Freedomotic.getStackTraceInfo(er));
            return null;
        }
    }

    /**
     * Gets image resource given by name.
     *
     * @param imageName the name of the image
     * @return image data or null when cannot find image by name of some error occurred
     */
    public static synchronized BufferedImage getResource(String imageName) {
        try {
            return imagesCache.get(imageName);
        } catch (ExecutionException ex) {
            LOG.error(ex.getMessage());
            return null;
        }
    }

    /**
     * Gets file given by file name seeking recursively inside folder and subfolders.
     *
     * @param folder the folder where searching for a file is started
     * @param fileName the name of file to search
     * @return file or null when cannot find
     */
    public static File getFile(File folder, String fileName) {
        DirectoryReader dirReader = new DirectoryReader();
        dirReader.find(folder, fileName);
        return dirReader.getFile();
    }

    /**
     * Adds image to {@link ResourcesManager} under the name given by imageName.
     *
     * @param imageName the name of the image resource
     * @param image image data
     */
    public static synchronized void addResource(String imageName, BufferedImage image) {
        imagesCache.put(imageName, image);
    }

    /**
     * Removes any cached element.
     *
     */
    public static void clear() {
        imagesCache.cleanUp();
    }

    private static BufferedImage resizeImage(BufferedImage image, int width, int height) {
        int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType();
        BufferedImage resizedImage = new BufferedImage(width, height, type);
        Graphics2D tmp = resizedImage.createGraphics();
        tmp.setComposite(AlphaComposite.Src);
        tmp.drawImage(image, 0, 0, width, height, null);
        return resizedImage;
    }

    /**
     * Helper class that allows to recursively seeking for a file.
     */
    private static class DirectoryReader {

        private File output = null;

        private void find(File folder, String fileName) {
            if (folder.isFile()) {
                if (folder.getName().equalsIgnoreCase(fileName)) {
                    output = folder; //the fileName is found
                }
            } else if (folder.isDirectory()) { //a subdirectory
                File[] listOfFiles = folder.listFiles();
                if (listOfFiles != null) {
                    for (File file : listOfFiles) {
                        find(file, fileName);
                    }
                }
            }
        }

        /**
         * Gets the result of seeking the file.
         *
         * @return file or null when file cannot be found
         */
        public File getFile() {
            return output;
        }
    }

    /**
     * Helper class that contains a method used when trying to access resource that is not available in
     * {@link ResourcesManager} cache.
     */
    private static class ImageCacheLoader extends CacheLoader<String, BufferedImage> {
        @Override
        public BufferedImage load(String imageName) throws Exception {
            //loads image from disk searching it recursively in a given folder
            BufferedImage img = fetchFromHDD(Info.PATHS.PATH_RESOURCES_FOLDER, imageName);
            if (img != null) {
                return img;
            } else {
                throw new IOException("Cannot recursively find image " + imageName + " in " + Info.PATHS.PATH_RESOURCES_FOLDER);
            }
        }

        /**
         * Fetches image from HDD.
         *
         * @param folder the start folder when searching for a image should start
         * @param imageName the name of the image
         * @return image data
         * @throws IOException
         */
        private BufferedImage fetchFromHDD(File folder, String imageName) throws IOException {
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
    }

    private ResourcesManager() {
    }
}
