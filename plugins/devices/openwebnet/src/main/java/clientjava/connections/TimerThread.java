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
import java.io.IOException;

/**
 *
 * Dopo 30 sec chiude i thread di comunicazione con il server per handshacking
 * di sessioni comando o monitor
 *
 * Description: Gestisce i timeout durante la procedura di connessione al
 * WebServer e l'invio dei comandi open
 *
 */
public class TimerThread extends Thread {

    String name;
    int time = 0; //rappresenta lo sleep del thread (15 sec o 30 sec)
    int statoEntrata = 0;
    int tipoSocket; // 0 se la socket è di tipo comandi, 1 se è di tipo monitor
    private OpenWebNet pluginRef;

    /**
     * Costruttore
     *
     * @param threadName Nome del Thread
     * @param tipoSocket Tipo di socket che richiama il costruttore, 0 se è
     * socket comandi, 1 se è monitor
     */
    public TimerThread(String threadName, int tipoSocket, OpenWebNet pluginRef) {

        this.pluginRef = pluginRef;
        name = threadName;
        this.tipoSocket = tipoSocket;
        if (tipoSocket == 0) {
            statoEntrata = ConnectionsManager.gestSocketComandi.stato;
        } else {
            statoEntrata = SocketMonitorManager.statoMonitor;
        }
        pluginRef.getLogger().debug("Thread per il timeout attivato");
    }

    /**
     * Avvia il Thread per gestire il timeout
     */
    public void run() {
        do {
            time = 30000; //30 sec di timeout
            //time = 30000000; //30 sec di timeout

            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                //pluginRef.getLogger().log(Level.INFO, "Thread timeout interrotto!");
                break;
                //e.printStackTrace();
            }

            //pluginRef.getLogger().log(Level.INFO, "Thread timeout SCADUTO!");
            //chiudo il thread per la ricezione dei caratteri
            if (tipoSocket == 0) {
                if (CommandsSocketManager.readTh != null) {
                    CommandsSocketManager.readTh.interrupt();
                }
            } else {
                if (SocketMonitorManager.readThMon != null) {
                    SocketMonitorManager.readThMon.interrupt();
                }
            }
            break;
        } while (true);
    }
}
