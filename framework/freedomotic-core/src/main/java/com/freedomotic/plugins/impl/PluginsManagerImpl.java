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
package com.freedomotic.plugins.impl;

import com.freedomotic.api.Client;
import com.freedomotic.api.Plugin;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.exceptions.PluginLoadingException;
import com.freedomotic.i18n.I18n;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.PluginsManager;
import com.freedomotic.reactions.CommandRepository;
import com.freedomotic.reactions.ReactionRepository;
import com.freedomotic.reactions.TriggerRepository;
import com.freedomotic.settings.Info;
import com.freedomotic.util.Unzip;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An helper class that uses an internal DAO pattern to addBoundle plugins of
 * different types from local filesystem
 *
 * @author Enrico Nicoletti
 */
class PluginsManagerImpl implements PluginsManager {

    private static final Logger LOG = LoggerFactory.getLogger(PluginsManager.class.getName());

    // Depedencies
    private final ClientStorage clientStorage;
    private final TriggerRepository triggers;
    private final I18n i18n;
    private final CommandRepository commandRepository;
    private final ReactionRepository reactionRepository;

    @Inject
    Injector injector;

    @Inject
    PluginsManagerImpl(
            ClientStorage clientStorage,
            TriggerRepository triggers,
            CommandRepository commandRepository,
            ReactionRepository reactionRepository,
            I18n i18n) {
        this.clientStorage = clientStorage;
        this.triggers = triggers;
        this.i18n = i18n;
        this.commandRepository = commandRepository;
        this.reactionRepository = reactionRepository;
    }

    /**
     * Loads all plugins of a given type (device, object, event) taken from
     * their default folder.
     *
     * @param TYPE
     * @throws com.freedomotic.exceptions.PluginLoadingException
     */
    @Override
    public void loadAllPlugins(int TYPE) throws PluginLoadingException {
        List<BoundleLoader> boundleLoaders = new BoundleLoaderFactory().getBoundleLoaders(TYPE);

        for (BoundleLoader boundleLoader : boundleLoaders) {
            //a jar package can contain more that one plugin
            loadSingleBundle(boundleLoader);
        }
    }

    /**
     * Loads all plugins from filesystem regardless their type
     *
     * @param TYPE
     * @throws com.freedomotic.exceptions.PluginLoadingException
     */
    @Override
    public void loadAllPlugins() throws PluginLoadingException {
        List<BoundleLoader> boundleLoaders = new ArrayList<BoundleLoader>();
        BoundleLoaderFactory boundleLoaderFactory = new BoundleLoaderFactory();
        boundleLoaders.addAll(boundleLoaderFactory.getBoundleLoaders(TYPE_EVENT));
        boundleLoaders.addAll(boundleLoaderFactory.getBoundleLoaders(TYPE_OBJECT));
        boundleLoaders.addAll(boundleLoaderFactory.getBoundleLoaders(TYPE_DEVICE));

        for (BoundleLoader boundleLoader : boundleLoaders) {
            loadSingleBundle(boundleLoader);
        }
    }

    /**
     * Load a single plugin package from a given directory. This directory
     * should be the root path of the plugin package, not a directory containing
     * more than one plugin package.
     *
     * @param directory
     * @throws PluginLoadingException
     */
    @Override
    public void loadSingleBoundle(File directory) throws PluginLoadingException {
        BoundleLoader pluginPackageDao = new BoundleLoaderFactory().getSingleBoundleLoader(directory);
        loadSingleBundle(pluginPackageDao);
    }

    private void loadSingleBundle(BoundleLoader loader) throws PluginLoadingException {
        List<Client> loaded = loader.loadBoundle();
        //load the package resources or create plugin templates if it's on object plugin
        loadPluginResources(loader.getPath());

        for (Client client : loaded) {
            // Merge the config in PACKAGE file (eg: boundle version) with the plugin specific config
            try {
                client = mergePackageConfiguration(client, loader.getPath());
            } catch (IOException ex) {
                throw new PluginLoadingException("Missing PACKAGE info file " + ex.getMessage(), ex);
            }
            // Now the plugin is fully loaded, take care of initialization and permissions management
            if (client instanceof Plugin) {
                Plugin p = (Plugin) client;
                p.loadPermissionsFromManifest();
                if (p.getConfiguration().getBooleanProperty("enable-i18n", false)) {
                    i18n.registerBundleTranslations(
                            p.getClass().getPackage().getName(),
                            new File(p.getFile().getParentFile() + "/data/i18n"));
                }
            }
            clientStorage.add(client);
        }
    }

