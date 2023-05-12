package net.playeranalytics.plugin.server;

import com.djrapitops.plan.component.Component;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class ForgePluginLogger implements PluginLogger {
    private static final String CHAT_COLOR_REGEX = "§[0-9a-fk-or]";
    private static final String MESSAGE_FORMAT = "[Plan] {}";

    private final Logger logger;

    public ForgePluginLogger(Logger logger) {
        this.logger = logger;
    }

    @NotNull
    private static String removeChatColors(String message) {
        return message.replaceAll(CHAT_COLOR_REGEX, "");
    }

    @Override
    public PluginLogger info(String message) {
        logger.info(MESSAGE_FORMAT, StringUtils.contains(message, Component.SECTION) ? removeChatColors(message) : message);
        return this;
    }

    @Override
    public PluginLogger warn(String message) {
        logger.warn(MESSAGE_FORMAT, StringUtils.contains(message, Component.SECTION) ? removeChatColors(message) : message);
        return this;
    }

    @Override
    public PluginLogger error(String message) {
        logger.error(MESSAGE_FORMAT, StringUtils.contains(message, Component.SECTION) ? removeChatColors(message) : message);
        return this;
    }

    @Override
    public PluginLogger warn(String message, Throwable throwable) {
        logger.warn(MESSAGE_FORMAT, StringUtils.contains(message, Component.SECTION) ? removeChatColors(message) : message, throwable);
        return this;
    }

    @Override
    public PluginLogger error(String message, Throwable throwable) {
        logger.error(MESSAGE_FORMAT, StringUtils.contains(message, Component.SECTION) ? removeChatColors(message) : message, throwable);
        return this;
    }
}
