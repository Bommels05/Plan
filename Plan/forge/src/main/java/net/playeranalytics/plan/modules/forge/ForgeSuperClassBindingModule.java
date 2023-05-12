package net.playeranalytics.plan.modules.forge;

import com.djrapitops.plan.gathering.ServerSensor;
import com.djrapitops.plan.gathering.ServerShutdownSave;
import com.djrapitops.plan.gathering.listeners.ListenerSystem;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerServerInfo;
import com.djrapitops.plan.settings.ConfigSystem;
import com.djrapitops.plan.settings.ForgeConfigSystem;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.version.VersionChecker;
import dagger.Binds;
import dagger.Module;
import net.minecraft.server.level.ServerLevel;
import net.playeranalytics.plan.ForgeServerShutdownSave;
import net.playeranalytics.plan.gathering.ForgeSensor;
import net.playeranalytics.plan.gathering.listeners.ForgeListenerSystem;
import net.playeranalytics.plan.storage.database.ForgeDBSystem;
import net.playeranalytics.plan.version.ForgeVersionChecker;

@Module
public interface ForgeSuperClassBindingModule {

    @Binds
    ServerInfo bindServerInfo(ServerServerInfo serverInfo);

    @Binds
    DBSystem bindDBSystem(ForgeDBSystem dbSystem);

    @Binds
    ConfigSystem bindConfigSystem(ForgeConfigSystem configSystem);

    @Binds
    ListenerSystem bindListenerSystem(ForgeListenerSystem listenerSystem);

    @Binds
    ServerShutdownSave bindServerShutdownSave(ForgeServerShutdownSave shutdownSave);

    @Binds
    ServerSensor<ServerLevel> bindServerSensor(ForgeSensor sensor);

    @Binds
    ServerSensor<?> bindGenericsServerSensor(ServerSensor<ServerLevel> sensor);

    @Binds
    VersionChecker bindVersionChecker(ForgeVersionChecker versionChecker);

}
