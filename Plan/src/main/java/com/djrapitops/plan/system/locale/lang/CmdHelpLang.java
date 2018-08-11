package com.djrapitops.plan.system.locale.lang;

/**
 * Lang for short help messages in Commands.
 *
 * @author Rsl1122
 */
public enum CmdHelpLang implements Lang {
    ANALYZE("Command Help - /plan analyze", "View the Server Page"),
    HELP("Command Help - /plan help", "Show command list"),
    INFO("Command Help - /plan info", "Check the version of Plan"),
    INSPECT("Command Help - /plan inspect", "View a Player Page"),
    QINSPECT("Command Help - /plan qinspect", "View Player info in game"),
    SEARCH("Command Help - /plan search", "Search for a player name"),
    PLAYERS("Command Help - /plan players", "View the Players Page"),
    SERVERS("Command Help - /plan servers", "List servers in Database"),
    NETWORK("Command Help - /plan network", "View the Network Page"),
    RELOAD("Command Help - /plan reload", "Restart Plan"),
    MANAGE("Command Help - /plan manage", "Manage Plan Database"),
    WEB_REGISTER("Command Help - /plan register", "Register a Web User"),
    WEB("Command Help - /plan webuser", "Manage Web Users"),
    DEV("Command Help - /plan dev", "Development mode command"),
    SETUP("Command Help - /planbungee setup", "Toggle set-up mode"),
    CON("Command Help - /planbungee con", "Debug Bungee-Server connections"),
    DISABLE("Command Help - /planbungee disable", "Disable the plugin temporarily"),
    UPDATE("Command Help - /plan update", "Get change log link or update plugin"),

    MANAGE_MOVE("Command Help - /plan manage move", "Move data between Databases"),
    MANAGE_BACKUP("Command Help - /plan manage backup", "Backup a Database"),
    MANAGE_RESTORE("Command Help - /plan manage restore", "Restore a previous Backup"),
    MANAGE_REMOVE("Command Help - /plan manage remove", "Remove Player's data"),
    MANAGE_HOTSWAP("Command Help - /plan manage hotswap", "Change Database quickly"),
    MANAGE_CLEAR("Command Help - /plan manage clear", "Clear a Database"),
    MANAGE_CON("Command Help - /plan manage con", "Debug Server-Bungee connections"),
    MANAGE_IMPORT("Command Help - /plan manage import", "Import data from elsewhere"),
    MANAGE_DISABLE("Command Help - /plan manage disable", "Disable a feature temporarily"),
    MANAGE_SETUP("Command Help - /plan manage setup", "Set-up Server-Bungee connection"),

    WEB_LEVEL("Command Help - /plan web level", "Information about permission levels"),
    WEB_LIST("Command Help - /plan web list", "List Web Users"),
    WEB_CHECK("Command Help - /plan web check", "Inspect a Web User"),
    WEB_DELETE("Command Help - /plan web delete", "Delete a Web User"),
    MANAGE_RAW_DATA("Command Help - /plan manage raw", "View raw JSON of player data");

    private final String identifier;
    private final String defaultValue;

    CmdHelpLang(String identifier, String defaultValue) {
        this.identifier = identifier;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getDefault() {
        return defaultValue;
    }
}