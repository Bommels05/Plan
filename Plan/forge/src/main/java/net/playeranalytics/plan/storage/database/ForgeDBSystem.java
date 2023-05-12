package net.playeranalytics.plan.storage.database;

import com.djrapitops.plan.settings.config.PlanConfig;
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


}
