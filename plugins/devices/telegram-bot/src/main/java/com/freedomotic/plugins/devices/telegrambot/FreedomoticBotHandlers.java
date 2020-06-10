/**
 *
 * Copyright (c) 2009-2020 Freedomotic Team http://freedomotic.com
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

import com.freedomotic.api.API;
import com.freedomotic.api.Client;
import com.freedomotic.api.Plugin;
import com.freedomotic.api.Protocol;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.behaviors.BehaviorLogic;
import com.freedomotic.environment.Room;
import com.freedomotic.events.ObjectReceiveClick;
import com.freedomotic.i18n.I18n;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.model.object.EnvObject;
import com.freedomotic.reactions.Command;
import com.freedomotic.things.EnvObjectLogic;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

/**
 *
 * @author Mauro Cicolella
 */
public class FreedomoticBotHandlers extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(FreedomoticBotHandlers.class.getName());
    private final String botToken;
    private final String botUsername;
    private final String chatID;
    private final String START = "✅ ";
    private final String STOP = "⛔️ ";
    private final String DELETE = "🚫 ";
    private final String BACK = "⬅️ ";
    private final String NEXT = "➡️ ";
    private final API api;
    private final I18n i18n;

    private enum excludedPlugins {

        DELAYER, PLUGINS_REMOTE_CONTROLLER, RESTAPI_V3, SCHEDULER, SUCCESSFUL_TEST, TELEGRAM_BOT
    };

    /**
     *
     * @param botToken
     * @param botUsername
     * @param chatID
     */
    public FreedomoticBotHandlers(String botToken, String botUsername, String chatID, Protocol master) {
        this.api = master.getApi();
        this.i18n = api.getI18n();
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.chatID = chatID;
    }

    /**
     *
     * @return the bot username
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

    public String getChatID() {
        return chatID;
    }

    /**
     *
     * @param update
     */
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()) {
            Message message = update.getMessage();
            SendMessage sendMessagerequest;
            if (message.hasText()) {

                //manage '/execute' commands
                if (message.getText().startsWith("/execute")) {
                    executeCommand(message.getText().substring(message.getText().indexOf("["), message.getText().indexOf("]")));
                    mainMenu(message.getChatId().toString());
                } else {
                    String[] input = message.getText().split(" ");

                    if (input[0].equalsIgnoreCase("/start")) {
                        mainMenu(message.getChatId().toString());
                    } else if (input[0].equalsIgnoreCase(i18n.msg("plugins").trim())) {

                        sendTelegramMessage(message.getChatId().toString(),
                                i18n.msg("plugins_message"),
                                true,
                                this.getPluginView(0, -1));

                    } else if (input[0].equalsIgnoreCase(i18n.msg("rooms").trim())) {
                        sendTelegramMessage(message.getChatId().toString(),
                                i18n.msg("rooms_message"),
                                true,
                                this.getRoomView(0, -1));

                    } else if (input[0].equalsIgnoreCase(i18n.msg("objects").trim())) {
                        sendTelegramMessage(message.getChatId().toString(),
                                i18n.msg("things_message"),
                                true,
                                this.getThingView(0, -1));

                    } else if (input[0].equalsIgnoreCase(i18n.msg("languages").trim())) {
                        sendTelegramMessage(message.getChatId().toString(),
                                i18n.msg("languages_message"),
                                true,
                                this.getLanguageView(0, -1));

                    }

                }
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackquery = update.getCallbackQuery();
            String[] data = callbackquery.getData().split(":");
            // add string variable
            String section = data[0];
            String action = data[1];
            String target = data[2];

            if (section.equals("plugins")) {
                InlineKeyboardMarkup markup = null;
                String startStr = "";
                String responseToUser = "";
                boolean start = false;
                boolean response = false;

                switch (action) {

                    case "back":
                        markup = this.getPluginView(Integer.parseInt(target), 1);
                        break;

                    case "next":
                        markup = this.getPluginView(Integer.parseInt(target), 2);
                        break;

                    case "start":
                        start = false;
                        startStr = "stopped";
                        if (action.equals("start")) {
                            start = true;
                            startStr = "started";
                        }

                        response = false;
                        try {
                            response = this.startPlugin(data[3], start);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        responseToUser = "";
                        if (response) {
                            responseToUser = "Plugin " + startStr;
                        } else {
                            responseToUser = i18n.msg("error_request");
                        }
                        try {
                            this.sendAnswerCallbackQuery(responseToUser, true, callbackquery);
                            markup = getPluginView(Integer.parseInt(target) - 1, 2);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        break;

                    case "stop":
                        start = false;
                        startStr = "stopped";
                        if ("start".equalsIgnoreCase(startStr)) {
                            start = true;
                            startStr = "started";
                        }

                        response = false;
                        try {
                            response = this.startPlugin(data[3], start);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        responseToUser = "";
                        if (response) {
                            responseToUser = "Plugin " + startStr;
                        } else {
                            responseToUser = i18n.msg("error_request");
                        }
                        try {
                            this.sendAnswerCallbackQuery(responseToUser, true, callbackquery);
                            markup = getPluginView(Integer.parseInt(target) - 1, 2);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        break;

                    case "text":
                        try {
                            this.sendAnswerCallbackQuery("Please use one of the given actions below, instead.", false, callbackquery);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        break;

                }

                if (markup == null) {
                    try {
                        this.sendAnswerCallbackQuery(i18n.msg("index_out_of_range"), false, callbackquery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    editMessageMarkup(
                            callbackquery.getMessage().getChatId().toString(),
                            callbackquery.getInlineMessageId(),
                            callbackquery.getMessage().getMessageId(),
                            markup);
                }
            } else if ("things".equalsIgnoreCase(action)) {
                InlineKeyboardMarkup markup = null;
                if ("back".equalsIgnoreCase(action)) {
                    markup = this.getThingView(Integer.parseInt(target), 1);
                } else if ("next".equalsIgnoreCase(action)) {
                    markup = this.getThingView(Integer.parseInt(target), 2);
                } else if ("text".equalsIgnoreCase(action)) {
                    try {
                        this.sendAnswerCallbackQuery("Please use one of the given actions below, instead.", false, callbackquery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else if (action.equals("click")) {
                    boolean response = false;
                    try {
                        response = this.clickThing(data[3]);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    String responseStr = i18n.msg("error_request");
                    if (response) {
                        responseStr = "Thing clicked!";
                    }

                    try {
                        this.sendAnswerCallbackQuery(responseStr, true, callbackquery);
                        markup = getThingView(Integer.parseInt(target) - 1, 2);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else if ("status".equalsIgnoreCase(action)) {
                    String response = "";
                    response = this.getThingStatus(data[3]);
                    try {
                        this.sendAnswerCallbackQuery(response, true, callbackquery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                if (markup == null) {
                    try {
                        this.sendAnswerCallbackQuery(i18n.msg("index_out_of_range"), false, callbackquery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    editMessageMarkup(
                            callbackquery.getMessage().getChatId().toString(),
                            callbackquery.getInlineMessageId(),
                            callbackquery.getMessage().getMessageId(),
                            markup);
                }
            } else if ("languages".equalsIgnoreCase(section)) {
                InlineKeyboardMarkup markup = null;
                if ("back".equalsIgnoreCase(action)) {
                    markup = this.getLanguageView(Integer.parseInt(target), 1);
                } else if ("next".equalsIgnoreCase(action)) {
                    markup = this.getLanguageView(Integer.parseInt(target), 2);
                } else if ("text".equalsIgnoreCase(action)) {
                    try {
                        this.sendAnswerCallbackQuery("Please use one of the given actions below, instead.", false, callbackquery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else if ("set".equalsIgnoreCase(action)) {
                    boolean response = this.setLanguage(data[3]);
                    String responseStr = i18n.msg("error_request");
                    if (response) {
                        responseStr = "Language changed!";
                    }
                    mainMenu(this.getChatID());
                    /**
                     * try { this.sendAnswerCallbackQuery(responseStr, true,
                     * callbackquery); markup =
                     * getThingView(Integer.parseInt(target) - 1, 2); } catch
                     * (TelegramApiException e) { e.printStackTrace(); }*
                     */
                }
                if (markup == null) {
                    try {
                        this.sendAnswerCallbackQuery(i18n.msg("index_out_of_range"), false, callbackquery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    editMessageMarkup(
                            callbackquery.getMessage().getChatId().toString(),
                            callbackquery.getInlineMessageId(),
                            callbackquery.getMessage().getMessageId(),
                            markup);
                }
            } else if ("rooms".equalsIgnoreCase(section)) {
                InlineKeyboardMarkup markup = null;
                if ("back".equalsIgnoreCase(action)) {
                    markup = this.getRoomView(Integer.parseInt(target), 1);
                } else if ("next".equalsIgnoreCase(action)) {
                    markup = this.getRoomView(Integer.parseInt(target), 2);
                } else if ("things-list".equalsIgnoreCase(action)) {
                    markup = this.getRoomThingView(0, -1, String.valueOf(data[3]));
                } else if ("text".equalsIgnoreCase(action)) {
                    try {
                        this.sendAnswerCallbackQuery("Please use one of the given actions below, instead.", false, callbackquery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                if (markup == null) {
                    try {
                        this.sendAnswerCallbackQuery(i18n.msg("index_out_of_range"), false, callbackquery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    editMessageMarkup(
                            callbackquery.getMessage().getChatId().toString(),
                            callbackquery.getInlineMessageId(),
                            callbackquery.getMessage().getMessageId(),
                            markup);
                }
            } else if ("rooms-things".equalsIgnoreCase(section)) {
                InlineKeyboardMarkup markup = null;
                if ("back".equalsIgnoreCase(action)) {
                    markup = this.getRoomThingView(Integer.parseInt(target), 1, String.valueOf(data[3]));
                } else if ("next".equalsIgnoreCase(action)) {
                    markup = this.getRoomThingView(Integer.parseInt(target), 2, String.valueOf(data[3]));
                } else if ("click".equalsIgnoreCase(action)) {
                    boolean response = false;
                    try {
                        response = this.clickThing(data[3]);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    String responseStr = i18n.msg("error_request");
                    if (response) {
                        responseStr = "Thing clicked!";
                    }
                    try {
                        this.sendAnswerCallbackQuery(responseStr, true, callbackquery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else if ("text".equalsIgnoreCase(action)) {
                    try {
                        this.sendAnswerCallbackQuery("Please use one of the given actions below, instead.", false, callbackquery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                if (markup == null) {
                    try {
                        this.sendAnswerCallbackQuery(i18n.msg("index_out_of_range"), false, callbackquery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    editMessageMarkup(
                            callbackquery.getMessage().getChatId().toString(),
                            callbackquery.getInlineMessageId(),
                            callbackquery.getMessage().getMessageId(),
                            markup);
                }
            }
        }
    }

    /**
     * Sends an ObjectReceiveClick event.
     *
     * @param uuid thing uuid
     * @return true if the operation is completed, false otherwise
     * @throws IOException
     */
    private boolean clickThing(String uuid) throws IOException {

        try {
            EnvObjectLogic el = api.things().findOne(uuid);
            ObjectReceiveClick event = new ObjectReceiveClick(this, el, ObjectReceiveClick.SINGLE_CLICK);
            Freedomotic.sendEvent(event);
            return true;
        } catch (Exception e) {
            LOG.error("Error clicking on a thing", e);
            return false;
        }
    }

    /**
     * Sets a language.
     *
     * @param locale language to set
     * @return true if the plugin is set, false otherwise
     */
    private boolean setLanguage(String locale) {
        i18n.setDefaultLocale(locale);
        return true;
    }

    /**
     * Returns the plugin status.
     *
     * @param uuid plugin uuid
     * @return true if the plugin is running, false otherwise
     * @throws IOException
     */
    private boolean pluginStatus(String uuid) {
        Plugin p = getPlugin(uuid);
        if (p.isRunning()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Starts the plugin.
     *
     * @param uuid
     * @param start
     * @return
     * @throws IOException
     */
    private boolean startPlugin(String uuid, boolean start) throws IOException {
        if (start) {
            if (!api.getAuth().isPermitted("sys:plugins:start:" + uuid)) {
                LOG.error("Plugin starting not allowed");
                return false;
            } else {
                Plugin p = getPlugin(uuid);
                if (p != null) {
                    if (p.isAllowedToStart()) {
                        p.start();
                        return true;
                    } else {
                        LOG.error("Plugin \"{}\" not found", p.getName());
                        return false;
                    }
                }
            }
        } else {
            if (!api.getAuth().isPermitted("sys:plugins:stop:" + uuid)) {
                LOG.error("Plugin stopping not allowed");
                return false;
            } else {
                Plugin p = getPlugin(uuid);
                if (p != null) {
                    if (p.isRunning()) {
                        p.stop();
                        return true;
                    } else {
                        LOG.error("Plugin \"{}\" not found", p.getName());
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns the list of plugins.
     *
     *
     * @return the list of loaded plugins
     */
    private List<Plugin> getPluginsList() {
        List<Plugin> plugins = new ArrayList<Plugin>();

        for (Client c : api.getClients("plugin")) {
            if (!isInEnum(((Plugin) c).getName().toUpperCase().replace(" ", "_"), excludedPlugins.class)) {
                plugins.add(
                        (Plugin) c);
            }
        }
        return plugins;
    }

    /**
     * Returns the list of rooms.
     *
     *
     * @return the list of rooms
     */
    private List<Zone> getRoomsList() {
        List<Zone> rooms = new ArrayList<Zone>();
        for (Room r : api.environments().findAll().get(0).getRooms()) {
            rooms.add(r.getPojo());
        }
        return rooms;
    }

    /**
     * Returns the list of things.
     *
     * @return the list of things
     */
    private List<EnvObject> getThingsList() {
        List<EnvObject> things = new ArrayList<>();
        for (EnvObjectLogic objLogic : api.things().findAll()) {
            things.add(objLogic.getPojo());
        }
        return things;
    }

    /**
     * Returns the list of things into a specific room.
     *
     * @param uuid the room uuid
     * @return a list of things
     */
    private List<EnvObject> getThingsListInRoom(String uuid) {
        List<EnvObject> things = new ArrayList<>();
        things.addAll(api.environments().findAll().get(0).getZoneByUuid(uuid).getPojo().getObjects());
        return things;
    }

    /**
     * Returns the thing status.
     *
     * @return a string containing all behaviors and their values
     */
    private String getThingStatus(String uuid) {
        String status = "";

        EnvObjectLogic thing = api.things().findOne(uuid);
        status = thing.getPojo().getName() + "\n\n";
        for (BehaviorLogic b : thing.getBehaviors()) {
            status += b.getName() + " " + b.getValueAsString() + "\n";
        }
        return status;
    }

    /**
     * Returns the main thing status. For each thing class a "main" behavior is
     * reported.
     *
     * @param uuid thing uuid
     * @return
     */
    private String getMainThingStatus(String uuid) {
        String status = "";
        EnvObjectLogic thing = api.things().findOne(uuid);

        switch (thing.getPojo().getSimpleType()) {

            case "barometer":
                status = "pressure: " + thing.getBehavior("pressure").getValueAsString();
                break;

            case "electricdevice":
                status = "powered: " + thing.getBehavior("powered").getValueAsString();
                break;

            case "gate":
                status = "open: " + thing.getBehavior("open").getValueAsString();
                break;

            case "hygrometer":
                status = "humidity: " + thing.getBehavior("humidity").getValueAsString();
                break;

            case "light":
                status = "powered: " + thing.getBehavior("powered").getValueAsString();
                break;

            case "lightsensor":
                status = "luminosity: " + thing.getBehavior("luminosity").getValueAsString();
                break;

            case "thermometer":
                status = "temperature: " + thing.getBehavior("temperature").getValueAsString();
                break;

            case "thermostat":
                status = "temperature: " + thing.getBehavior("temperature").getValueAsString();
                break;

        }
        return status;
    }

    /**
     * Shows the main menu.
     *
     * @param message
     */
    private void mainMenu(String message) {

        SendMessage sendMessagerequest = new SendMessage();
        sendMessagerequest.setChatId(message);
        sendMessagerequest.setText(i18n.msg("welcome_message"));

        // main keyboard
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(i18n.msg("rooms")));
        row.add(new KeyboardButton(i18n.msg("objects")));
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(i18n.msg("plugins")));
        //row2.add(new KeyboardButton(i18n.msg("languages")));
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        ArrayList<KeyboardRow> rows = new ArrayList<>();
        rows.add(row);
        rows.add(row2);
        markup.setKeyboard(rows);
        sendMessagerequest.setReplyMarkup(markup);

        try {
            sendMessage(sendMessagerequest);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param index
     * @param action
     * @return
     */
    public InlineKeyboardMarkup getPluginView(int index, int action) {

        List<Plugin> pluginsList = this.getPluginsList();

        /*
         * action = 1 -> back
         * action = 2 -> next
         * action = -1 -> nothing
         */
        if (action == 1 && index > 0) {
            index--;
        } else if ((action == 1 && index == 0)) {
            return null;
        } else if (action == 2 && index >= pluginsList.size() - 1) {
            return null;
        } else if (action == 2) {
            index++;
        }

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        Plugin plugin = pluginsList.get(index);
        rowInline.add(new InlineKeyboardButton().setText(plugin.getName() + "\n" + plugin.getStatus()).setCallbackData("plugins:text:" + index + ":" + plugin.getClassName()));

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline2.add(new InlineKeyboardButton().setText(BACK + i18n.msg("back")).setCallbackData("plugins:back:" + index + ":" + plugin.getClassName()));
        rowInline2.add(new InlineKeyboardButton().setText(i18n.msg("next") + NEXT).setCallbackData("plugins:next:" + index + ":" + plugin.getClassName()));

        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        if (!plugin.isRunning()) {
            rowInline3.add(new InlineKeyboardButton().setText(START + i18n.msg("start")).setCallbackData("plugins:start:" + index + ":" + plugin.getClassName()));
        } else {
            rowInline3.add(new InlineKeyboardButton().setText(STOP + i18n.msg("stop")).setCallbackData("plugins:stop:" + index + ":" + plugin.getClassName()));
        }

        rowsInline.add(rowInline);
        rowsInline.add(rowInline3);
        rowsInline.add(rowInline2);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }

    /**
     *
     * @param index
     * @param action
     * @return
     */
    public InlineKeyboardMarkup getRoomView(int index, int action) {

        List<Zone> roomsList = this.getRoomsList();

        /*
         * action = 1 -> back
         * action = 2 -> next
         * action = -1 -> nothing
         */
        if (action == 1 && index > 0) {
            index--;
        } else if ((action == 1 && index == 0)) {
            return null;
        } else if (action == 2 && index >= roomsList.size() - 1) {
            return null;
        } else if (action == 2) {
            index++;
        }

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        Zone room = roomsList.get(index);
        rowInline.add(new InlineKeyboardButton().setText(room.getName()).setCallbackData("rooms:text:" + index + ":" + room.getUuid()));

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline2.add(new InlineKeyboardButton().setText(BACK + i18n.msg("back")).setCallbackData("rooms:back:" + index + ":" + room.getUuid()));
        rowInline2.add(new InlineKeyboardButton().setText(i18n.msg("objects")).setCallbackData("rooms:things-list:" + index + ":" + room.getUuid()));
        rowInline2.add(new InlineKeyboardButton().setText(i18n.msg("next") + NEXT).setCallbackData("rooms:next:" + index + ":" + room.getUuid()));

        rowsInline.add(rowInline);
        rowsInline.add(rowInline2);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }

    public void sendAnswerCallbackQuery(String text, boolean alert, CallbackQuery callbackquery) throws TelegramApiException {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackquery.getId());
        answerCallbackQuery.setShowAlert(alert);
        answerCallbackQuery.setText(text);
        answerCallbackQuery(answerCallbackQuery);
    }

    /**
     * Sends a message to Telegram.
     *
     *
     * @param chatID
     * @param text
     * @param enableMarkdown
     * @param replyMarkup
     */
    public void sendTelegramMessage(String chatID, String text, boolean enableMarkdown, InlineKeyboardMarkup replyMarkup) {
        SendMessage sendMessagerequest = new SendMessage();
        sendMessagerequest.setChatId(chatID);
        sendMessagerequest.setText(text);
        sendMessagerequest.enableMarkdown(enableMarkdown);

        sendMessagerequest.setReplyMarkup(replyMarkup);

        try {
            sendMessage(sendMessagerequest);
        } catch (TelegramApiException e) {
            LOG.error("Error sending Telegram message", e);
        }
    }

    /**
     *
     *
     * @param chatID
     * @param inlineMessageID
     * @param messageID
     * @param markup
     */
    public void editMessageMarkup(String chatID, String inlineMessageID, Integer messageID, InlineKeyboardMarkup markup) {

        EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
        editMarkup.setChatId(chatID);
        editMarkup.setInlineMessageId(inlineMessageID);
        editMarkup.setMessageId(messageID);
        editMarkup.setReplyMarkup(markup);
        try {
            editMessageReplyMarkup(editMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param index
     * @param action
     * @return
     */
    public InlineKeyboardMarkup getThingView(int index, int action) {
        List<EnvObject> thingsList;

        thingsList = getThingsList();

        /*
         * action = 1 -> back
         * action = 2 -> next
         * action = -1 -> nothing
         */
        if (action == 1 && index > 0) {
            index--;
        } else if ((action == 1 && index == 0)) {
            return null;
        } else if (action == 2 && index >= thingsList.size() - 1) {
            return null;
        } else if (action == 2) {
            index++;
        }

        EnvObject thing = thingsList.get(index);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText(thing.getName() + "\n" + getMainThingStatus(thing.getUUID())).setCallbackData("things:text:" + index + ":" + thing.getUUID()));

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline2.add(new InlineKeyboardButton().setText(BACK + i18n.msg("back")).setCallbackData("things:back:" + index + ":" + thing.getUUID()));
        rowInline2.add(new InlineKeyboardButton().setText(i18n.msg("next") + NEXT).setCallbackData("things:next:" + index + ":" + thing.getUUID()));

        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        rowInline3.add(new InlineKeyboardButton().setText(i18n.msg("status")).setCallbackData("things:status:" + index + ":" + thing.getUUID()));
        rowInline3.add(new InlineKeyboardButton().setText(i18n.msg("click")).setCallbackData("things:click:" + index + ":" + thing.getUUID()));

        rowsInline.add(rowInline);
        rowsInline.add(rowInline3);
        rowsInline.add(rowInline2);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }

    /**
     *
     * @param index
     * @param action
     * @return
     */
    public InlineKeyboardMarkup getLanguageView(int index, int action) {
        List<Locale> languagesList;

        languagesList = api.getI18n().getAvailableLocales();

        /*
         * action = 1 -> back
         * action = 2 -> next
         * action = -1 -> nothing
         */
        if (action == 1 && index > 0) {
            index--;
        } else if ((action == 1 && index == 0)) {
            return null;
        } else if (action == 2 && index >= languagesList.size() - 1) {
            return null;
        } else if (action == 2) {
            index++;
        }

        Locale locale = languagesList.get(index);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText(locale.getDisplayCountry(i18n.getDefaultLocale()) + " - " + locale.getDisplayLanguage(locale)).setCallbackData("languages:text:" + index + ":" + locale.toString()));

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline2.add(new InlineKeyboardButton().setText(BACK + i18n.msg("back")).setCallbackData("languages:back:" + index + ":" + locale.getDisplayLanguage(locale)));
        rowInline2.add(new InlineKeyboardButton().setText(i18n.msg("set")).setCallbackData("languages:set:" + index + ":" + locale.getDisplayLanguage(locale)));
        rowInline2.add(new InlineKeyboardButton().setText(i18n.msg("next") + NEXT).setCallbackData("languages:next:" + index + ":" + locale.getDisplayLanguage(locale)));

        rowsInline.add(rowInline);
        rowsInline.add(rowInline2);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }

    /**
     *
     * @param index
     * @param action
     * @return
     */
    public InlineKeyboardMarkup getRoomThingView(int index, int action, String roomUuid) {
        List<EnvObject> thingsList;

        thingsList = getThingsListInRoom(roomUuid);

        if (!thingsList.isEmpty()) {

            /*
             * action = 1 -> back
             * action = 2 -> next
             * action = -1 -> nothing
             */
            if (action == 1 && index > 0) {
                index--;
            } else if ((action == 1 && index == 0)) {
                return null;
            } else if (action == 2 && index >= thingsList.size() - 1) {
                return null;
            } else if (action == 2) {
                index++;
            }

            EnvObject thing = thingsList.get(index);
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();

            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(new InlineKeyboardButton().setText(thing.getName()).setCallbackData("rooms-things:text:" + index + ":" + roomUuid));

            List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
            rowInline2.add(new InlineKeyboardButton().setText(BACK + i18n.msg("back")).setCallbackData("rooms-things:back:" + index + ":" + roomUuid));
            rowInline2.add(new InlineKeyboardButton().setText(i18n.msg("next") + NEXT).setCallbackData("rooms-things:next:" + index + ":" + roomUuid));

            List<InlineKeyboardButton> rowInline3 = new ArrayList<>();

            rowInline3.add(new InlineKeyboardButton().setText(i18n.msg("status")).setCallbackData("things:status:" + index + ":" + thing.getUUID()));
            rowInline3.add(new InlineKeyboardButton().setText(i18n.msg("click")).setCallbackData("rooms-things:click:" + index + ":" + thing.getUUID()));

            rowsInline.add(rowInline);
            rowsInline.add(rowInline3);
            rowsInline.add(rowInline2);

            markupInline.setKeyboard(rowsInline);

            return markupInline;
        } else {
            return null;
        }
    }

    /**
     * Returns a plugin given its name.
     *
     * @param name plugin name
     * @return the plugin or null if not found
     */
    private Plugin getPlugin(String name) {
        for (Client c : api.getClients("plugin")) {
            Plugin plug = (Plugin) c;
            if (plug.getClassName().equalsIgnoreCase(name)) {
                return plug;
            }
        }
        LOG.error("Plugin \"{}\" not found", name);
        return null;
    }

    /**
     *
     * @param commandToExecute
     */
    private void executeCommand(String commandToExecute) {
        Command nlpCommand = new Command();
        nlpCommand.setName("Send command via Telegram Bot");
        nlpCommand.setReceiver("app.commands.interpreter.nlp");
        nlpCommand.setProperty("text", commandToExecute);
        nlpCommand.setReplyTimeout(1000);
        Command reply = Freedomotic.sendCommand(nlpCommand);

        if (reply != null) {
            String executedCommand = reply.getProperty("result");
            if (executedCommand != null) {
                sendMessageToChannel(chatID, "Command \"" + commandToExecute + "\" executed!");
            } else {
                sendMessageToChannel(chatID, "Command \"" + commandToExecute + "\" not executed!");
            }
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

        if (getChatID().equalsIgnoreCase(chatID)) {
            try {
                sendMessage(message);
            } catch (TelegramApiException ex) {
                LOG.error("Error sending the message ", ex);
            }
        }

    }

    /**
     *
     * @param chatID
     * @param photoPath
     */
    public void sendPhotoToChannel(String chatID, String message, String photoPath) {
        SendPhoto sendPhotoRequest = new SendPhoto();
        sendPhotoRequest.setChatId(chatID);
        sendPhotoRequest.setNewPhoto(new File(photoPath));

        try {
            sendPhoto(sendPhotoRequest);
            sendMessageToChannel(chatID, message);
        } catch (TelegramApiException ex) {
            LOG.error("Error sending a photo", ex);
        }

    }

    /**
     *
     * @param <E>
     * @param value
     * @param enumClass
     * @return
     */
    private <E extends Enum<E>> boolean isInEnum(String value, Class<E> enumClass) {
        for (E e : enumClass.getEnumConstants()) {
            if (e.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
