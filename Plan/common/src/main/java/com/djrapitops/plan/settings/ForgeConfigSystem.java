package com.djrapitops.plan.settings;

import com.djrapitops.plan.settings.config.ConfigReader;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.changes.ConfigUpdater;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.network.ServerSettingsManager;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import java.io.IOException;

public class ForgeConfigSystem extends ConfigSystem {

    private final ConfigUpdater configUpdater;
    private final ServerSettingsManager serverSettingsManager;

    @Inject
    public ForgeConfigSystem(PlanFiles files, PlanConfig config, ConfigUpdater configUpdater, ServerSettingsManager serverSettingsManager, Theme theme, PluginLogger logger, ErrorLogger errorLogger) {
        super(files, config, theme, logger, errorLogger);
        this.configUpdater = configUpdater;
        this.serverSettingsManager = serverSettingsManager;
    }

    @Override
    public void enable() {
        super.enable();
        if (config.isTrue(PluginSettings.PROXY_COPY_CONFIG)) {
            serverSettingsManager.enable();
        }
    }

    @Override
    public void disable() {
        serverSettingsManager.disable();
        super.disable();
    }

    @Override
    protected void copyDefaults() throws IOException {
        configUpdater.applyConfigUpdate(config);
        try (ConfigReader reader = new ConfigReader(files.getResourceFromJar("config.yml").asInputStream())) {
            config.copyMissing(reader.read());
        }
    }
}
