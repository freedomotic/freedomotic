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
package clientjava.openwebnet;

/***************************************************************************
 * 			                  OpenWebNet.java                              *
 * 			              --------------------------                       *
 *   date          : Feb 22, 2005                                          *
 *   copyright     : (C) 2005 by Bticino S.p.A. Erba (CO) - Italy 	       *
 *   				 Embedded Software Development Laboratory              *
 *   license       : GPL                                                   *
 *   email         : 		             				                   *
 *   web site      : www.bticino.it; www.myhome-bticino.it                 *
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

/**
 * Description:
 * Classe per gestire l'OPEN
 * 
 */
public class OWN implements InterfaceOpenWebNet{
	
//	rappresenta la frame open creata
	private String frameOpen = new String();
//	tipo di frame open
	private int tipoFrame;
//	lunghezza frame
	private int	lengthFrameOpen;
//	indica le frame estese
	private boolean estesa;
//	 numero valori
	int numValori;
//	numero indirizzo
	private int numIndirizzo;
//	contenuti  della frame normale
	
	private String chi = "";
	private String indirizzo[] = new String[MAX_INDIRIZZO];
	private String cosa = "";
	private String dove = "";
	private String livello = "";
	private String interfaccia = "";
	private String quando = "";
	private String grandezza = "";
	private String valori[] = new String[MAX_NUM_VALORI];

	/**
	 * Costruttore
	 * Crea una NULL_FRAME
	 *
	 */
	public OWN(){
		//richiamo la procedura createNullMsgOpen
		createNullMsgOpen();		
	}
	 
	/**
	 * Costruttore
	 * 
	 * @param message Frame open 
	 */
	public OWN(String message){
		//richiamo la procedura CreateMsgOpen(String message)
		createMsgOpen(message);
	}

	/**
	 * Crea il messaggio OPEN *chi*cosa*dove*quando##
	 * 
	 * @param who Campo chi
	 * @param what Campo cosa
	 * @param where Campo dove
	 * @param when Campo quando
	 */
	public OWN(String who, String what, String where, String when){
		//richiamo la procedura CreateMsgOpen()
		createMsgOpen(who, what, where,	when);
	}

	/**
	 * Crea il messaggio OPEN tramite la stringa passata come parametro
	 * 
	 * @param message Frame open
	 */
	public void createMsgOpen(String message)
	{
		//System.out.println("creo msg open da stringa");
		//richiamo la procedura CreateNullMsgOpen()
		createNullMsgOpen();

		// salva il tipo di frame e la sua lunghezza
		frameOpen = message;
		lengthFrameOpen = frameOpen.length();
		// elimino i caratteri di controllo ....
		if ((frameOpen.charAt(lengthFrameOpen-1) == '\n') || ((frameOpen.charAt(lengthFrameOpen-1) == '\r')))
			lengthFrameOpen--;
		if ((frameOpen.charAt(lengthFrameOpen-2) == '\n') || ((frameOpen.charAt(lengthFrameOpen-2) == '\r')))
			lengthFrameOpen--;
	  // per scrupolo ...
		//frameOpen[lengthFrameOpen] = '\0';
		// controlla se sintassi corretta ...
		isCorrect();
	}
	
	/**
	 * Crea il messaggio OPEN *chi*cosa*dove*quando##
	 * 
	 * @param who Campo chi
	 * @param what Campo cosa
	 * @param where Campo dove
	 * @param when Campo quando
	 */
	public void createMsgOpen(String who, String what, String where, String when){
		//richiamo la procedura CreateNullMsgOpen()
		//System.out.println("creo msg open tramite chi cosa dove quando");
		createNullMsgOpen();
		
		// creo il messaggio open
		frameOpen = "*";
		frameOpen = frameOpen.concat(who);
		frameOpen = frameOpen.concat("*");
		frameOpen = frameOpen.concat(what);
		frameOpen = frameOpen.concat("*");
		frameOpen = frameOpen.concat(where);
		//per togliere l'asterisco finale
	    if (!when.equals("")){
	    	frameOpen = frameOpen.concat("*");
	    }	  
	    frameOpen = frameOpen.concat(when);
	    frameOpen = frameOpen.concat("##");

		lengthFrameOpen = frameOpen.length();
		// elimino i caratteri di controllo ....
		if ((frameOpen.charAt(lengthFrameOpen-1) == '\n') || ((frameOpen.charAt(lengthFrameOpen-1) == '\r')))
			lengthFrameOpen--;
		if ((frameOpen.charAt(lengthFrameOpen-1) == '\n') || ((frameOpen.charAt(lengthFrameOpen-1) == '\r')))
			lengthFrameOpen--;
	  // per scrupolo ...
		//frameOpen[lengthFrameOpen] = '\0';
		// controlla se sintassi corretta ...
		isCorrect();
	}
	
