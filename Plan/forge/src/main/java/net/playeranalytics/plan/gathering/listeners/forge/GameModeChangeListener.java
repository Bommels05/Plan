package net.playeranalytics.plan.gathering.listeners.forge;

import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.WorldAliasSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.playeranalytics.plan.gathering.listeners.ForgeListener;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

public class GameModeChangeListener implements ForgeListener {

    private final WorldAliasSettings worldAliasSettings;
    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final ErrorLogger errorLogger;

    private boolean isEnabled = false;
    private boolean wasRegistered = false;

    @Inject
    public GameModeChangeListener(WorldAliasSettings worldAliasSettings, ServerInfo serverInfo, DBSystem dbSystem, ErrorLogger errorLogger) {
        this.worldAliasSettings = worldAliasSettings;
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.errorLogger = errorLogger;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGameModeChange(PlayerEvent.PlayerChangeGameModeEvent event) {
        if (!this.isEnabled) {
            return;
        }
        try {
            actOnEvent((ServerPlayer) event.getPlayer(), event.getNewGameMode());
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(getClass(), event.getPlayer(), event.getNewGameMode()).build());
        }
    }

    private void actOnEvent(ServerPlayer player, GameType newGameMode) {
        UUID uuid = player.getUUID();
        long time = System.currentTimeMillis();
        String gameMode = newGameMode.name();
        String worldName = player.getLevel().dimension().location().toString();

        dbSystem.getDatabase().executeTransaction(new StoreWorldNameTransaction(serverInfo.getServerUUID(), worldName));
        worldAliasSettings.addWorld(worldName);

        Optional<ActiveSession> cachedSession = SessionCache.getCachedSession(uuid);
        cachedSession.ifPresent(session -> session.changeState(worldName, gameMode, time));
    }

    @Override
    public void register() {
        if (this.wasRegistered) {
            return;
        }

        MinecraftForge.EVENT_BUS.register(this);

        this.enable();
        this.wasRegistered = true;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

    @Override
    public void enable() {
        this.isEnabled = true;
    }

    @Override
    public void disable() {
        this.isEnabled = false;
    }

}
