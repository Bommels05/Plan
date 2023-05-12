package net.playeranalytics.plan.version;

import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plan.version.VersionChecker;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class ForgeVersionChecker extends VersionChecker {

    @Inject
    public ForgeVersionChecker(@Named("currentVersion") String currentVersion, Locale locale, PlanConfig config, PluginLogger logger, RunnableFactory runnableFactory, ErrorLogger errorLogger) {
        super(currentVersion, locale, config, logger, runnableFactory, errorLogger);
    }
}