	/**
	 * Crea il messaggio OPEN *chi*cosa*dove#livello#interfaccia*quando##
	 * 
	 * @param who Campo chi
	 * @param what Campo cosa
	 * @param where Campo dove
	 * @param lev Campo livello
	 * @param interfac Campo interfaccia
	 * @param when Campo quando
	 */
	public void createMsgOpen(String who, String what,	String where, String lev, String interfac, String when)
	{
		//richiamo la procedura CreateNullMsgOpen()
		createNullMsgOpen();

		frameOpen = "*";
		frameOpen = frameOpen.concat(who);
		frameOpen = frameOpen.concat("*");
		frameOpen = frameOpen.concat(what);
		frameOpen = frameOpen.concat("*");
		frameOpen = frameOpen.concat(where);
		frameOpen = frameOpen.concat("#");
		
	  if (lev.equals("")){
	  	frameOpen = frameOpen.concat("4");
		//strcat(frameOpen, "4");
	  }else{
	  	frameOpen = frameOpen.concat(lev);	
		//strcat(frameOpen, lev);
	  }
	  frameOpen = frameOpen.concat("#");
	  frameOpen = frameOpen.concat(interfac);
		//per togliere l'asterisco finale
	  if (when.equals("")){
	  	frameOpen = frameOpen.concat("*");
	  }
	  frameOpen = frameOpen.concat("when");
	  frameOpen = frameOpen.concat("##");

		lengthFrameOpen = frameOpen.length();
		// elimino i caratteri di controllo ....
		if ((frameOpen.charAt(lengthFrameOpen-1) == '\n') || ((frameOpen.charAt(lengthFrameOpen-1) == '\r')))
			lengthFrameOpen--;
		if ((frameOpen.charAt(lengthFrameOpen-1) == '\n') || ((frameOpen.charAt(lengthFrameOpen-1) == '\r')))
			lengthFrameOpen--;
	  // per scrupolo ...
		//frameOpen[lengthFrameOpen] = '\0';
		// controlla se sintassi corretta ...
		isCorrect();
	}
	
	
	/**
	 * Crea il messaggio OPEN *#chi*dove##
	 * 
	 * @param who Campo chi
	 * @param where Campo dove
	 */
	public void CreateStateMsgOpen(String who, String where) {
//      richiamo la procedura CreateNullMsgOpen()
        createNullMsgOpen();

        // creo il messaggio open
        frameOpen = "*#";
		frameOpen = frameOpen.concat(who);
		frameOpen = frameOpen.concat("*");
		frameOpen = frameOpen.concat(where);
		frameOpen = frameOpen.concat("##");

        lengthFrameOpen = frameOpen.length();
        
        // elimino i caratteri di controllo ....
        if ((frameOpen.charAt(lengthFrameOpen -1) == '\n') || ((frameOpen.charAt(lengthFrameOpen-1) == '\r')))
          lengthFrameOpen--;
        if ((frameOpen.charAt(lengthFrameOpen -1) == '\n') || ((frameOpen.charAt(lengthFrameOpen-1) == '\r')))
            lengthFrameOpen--;
        
        // controlla se sintassi corretta ...
        isCorrect();
    }

	
	/**
	 * Crea il messaggio OPEN *#chi*dove#livello#interfaccia##
	 * 
	 * @param who Campo chi
	 * @param where Campo dove
	 * @param lev Campo livello
	 * @param interfac Campo interfaccia
	 */
    public void CreateStateMsgOpen(String who, String where, String lev, String interfac) {
        
//      richiamo la procedura CreateNullMsgOpen()
        createNullMsgOpen();

        frameOpen = "*#";
		frameOpen = frameOpen.concat(who);
		frameOpen = frameOpen.concat("*");
		frameOpen = frameOpen.concat(where);
		frameOpen = frameOpen.concat("#");
		
		if (lev.equals("")){
		    frameOpen = frameOpen.concat("4");
		}else{
		    frameOpen = frameOpen.concat(lev);
		}
		frameOpen = frameOpen.concat("#");
		frameOpen = frameOpen.concat(interfac);
		frameOpen = frameOpen.concat("##");

        lengthFrameOpen = frameOpen.length();
        
        // elimino i caratteri di controllo ....
        if ((frameOpen.charAt(lengthFrameOpen -1) == '\n') || ((frameOpen.charAt(lengthFrameOpen-1) == '\r')))
            lengthFrameOpen--;
        if ((frameOpen.charAt(lengthFrameOpen -1) == '\n') || ((frameOpen.charAt(lengthFrameOpen-1) == '\r')))
            lengthFrameOpen--;
        
        // controlla se sintassi corretta ...
        isCorrect();
        
    }
    
    
    /**
	 * Crea il messaggio OPEN *#chi*dove*grandezza##
	 * 
	 * @param who Campo chi
	 * @param where Campo dove
	 * @param dimension Campo grandezza
	 */
    public void CreateDimensionMsgOpen(String who, String where, String dimension) {

        //richiamo la procedura CreateNullMsgOpen()
        createNullMsgOpen();

        // creo il messaggio open
        frameOpen = "*#";
		frameOpen = frameOpen.concat(who);
		frameOpen = frameOpen.concat("*");
		frameOpen = frameOpen.concat(where);

		//per togliere l'asterisco finale
        if (!dimension.equals("")){
            frameOpen = frameOpen.concat("*");
        }            
        frameOpen = frameOpen.concat(dimension);
		frameOpen = frameOpen.concat("##"); 

        lengthFrameOpen = frameOpen.length();
        // elimino i caratteri di controllo ....
        if ((frameOpen.charAt(lengthFrameOpen -1) == '\n') || ((frameOpen.charAt(lengthFrameOpen-1) == '\r')))
            lengthFrameOpen--;
        if ((frameOpen.charAt(lengthFrameOpen -1) == '\n') || ((frameOpen.charAt(lengthFrameOpen-1) == '\r')))
            lengthFrameOpen--;
        
        // controlla se sintassi corretta ...
        isCorrect();
    }

