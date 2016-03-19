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
/* * This Program is free software; you can redistribute it and/or modify

 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.core;

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
 *
 * @author Enrico Nicoletti
 */
public final class ResourcesManager {

    // private static final Map<String, BufferedImage> CACHE = new HashMap<String, BufferedImage>();
    private static final Logger LOG = LoggerFactory.getLogger(ResourcesManager.class.getName());
    private static final LoadingCache<String, BufferedImage> imagesCache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .weakValues()
            .build(new CacheLoader<String, BufferedImage>() {
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
            });

    /**
     *
     * @param imageName
     * @param width
     * @param height
     * @return
     */
    public static BufferedImage getResource(String imageName, int width, int height) {
        try {
            // Resizeing not needed, return standard image
            if (!(width > 0) || !(height > 0)) {
                return imagesCache.get(imageName);
            }
            // Compute the resized image key
            String resizedImageName = imageName + "_" + width + "x" + height;
            BufferedImage img = imagesCache.getIfPresent(resizedImageName.toLowerCase());
            // Resized image is in cache
            if (img != null) {
                return img;
            } else {
                //get the unresized image from cache or disk
                try {
                    img = imagesCache.get(imageName);
                } catch (Exception e) {
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

    /**
     *
     * @param imageName
     * @return
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
     *
     * @param folder
     * @param fileName
     * @return
     */
    public static File getFile(File folder, String fileName) {
        DirectoryReader dirReader = new DirectoryReader();
        dirReader.find(folder, fileName);
        return dirReader.getFile();
    }

    /**
     *
     * @param imageName
     * @param image
     */
    public static synchronized void addResource(String imageName, BufferedImage image) {
        imagesCache.put(imageName, image);
    }

    /**
     * Removes any cached element
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
