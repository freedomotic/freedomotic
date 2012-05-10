/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;


import it.freedomotic.api.Tool;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.plugins.video.VideoPlayerConfig;
import it.freedomotic.reactions.Command;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Enrico Nicoletti
 */
public class VideoPlayer extends Tool {

    static Process p;

    public VideoPlayer() {
        super("VLC Media Player", "/it.nicoletti.media/vlc-media-player.xml");
        gui= new VideoPlayerConfig();
        start();
    }

    public void play(String url) throws IOException {
        File path = new File(configuration.getStringProperty("vlc-bin", "C:/Program Files (x86)/VideoLAN/VLC/vlc.exe"));
        p = Runtime.getRuntime().exec(path.getAbsolutePath() + " -vvv " + url + "\"");
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        if (!isRunning()) {
            start();
        }
        String url = c.getProperty("url");
        String close = c.getProperty("close");
        if (close==null){
            close="no";
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
}