    /**
	 * Crea il messaggio OPEN *#chi*dove#livello#interfaccia*grandezza##
	 * 
	 * @param who Campo chi
	 * @param where Campo dove
	 * @param lev Campo livello
	 * @param interfac Campo interfaccia
	 * @param dimension Campo grandezza
	 */
    public void CreateDimensionMsgOpen(String who, String where, String lev, String interfac, String dimension) {

        //richiamo la procedura CreateNullMsgOpen()
        createNullMsgOpen();

        frameOpen = "*#";
		frameOpen = frameOpen.concat(who);
		frameOpen = frameOpen.concat("*");
		frameOpen = frameOpen.concat(where);
		frameOpen = frameOpen.concat("#");
		
		if (lev.equals("")){
		    frameOpen = frameOpen.concat("4");
		}else{
		    frameOpen = frameOpen.concat(lev);
		}
		frameOpen = frameOpen.concat("#");
		frameOpen = frameOpen.concat(interfac);
//		per togliere l'asterisco finale
        if (!dimension.equals("")){
            frameOpen = frameOpen.concat("*");
        }            
        frameOpen = frameOpen.concat(dimension);
		frameOpen = frameOpen.concat("##"); 
		
        lengthFrameOpen = frameOpen.length();
        
        // elimino i caratteri di controllo ....
        if ((frameOpen.charAt(lengthFrameOpen -1) == '\n') || ((frameOpen.charAt(lengthFrameOpen-1) == '\r')))
            lengthFrameOpen--;
        if ((frameOpen.charAt(lengthFrameOpen -1) == '\n') || ((frameOpen.charAt(lengthFrameOpen-1) == '\r')))
            lengthFrameOpen--;
        
        // controlla se sintassi corretta ...
        isCorrect();
    }

    
    /**
	 * Crea il messaggio OPEN *#chi*dove*#grandezza*val_1*val_2*....*val_n##
	 * 
	 * @param who Campo chi
	 * @param where Campo dove
	 * @param dimension Campo grandezza
	 * @param value Campo contenente i valori
	 * @param numValori Numero di valori
	 */
    public void CreateWrDimensionMsgOpen(String who, String where, String dimension, String value[], int numValue){

        //richiamo la procedura CreateNullMsgOpen()
        createNullMsgOpen();

        // creo il messaggio open
        frameOpen = "*#";
		frameOpen = frameOpen.concat(who);
		frameOpen = frameOpen.concat("*");
		frameOpen = frameOpen.concat(where);
		frameOpen = frameOpen.concat("*#");
		frameOpen = frameOpen.concat(dimension);
        for(int i = 0; i < numValue; i++){
            frameOpen = frameOpen.concat("*");
            frameOpen = frameOpen.concat(value[i]);
        }
        frameOpen = frameOpen.concat("##");

        lengthFrameOpen = frameOpen.length();
        
        // elimino i caratteri di controllo ....
        if ((frameOpen.charAt(lengthFrameOpen -1) == '\n') || ((frameOpen.charAt(lengthFrameOpen-1) == '\r')))
            lengthFrameOpen--;
        if ((frameOpen.charAt(lengthFrameOpen -1) == '\n') || ((frameOpen.charAt(lengthFrameOpen-1) == '\r')))
            lengthFrameOpen--;
        
        // controlla se sintassi corretta ...
        isCorrect();    
    }
    
    
    /**
	 * Crea il messaggio OPEN *#chi*dove#livello#interfaccia*#grandezza*val_1*val_2*....*val_n##
	 * 
	 * @param who Campo chi
	 * @param where Campo dove
	 * @param lev Campo livello
	 * @param interfac Campo interfaccia
	 * @param dimension Campo grandezza
	 * @param value Campo contenente i valori
	 * @param numValori Numero di valori
	 */
    public void CreateWrDimensionMsgOpen(String who, String where, String lev, String interfac, String dimension, String value[], int numValue){

        //richiamo la procedura CreateNullMsgOpen()
        createNullMsgOpen();

        // creo il messaggio open
        frameOpen = "*#";
		frameOpen = frameOpen.concat(who);
		frameOpen = frameOpen.concat("*");
		frameOpen = frameOpen.concat(where);
		frameOpen = frameOpen.concat("#");
		if (lev.equals("")){
		    frameOpen = frameOpen.concat("4");
		}else{
		    frameOpen = frameOpen.concat(lev);
		}
		frameOpen = frameOpen.concat("#");
		frameOpen = frameOpen.concat(interfac);
		frameOpen = frameOpen.concat("*#");
		frameOpen = frameOpen.concat(dimension);
        for(int i = 0; i < numValue; i++){
            frameOpen = frameOpen.concat("*");
            frameOpen = frameOpen.concat(value[i]);
        }
        frameOpen = frameOpen.concat("##");

        lengthFrameOpen = frameOpen.length();
        
        // elimino i caratteri di controllo ....
        if ((frameOpen.charAt(lengthFrameOpen -1) == '\n') || ((frameOpen.charAt(lengthFrameOpen-1) == '\r')))
            lengthFrameOpen--;
        if ((frameOpen.charAt(lengthFrameOpen -1) == '\n') || ((frameOpen.charAt(lengthFrameOpen-1) == '\r')))
            lengthFrameOpen--;
        
        // controlla se sintassi corretta ...
        isCorrect();      
    }
    
    
	/**
	 * Crea il messaggio OPEN ****##
	 *
	 */
	public void createNullMsgOpen(){
//		contatore per azzerare i valori
		//System.out.println("creo msg open null");
		int i = 0;

		// azzera tutto
		frameOpen = null;		
		
	 	tipoFrame = NULL_FRAME;

		lengthFrameOpen = 0;

		estesa = false;

	  // azzera tutto
//		numIndirizzo = 0;
//		for (i = 0; i < MAX_INDIRIZZO; i++)
//			memset(indirizzo[i], '\0', indirizzo[i].length);
//		numValori = 0;
//		for (i = 0; i < MAX_NUM_VALORI; i++)
//			memset(valori[i], '\0', valori[i].length);
		chi = "";
		cosa = "";
		dove = "";
		livello = "";
		interfaccia = "";
		quando = "";
		grandezza = "";
	}
	
	
//	public boolean isEqual(OpenWebNet msgToCompare){
//		// controlla se sintassi corretta ...
//		isCorrect();
//
//		//conttrollo che sia la stessa tipologia
//		if(msgToCompare.tipoFrame != tipoFrame)
//			return false;
//
//		//controllo che siano entrambe due frame estese o meno
//		if(msgToCompare.estesa != estesa)
//			return false;
//
//		if(!estesa)
//		{
//			if ((msgToCompare.getChi() == chi) &&
//					(msgToCompare.getCosa() == cosa) &&
//					(msgToCompare.getDove() == dove) &&
//					(msgToCompare.getQuando() == quando) &&
//					(msgToCompare.getGrandezza() == grandezza))
//				
//				
//				return true;
//			else
//				return false;
//		}
//		else
//		{			
//			System.out.println("%%%%%%%%%%%%");
//			System.out.println(msgToCompare.getChi()+"  "+chi);
//			System.out.println(msgToCompare.getCosa()+"  "+cosa);
//			System.out.println(msgToCompare.getDove()+"  "+dove);
//			System.out.println(msgToCompare.getLivello()+"  "+livello);
//			System.out.println(msgToCompare.getInterfaccia()+"  "+interfaccia);
//			System.out.println(msgToCompare.getQuando()+"  "+quando);
//			System.out.println(msgToCompare.getGrandezza()+"  "+grandezza);
//			if ((msgToCompare.getChi().equals(chi)) &&
//					((String)msgToCompare.getCosa() == (String)cosa) &&
//					((String)msgToCompare.getDove() == dove) &&
//					((String)msgToCompare.getLivello() == livello) &&
//					((String)msgToCompare.getInterfaccia() == interfaccia) &&
//					((String)msgToCompare.getQuando() == quando) &&
//					((String)msgToCompare.getGrandezza() == grandezza))
//				return true;
//			else
//				return false;
//		}
//	}
	
