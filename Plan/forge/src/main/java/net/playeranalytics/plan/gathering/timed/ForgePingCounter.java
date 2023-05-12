package net.playeranalytics.plan.gathering.timed;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.PingStoreTransaction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.playeranalytics.plan.gathering.listeners.ForgeListener;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.scheduling.TimeAmount;
import net.playeranalytics.plugin.server.Listeners;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ForgePingCounter extends TaskSystem.Task implements ForgeListener {

    private final Map<UUID, Long> startRecording;
    private final Map<UUID, List<DateObj<Integer>>> playerHistory;

    private final Listeners listeners;
    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final MinecraftServer server;

    private boolean isEnabled = false;

    @Inject
    public ForgePingCounter(Listeners listeners, PlanConfig config, DBSystem dbSystem, ServerInfo serverInfo, MinecraftServer server) {
        this.listeners = listeners;
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.server = server;
        startRecording = new ConcurrentHashMap<>();
        playerHistory = new HashMap<>();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void run() {
        if (!this.isEnabled) {
            return;
        }
        long time = System.currentTimeMillis();

        for (Map.Entry<UUID, Long> start : startRecording.entrySet()) {
            if (time >= start.getValue()) {
                addPlayer(start.getKey());
                startRecording.entrySet().remove(start);
            }
        }

        for (Map.Entry<UUID, List<DateObj<Integer>>> entry : playerHistory.entrySet()) {
            UUID uuid = entry.getKey();
            List<DateObj<Integer>> history = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player != null) {
                int ping = getPing(player);
                if (ping <= -1 || ping > TimeUnit.SECONDS.toMillis(8L)) {
                    // Don't accept bad values
                    continue;
                }
                history.add(new DateObj<>(time, ping));
                if (history.size() >= 30) {
                    dbSystem.getDatabase().executeTransaction(new PingStoreTransaction(uuid, serverInfo.getServerUUID(), new ArrayList<>(history)));
                    history.clear();
                }
            } else {
                playerHistory.entrySet().remove(entry);
            }
        }
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        Long startDelay = config.get(TimeSettings.PING_SERVER_ENABLE_DELAY);
        if (startDelay < TimeUnit.HOURS.toMillis(1L) && config.isTrue(DataGatheringSettings.PING)) {
            listeners.registerListener(this);
            long delay = TimeAmount.toTicks(startDelay, TimeUnit.MILLISECONDS);
            long period = 40L;
            runnableFactory.create(this).runTaskTimer(delay, period);
        }
        this.enable();
    }

    public void addPlayer(UUID uuid) {
        playerHistory.put(uuid, new ArrayList<>());
    }

    public void removePlayer(ServerPlayer player) {
        playerHistory.remove(player.getUUID());
        startRecording.remove(player.getUUID());
    }

    private int getPing(ServerPlayer player) {
        return player.latency;
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {;
        if (!this.isEnabled) {
            return;
        }

        Long pingDelayMs = config.get(TimeSettings.PING_PLAYER_LOGIN_DELAY);
        if (pingDelayMs >= TimeUnit.HOURS.toMillis(2L)) {
            return;
        }
        startRecording.put(event.getPlayer().getUUID(), System.currentTimeMillis() + pingDelayMs);
    }

    @SubscribeEvent
    public void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!this.isEnabled) {
            return;
        }

        removePlayer((ServerPlayer) event.getPlayer());
    }

    public void clear() {
        playerHistory.clear();
    }

    @Override
    public void register() {
        this.enable();
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
