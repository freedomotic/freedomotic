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

/**
 * Interfaccia High Level di accesso alle API di basso livello
 *
 * @author Maurizio Lorenzoni (loremaur@libero.it)
 */
public interface ConnectionsManagerInterface {

    /**
     * Invia al sistema un comando OpenWebNet.
     *
     * @param comando una stringa contenente il comando OpenWebNet
     * @return true se il comando è inviato, false se non è possibile inviare il
     * comando
     */
    public boolean inviaComandoOpen(String comando);

    /**
     * Inizia una connessione di monitoring con il sistema. Utilizzare lo stdOut
     * per intercettare i messaggi (prefisso "Mon:").
     *
     * @return true se la connessione è stabilita, false altrimenti
     */
    public boolean startMonitoring();

    /**
     * Ferma la connessione di monitoring con il sistema.
     *
     * @return true se l'azione e riuscita, false altrimenti
     */
    public boolean stopMonitoring();

}