	/**
	 * Esegue il controllo sintattico
	 */
	private void isCorrect(){
		int j  = 0;
		int i = 0;
		//char sup[] = new char[MAX_LENGTH_OPEN];
		//char campo[] = new char[MAX_LENGTH];
		String sup = null;
		String campo = null;

		// se frame ACK -->
		if (frameOpen.equals(MSG_OPEN_OK))
		{
			//System.out.println("frame di tipo ack");
			tipoFrame = OK_FRAME;
			return;
		}

		// se frame NACK -->
		if (frameOpen.equals(MSG_OPEN_KO))
		{
			//System.out.println("frame di tipo nack");
			tipoFrame = KO_FRAME;
			return;
		}


		//se il primo carattere non è *
		//oppure la frame è troppo lunga
		//oppure se non termina con due '#'
		if ((frameOpen.charAt(0) != '*') ||
				(lengthFrameOpen > MAX_LENGTH_OPEN) ||
				(frameOpen.charAt(lengthFrameOpen-1) != '#') ||
				(frameOpen.charAt(lengthFrameOpen-2) != '#'))
		{
			System.out.println("*** FRAME ERRORE ***");
			tipoFrame = ERROR_FRAME;
			return;
		}

		//Controllo se sono stati digitati dei caratteri
		for (j=0;j<lengthFrameOpen;j++)
		{
			if(!Character.isDigit(frameOpen.charAt(j)))
			{
				if((frameOpen.charAt(j) != '*') && (frameOpen.charAt(j) != '#'))
				{
					System.out.println("°°° FRAME ERROR °°°");
					tipoFrame = ERROR_FRAME;
					return;
				}	
			}
		}

		// frame normale ...	
		//*chi#indirizzo*cosa*dove#livello#indirizzo*quando##
		if (frameOpen.charAt(1) != '#')
		{
			//System.out.println("frame normale");
			tipoFrame = NORMAL_FRAME;
			//estraggo i vari campi della frame open nella prima modalità (chi+indirizzo e dove+livello+interfaccia)
			assegnaChiCosaDoveQuando();
			//estraggo gli eventuali valori di indirizzo
			assegnaIndirizzo();
			//estraggo gli eventuali valori di livello ed interfaccia
			assegnaLivelloInterfaccia();
			return;
		}

		// frame password ...
		//*#pwd##
		if(contains(frameOpen.substring(2), '*') == false)
		{
			//System.out.println("frame password");
			tipoFrame = PWD_FRAME;
			// estraggo il chi
			assegnaChi();
			return;
		}

		//per le altre tipologie di frame
		sup = null;
		sup = frameOpen.substring(2, frameOpen.length()); 
		//sprintf(sup, "%s", frameOpen+2);
		campo = null;
		i = 0;
		while(sup.charAt(i) != '*'){
			i++;			
		}
		campo = sup.substring(0, i);
		//sprintf(campo, "%s", strtok(sup, "*"));
		sup = null;
		sup = frameOpen.substring(2+campo.length()+1, frameOpen.length());
		//sprintf(sup, "%s", frameOpen+2+strlen(campo)+1);
		if(sup.charAt(0) != '*')
		{
			i = 0;
			boolean trovato = false;
			while(i < sup.length()){
				if(sup.charAt(i) == '*'){
					trovato = true;
					break;
				}
				i++;
			}			
			if(trovato) campo = campo.concat(sup.substring(0, i));
			
			//sprintf(campo, "%s%s", campo, strtok(sup, "*"));
			sup = null;
			sup = frameOpen.substring(2+campo.length()+1, frameOpen.length());
			//sprintf(sup, "%s", frameOpen+2+strlen(campo)+1);
		}

		//frame richiesta stato ...
		//*#chi*dove##
		if(sup.charAt(0) != '*')
		{
			//System.out.println("frame stato");
			tipoFrame = STATE_FRAME;
			//estraggo i vari campi della frame open nella prima modalit� (chi+indirizzo e dove+livello+interfaccia)
			assegnaChiDove();
			//estraggo gli eventuali valori di indirizzo
			assegnaIndirizzo();
			//estraggo gli eventuali valori di livello ed interfaccia
			assegnaLivelloInterfaccia();
			return;
		}
		else
		{
			//frame di richiesta misura ...
			//*#chi*dove*grandezza## o *#chi*dove*#grandezza*valore_N�##
			if(sup.charAt(1) != '#')
			{
				//System.out.println("Measure state");
				tipoFrame = MEASURE_FRAME;
				//estraggo i vari campi della frame open nella prima modalit� (chi+indirizzo e dove+livello+interfaccia)
				assegnaChiDoveGrandezza();
				//estraggo gli eventuali valori di indirizzo
				assegnaIndirizzo();
				//estraggo gli eventuali valori di livello ed interfaccia
				assegnaLivelloInterfaccia();
				return;
			}
			//frame di scrittura grandezza ...
			//*#chi*dove*#grandezza*valore_N�##
			else
			{
				//System.out.println("frame write");
				tipoFrame = WRITE_FRAME;
				//estraggo i vari campi della frame open nella prima modalit� (chi+indirizzo e dove+livello+interfaccia)
				assegnaChiDoveGrandezzaValori();
				//estraggo gli eventuali valori di indirizzo
				assegnaIndirizzo();
				//estraggo gli eventuali valori di livello ed interfaccia
				assegnaLivelloInterfaccia();
				return;
			}
		}
	}
	
	
	
