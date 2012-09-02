/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.plugins.video.VideoPlayerConfig;
import it.freedomotic.reactions.Command;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Enrico Nicoletti
 */
public class VideoPlayer extends Protocol {

    static Process p;

    public VideoPlayer() {
        super("VLC Media Player", "/it.nicoletti.media/vlc-media-player.xml");
    }

    @Override
    public void onShowGui() {
        bindGuiToPlugin(new VideoPlayerConfig(this));
    }

    public void play(String url) throws IOException {
        File path = new File(configuration.getStringProperty("vlc-bin", "/usr/bin/cvlc"));
        String[] parameters = new String[3];
        //path to vlc executable
        parameters[0] = path.getAbsolutePath();
        //path to file
        try {
            parameters[1] = new URI("file", "", url, "").toString();
        } catch (URISyntaxException ex) {
            Logger.getLogger(VideoPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        //vlc options
        parameters[2] = "--fullscreen";
        p = Runtime.getRuntime().exec(parameters);
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        if (!isRunning()) {
            start();
        }
        String url = c.getProperty("url");
        String close = c.getProperty("close");
        if (close == null) {
            close = "no";
        }
        if (close.equalsIgnoreCase("yes")) {
            try {
                p.destroy();
            } catch (Exception e) {
            }
        } else {
            play(url);
        }
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onRun() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
