/**
* This class implements 3 priority queues of Open commands to be sent
*/

package com.myhome.fcrisciani.queue;

import com.freedomotic.plugins.devices.openwebnet.OpenWebNet;
import java.util.Vector;
import java.util.concurrent.Semaphore;

/**
* @author Flavio Crisciani
*
*/
public class PriorityCommandQueue {
        // ----- TYPES ----- //

        // ---- MEMBERS ---- //

        final Vector<String> level1 = new Vector<String>();
        final Vector<String> level2 = new Vector<String>();
        final Vector<String> level3 = new Vector<String>();

        final Semaphore commandsAvailable = new Semaphore(0);

        // ---- METHODS ---- //
        /**
         * Add a command to the high priority queue
         * @param c command to queue
         * @return true if correctly queued
         */
        public void addHighLevel(String c){
                this.level1.add(c);
                commandsAvailable.release();
        }
        /**
         * Add a command to the medium priority queue
         * @param c command to queue
         * @return true if correctly queued
         */
        public void addMediumLevel(String c){
                this.level2.add(c);
                commandsAvailable.release();
        }
        /**
         * Add a command to the low priority queue
         * @param c command to queue
         * @return true if correctly queued
         */
        public void addLowLevel(String c){
                this.level3.add(c);
                commandsAvailable.release();
        }
        /**
         * Get a command, when available from one of the queue, if they are all empty it suspend the thread on a semaphone
         * @return the command to execute
         */
        public String getCommand(){
                String resultCommand = null;
                try{
                        if(commandsAvailable.availablePermits() == 0){
                                // System.out.println("CommandTail: Non ci sono comandi da eseguire nelle code mi sospendo");
                        }
                        commandsAvailable.acquire();
                }catch(InterruptedException e){
                        OpenWebNet.LOG.severe("PriorityCommandQueue: Exception during suspetion on the semaphore: " + e.toString());
                        resultCommand = null;
                }
                if(level1.size() > 0)
                        resultCommand = level1.remove(0);
                else if(level2.size() > 0)
                        resultCommand = level2.remove(0);
                else if(level3.size() > 0)
                        resultCommand = level3.remove(0);

                return resultCommand;
        }
        /**
         * Returns number of commands available
         * @return the number of command available
         */
        public int numCommands(){
                return commandsAvailable.availablePermits();
        }

}