	/**
	 * Assegna chi dove e la grandezza richiesta per le frame di richiesta grandezze
	 *
	 */
	private void assegnaChiDoveGrandezza()
	{
		//System.out.println("assegno chi dove grandezza");
		//char sup[MAX_LENGTH_OPEN];
		String sup = null;
		int len = 0;
		int j = 0;
		int i = 0;

	 	// CHI
		sup = null;
		sup = frameOpen.substring(2, frameOpen.length());
		//sprintf(sup, "%s", frameOpen+2);
		if(sup.charAt(0) != '*'){
			i = 0;
			while(sup.charAt(i) != '*'){
				i++;			
			}	
			chi = sup.substring(0, i);
			//sprintf(chi, "%s", strtok(sup, "*"));
		}
		// DOVE
		sup = null;
		sup = frameOpen.substring(2+chi.length()+1, frameOpen.length());
		//sprintf(sup, "%s", frameOpen+2+strlen(chi)+1);
		if(contains(sup, '*') == false){
			dove = sup.substring(0, sup.length()-2);
			//strncpy(dove, sup, strlen(sup)-2);
		}
		else
		{
			if(sup.charAt(0) != '*'){
				i = 0;
				while(sup.charAt(i) != '*'){
					i++;			
				}	
				dove = sup.substring(0, i);
				//sprintf(dove, "%s", strtok(sup, "*"));
			}
			// GRANDEZZA
			sup = null;
			sup = frameOpen.substring(2+chi.length()+1+dove.length()+1, frameOpen.length());
			//sprintf(sup, "%s", frameOpen+2+strlen(chi)+1+strlen(dove)+1);
			if(contains(sup, '*') == false){
				grandezza = sup.substring(0, sup.length()-2);
				//strncpy(grandezza, sup, strlen(sup)-2);
			}
			else
			{
				if(sup.charAt(0) != '*'){
					i = 0;
					while(sup.charAt(i) != '*'){
						i++;			
					}	
					grandezza = sup.substring(0, i);
					//sprintf(grandezza, "%s", strtok(sup, "*"));
				}
				// VALORI
				sup = null;
				sup = frameOpen.substring(2+chi.length()+1+dove.length()+1+grandezza.length()+1, frameOpen.length());
				//sprintf(sup, "%s", frameOpen+2+strlen(chi)+1+strlen(dove)+1+strlen(grandezza)+1);
				j = 0;
				len = 2+chi.length()+1+dove.length()+1+grandezza.length()+1;
				//len = 2+strlen(chi)+1+strlen(dove)+1+strlen(grandezza)+1;
				while (contains(sup, '*') != false)
				{
					i = 0;
					while(sup.charAt(i) != '*'){
						i++;			
					}	
					valori[j] = sup.substring(0, i);
					//sprintf(valori[j], "%s", strtok(sup, "*"));
					sup = null;
					len = len+valori[j].length()+1;
					sup = frameOpen.substring(len, frameOpen.length());
					//sprintf(sup, "%s", frameOpen+len);
					j++;
				}
				valori[j] = sup.substring(sup.length()-2, sup.length());
				//strncpy(valori[j], sup, strlen(sup)-2);
			}
		}

		//assegno numero valori
		numValori = j+1;

		return;
	}

