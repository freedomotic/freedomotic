/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.util;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
public class InternetBrowser {

    public InternetBrowser(String url) {
        try {
            try {
                java.awt.Desktop.getDesktop().browse(new URI(url));
            } catch (IOException ex) {
                Logger.getLogger(InternetBrowser.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(InternetBrowser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
