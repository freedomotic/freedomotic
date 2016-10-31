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

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import sun.misc.BASE64Encoder;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.plugins.devices.telegrambot.TelegramBot;
import com.freedomotic.reactions.Command;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
    final private String START = "✅ Start";
    final private String STOP = "⛔️ Stop";
    final private String DELETE = "🚫 Delete";
    final private String BACK = "⬅️  Back";
    final private String NEXT = "Next ➡️";
    final private String INDEX_OUT_OF_RANGE = "Requested index is out of range!";
    final private String ERROR = "There was an error during your request.";
    final private String USER_AGENT = "Mozilla/5.0";
    final private String API_USER = "admin";
    final private String API_PW = "admin";

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

        if (update.hasMessage()) {
            Message message = update.getMessage();

            if (message.hasText()) {
                String[] input = message.getText().split(" ");
                if (input[0].equals("/start")) {
                    SendMessage sendMessagerequest = new SendMessage();
                    sendMessagerequest.setChatId(message.getChatId().toString());
                    sendMessagerequest.setText("Hello");

                    KeyboardRow row = new KeyboardRow();
                    row.add(new KeyboardButton("Plugins"));
                    row.add(new KeyboardButton("Things"));
                    ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
                    markup.setResizeKeyboard(true);
                    ArrayList<KeyboardRow> rows = new ArrayList<>();
                    rows.add(row);
                    markup.setKeyboard(rows);
                    sendMessagerequest.setReplyMarkup(markup);

                    try {
                        sendMessage(sendMessagerequest);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else if (input[0].equals("Plugins")) {
                    SendMessage sendMessagerequest = new SendMessage();
                    sendMessagerequest.setChatId(message.getChatId().toString());
                    sendMessagerequest.setText("Here is a lust of your plugins. Please use Back and Next to navigate through them.");
                    sendMessagerequest.enableMarkdown(true);

                    sendMessagerequest.setReplyMarkup(this.getPluginView(0, -1));

                    try {
                        sendMessage(sendMessagerequest);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }

                } else if (input[0].equals("Things")) {
                    SendMessage sendMessagerequest = new SendMessage();
                    sendMessagerequest.setChatId(message.getChatId().toString());
                    sendMessagerequest.setText("Here is a lust of your things. Please use Back and Next to navigate through them.");
                    sendMessagerequest.enableMarkdown(true);

                    sendMessagerequest.setReplyMarkup(this.getThingView(0, -1));

                    try {
                        sendMessage(sendMessagerequest);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else if (input[0].equals("/move_thing")) {
                    String responseStr = "Thing moved!";
                    SendMessage sendMessageRequest = new SendMessage();
                    sendMessageRequest.setChatId(message.getChatId().toString());
                    boolean response = false;
                    try {
                        response = this.moveThing(input[1], Integer.parseInt(input[2]), Integer.parseInt(input[2]));
                    } catch (NumberFormatException | IOException e1) {
                        e1.printStackTrace();
                        responseStr = ERROR;
                    }
                    if (!response) {
                        responseStr = ERROR;
                    }

                    sendMessageRequest.setText(responseStr);
                    try {
                        sendMessage(sendMessageRequest);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackquery = update.getCallbackQuery();
            String[] data = callbackquery.getData().split(":");

            if (data[0].equals("plugins")) {
                InlineKeyboardMarkup markup = null;

                if (data[1].equals("back")) {
                    markup = this.getPluginView(Integer.parseInt(data[2]), 1);
                } else if (data[1].equals("next")) {
                    markup = this.getPluginView(Integer.parseInt(data[2]), 2);
                } else if (data[1].equals("start") || data[1].equals("stop")) {
                    boolean start = false;
                    String startStr = "stoped";
                    if (data[1].equals("start")) {
                        start = true;
                        startStr = "started";
                    }

                    boolean response = false;
                    try {
                        response = this.startPlugin(data[3], start);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    String responsetoUser = "";
                    if (response) {
                        responsetoUser = "Plugin " + startStr;
                    } else {
                        responsetoUser = ERROR;
                    }
                    try {
                        this.sendAnswerCallbackQuery(responsetoUser, true, callbackquery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else if (data[1].equals("delete")) {
                    try {
						//TODO
						/*
                         * I think this functionality should be implemented not like "click", "ok, deleted". I think there is a need of an extra step that deleting is really wanted
                         */
                        this.sendAnswerCallbackQuery("Plugin deleted", true, callbackquery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else if (data[1].equals("text")) {
                    try {
                        this.sendAnswerCallbackQuery("Please use one of the given actions below, instead.", false, callbackquery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }

                if (markup == null) {
                    try {
                        this.sendAnswerCallbackQuery(INDEX_OUT_OF_RANGE, false, callbackquery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {

                    EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
                    editMarkup.setChatId(callbackquery.getMessage().getChatId().toString());
                    editMarkup.setInlineMessageId(callbackquery.getInlineMessageId());
                    editMarkup.setMessageId(callbackquery.getMessage().getMessageId());
                    editMarkup.setReplyMarkup(markup);
                    try {
                        editMessageReplyMarkup(editMarkup);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }

                }
            } else if (data[0].equals("things")) {

                InlineKeyboardMarkup markup = null;
                if (data[1].equals("back")) {
                    markup = this.getThingView(Integer.parseInt(data[2]), 1);
                } else if (data[1].equals("next")) {
                    markup = this.getThingView(Integer.parseInt(data[2]), 2);
                } else if (data[1].equals("text")) {
                    try {
                        this.sendAnswerCallbackQuery("Please use one of the given actions below, instead.", false, callbackquery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else if (data[1].equals("click")) {
                    boolean response = false;
                    try {
                        response = this.clickThing(data[3]);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    String responseStr = ERROR;
                    if (response) {
                        responseStr = "Thing clicked!";
                    }

                    try {
                        this.sendAnswerCallbackQuery(responseStr, true, callbackquery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                if (markup == null) {
                    try {
                        this.sendAnswerCallbackQuery(INDEX_OUT_OF_RANGE, false, callbackquery);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {

                    EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
                    editMarkup.setChatId(callbackquery.getMessage().getChatId().toString());
                    editMarkup.setInlineMessageId(callbackquery.getInlineMessageId());
                    editMarkup.setMessageId(callbackquery.getMessage().getMessageId());
                    editMarkup.setReplyMarkup(markup);
                    try {
                        editMessageReplyMarkup(editMarkup);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }

                }
            }

        } else if (update.hasInlineQuery()) {
            InlineQuery inlineQuery = update.getInlineQuery();
            String[] data = inlineQuery.getQuery().split(" ");
            if (data[0].equals("move_thing")) {
                AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
                List<InlineQueryResult> results = new ArrayList<>();
                InputTextMessageContent messageContent = new InputTextMessageContent();
                messageContent.setMessageText("/move_thing " + data[1] + " " + data[2] + " " + data[3]);

                results.add(new InlineQueryResultArticle().setTitle("Move thing '" + data[1] + "' to " + data[2] + " " + data[3]).setId("move_thing:" + data[1] + ":" + data[2] + ":" + data[3]).setInputMessageContent(messageContent));
                answerInlineQuery.setResults(results);

                answerInlineQuery.setInlineQueryId(inlineQuery.getId());
                try {
                    answerInlineQuery(answerInlineQuery);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private boolean clickThing(String uuid) throws IOException {

        BASE64Encoder enc = new sun.misc.BASE64Encoder();
        String userpassword = this.API_USER + ":" + this.API_PW;
        String encodedAuthorization = enc.encode(userpassword.getBytes());

        String url = "http://api.freedomotic.com:9111/v3/things/" + uuid + "/click";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
        con.setRequestMethod("POST");

        int responseCode = con.getResponseCode();
        System.out.println("Response Code : " + responseCode);

        if (responseCode != 202) {
            return false;
        }
        return true;
    }

    private boolean startPlugin(String uuid, boolean start) throws IOException {
        String action = "";
        if (start) {
            action = "start";
        } else {
            action = "stop";
        }

        BASE64Encoder enc = new sun.misc.BASE64Encoder();
        String userpassword = this.API_USER + ":" + this.API_PW;
        String encodedAuthorization = enc.encode(userpassword.getBytes());

        String url = "http://api.freedomotic.com:9111/v3/plugins/" + uuid + "/" + action;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
        con.setRequestMethod("POST");

        int responseCode = con.getResponseCode();
        System.out.println("Response Code : " + responseCode);

        if (responseCode != 202) {
            return false;
        }
        return true;
    }

    private JSONArray getPlugins() throws IOException {
        BASE64Encoder enc = new sun.misc.BASE64Encoder();
        String userpassword = this.API_USER + ":" + this.API_PW;
        String encodedAuthorization = enc.encode(userpassword.getBytes());

        String url = "http://api.freedomotic.com:9111/v3/plugins";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", this.USER_AGENT);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return new JSONArray(response.toString());
    }

    private boolean moveThing(String uuid, int x, int y) throws IOException {
        BASE64Encoder enc = new sun.misc.BASE64Encoder();
        String userpassword = this.API_USER + ":" + this.API_PW;
        String encodedAuthorization = enc.encode(userpassword.getBytes());

        String url = "http://api.freedomotic.com:9111/v3/things/" + uuid + "/move/" + x + "/" + y;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
        con.setRequestMethod("POST");

        int responseCode = con.getResponseCode();
        System.out.println("Response Code : " + responseCode);

        if (responseCode != 202) {
            return false;
        }
        return true;
    }

    private JSONArray getThings() throws IOException {
        BASE64Encoder enc = new sun.misc.BASE64Encoder();
        String userpassword = this.API_USER + ":" + this.API_PW;
        String encodedAuthorization = enc.encode(userpassword.getBytes());

        String url = "http://api.freedomotic.com:9111/v3/things";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", this.USER_AGENT);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return new JSONArray(response.toString());
    }

    public InlineKeyboardMarkup getPluginView(int index, int action) {
        JSONArray jsonArray = null;
        try {
            jsonArray = this.getPlugins();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        /*
         * action = 1 -> back
         * action = 2 -> next
         * action = -1 -> nothing
         */
        if (action == 1 && index > 0) {
            index--;
        } else if ((action == 1 && index == 0)) {
            return null;
        } else if (action == 2 && index >= jsonArray.length() - 1) {
            return null;
        } else if (action == 2) {
            index++;
        }

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        JSONObject objectJson = (JSONObject) jsonArray.get(index);
        rowInline.add(new InlineKeyboardButton().setText(objectJson.getString("pluginName")).setCallbackData("plugins:text:" + index + ":" + objectJson.get("uuid")));

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline2.add(new InlineKeyboardButton().setText(BACK).setCallbackData("plugins:back:" + index + ":" + objectJson.get("uuid")));
        rowInline2.add(new InlineKeyboardButton().setText(NEXT).setCallbackData("plugins:next:" + index + ":" + objectJson.get("uuid")));

        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        rowInline3.add(new InlineKeyboardButton().setText(START).setCallbackData("plugins:start:" + index + ":" + objectJson.get("uuid")));
        rowInline3.add(new InlineKeyboardButton().setText(STOP).setCallbackData("plugins:stop:" + index + ":" + objectJson.get("uuid")));
        rowInline3.add(new InlineKeyboardButton().setText(DELETE).setCallbackData("plugins:delete:" + index + ":" + objectJson.get("uuid")));

        rowsInline.add(rowInline);
        rowsInline.add(rowInline3);
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

    public InlineKeyboardMarkup getThingView(int index, int action) {
        JSONArray jsonArray = null;
        try {
            jsonArray = this.getThings();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        /*
         * action = 1 -> back
         * action = 2 -> next
         * action = -1 -> nothing
         */

        if (action == 1 && index > 0) {
            index--;
        } else if ((action == 1 && index == 0)) {
            return null;
        } else if (action == 2 && index >= jsonArray.length() - 1) {
            return null;
        } else if (action == 2) {
            index++;
        }
        JSONObject objectJson = (JSONObject) jsonArray.get(index);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText(objectJson.getString("name")).setCallbackData("things:text:" + index + ":" + objectJson.getString("uuid")));

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline2.add(new InlineKeyboardButton().setText(BACK).setCallbackData("things:back:" + index + ":" + objectJson.getString("uuid")));
        rowInline2.add(new InlineKeyboardButton().setText(NEXT).setCallbackData("things:next:" + index + ":" + objectJson.getString("uuid")));

        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        rowInline3.add(new InlineKeyboardButton().setText("Click").setCallbackData("things:click:" + index + ":" + objectJson.getString("uuid")));
        rowInline3.add(new InlineKeyboardButton().setText("Move").setSwitchInlineQueryCurrentChat("move_thing " + objectJson.getString("uuid") + " {x} " + "{y} "));

        rowsInline.add(rowInline);
        rowsInline.add(rowInline3);
        rowsInline.add(rowInline2);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
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
}
