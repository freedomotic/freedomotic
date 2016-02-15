/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.freedomotic.plugins.devices.video;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
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
    
    private static final Logger LOG = Logger.getLogger(VideoPlayer.class.getName());   
    static Process p;

    public VideoPlayer() {
        super("VLC Media Player", "/media/vlc-media-player.xml");
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
