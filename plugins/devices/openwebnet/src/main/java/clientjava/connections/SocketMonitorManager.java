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
import clientjava.openwebnet.OWN;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import java.util.logging.*;

/**
 * Description: Gestione della socket Monitor, apertura monitor, chiusura
 * monitor
 *
 */
public class SocketMonitorManager {

    //THREAD DI SUPPORTO
    static ReadThread readThMon = null; //thread per la ricezione dei caratteri inviati dal webserver
    static TimerThread timeoutThreadMon = null; //thread per la gestione dei timeout
    MonitorThread monThread = null;  //thread che si occupa di raccogliere tutti i msg che passano sulla socket monitor
    //STATO
    static String responseLineMon = null; //stringa in ricezione dal Webserver
    static int statoMonitor = 0;  //stato socket monitor
    //SUPPORTO
    static final String socketMonitor = "*99*1##";
    static Socket socketMon = null;
    BufferedReader inputMon = null;
    PrintWriter outputMon = null;
    private static FileHandler fh;
    private OpenWebNet pluginRef = null;

    /**
     * Costruttore
     *
     */
    public SocketMonitorManager(OpenWebNet pluginRef) {
        this.pluginRef = pluginRef;

    }

    /**
     *
     * Apre una socket di monitor.
     *
     * Tentativo di apertura socket monitor verso il webserver
     *
     * Va in loop finchè non riesce a completare l'handshacking di una sessione
     * monitor. Quando ci riesce fa partire un thread di monitoraggio e il
     * metodo termina ritornando true.
     *
     *
     * @param ip Ip del webserver al quale connettersi
     * @param port porta sulla quale aprire la connessione
     * @param passwordOpen password open del webserver
     * @return true se la connessione va a buon fine, false altrimenti
     */
    public boolean connect(String ip, int port, long passwordOpen) { //tipo rappresenta socket comandi o monitor
        try {
            //pluginRef.getLogger().log(Level.CONFIG, "Mon: Tentativo connessione a " + ip + "  Port: " + port);
            socketMon = new Socket(ip, port);
            setTimeout(1); //se entro 30 sec non risponde chiudo connessione
            inputMon = new BufferedReader(new InputStreamReader(socketMon.getInputStream()));
            //pluginRef.getLogger().log(Level.INFO, "Mon: Buffer reader creato");
            outputMon = new PrintWriter(socketMon.getOutputStream(), true);
            //pluginRef.getLogger().log(Level.INFO, "Mon: Print Writer creato");
        } catch (IOException e) {
            pluginRef.getLogger().error("Mon: Impossibile connettersi con host " + ip + "\n");
            this.disconnect();
            //e.printStackTrace();
        }

        if (socketMon != null) {  //se abbiamo aperto la socket
            while (true) {
                readThMon = null;
                readThMon = new ReadThread(socketMon, inputMon, 1);
                readThMon.start();  //aspettiamo la risposta del server
                try {
                    readThMon.join();
                } catch (InterruptedException e1) {
                    pluginRef.getLogger().debug("Mon: ----- ERRORE readThread.join() durante la connect:");
                    e1.printStackTrace();
                }

                //VARI STATI POSSIBILI: (se tutto va bene vengono svolti in cascata)
                if (responseLineMon != null) {
                    if (statoMonitor == 0) { //non connesso, attendo ACK dal server (ho mandato la richiesta di connessione)
                        //pluginRef.getLogger().log(Level.INFO, "\nMon: ----- STATO 0 ----- ");
                        //pluginRef.getLogger().log(Level.CONFIG, "Mon: Rx: " + responseLineMon);
                        if (responseLineMon.equals(OWN.MSG_OPEN_OK)) {
                            //pluginRef.getLogger().log(Level.CONFIG, "--- Stabilita comunicazione TCP/IP con il server.");
                            //pluginRef.getLogger().log(Level.CONFIG, "Mon: Tx: " + socketMonitor);
                            outputMon.write(socketMonitor); //comandi
                            outputMon.flush();
                            statoMonitor = 1; //setto stato autenticazione
                            setTimeout(1); //se entro 30 sec non risponde chiudo connessione
                        } else {
                            //se non mi connetto chiudo la socket
                            //pluginRef.getLogger().log(Level.CONFIG, "Mon: Chiudo la socket verso il server ");
                            //pluginRef.getLogger().log(Level.CONFIG, "--- Comunicazione TCP/IP con il server non riuscita.");
                            this.disconnect();
                            break;
                        }
                    } else if (statoMonitor == 1) { //ho mandato il tipo di servizio richiesto
                        //pluginRef.getLogger().log(Level.CONFIG, "Mon: ----- STATO 1 -----");
                        //pluginRef.getLogger().log(Level.CONFIG, "Mon: Rx: " + responseLineMon);

                        //ATTENZIONE: non faccio mai controllo password
                        /*
                         * if(ClientFrame.abilitaPass.isSelected()){ //applico
                         * algoritmo di conversione long risultato =
                         * gestPassword.applicaAlgoritmo(passwordOpen,
                         * responseLineMon); logger.log(Level.CONFIG, "Mon: Tx:
                         * "+"*#"+risultato+"##");
                         * outputMon.write("*#"+risultato+"##");
                         * outputMon.flush(); statoMonitor = 2; //setto stato
                         * dopo l'autenticazione setTimeout(1);
                         }else{
                         */
                        //non devo fare il controllo della password
                        //pluginRef.getLogger().log(Level.INFO, "Mon: NON effettuo il controllo sulla password - mi aspetto ACK");
                        if (responseLineMon.equals(OWN.MSG_OPEN_OK)) { //CONTROLLO ACK
                            //pluginRef.getLogger().log(Level.CONFIG, "--- Stabilita sessione monitor con il server.");
                            //pluginRef.getLogger().log(Level.INFO, "Mon: Ricevuto ack, statoMonitor = 3");
                            statoMonitor = 3;
                            //pluginRef.getLogger().log(Level.CONFIG, "Mon: Monitor attivata con successo");
                            break;
                        } else {
                            pluginRef.getLogger().error("Mon: Impossibile connettersi!!");
                            pluginRef.getLogger().debug("--- Sessione monitor con il server non riuscita.");
                            //se non mi connetto chiudo la socket
                            //pluginRef.getLogger().log(Level.INFO, "Mon: Chiudo la socket verso il server " + ip);
                            this.disconnect();
                            break;
                        }
                        //}
                    } else if (statoMonitor == 2) {//attesa password (disattivato)
                        //pluginRef.getLogger().log(Level.INFO, "Mon: ----- STATO 2 -----");
                        //pluginRef.getLogger().log(Level.CONFIG, "Mon: Rx: " + responseLineMon);
                        if (responseLineMon.equals(OWN.MSG_OPEN_OK)) {
                            //pluginRef.getLogger().log(Level.CONFIG, "Mon: Monitor attivata con successo");
                            statoMonitor = 3;
                            break;
                        } else {
                            pluginRef.getLogger().debug("Mon: Impossibile attivare la monitor");
                            //se non mi connetto chiudo la socket
                            //pluginRef.getLogger().log(Level.INFO, "Mon: Chiudo la socket monitor\n");
                            this.disconnect();
                            break;
                        }
                    } else {
                        break; //non dovrebbe servire (quando passo per lo stato tre esco dal ciclo con break)
                    }                    //UNA VOLTA ARRIVATO ALLO STATO TRE ESCO DAL WHILE TRUE
                } else {
                    //pluginRef.getLogger().log(Level.INFO, "Mon: Risposta dal webserver NULL");
                    this.disconnect();
                    break;//ramo else della funzione riceviStringa()
                }
            }//chiude while(true)
        } else {
            //System.out.println("$$$$$$$");
        }

        if (statoMonitor == 3) { //siamo connessi --> MONITORIZZA
            monThread = null;
            monThread = new MonitorThread(socketMon, inputMon, pluginRef);
            monThread.start();
        }

        if (statoMonitor == 3) {
            return true;
        } else {
            return false;
        }

    }//chiude connect()

    /**
     * Chiude la socket monitor ed imposta statoMonitor = 0
     *
     */
    public void disconnect() {
        if (socketMon != null) {
            try {
                socketMon.close();
                socketMon = null;
                statoMonitor = 0;

                //pluginRef.getLogger().log(Level.CONFIG, "--- Chiusa sessione monitor con il server.");
                pluginRef.getLogger().debug("MON: Socket monitor chiusa correttamente-----\n");
            } catch (IOException e) {
                pluginRef.getLogger().debug("MON: Errore chiusura Socket: <GestioneSocketMonitor>");
                e.printStackTrace();
            }
        }
    }

    /**
     * Attiva il thread per il timeout sulla risposta inviata dal WebServer.
     *
     * @param tipoSocket: 0 se è socket comandi, 1 se è socket monitor
     */
    public void setTimeout(int tipoSocket) {
        timeoutThreadMon = null;
        timeoutThreadMon = new TimerThread("timeout", tipoSocket, pluginRef);
        timeoutThreadMon.start();
    }
}
