/*
 Copyright (c) Matteo Mazzoni 2012  

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */
package it.mazzoni.freedomchat;

import asg.cliche.Command;


import asg.cliche.Param;
import asg.cliche.util.Strings;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.core.NaturalLanguageProcessor;
import it.freedomotic.reactions.Reaction;
import it.freedomotic.reactions.ReactionPersistence;
import it.freedomotic.reactions.Trigger;
import it.freedomotic.reactions.TriggerPersistence;
import java.io.IOException;
import java.util.List;

public class FreedomCommands {

    @Command
    public String hello() {
        return "Hello, World!";
    }

    @Command(description = "Sends a single command", name = "cmd")
    public String cmd(
            @Param(description = "The command you'd like to execute", name = "CommandName") String... cmd) {
        return conditionedCommand("", cmd);
    }

    @Command(name = "if")
    public String ifCommand(String... mess) {
        return conditionedCommand(FreedomChat.IF, mess);
    }

    @Command(name = "when")
    public String whenCommand(String... mess) {
        return conditionedCommand(FreedomChat.WHEN, mess);
    }

    private String conditionedCommand(String type, String... tokenMess) {
        it.freedomotic.reactions.Command c;
        Trigger t = null;
        Reaction r;
        NaturalLanguageProcessor nlp2 = new NaturalLanguageProcessor();
        String triggername = "";
        int conditionSep = 0;
        if (tokenMess[0].equals(FreedomChat.IF) || tokenMess[0].equals(FreedomChat.WHEN)) {
            for (int i = 1; i < tokenMess.length; i++) {
                if (tokenMess[i].equals(FreedomChat.THEN)) {
                    triggername = FreedomChat.unsplit(tokenMess, 1, i - 1, " ");
                    conditionSep = i + 1;
                    break;
                }
            }
            t = TriggerPersistence.getTrigger(triggername);
        }

        String commandName = FreedomChat.unsplit(tokenMess, conditionSep, tokenMess.length - conditionSep, " ");
        List<NaturalLanguageProcessor.Rank> mostSimilar = nlp2.getMostSimilarCommand(commandName, 3);

        if (!mostSimilar.isEmpty() && mostSimilar.get(0).getSimilarity() > 0) {
            c = mostSimilar.get(0).getCommand();
        } else {
            return "No available commands similar to: " + commandName;
        }
        if (tokenMess[0].equals(FreedomChat.IF)) {
            Trigger NEWt = t.clone();
            NEWt.setNumberOfExecutions(1);
            r = new Reaction(NEWt, c);
            ReactionPersistence.add(r);
        } else if (tokenMess[0].equals(FreedomChat.WHEN)) {
            // do something
            r = new Reaction(t, c);
            ReactionPersistence.add(r);
        } else {
            Freedomotic.sendCommand(c);
            return c.getName() + "\n DONE.";
        }
        return "DONE";
    }
}