    @Override
    public boolean installBoundle(URL fromURL) {

        try {
            String url = fromURL.toString();

            if (url.lastIndexOf("&") > -1) {
                //remove any parameter (starts with '&' char) at the end of url
                url = url.substring(0,
                        url.lastIndexOf('&'));
            }

            String filename = url.substring(url.lastIndexOf('/') + 1);

            //extracts plugin name from zip file name
            String pluginNameRegex = "\\-\\d.\\d.";
            Pattern p = Pattern.compile(pluginNameRegex);
            String[] items = p.split(filename);
            String pluginName = items[0];

            //get the zip from the url and copy in plugin/device folder
            if (filename.endsWith(".device")) {
                File zipFile = new File(Info.PATHS.PATH_DEVICES_FOLDER + "/" + filename);
                FetchHttpFiles.download(fromURL, Info.PATHS.PATH_DEVICES_FOLDER, filename);
                unzipAndDelete(zipFile);
                loadSingleBoundle(new File(Info.PATHS.PATH_DEVICES_FOLDER + "/" + pluginName));
            } else if (filename.endsWith(".object")) {
                FetchHttpFiles.download(fromURL,
                        new File(Info.PATHS.PATH_PLUGINS_FOLDER + "/objects"),
                        filename);

                File zipFile = new File(Info.PATHS.PATH_PLUGINS_FOLDER + "/objects/" + filename);
                unzipAndDelete(zipFile);
                loadSingleBoundle(new File(Info.PATHS.PATH_OBJECTS_FOLDER + "/" + pluginName));
            } else {
                LOG.warn("No installable Freedomotic plugins at URL " + fromURL);
            }
        } catch (Exception ex) {
            LOG.error("Error while installing boundle downloaded from " + fromURL, ex);

            return false; //not done
        }

        return true;
    }

    @Override
    public boolean uninstallBundle(Client client) {
        boolean isDeleted = false;
        if (client instanceof Plugin) {
            Plugin toBeUninstalled = (Plugin) client;
            File boundleRootFolder = toBeUninstalled.getFile().getParentFile();

            // Find boundle companions (they also should be stopped and removed)
            List<Plugin> uninstallCandidates = new ArrayList<Plugin>();
            for (Client tmp : clientStorage.getClients()) {
                if (tmp instanceof Plugin) {
                    Plugin boundleCompanion = (Plugin) tmp;
                    //if this plugin is in the same plugin boundle of the one
                    //the user is trying to uninstall it is an uninstallCandidate
                    if (boundleCompanion.getFile().getParentFile().equals(boundleRootFolder)) {
                        uninstallCandidates.add(boundleCompanion);
                    }
                }
            }

            // Stop, remove and uninstall all plugins in the same boundle as client
            for (Plugin plugin : uninstallCandidates) {
                LOG.info("Uninstalling plugin " + plugin.getName() + " from boundle" + boundleRootFolder.getAbsolutePath());
                plugin.stop();
                clientStorage.remove(plugin);
            }

            // Remove the boundle root folder from filesystem
            try {
                FileUtils.deleteDirectory(boundleRootFolder);
                isDeleted = true;
            } catch (IOException ex) {
                LOG.error("Error while unistalling plugin boundle " + boundleRootFolder.getAbsolutePath(), ex);
            }

        } else {
            LOG.warn("Cannot uninstall " + client.getName() + " it is not a filesystem plugin");
        }

        return isDeleted;
    }