	/**
	 * Assegna chi e dove per le frame di richiesta stato
	 *
	 */
	private void assegnaChiDove()
	{
		//System.out.println("assegno chi dove");
		//char sup[MAX_LENGTH_OPEN];
		String sup = null;
		int i = 0;
		
	 	// CHI
		sup = null;
		sup = frameOpen.substring(2, frameOpen.length());
		//sprintf(sup, "%s", frameOpen+2);
		if(sup.charAt(0) != '*'){
			i = 0;
			while(sup.charAt(i) != '*'){
				i++;			
			}
			chi = sup.substring(0, i);
			//sprintf(chi, "%s", strtok(sup, "*"));
		}
		// DOVE
		sup = null;	
		sup = frameOpen.substring(2+chi.length()+1, frameOpen.length());
		//sprintf(sup, "%s", frameOpen+2+strlen(chi)+1);
		if(contains(sup, '*') == false){
			dove = sup.substring(0, sup.length()-2);
			//strncpy(dove, sup, strlen(sup)-2);
		}
		else
		{
			if(sup.charAt(0) != '*'){
				i = 0;
				while(sup.charAt(i) != '*'){
					i++;			
				}
				dove = sup.substring(0, i);
				//sprintf(dove, "%s", strtok(sup, "*"));				
			}
		}

		return;
	}
	
	/**
	 * Assegna chi cosa dove e quando per le frame "normali"
	 *
	 */
	private void assegnaChiCosaDoveQuando()
	{
		//System.out.println("assegno chi cosa dove quando");
		int i = 0;
		
		String sup = null;
		//System.out.println("chi prima "+chi);
	 	// CHI
//		copio in temp la frame open togliendo il primo carattere, dovrebbe essere *
		try{
		sup = frameOpen.substring(1, frameOpen.length());
		if(sup.charAt(0) != '#'){
			i = 0;
			while(Character.getType(sup.charAt(i)) == Character.DECIMAL_DIGIT_NUMBER){
				i++;			
			}
			chi = sup.substring(0, i);
		}
		//System.out.println("Chi: "+ chi);

		// COSA
		sup = null;
		sup = frameOpen.substring(1+chi.length()+1, frameOpen.length());
		if(contains(sup, '*') == false)
			cosa = sup.substring(0, sup.length()-2);
		else
		{
			if(sup.charAt(0) != '*'){
				i = 0;
				while(Character.getType(sup.charAt(i)) == Character.DECIMAL_DIGIT_NUMBER){
					i++;			
				}
				cosa = sup.substring(0, i);
			}else{
				cosa = "";
			}
			//System.out.println("Cosa: "+ cosa);	
			
			// DOVE
			sup = null;
			sup = frameOpen.substring(1+chi.length()+1+cosa.length()+1, frameOpen.length());
			if(contains(sup, '*') == false){
				dove = sup.substring(0, sup.length()-2);
			}else{
				if(sup.charAt(0) != '*'){
					i = 0;
					while(Character.getType(sup.charAt(i)) == Character.DECIMAL_DIGIT_NUMBER){
						i++;			
					}	
					dove = sup.substring(0, i);
				}
//				System.out.println("Dove: "+ dove);
//				System.out.println("chi "+chi);
//				System.out.println("cosa "+cosa);
				// QUANDO
				sup = null;
				sup = frameOpen.substring(1+chi.length()+1+cosa.length()+1+dove.length()+1, frameOpen.length());
				if(contains(sup, '*') == false){
					quando = sup.substring(0, sup.length()-2);
				}
				else{
					if(sup.charAt(0) != '*'){
						i = 0;
						while(Character.getType(sup.charAt(i)) == Character.DECIMAL_DIGIT_NUMBER){
							i++;			
						}	
						quando = sup.substring(0, i);
					}
					//System.out.println("Quando: "+ quando);
				}
			}
		}
		}catch(StringIndexOutOfBoundsException e){
		    System.out.println("Eccezione1");
		    tipoFrame = ERROR_FRAME;		    
		    }
		return;
	}
	
	/**
	 * Assegna chi dove grandezza e i valori per le frame di scrittura grandezze
	 *
	 */
	private void assegnaChiDoveGrandezzaValori()
	{
		//System.out.println("assegno chi dove grandezza valori");
		//char sup[MAX_LENGTH_OPEN];
		String sup = null;
		int len = 0;
		int j = 0;
		int i = 0;

	 	// CHI
		sup = null;
		sup = frameOpen.substring(2, frameOpen.length());
		//sprintf(sup, "%s", frameOpen+2);
		if(sup.charAt(0) != '*'){
			i = 0;
			while(sup.charAt(i) != '*'){
				i++;			
			}
			chi = sup.substring(0, i);
			//sprintf(chi, "%s", strtok(sup, "*"));
		}
		// DOVE
		sup = null;
		sup = frameOpen.substring(2+chi.length()+1, frameOpen.length());
		//sprintf(sup, "%s", frameOpen+2+strlen(chi)+1);
		if(contains(sup, '*') == false){
			dove = sup.substring(0, sup.length()-2);
			//strncpy(dove, sup, strlen(sup)-2);
		}
		else
		{
			if(sup.charAt(0) != '*'){
				i = 0;
				while(sup.charAt(i) != '*'){
					i++;			
				}
				dove = sup.substring(0, i);
				//sprintf(dove, "%s", strtok(sup, "*"));
			}
			// GRANDEZZA
			sup = null;
			sup = frameOpen.substring(2+chi.length()+1+dove.length()+2, frameOpen.length());
			//sprintf(sup, "%s", frameOpen+2+strlen(chi)+1+strlen(dove)+2);
			if(contains(sup, '*') == false){
				tipoFrame = ERROR_FRAME;
			}
			else
			{
				if(sup.charAt(0) != '*'){
					i = 0;
					while(sup.charAt(i) != '*'){
						i++;			
					}
					grandezza = sup.substring(0, i);
					//sprintf(grandezza, "%s", strtok(sup, "*"));
				}
				// VALORI
				sup = null;
				sup = frameOpen.substring(2+chi.length()+1+dove.length()+2+grandezza.length()+1, frameOpen.length());
				//sprintf(sup, "%s", frameOpen+2+strlen(chi)+1+strlen(dove)+2+strlen(grandezza)+1);
				j = 0;
				len = 2+chi.length()+1+dove.length()+2+grandezza.length()+1;
				while (contains(sup, '*') != false)
				{
					i = 0;
					while(sup.charAt(i) != '*'){
						i++;			
					}
					valori[j] = sup.substring(0, i);
					//sprintf(valori[j], "%s", strtok(sup, "*"));
					sup = null;
					len = len+valori[j].length()+1;
					sup = frameOpen.substring(len, frameOpen.length());
					//sprintf(sup, "%s", frameOpen+len);
					j++;
				}
				valori[j] = sup.substring(0, sup.length()-2);
				//strncpy(valori[j], sup, strlen(sup)-2);
			}
		}

		//assegno numero valori
		numValori = j+1;

		return;
	}
	
