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
package net.playeranalytics.plan.storage.database;

import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DatabaseSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.MySQLDB;
import com.djrapitops.plan.storage.database.SQLiteDB;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ForgeDBSystem extends DBSystem {

    @Inject
    public ForgeDBSystem(PlanConfig config, Locale locale, MySQLDB mySQLDB, SQLiteDB.Factory sqLiteDB, PluginLogger logger) {
        super(config, locale, sqLiteDB, logger);

        databases.add(mySQLDB);
        databases.add(sqLiteDB.usingDefaultFile());
    }

    @Override
    public void enable() {
        String dbType = config.get(DatabaseSettings.TYPE).toLowerCase().trim();
        db = getActiveDatabaseByName(dbType);
        super.enable();
    }

}