    private boolean unzipAndDelete(File zipFile) {
        LOG.info("Uncompressing plugin archive " + zipFile);

        try {
            Unzip.unzip(zipFile.toString());
        } catch (Exception e) {
            LOG.error("Error while unzipping boundle " + zipFile.getAbsolutePath(), e);

            return false;
        }

        //remove zip file
        try {
            zipFile.delete();
        } catch (Exception e) {
            LOG.info("Unable to delete compressed file " + zipFile.toString());
        }

        return true; //done
    }

    /**
     * Load the resources linked to a plugin package. Note a plugin package may
     * be composed of more than one plugin, so resources must be added just once
     * for every package.
     *
     */
    private void loadPluginResources(File directory)
            throws PluginLoadingException {
        //now loadBoundle data for this jar (can contain more than one plugin)
        //resources are mergend in the default resources folder
        commandRepository.loadCommands(new File(directory + "/data/cmd"));
        triggers.loadTriggers(new File(directory + "/data/trg"));
        reactionRepository.loadReactions(new File(directory + "/data/rea"));

        //create ad-hoc subfolders of temp
        File destination = new File(Info.PATHS.PATH_RESOURCES_FOLDER + "/temp/" + directory.getName());
        destination.mkdir();
        recursiveCopy(new File(directory + "/data/resources"), destination);

        File templatesFolder = new File(directory + "/data/templates/");

        if (templatesFolder.exists()) {
            LOG.info("Loading object templates from {}", templatesFolder.getAbsolutePath());
            //for every envobject class a placeholder is created
            File[] templates
                    = templatesFolder.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return (name.endsWith(".xobj"));
                        }
                    });

            for (File template : templates) {
                Client placeholder;

                try {
                    placeholder = clientStorage.createObjectPlaceholder(template);
                    placeholder = mergePackageConfiguration(placeholder, directory);
                    clientStorage.add(placeholder);
                } catch (RepositoryException ex) {
                    throw new PluginLoadingException("Cannot create object plugin " + "placeholder from template "
                            + template.getAbsolutePath(), ex);
                } catch (IOException ex) {
                    throw new PluginLoadingException("Missing PACKAGE info" + "for template at "
                            + template.getAbsolutePath(), ex);
                }
            }
        } else {
            LOG.debug("No object templates to load from {}", templatesFolder.getAbsolutePath());
        }
    }

    private void recursiveCopy(File source, File target) {
        InputStream input = null;
        OutputStream output = null;

        try {
            if (source.isDirectory()) {
                if (!target.exists()) {
                    target.mkdir();
                }

                String[] children = source.list();

                for (int i = 0; i < children.length; i++) {
                    recursiveCopy(new File(source, children[i]),
                            new File(target, children[i]));
                }
            } else {
                input = new FileInputStream(source);
                output = new FileOutputStream(target);

                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;

                while ((len = input.read(buf)) > 0) {
                    output.write(buf, 0, len);
                }
            }
        } catch (FileNotFoundException foundEx) {
            LOG.debug("No file to copy in " + source);
        } catch (IOException ex) {
            LOG.warn("Error while coping resources", ex);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }

                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
                //second catch block
            }
        }
    }

    private Client mergePackageConfiguration(Client client, File pluginFolder)
            throws IOException {
        //seach for a file called PACKAGE
        Properties packageFile = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(pluginFolder + "/PACKAGE"));
            packageFile.load(fis);
            fis.close();
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        //merges data found in file PACKGE to the the configuration of every single plugin in this package
        client.getConfiguration().setProperty("package.name",
                packageFile.getProperty("package.name"));
        client.getConfiguration().setProperty("package.nodeid",
                packageFile.getProperty("package.nodeid"));
        client.getConfiguration()
                .setProperty("framework.required.version",
                        packageFile.getProperty("framework.required.major") + "."
                        + packageFile.getProperty("framework.required.minor") + "."
                        + packageFile.getProperty("framework.required.build"));
        client.getConfiguration().setProperty("framework.required.major",
                packageFile.getProperty("framework.required.major"));
        client.getConfiguration().setProperty("framework.required.minor",
                packageFile.getProperty("framework.required.minor"));
        client.getConfiguration().setProperty("framework.required.build",
                packageFile.getProperty("framework.required.build"));

        //TODO: add also the other properties
        return client;
    }
}