	/**
	 * Assegna chi per le frame di risultato elaborazione password
	 *
	 */
	private void assegnaChi()
	{
		//System.out.println("assegno chi");
		int i = 0;
		String sup = null;

		// CHI
		sup = frameOpen.substring(2, frameOpen.length());
		//sprintf(sup, "%s", frameOpen+2);
		if(sup.charAt(0) != '#'){
			i = 0;
			while(sup.charAt(i) != '#'){
				i++;			
			}
			chi = sup.substring(0, i);
			//sprintf(chi, "%s", strtok(sup, "#"));
		}
		else{
			tipoFrame = ERROR_FRAME;
		}
		sup = null;
		sup = frameOpen.substring(2+chi.length(), frameOpen.length());
		//sprintf(sup, "%s", frameOpen+2+strlen(chi));
		if(sup.charAt(1) != '#'){
			tipoFrame = ERROR_FRAME;
		}
		return;
	}
	
	/**
	 * Assegna livello, interfaccia per le frame estese
	 *
	 */
	private void assegnaLivelloInterfaccia()
	{
		//System.out.println("assegno livello interfaccia");
		
		String sup = null;
		String orig = null;
		int i = 0;
		
	 	// DOVE
		if(!dove.equals("")){
			if(dove.charAt(0) == '#'){
				sup = dove.substring(1, dove.length());
				//sprintf(sup, "%s", dove+1);
			}
			else{
				sup = dove;
				//sprintf(sup, "%s", dove);
			}
			orig = null;
			orig = dove;
			//sprintf(orig, "%s", dove);
			if(contains(sup, '#') != false)
			{
				estesa = true;
				dove = null;
				if(orig.charAt(0) == '#'){
					i = 0;
					while(sup.charAt(i) != '#'){
						i++;			
					}
					//mao da vedere se è giusta____cos'è "#%s" ?
					dove = sup.substring(0, i);
					//System.out.println("Dove$$$$$$$:"+dove);
					//sprintf(dove, "#%s", strtok(sup, "#"));				
				}
				else{
					i = 0;
					while(sup.charAt(i) != '#'){
						i++;			
					}
					dove = sup.substring(0, i);
					//sprintf(dove, "%s", strtok(sup, "#"));
				}
				// LIVELLO + INTERFACCIA
				sup = null;
				sup = orig.substring(dove.length()+1, orig.length());
				//sprintf(sup, "%s", orig+strlen(dove)+1);
				if(contains(sup, '#') != false)
				{
					i = 0;
					while(sup.charAt(i) != '#'){
						i++;			
					}
					livello = sup.substring(0, i);
					//sprintf(livello, "%s", strtok(sup, "#"));
					interfaccia = orig.substring(dove.length()+1+livello.length()+1);
					//sprintf(interfaccia, "%s", orig+strlen(dove)+1+strlen(livello)+1);
					if(interfaccia.length() == 0)
						tipoFrame = ERROR_FRAME;
				}
				else
					tipoFrame = ERROR_FRAME;
			}
		}else{ //il campo dove è vuoto
		    
		}
			
		return;
	}

	
	/**
	 * Assegna indirizzo
	 *
	 */
	private void assegnaIndirizzo()
	{
		//System.out.println("assegno indirizzo");
		
		int i = 0;
		String sup = null;
		String orig = null;
		
	 	// CHI
		sup = chi;
		orig = chi;
		if(contains(sup, '#') != false)
		{
			chi = null;
			i = 0;
			while(sup.charAt(i) != '#'){
				i++;			
			}
			chi = sup.substring(0, i);
			//sprintf(chi, "%s", strtok(sup, "#"));
			// INDIRIZZO
			numIndirizzo++;
			sup = null;
			sup = orig.substring(chi.length()+1, orig.length());
			//sprintf(sup, "%s", orig+strlen(chi)+1);
			if(contains(sup, '#') == false)
			{
				// solo indirizzo seriale
				if(sup.length() != 0){
					indirizzo[0] = sup;
					//sprintf(indirizzo[0], "%s", sup);					
				}
				else{
					tipoFrame = ERROR_FRAME;					
				}
			}
			else
			{
				// indirizzo IP
				i = 0;
				while(sup.charAt(i) != '#'){
					i++;			
				}
				indirizzo[0] = sup.substring(0, i);
				//sprintf(indirizzo[0], "%s", strtok(sup, "#"));
				sup = null;
				sup = orig.substring(chi.length()+1+indirizzo[0].length()+1, orig.length());
				//sprintf(sup, "%s", orig+strlen(chi)+1+strlen(indirizzo[0])+1);
				if(contains(sup, '#') != false)
				{
					// indirizzo IP
					i = 0;
					while(sup.charAt(i) != '#'){
						i++;			
					}
					indirizzo[1] = sup.substring(0, i);
					//sprintf(indirizzo[1], "%s", strtok(sup, "#"));
					sup = null;
					sup = orig.substring(chi.length()+1+indirizzo[0].length()+1+indirizzo[1].length()+1, orig.length());
					//sprintf(sup, "%s", orig+strlen(chi)+1+strlen(indirizzo[0])+1+strlen(indirizzo[1])+1);
					if(contains(sup, '#') != false)
					{
						// indirizzo IP
						i = 0;
						while(sup.charAt(i) != '#'){
							i++;			
						}
						indirizzo[2] = sup.substring(0, i);
						//sprintf(indirizzo[2], "%s", strtok(sup, "#"));
						sup = null;
						sup = orig.substring(chi.length()+1+indirizzo[0].length()+1+indirizzo[1].length()+1+indirizzo[2].length()+1, orig.length());
						//sprintf(sup, "%s", orig+strlen(chi)+1+strlen(indirizzo[0])+1+strlen(indirizzo[1])+1+strlen(indirizzo[2])+1);
						if(contains(sup, '#') == false)
						{
							// indirizzo IP
							if(sup.length() != 0){
								indirizzo[3] = sup;
								//sprintf(indirizzo[3], "%s", sup);
							}
							else{
								tipoFrame = ERROR_FRAME;
							}
						}
						else
							tipoFrame = ERROR_FRAME;
					}
					else
						tipoFrame = ERROR_FRAME;
				}
				else
					tipoFrame = ERROR_FRAME;
			}
		}

		return;
	}
	
