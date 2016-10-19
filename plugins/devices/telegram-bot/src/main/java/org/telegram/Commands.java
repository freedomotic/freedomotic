package org.telegram;

/**
 * Commands for the bot.
 *
 * @author Mauro Cicolella
 *
 */
public class Commands {

    // BOT COMMANDS
    public static final String COMMAND_INIT_CHAR = "/";
    // Execute command
    public static final String EXECUTE_COMMAND = COMMAND_INIT_CHAR + "execute";
    // Help command
    public static final String HELP_COMMAND = COMMAND_INIT_CHAR + "help";
    // List command
    public static final String LIST_COMMAND = COMMAND_INIT_CHAR + "list";
    // Stop command
    public static final String STOP_COMMAND = COMMAND_INIT_CHAR + "stop";

    // BOT TEXT MESSAGES 
    public static final String HELP_TEXT = "Send me the channel username where you added me as admin.";
    public static final String LIST_TEXT = "Puoi utilizzare i seguenti comandi:\n"
            + "\\execute [nome comando]\n - Esegui comando"
            + "\\help - Richiesta aiuto\n"
            + "\\list - Elenca comandi\n";

}
