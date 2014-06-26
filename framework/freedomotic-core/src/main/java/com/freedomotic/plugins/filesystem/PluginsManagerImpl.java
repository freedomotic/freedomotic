/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.filesystem;

import com.freedomotic.api.Client;
import com.freedomotic.api.Plugin;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.DaoLayerException;
import com.freedomotic.exceptions.PluginLoadingException;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.reactions.CommandPersistence;
import com.freedomotic.reactions.ReactionPersistence;
import com.freedomotic.reactions.TriggerPersistence;
import com.freedomotic.util.FetchHttpFiles;
import com.freedomotic.util.Info;
import com.freedomotic.util.Unzip;
import com.google.inject.Inject;
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
import java.util.logging.Logger;

/**
 * An helper class that uses an internal DAO pattern to loadBoundle plugins of
 * different types from local filesystem
 *
 * @author enrico
 */
public class PluginsManagerImpl implements PluginsManager {
    //depedencies

    private ClientStorage clientStorage;
    private TriggerPersistence triggers;

    @Inject
    PluginsManagerImpl(ClientStorage clientStorage, TriggerPersistence triggers) {
        this.clientStorage = clientStorage;
        this.triggers = triggers;
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
        boundleLoaders.addAll(boundleLoaderFactory.getBoundleLoaders(TYPE_DEVICE));
        boundleLoaders.addAll(boundleLoaderFactory.getBoundleLoaders(TYPE_OBJECT));
        boundleLoaders.addAll(boundleLoaderFactory.getBoundleLoaders(TYPE_EVENT));

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
            try {
                client = mergePackageConfiguration(client, loader.getPath());
            } catch (IOException ex) {
                throw new PluginLoadingException("Missing PACKAGE info file " + ex.getMessage(), ex);
            }
            if (client instanceof Plugin) {
                Plugin p = (Plugin) client;
                p.loadPermissionsFromManifest();
                if (p.getConfiguration().getBooleanProperty("enable-i18n", false)) {
                    p.getApi().getI18n().registerPluginBundleDir(p);
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
            String pluginName = filename.substring(0,
                    filename.indexOf("-"));

            //get the zip from the url and copy in plugin/device folder
            if (filename.endsWith(".device")) {
                File zipFile = new File(Info.PATHS.PATH_DEVICES_FOLDER + "/" + filename);
                FetchHttpFiles.download(fromURL, Info.PATHS.PATH_DEVICES_FOLDER, filename);
                unzipAndDelete(zipFile);
                loadSingleBoundle(new File(Info.PATHS.PATH_DEVICES_FOLDER + "/" + pluginName));
            } else {
                if (filename.endsWith(".object")) {
                    FetchHttpFiles.download(fromURL,
                            new File(Info.PATHS.PATH_PLUGINS_FOLDER + "/objects"),
                            filename);

                    File zipFile = new File(Info.PATHS.PATH_PLUGINS_FOLDER + "/objects/" + filename);
                    unzipAndDelete(zipFile);
                    loadSingleBoundle(new File(Info.PATHS.PATH_OBJECTS_FOLDER + "/" + pluginName));
                } else {
                    LOG.warning("No installable Freedomotic plugins at URL " + fromURL);
                }
            }
        } catch (Exception ex) {
            LOG.severe(Freedomotic.getStackTraceInfo(ex));

            return false; //not done
        }

        return true;
    }

    private boolean unzipAndDelete(File zipFile) {
        LOG.info("Uncompressing plugin archive " + zipFile);

        try {
            Unzip.unzip(zipFile.toString());
        } catch (Exception e) {
            LOG.severe(Freedomotic.getStackTraceInfo(e));

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
     * be composed of more than one plugin, so resources must be loaded just
     * once for every package.
     *
     */
    private void loadPluginResources(File directory)
            throws PluginLoadingException {
        //now loadBoundle data for this jar (can contain more than one plugin)
        //resources are mergend in the default resources folder
        CommandPersistence.loadCommands(new File(directory + "/data/cmd"));
        triggers.loadTriggers(new File(directory + "/data/trg"));
        ReactionPersistence.loadReactions(new File(directory + "/data/rea"));

        //create ad-hoc subfolders of temp
        File destination = new File(Info.PATHS.PATH_RESOURCES_FOLDER + "/temp/" + directory.getName());
        destination.mkdir();
        recursiveCopy(new File(directory + "/data/resources"),
                destination);

        File templatesFolder = new File(directory + "/data/templates/");

        if (templatesFolder.exists()) {
            //for every envobject class a placeholder is created
            File[] templates =
                    templatesFolder.listFiles(new FilenameFilter() {
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
                } catch (DaoLayerException ex) {
                    throw new PluginLoadingException("Cannot create object plugin " + "placeholder from template "
                            + template.getAbsolutePath(), ex);
                } catch (IOException ex) {
                    throw new PluginLoadingException("Missing PACKAGE info" + "for template at "
                            + template.getAbsolutePath(), ex);
                }
            }
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
            LOG.config("No file to copy in " + source);
        } catch (IOException ex) {
            LOG.warning(ex.getMessage());
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
    private static final Logger LOG = Logger.getLogger(PluginsManager.class.getName());
}
