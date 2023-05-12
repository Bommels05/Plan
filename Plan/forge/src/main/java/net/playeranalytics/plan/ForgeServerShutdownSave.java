package net.playeranalytics.plan;

import com.djrapitops.plan.gathering.ServerShutdownSave;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.minecraft.server.MinecraftServer;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;

public class ForgeServerShutdownSave extends ServerShutdownSave {

    private final MinecraftServer server;

    @Inject
    public ForgeServerShutdownSave(MinecraftServer server, Locale locale, DBSystem dbSystem, PluginLogger logger, ErrorLogger errorLogger) {
        super(locale, dbSystem, logger, errorLogger);
        this.server = server;
    }

    @Override
    protected boolean checkServerShuttingDownStatus() {
        return !server.isRunning();
    }
}
