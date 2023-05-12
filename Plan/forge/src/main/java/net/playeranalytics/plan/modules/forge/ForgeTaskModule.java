package net.playeranalytics.plan.modules.forge;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.delivery.web.ResourceWriteTask;
import com.djrapitops.plan.delivery.webserver.auth.ActiveCookieExpiryCleanupTask;
import com.djrapitops.plan.delivery.webserver.cache.JSONFileStorage;
import com.djrapitops.plan.delivery.webserver.configuration.AddressAllowList;
import com.djrapitops.plan.extension.ExtensionServerDataUpdater;
import com.djrapitops.plan.gathering.ShutdownDataPreservation;
import com.djrapitops.plan.gathering.ShutdownHook;
import com.djrapitops.plan.gathering.timed.ServerTPSCounter;
import com.djrapitops.plan.gathering.timed.SystemUsageBuffer;
import com.djrapitops.plan.settings.upkeep.ConfigStoreTask;
import com.djrapitops.plan.storage.upkeep.DBCleanTask;
import com.djrapitops.plan.storage.upkeep.LogsFolderCleanTask;
import com.djrapitops.plan.storage.upkeep.OldDependencyCacheDeletionTask;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import net.minecraft.server.level.ServerLevel;
import net.playeranalytics.plan.gathering.timed.ForgePingCounter;

@Module
public interface ForgeTaskModule {

    @Binds
    @IntoSet
    TaskSystem.Task bindTPSCounter(ServerTPSCounter<ServerLevel> tpsCounter);

    @Binds
    @IntoSet
    TaskSystem.Task bindPingCounter(ForgePingCounter pingCounter);

    @Binds
    @IntoSet
    TaskSystem.Task bindExtensionServerDataUpdater(ExtensionServerDataUpdater extensionServerDataUpdater);

    @Binds
    @IntoSet
    TaskSystem.Task bindLogCleanTask(LogsFolderCleanTask logsFolderCleanTask);

    @Binds
    @IntoSet
    TaskSystem.Task bindConfigStoreTask(ConfigStoreTask configStoreTask);

    @Binds
    @IntoSet
    TaskSystem.Task bindDBCleanTask(DBCleanTask cleanTask);

    @Binds
    @IntoSet
    TaskSystem.Task bindRamAndCpuTask(SystemUsageBuffer.RamAndCpuTask ramAndCpuTask);

    @Binds
    @IntoSet
    TaskSystem.Task bindDiskTask(SystemUsageBuffer.DiskTask diskTask);

    @Binds
    @IntoSet
    TaskSystem.Task bindShutdownHookRegistration(ShutdownHook.Registrar registrar);

    @Binds
    @IntoSet
    TaskSystem.Task bindJSONFileStorageCleanTask(JSONFileStorage.CleanTask cleanTask);

    @Binds
    @IntoSet
    TaskSystem.Task bindShutdownDataPreservation(ShutdownDataPreservation dataPreservation);

    @Binds
    @IntoSet
    TaskSystem.Task bindOldDependencyCacheDeletion(OldDependencyCacheDeletionTask deletionTask);

    @Binds
    @IntoSet
    TaskSystem.Task bindResourceWriteTask(ResourceWriteTask resourceWriteTask);

    @Binds
    @IntoSet
    TaskSystem.Task bindActiveCookieStoreExpiryTask(ActiveCookieExpiryCleanupTask activeCookieExpiryCleanupTask);

    @Binds
    @IntoSet
    TaskSystem.Task bindAddressAllowListUpdateTask(AddressAllowList addressAllowList);
}