	/**
	 * Verifica se in una stringa c'è un determinato carattere
	 * 
	 * @param testo Stringa da controllare
	 * @param c Carattere da cercare in testo
	 * @return True se testo contiene c; False altrimenti
	 */
	private boolean contains(String testo, char c){
		int i = 0;
		while(i != testo.length()){
			if(testo.charAt(i) == c) return true;
			i++;
		}
		return false;
	}
	
	/**
	 * @return Returns the chi.
	 */
	public String getChi() {
		return chi;
	}
	
	/**
	 * @return Returns the cosa.
	 */
	public String getCosa() {
		return cosa;
	}
	
	/**
	 * @return Returns the dove.
	 */
	public String getDove() {
		return dove;
	}
	
	/**
	 * @return Returns the estesa.
	 */
	public boolean isEstesa() {
		return estesa;
	}
	
	/**
	 * @return Returns the frameOpen.
	 */
	public String getFrameOpen() {
		return frameOpen;
	}
	
	/**
	 * @return Returns the grandezza.
	 */
	public String getGrandezza() {
		return grandezza;
	}
	
	/**
	 * @return Returns the interfaccia.
	 */
	public String getInterfaccia() {
		return interfaccia;
	}
	
	/**
	 * @return Returns the lengthFrameOpen.
	 */
	public int getLengthFrameOpen() {
		return lengthFrameOpen;
	}
	
	/**
	 * @return Returns the quando.
	 */
	public String getQuando() {
		return quando;
	}
	
	/**
	 * @return Returns the tipoFrame.
	 */
	public int getTipoFrame() {
		return tipoFrame;
	}
	
	/**
	 * @return Returns the indirizzo.
	 */
	public String getIndirizzo(int i) {
		return indirizzo[i];
	}

	/**
	 * @return Returns the valori.
	 */
	public String getValori(int i) {
		return valori[i];
	}
	
	/**
	 * @return Returns the livello.
	 */
	public String getLivello() {
		return livello;
	}
	
	/**
	 * @param dove The dove to set.
	 */
	public void setDove(String dove) {
		this.dove = dove;
	}
	
//	 tipo di frame ?
	
	/**
	 * 
	 * @return True se la frame è di tipo ERROR_FRAME; False altrimenti
	 */
	public boolean isErrorFrame(){
		return (tipoFrame == ERROR_FRAME);
	}
	
	/**
	 * 
	 * @return True se la frame è di tipo NULL_FRAME; False altrimenti
	 */
	public boolean isNullFrame(){
		return (tipoFrame == NULL_FRAME);
	}
	
	/**
	 * 
	 * @return True se la frame è di tipo NORMAL_FRAME; False altrimenti
	 */
	public boolean isNormalFrame(){
		return (tipoFrame == NORMAL_FRAME);
	}
	
	/**
	 * 
	 * @return True se la frame è di tipo MEASURE_FRAME; False altrimenti
	 */
	public boolean isMeasureFrame(){
		return (tipoFrame == MEASURE_FRAME);
	}
	
	/**
	 * 
	 * @return True se la frame è di tipo STATE_FRAME; False altrimenti
	 */
	public boolean isStateFrame(){
		return (tipoFrame == STATE_FRAME);
	}
	
	/**
	 * 
	 * @return True se la frame è di tipo WRITE_FRAME; False altrimenti
	 */
	public boolean isWriteFrame(){
		return (tipoFrame == WRITE_FRAME);
	}
	
	/**
	 * 
	 * @return True se la frame è di tipo PWD_FRAME; False altrimenti
	 */
	public boolean isPwdFrame(){
		return (tipoFrame == PWD_FRAME);
	}
	
	/**
	 * 
	 * @return True se la frame è di tipo OK_FRAME; False altrimenti
	 */
	public boolean isOKFrame(){
		return (tipoFrame == OK_FRAME);
	}
	
	/**
	 * 
	 * @return True se la frame è di tipo KO_FRAME; False altrimenti
	 */
	public boolean isKOFrame(){
		return (tipoFrame == KO_FRAME);
	}
    
}
