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
package clientjava.connections;

import com.freedomotic.plugins.devices.openwebnet.OpenWebNet;

/**
 * Adapter per i gestori connessione di basso livello (Object Adapter).
 * L'istanza del gestore è Singleton.
 *
 * @author Maurizio Lorenzoni (loremaur@libero.it)
 *
 */
public class ConnectionsManager implements ConnectionsManagerInterface {

    String Client_IPaddress = "192.168.1.101"; //tabella indirizzi aperti (da 101-103)
    String gatewayAddress = "192.168.0.3";
    int gatewayPort = 20000;
    OpenWebNet pluginRef = null;
    /*
     * *********************
     */
    //singleton instance of manager
    private static ConnectionsManager instance;
    static CommandsSocketManager gestSocketComandi = null;
    static SocketMonitorManager gestSocketMonitor = null;

    /**
     * Private constructor (singleton)
     */
    private ConnectionsManager() {
    }

    /**
     * Si assicura che ci sia una sola istanza del gestore nel sistema
     *
     * @return manager singleton instance
     */
    public static synchronized ConnectionsManager getInstance() {
        if (instance == null) {
            instance = new ConnectionsManager();
        }
        return instance;
    }

    public void init(String gatewayAddress, int gatewayPort, OpenWebNet pluginRef) {
        this.gatewayAddress = gatewayAddress;
        this.gatewayPort = gatewayPort;
        this.pluginRef = pluginRef;
        gestSocketComandi = new CommandsSocketManager(pluginRef);
        gestSocketMonitor = new SocketMonitorManager(pluginRef);
    }

    /**
     * Invia al sistema un comando OpenWebNet utilizzando le API di basso
     * livello.
     *
     * @param comando una stringa contenente il comando OpenWebNet
     * @return true se il comando è inviato, false se non è possibile inviare il
     * comando
     */
    public boolean inviaComandoOpen(String comando) {

        if (comando.length() != 0) {
            if (gestSocketComandi.connect(gatewayAddress, gatewayPort, 000)) {
                gestSocketComandi.inviaComando(comando);
                gestSocketComandi.disconnect();
                return true;
            } else {
                //connessione ko
                pluginRef.getLogger().error("Connessione con il server KO");
                return false;
            }
        } else {
            pluginRef.getLogger().error("comando open non valido");
            return false;
        }

    }

    /**
     * Inizia una connessione di monitoring con il sistema. Utilizzare lo stdOut
     * per intercettare i messaggi (prefisso "Mon:").
     *
     * @return true se la connessione è stabilita, false altrimenti
     */
    public boolean startMonitoring() {

        if (gestSocketMonitor.connect(gatewayAddress, gatewayPort, 000)) {
            //connessione OK thread monitorizza giÃ  attivato
            return true;
        } else {
            return false;
        }
    }

    /**
     * Stops monitoring connection.
     *
     * @return true if succeed, false otherwise
     */
    public boolean stopMonitoring() {
        if (gestSocketMonitor != null) {
            gestSocketMonitor.disconnect();
            return true;
        }
        return false;
    }
}
