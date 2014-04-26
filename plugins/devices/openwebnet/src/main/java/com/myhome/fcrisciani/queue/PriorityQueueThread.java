/**
 * This thread extract new command from the priority queue and send them on the Plant.
 * Between each one it put a 300ms of delay to assure the correct execution on the plant.
 * If there is no available command it suspends on the priority queue semaphore.
 */

package com.myhome.fcrisciani.queue;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import com.myhome.fcrisciani.connector.MyHomeJavaConnector;
import com.myhome.fcrisciani.connector.MyHomeSocketFactory;
import com.freedomotic.plugins.devices.openwebnet.OpenWebNet;

/**
 * @author Flavio Crisciani
 *
 */
public class PriorityQueueThread implements Runnable{
	// ----- TYPES ----- //

	// ---- MEMBERS ---- //
	MyHomeJavaConnector myConnector = null;
	PriorityCommandQueue list = null;
	Socket sk = null;
	PrintWriter output = null;
	
	// ---- METHODS ---- //
	private void closeSocket(){
		if (output != null) {
			output.close();
			output = null;
		}
		if (sk != null) {
			try {
				MyHomeSocketFactory.disconnect(sk);
			} catch (IOException e) {
				OpenWebNet.LOG.severe("PriorityQueueThread: Problem during connection closure - " + e.toString());
				e.printStackTrace();
			}
			sk = null;
		}
	}
	/**
	 * Create the Priority Queue Thread giving the reference to the MyHome connector and the Priority queue
	 * @param myConnector myhome connector used only for IP, port read
	 * @param list priority queue to handle
	 */
	public PriorityQueueThread(final MyHomeJavaConnector myConnector, final PriorityCommandQueue list) {
		this.myConnector = myConnector;
		this.list = list;
	}

	@Override
	public void run() {
		String tosend = null;
		do{
			try{
				tosend = list.getCommand();
				if(sk == null){                  // Create a new command session
					try{
						sk = MyHomeSocketFactory.openCommandSession(myConnector.ip, myConnector.port);
					}catch(IOException e){
						OpenWebNet.LOG.severe("PriorityQueueThread: Problem during socket monitor opening - " + e.toString());
						continue;
					}
				}
				try{
					if (output == null) {
						output = new PrintWriter(sk.getOutputStream());
					}
					output.write(tosend);
					output.flush();
				}catch(IOException e){
					OpenWebNet.LOG.severe("PriorityQueueThread: Problem during command sending - " + e.toString());
					closeSocket();
					continue;
				}
				try{
					Thread.sleep(300);				// Wait 300ms to be sure that command sent had been executed
				}catch(InterruptedException e){
					OpenWebNet.LOG.severe("PriorityQueueThread: Problem during suspension - " + e.toString());
					continue;
				}
				if(list.numCommands() == 0){        // There are no more message to handle close command session
					closeSocket();
				}
			}catch (Exception e) {
				OpenWebNet.LOG.severe("PriorityQueueThread: Not handled exception - " + e.toString());
				closeSocket();
			}
		}while(true);
	}

}
