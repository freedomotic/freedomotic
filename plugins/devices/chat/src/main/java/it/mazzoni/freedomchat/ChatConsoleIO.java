/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.mazzoni.freedomchat;

import asg.cliche.Input;
import asg.cliche.Output;
import asg.cliche.OutputConversionEngine;
import asg.cliche.ShellManageable;
import asg.cliche.TokenException;
import asg.cliche.util.Strings;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
public class ChatConsoleIO implements Input,Output, ShellManageable{

    private Chat chat;
    private String outputMessage = "";
    public ChatConsoleIO(Chat chat){
        this.chat = chat;
    }
    
    @Override
    public String readCommand(List<String> list) {
        
        return Strings.joinStrings(list, true, '/');
        
    }

    @Override
       public void output(Object obj, OutputConversionEngine oce) {
        if (obj == null) {
            return;
        } else {
            obj = oce.convertOutput(obj);
        }

        if (obj.getClass().isArray()) {
            int length = Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                output(Array.get(obj, i), 0, oce);
            }
        } else if (obj instanceof Collection) {
            for (Object elem : (Collection)obj) {
                output(elem, 0, oce);
            }
        } else {
            output(obj, 0, oce);
        }
    }

    private void output(Object obj, int indent, OutputConversionEngine oce) {
        if (obj == null) {
            return;
        }

        if (obj != null) {
            obj = oce.convertOutput(obj);
        }

        for (int i = 0; i < indent; i++) {
            print("\t");
        }

        if (obj == null) {
            println("(null)");
        } else if (obj.getClass().isPrimitive() || obj instanceof String) {
            println(obj);
        } else if (obj.getClass().isArray()) {
            println("Array");
            int length = Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                output(Array.get(obj, i), indent + 1, oce);
            }
        } else if (obj instanceof Collection) {
            println("Collection");
            for (Object elem : (Collection)obj) {
                output(elem, indent + 1, oce);
            }
        } else if (obj instanceof Throwable) {
            println(obj); // class and its message
            ((Throwable)obj).printStackTrace();
        } else {
            println(obj);
        }
    }

    @Override
    public void outputHeader(String text) {
        if (text != null) {
            println(text);
        }
    }

    private void print(Object x) {
       outputMessage =outputMessage.concat(x.toString());     
    }

    private void println(Object x) {
        try {
            chat.sendMessage(outputMessage.concat(x.toString()));
            outputMessage="";
        } catch (XMPPException ex) {
            Logger.getLogger(ChatConsoleIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


   private void printErr(Object x) {
       outputMessage =outputMessage.concat(x.toString());     
    }

    private void printlnErr(Object x) {
      try {
            chat.sendMessage(outputMessage.concat(x.toString()));
            outputMessage="";
        } catch (XMPPException ex) {
            Logger.getLogger(ChatConsoleIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void outputException(String input, TokenException error) {
        int errIndex = error.getToken().getIndex();
        while (errIndex-- > 0) {
            printErr("-");
        }
        for (int i = 0; i < error.getToken().getString().length(); i++) {
            printErr("^");
        }
        printlnErr("");
        printlnErr(error);
    }

    @Override
    public void outputException(Throwable e) {
        printlnErr(e);
        if (e.getCause() != null) {
            printlnErr(e.getCause());
        }
    }

    @Override
    public void cliEnterLoop() {
       
    }

    @Override
    public void cliLeaveLoop() {
       
    }
    
}
