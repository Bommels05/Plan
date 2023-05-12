package net.playeranalytics.plan.gathering.listeners.forge;

import com.djrapitops.plan.gathering.cache.JoinAddressCache;
import com.djrapitops.plan.gathering.domain.event.PlayerJoin;
import com.djrapitops.plan.gathering.domain.event.PlayerLeave;
import com.djrapitops.plan.gathering.events.PlayerJoinEventConsumer;
import com.djrapitops.plan.gathering.events.PlayerLeaveEventConsumer;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.BanStatusTransaction;
import com.djrapitops.plan.storage.database.transactions.events.KickStoreTransaction;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.playeranalytics.plan.gathering.ForgePlayerPositionTracker;
import net.playeranalytics.plan.gathering.domain.ForgePlayerData;
import net.playeranalytics.plan.gathering.listeners.ForgeListener;
import net.playeranalytics.plan.gathering.listeners.events.PlayerKickEvent;

import javax.inject.Inject;
import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerOnlineListener implements ForgeListener {

    private final PlayerJoinEventConsumer joinEventConsumer;
    private final PlayerLeaveEventConsumer leaveEventConsumer;
    private final JoinAddressCache joinAddressCache;

    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final ErrorLogger errorLogger;
    private final MinecraftServer server;

    private final AtomicReference<String> joinAddress = new AtomicReference<>();

    private boolean isEnabled = false;
    private boolean wasRegistered = false;

    @Inject
    public PlayerOnlineListener(PlayerJoinEventConsumer joinEventConsumer, PlayerLeaveEventConsumer leaveEventConsumer, JoinAddressCache joinAddressCache, ServerInfo serverInfo, DBSystem dbSystem, ErrorLogger errorLogger, MinecraftServer server) {
        this.joinEventConsumer = joinEventConsumer;
        this.leaveEventConsumer = leaveEventConsumer;
        this.joinAddressCache = joinAddressCache;
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.errorLogger = errorLogger;
        this.server = server;
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

    /*private void onHandshake(HandshakeC2SPacket packet) {
        try {
            if (packet.getIntendedState() == NetworkState.LOGIN) {
                String address = packet.getAddress();
                if (address != null && address.contains("\u0000")) {
                    address = address.substring(0, address.indexOf('\u0000'));
                }
                joinAddress.set(address);
            }
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(getClass(), "onHandshake").build());
        }
    }

     */
    public void onPlayerLogin(SocketAddress address, GameProfile profile, boolean banned) {
        try {
            UUID playerUUID = profile.getId();
            ServerUUID serverUUID = serverInfo.getServerUUID();

            joinAddressCache.put(playerUUID, joinAddress.get());

            dbSystem.getDatabase().executeTransaction(new BanStatusTransaction(playerUUID, serverUUID, banned));
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(getClass(), address, profile, banned).build());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerKick(PlayerKickEvent event) {
        if (!this.isEnabled) {
            return;
        }
        try {
            UUID uuid = event.getPlayer().getUUID();
            if (ForgeAFKListener.afkTracker.isAfk(uuid)) {
                return;
            }

            dbSystem.getDatabase().executeTransaction(new KickStoreTransaction(uuid));
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(getClass(), event.getPlayer()).build());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!this.isEnabled) {
            return;
        }
        try {
            actOnJoinEvent((ServerPlayer) event.getPlayer());
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(getClass(), event.getPlayer()).build());
        }
    }

    private void actOnJoinEvent(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        long time = System.currentTimeMillis();

        ForgeAFKListener.afkTracker.performedAction(playerUUID, time);

        joinEventConsumer.onJoinGameServer(PlayerJoin.builder()
                .server(serverInfo.getServer())
                .player(new ForgePlayerData(player, server, joinAddressCache.getNullableString(playerUUID)))
                .time(time)
                .build());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void beforePlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!this.isEnabled) {
            return;
        }
        leaveEventConsumer.beforeLeave(PlayerLeave.builder()
                .server(serverInfo.getServer())
                .player(new ForgePlayerData((ServerPlayer) event.getPlayer(), server, null))
                .time(System.currentTimeMillis())
                .build());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!this.isEnabled) {
            return;
        }
        try {
            actOnQuitEvent((ServerPlayer) event.getPlayer());
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(getClass(), event.getPlayer()).build());
        }
    }

    private void actOnQuitEvent(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        long time = System.currentTimeMillis();
        ForgeAFKListener.afkTracker.loggedOut(playerUUID, time);
        ForgePlayerPositionTracker.removePlayer(playerUUID);

        leaveEventConsumer.onLeaveGameServer(PlayerLeave.builder()
                .server(serverInfo.getServer())
                .player(new ForgePlayerData(player, server, null))
                .time(time)
                .build());
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
