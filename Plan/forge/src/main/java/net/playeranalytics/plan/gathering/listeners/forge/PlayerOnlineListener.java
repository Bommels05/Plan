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
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.playeranalytics.plan.gathering.ForgePlayerPositionTracker;
import net.playeranalytics.plan.gathering.domain.ForgePlayerData;
import net.playeranalytics.plan.gathering.listeners.ForgeListener;
import net.playeranalytics.plan.gathering.listeners.events.HandshakeEvent;
import net.playeranalytics.plan.gathering.listeners.events.PlayerKickEvent;
import net.playeranalytics.plan.gathering.listeners.events.PlayerLoginEvent;

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

    @SubscribeEvent
    public void onHandshake(HandshakeEvent event) {
        try {
            ClientIntentionPacket packet = event.getPacket();
            if (packet.getIntention() == ConnectionProtocol.LOGIN) {
                String address = packet.getHostName();
                if (address != null && address.contains("\u0000")) {
                    address = address.substring(0, address.indexOf('\u0000'));
                }
                joinAddress.set(address);
            }
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(getClass(), "onHandshake").build());
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoginEvent event) {
        try {
            UUID playerUUID = event.getProfile().getId();
            ServerUUID serverUUID = serverInfo.getServerUUID();

            joinAddressCache.put(playerUUID, joinAddress.get());

            dbSystem.getDatabase().executeTransaction(new BanStatusTransaction(playerUUID, serverUUID, event.isBanned()));
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(getClass(), event.getAddress(), event.getProfile(), event.isBanned()).build());
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
