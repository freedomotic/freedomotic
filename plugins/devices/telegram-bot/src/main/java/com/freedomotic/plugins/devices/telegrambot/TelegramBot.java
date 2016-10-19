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
package com.freedomotic.plugins.devices.telegrambot;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.updateshandlers.FreedomoticBotHandlers;

/**
 *
 * @author Mauro Cicolella
 */
public class TelegramBot
        extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(TelegramBot.class.getName());
    final int POLLING_WAIT;
    private final String BOT_TOKEN = configuration.getStringProperty("bot-token", "<token>");
    private final String BOT_USERNAME = configuration.getStringProperty("bot-username", "<bot-username>");
    private final String CHAT_ID = configuration.getStringProperty("chat-id", "<chat-id>");
    private TelegramBotsApi telegramBotsApi;
    private FreedomoticBotHandlers fdBotHandler;

    /**
     *
     */
    public TelegramBot() {
        super("Telegram Bot", "/telegram-bot/telegram-bot-manifest.xml");
        POLLING_WAIT = configuration.getIntProperty("time-between-reads", 2000);
        setPollingWait(-1); //disable plugin polling
    }

    @Override
    protected void onShowGui() {
        //bindGuiToPlugin(new HelloWorldGui(this));
    }

    @Override
    protected void onHideGui() {
        setDescription("My GUI is now hidden");
    }

    @Override
    protected void onRun() {
    }

    @Override
    protected void onStart() {
        telegramBotsApi = new TelegramBotsApi();
        fdBotHandler = new FreedomoticBotHandlers(BOT_TOKEN, BOT_USERNAME, CHAT_ID);
        try {
            telegramBotsApi.registerBot(fdBotHandler);
        } catch (TelegramApiException e) {
            LOG.error("TelegramApi Exception ", Freedomotic.getStackTraceInfo(e));
        }
        LOG.info("Telegram Bot plugin started");
    }

    @Override
    protected void onStop() {
        LOG.info("Telegram Bot plugin stopped");
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        fdBotHandler.sendMessageToChannel(CHAT_ID, c.getProperty("message"));
    }

    @Override
    protected boolean canExecute(Command c) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
