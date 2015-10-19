/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.freedomchat;

import com.freedomotic.api.API;
import com.freedomotic.exceptions.NoResultsException;
import com.freedomotic.nlp.Nlp;
import com.freedomotic.nlp.NlpCommand;
import static com.freedomotic.plugins.devices.freedomchat.FreedomChat.unsplit;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.Reaction;
import com.freedomotic.reactions.Trigger;
import com.google.inject.Inject;
import java.util.List;

/**
 *
 * @author matteo
 */


public class MessageService {
    public static String IF = "if";
    public static String THEN = "then";
    public static String WHEN = "when";
    public static String HELP = "help";
    public static String LIST = "list";
    public static String TRIGGER = "trigger";
    
    private final NlpCommand nlpCommands;
    
    private final API api;

    @Inject
    public MessageService(NlpCommand nlpCommands, API api) {
        this.nlpCommands = nlpCommands;
        this.api = api;
    }
    
    

    public String manageMessage(String mess) {
        Command c;
        Trigger t = null;
        Reaction r;

        //  String sentenceMess[] = nlp.getSentenceDetector().sentDetect(mess);
        String tokenMess[] = mess.split(" "); //nlp.getTokenizer().tokenize(sentenceMess[0]);
        String triggername = "";
        int conditionSep = 0;
        if (tokenMess[0].equalsIgnoreCase(HELP)) {
            return help(tokenMess);
        }
        if (tokenMess[0].equalsIgnoreCase(LIST)) {
            return list(tokenMess);
        }

        if (tokenMess[0].equalsIgnoreCase(IF) || tokenMess[0].equalsIgnoreCase(WHEN)) {
            for (int i = 1; i < tokenMess.length; i++) {
                if (tokenMess[i].equalsIgnoreCase(THEN)) {
                    triggername = unsplit(tokenMess, 1, i - 1, " ");
                    conditionSep = i + 1;
                    break;
                }
            }
            List<Trigger> lst = api.triggers().findByName(triggername);
            if (!lst.isEmpty()) {
                t = lst.get(0);
            }
        }

        String commandName = unsplit(tokenMess, conditionSep, tokenMess.length - conditionSep, " ");
        List<Nlp.Rank<Command>> mostSimilar;
        try {
            mostSimilar = nlpCommands.computeSimilarity(commandName, 10);
            // user is asking for help
            if (commandName.contains("*")) {
                String response = "";
                for (Nlp.Rank<Command> nlpr : mostSimilar) {
                    response += "? " + nlpr.getElement().getName() + "\n";
                }
                return response;
            }
            if (!mostSimilar.isEmpty() && mostSimilar.get(0).getSimilarity() > 0) {
                c = mostSimilar.get(0).getElement();
            } else {
                return "No available commands similar to: " + commandName;
            }
        } catch (NoResultsException e) {
            return "No available commands similar to: " + commandName;
        }
        
        if (tokenMess[0].equalsIgnoreCase(IF)) {
            Trigger NEWt = t.clone();
            NEWt.setNumberOfExecutions(1);
            r = new Reaction(NEWt, c);
            api.reactions().create(r);
        } else if (tokenMess[0].equalsIgnoreCase(WHEN)) {
            // do something
            r = new Reaction(t, c);
            api.reactions().create(r);
        } else {
            api.bus().send(c);
            return c.getName() + "\n DONE.";
        }
        return "DONE";
    }

    private String help(String[] tokenMess) {
        return "Freedomotic CHAT help:\n"
                + "- enter a command name to be executed\n"
                + "- enter a command with '*' in it to get a list of suggestions\n"
                + "- enter 'LIST [commands|objects|triggers]' to retrieve related list";
    }

    private String list(String[] tokenMess) {
        if (tokenMess.length>1 && tokenMess[1].equals(TRIGGER)){
            String res = "? \n";
            for (Trigger t : api.triggers().findAll()){
                res+= t.getName() + "\n";
            }
            return res;
        }
        return "Allowed tokens: trigger";
    }
}
