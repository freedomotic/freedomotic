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

import clientjava.openwebnet.OWN;
import com.freedomotic.plugins.devices.openwebnet.OpenWebNet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import java.util.logging.*;

/**
 *
 * @author Maurizio Lorenzoni (loremaur@libero.it)
 */
public class CommandsSocketManager {

    //THREAD DI SUPPORTO
    static ReadThread readTh = null; //thread per la ricezione dei caratteri inviati dal webserver
    static TimerThread timeoutThread = null; //thread per la gestione dei timeout
    //STATO
    static String responseLine = null; //stringa in ricezione dal Webserver
    int stato = 0;  //stato socket comandi
    //SUPPORTO
    static final String socketComandi = "*99*0##"; //messaggio comando per server
    Socket socket = null;
    BufferedReader input = null;
    PrintWriter output = null;
    OWN openWebNet = null;   //supporto per l' OpenWebNet
    //LOGGING
    private static Logger logger;
    private static FileHandler fh;
    private OpenWebNet pluginRef;

    public CommandsSocketManager(OpenWebNet pluginRef) {
        this.pluginRef = pluginRef;
    }

    /**
     * Si occupa dell' handshaking di sessioni comando.
     *
     * Apre una socket con il client (se possibile) e rende possibile il metodo
     * inviaComando (sulla stessa socket creata).
     *
     * Tentativo di apertura socket comandi verso il webserver Diversi possibili
     * stati: stato 0 = non connesso stato 1 = inviata richiesta socket comandi,
     * in attesa di risposta stato 2 = inviato risultato sulle operazioni della
     * password, attesa per ack o nack. Se la risposta e' ack si passa allo
     * stato 3 stato 3 = connesso correttamente
     *
     * @param ip Ip del webserver al quale connettersi
     * @param port Porta sulla quale aprire la connessione
     * @param passwordOpen Password open del webserver
     * @return true Se la connessione va a buon fine, false altrimenti
     */
    public boolean connect(String ip, int port, long password) {
        try {
            //opens a socket
            pluginRef.getLogger().info("Tentativo connessione a " + ip + "  Port: " + port);
            socket = new Socket(ip, port);

            //preparo stream di lettura/scrittura
            setTimeout(0);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pluginRef.getLogger().debug("Buffer reader creato");
            output = new PrintWriter(socket.getOutputStream(), true);
            pluginRef.getLogger().debug("Print Writer creato");

        } catch (IOException e) {
            pluginRef.getLogger().error("Server connection error");
            this.disconnect();
        }

        //uso la socket aperta

        if (socket != null) {
            while (true) {

                //ASPETTO DI AVERE QUALCOSA IN RESPONSELINE
                readTh = null;
                readTh = new ReadThread(socket, input, 0);
                readTh.start(); //leggo la prima risposta del server
                try {
                    readTh.join(); //aspetto di aver letto --> verrà copiata in responseLine
                } catch (InterruptedException e1) {
                    pluginRef.getLogger().error("----- ERRORE readThread.join() durante la connect:");
                    e1.printStackTrace();
                }
                //riempito responseLine, a seconda
                //dello stato in cui sono nel'handshake mi comporto diversamente
                if (responseLine != null) {

                    if (stato == 0) { //ho mandato la richiesta di connessione
                        pluginRef.getLogger().debug("\n----- STATO 0 ----- ");
                        pluginRef.getLogger().debug("Rx: " + responseLine);
                        //1.2- attendo messaggio ACK (per 30 sec)
                        if (responseLine.equals(OWN.MSG_OPEN_OK)) {//controllo ACK
                            pluginRef.getLogger().debug("--- Stabilita comunicazione TCP/IP con il server.");
                            //2.1 invia il codice *99*0## e rimane in attesa
                            pluginRef.getLogger().debug("Tx: " + socketComandi);
                            output.write(socketComandi); //comandi (invio codice al server)

                            output.flush();
                            stato = 1;
                            setTimeout(0);
                        } else { //caso NACK
                            //se non mi connetto chiudo la socket
                            pluginRef.getLogger().debug("--- Comunicazione TCP/IP con il server non riuscita.");
                            pluginRef.getLogger().debug("Chiudo la socket verso il server ");
                            this.disconnect();
                            break;
                        }

                    } else if (stato == 1) { //ho mandato il tipo di servizio richiesto
                        pluginRef.getLogger().debug("\n----- STATO 1 -----");
                        pluginRef.getLogger().debug("Rx: " + responseLine);

                        //controllo password disattivato
						/*
                         * if(GestoreConnessioniAdapter.abilitaPass.isSelected()){
                         * //applico algoritmo di conversione
                         * logger.log(Level.CONFIG, "Controllo sulla password");
                         * long risultato =
                         * gestPassword.applicaAlgoritmo(passwordOpen,
                         * responseLine); logger.log(Level.CONFIG, "Tx:
                         * "+"*#"+risultato+"##",1,0,0);
                         * output.write("*#"+risultato+"##"); output.flush();
                         * stato = 2; //setto stato dopo l'autenticazione
                         * setTimeout(0); }else{
                         */
                        //non devo fare il controllo della password
                        pluginRef.getLogger().debug("NON effettuo il controllo sulla password - mi aspetto ACK");

                        //2.6 se entro 30 sec non ricevo ACK -> chiudo la connessione
                        if (responseLine.equals(OWN.MSG_OPEN_OK)) {
                            pluginRef.getLogger().debug("--- Stabilita sessione comandi con il server.");
                            pluginRef.getLogger().debug("Ricevuto ack, stato = 3");
                            stato = 3;
                            break;
                        } else {
                            pluginRef.getLogger().debug("Impossibile connettersi!!");
                            pluginRef.getLogger().debug("--- Sessione comandi con il server non stabilita.");
                            //se non mi connetto chiudo la socket
                            pluginRef.getLogger().debug("Chiudo la socket verso il server ");
                            this.disconnect();
                            break;
                        }
                        //}
                    } else if (stato == 2) { //attesa password (disattivato)
                        pluginRef.getLogger().debug("\n----- STATO 2 -----");
                        pluginRef.getLogger().debug("Rx: " + responseLine);
                        if (responseLine.equals(OWN.MSG_OPEN_OK)) {
                            pluginRef.getLogger().debug("Connessione OK");
                            stato = 3;
                            break;
                        } else {
                            pluginRef.getLogger().error("Impossibile connettersi!!");
                            //se non mi connetto chiudo la socket
                            pluginRef.getLogger().debug("Chiudo la socket verso il server ");
                            this.disconnect();
                            break;
                        }
                    } else {
                        break; //non dovrebbe servire (quando passo per lo stato tre esco dal ciclo con break)
                    }
                } else {
                    pluginRef.getLogger().debug("--- Risposta dal webserver NULL");
                    this.disconnect();
                    break;//ramo else di if(responseLine != null)
                }
            }//chiude while(true)
        } else {
        }

        if (stato == 3) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Chiude la socket comandi ed imposta stato = 0
     *
     */
    public void disconnect() {
        if (socket != null) {
            try {
                socket.close();
                socket = null;
                stato = 0;
                pluginRef.getLogger().debug("-----Socket chiusa correttamente-----");
                pluginRef.getLogger().debug("--- Chiusa sessione comandi con il server.");
            } catch (IOException e) {
                pluginRef.getLogger().debug("Errore Socket: <GestioneSocketComandi>");
                e.printStackTrace();
            }
        }
    }

    /**
     * Metodo per l'invio di un comando open una volta aperta la socket con il
     * metodo connect
     *
     * @param comandoOpen comando da inviare
     * @return 0 se il comando vine inviato, 1 se non Ã¨ possibile inviare il
     * comando
     */
    public int inviaComando(String comandoOpen) {
        //creo l'oggetto openWebNet con il comandoOpen
        try {
            openWebNet = new OWN(comandoOpen);
            if (openWebNet.isErrorFrame()) {
                pluginRef.getLogger().error("ERRATA frame open " + comandoOpen + ", la invio comunque!!!");
            } else {
                pluginRef.getLogger().debug("CREATO oggetto OpenWebNet " + openWebNet.getFrameOpen());
            }
        } catch (Exception e) {
            pluginRef.getLogger().error("ERRORE nella creazione dell'oggetto OpenWebNet " + comandoOpen);
            pluginRef.getLogger().error("Eccezione in GestioneSocketComandi durante la creazione del'oggetto OpenWebNet");
            e.printStackTrace();
        }

        //3.1 invia il messaggio open e rimane in attesa della risposta (ACK/NACK) del server
        pluginRef.getLogger().debug("Tx: " + comandoOpen);
        output.write(comandoOpen);
        output.flush();

        do {
            setTimeout(0);
            readTh = null;
            readTh = new ReadThread(socket, input, 0);
            readTh.start(); //attendo risposta dal server
            try {
                readTh.join();
            } catch (InterruptedException e1) {
                pluginRef.getLogger().error("----- ERRORE readThread.join() durante l'invio comando:");
                e1.printStackTrace();
            }

            //3.2 la risposta può essere ACK(*#*1##) o NACK(*#*0##)
            if (responseLine != null) {
                if (responseLine.equals(OWN.MSG_OPEN_OK)) {//ACK
                    pluginRef.getLogger().debug("Rx: " + responseLine);
                    pluginRef.getLogger().debug("Comando inviato correttamente");
                    this.disconnect();//chiudo connessione
                    return 0;
                    //break;
                } else if (responseLine.equals(OWN.MSG_OPEN_KO)) {//NACK
                    pluginRef.getLogger().debug("Rx: " + responseLine);
                    pluginRef.getLogger().error("Comando NON inviato correttamente");
                    //if(!ClientFrame.mantieniSocket.isSelected()) this.disconnect();
                    this.disconnect();//chiudo connessione
                    return 0;
                    //break;
                } else {
                    //RICHIESTA STATO
                    System.out.println("Rx: " + responseLine);
                    if (responseLine == OWN.MSG_OPEN_OK) {
                        pluginRef.getLogger().debug("Comando inviato correttamente");
                        this.disconnect();//chiudo connessione
                        return 0;
                        //break;
                    } else if (responseLine == OWN.MSG_OPEN_KO) {
                        pluginRef.getLogger().error("Comando NON inviato correttamente");
                        this.disconnect();//chiudo connessione
                        return 0;
                        //break;
                    }
                }
            } else {
                pluginRef.getLogger().error("Impossibile inviare il comando");
                this.disconnect();//chiudo connessione
                return 1;
                //break;
            }
        } while (true);
    }

    /**
     * Attiva il thread per il timeout sulla risposta inviata dal WebServer.
     *
     * @param tipoSocket: 0 se Ã¨ socket comandi, 1 se Ã¨ socket monitor
     */
    public void setTimeout(int tipoSocket) {
        timeoutThread = null;
        timeoutThread = new TimerThread("timeout", tipoSocket, pluginRef);
        timeoutThread.start();
    }
}
