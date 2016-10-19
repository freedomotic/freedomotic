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
package org.telegram.updateshandlers;

import com.freedomotic.app.Freedomotic;
import com.freedomotic.plugins.devices.telegrambot.TelegramBot;
import com.freedomotic.reactions.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.Commands;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

/**
 *
 * @author mauro
 */
public class FreedomoticBotHandlers extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(FreedomoticBotHandlers.class.getName());

    private String botToken;
    private String botUsername;
    private String chatID;

    /**
     *
     * @param botToken
     * @param botUsername
     * @param chatID
     */
    public FreedomoticBotHandlers(String botToken, String botUsername, String chatID) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.chatID = chatID;
    }

    /**
     *
     * @return
     */
    @Override
    public String getBotUsername() {
        return botUsername;
    }

    /**
     *
     * @return
     */
    @Override
    public String getBotToken() {
        return botToken;
    }

    /**
     *
     * @param update
     */
    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Message message = update.getMessage();
                if (message.hasText() || message.hasLocation()) {
                    handleIncomingMessage(message);
                }
            }
        } catch (Exception e) {
            LOG.error(Freedomotic.getStackTraceInfo(e));
        }
    }
    
    /**
     * 
     * @param message
     * @throws TelegramApiException 
     */

    private void handleIncomingMessage(Message message) throws TelegramApiException {

        //String[] commandParts = message.getText().split(" ", 2);
        String command = message.getText();
        if (command.startsWith(Commands.HELP_COMMAND)) {
            sendMessageToChannel(chatID, Commands.HELP_TEXT);
        } else if (command.startsWith(Commands.LIST_COMMAND)) {
            sendMessageToChannel(chatID, Commands.LIST_TEXT);
        } else if (command.startsWith(Commands.EXECUTE_COMMAND)) {
            executeCommand(command.substring(command.indexOf("["),command.indexOf("]")));
        }
    }

    /**
     * Sends a message to the channel.
     * 
     * @param chatID chat identifier
     * @param text message to send
     */
    public void sendMessageToChannel(String chatID, String text) {
        //create an object that contains the information to send 
        SendMessage message = new SendMessage();
        message.setChatId(chatID); 
        message.setText(text);
        try {
            sendMessage(message);
        } catch (TelegramApiException ex) {
            LOG.error("Error sending the message ", ex);
        }
    }

    /**
     * 
     * @param commandToExecute 
     */
    private void executeCommand(String commandToExecute) {
        Command nlpCommand = new Command();
        nlpCommand.setName("Recognize command with NLP");
        nlpCommand.setReceiver("app.commands.interpreter.nlp");
        //nlpCommand.setDescription("A free-form text command to be interpreted by a NLP module");
        nlpCommand.setProperty("text", commandToExecute);
        nlpCommand.setReplyTimeout(1000);
        Command reply = Freedomotic.sendCommand(nlpCommand);

        if (reply != null) {
            String executedCommand = reply.getProperty("result");
            if (executedCommand != null) {
                sendMessageToChannel(chatID, "Comando eseguito!");
            } else {
                sendMessageToChannel(chatID, "Comando non eseguito!");
            }
        }
    }
}
