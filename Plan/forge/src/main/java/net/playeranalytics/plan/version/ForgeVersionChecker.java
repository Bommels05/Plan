/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
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
