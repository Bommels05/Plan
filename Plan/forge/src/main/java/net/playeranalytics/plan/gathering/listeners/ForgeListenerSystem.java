package net.playeranalytics.plan.gathering.listeners;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.capability.CapabilitySvc;
import com.djrapitops.plan.gathering.listeners.ListenerSystem;
import net.minecraftforge.common.MinecraftForge;
import net.playeranalytics.plan.PlanForge;
import net.playeranalytics.plan.gathering.listeners.events.PlanEnableEvent;
import net.playeranalytics.plan.gathering.listeners.forge.*;
import net.playeranalytics.plugin.server.Listeners;

import javax.inject.Inject;

public class ForgeListenerSystem extends ListenerSystem {

    private final ChatListener chatListener;
    private final DeathEventListener deathEventListener;
    private final ForgeAFKListener forgeAFKListener;
    private final GameModeChangeListener gameModeChangeListener;
    private final PlayerOnlineListener playerOnlineListener;
    private final WorldChangeListener worldChangeListener;
    private final Listeners listeners;

    @Inject
    public ForgeListenerSystem(ChatListener chatListener, DeathEventListener deathEventListener, ForgeAFKListener forgeAFKListener, GameModeChangeListener gameModeChangeListener, PlayerOnlineListener playerOnlineListener, WorldChangeListener worldChangeListener, Listeners listeners) {
        this.chatListener = chatListener;
        this.deathEventListener = deathEventListener;
        this.forgeAFKListener = forgeAFKListener;
        this.playerOnlineListener = playerOnlineListener;
        this.gameModeChangeListener = gameModeChangeListener;
        this.worldChangeListener = worldChangeListener;
        this.listeners = listeners;

    }

    @Override
    protected void registerListeners() {
        listeners.registerListener(chatListener);
        listeners.registerListener(deathEventListener);
        listeners.registerListener(forgeAFKListener);
        listeners.registerListener(gameModeChangeListener);
        listeners.registerListener(playerOnlineListener);
        listeners.registerListener(worldChangeListener);
    }

    @Override
    protected void unregisterListeners() {
        listeners.unregisterListener(chatListener);
        listeners.unregisterListener(deathEventListener);
        listeners.unregisterListener(forgeAFKListener);
        listeners.unregisterListener(gameModeChangeListener);
        listeners.unregisterListener(playerOnlineListener);
        listeners.unregisterListener(worldChangeListener);
    }

    @Override
    public void callEnableEvent(PlanPlugin plugin) {
        boolean isEnabled = plugin.isSystemEnabled();
        MinecraftForge.EVENT_BUS.post(new PlanEnableEvent((PlanForge) plugin));
        CapabilitySvc.notifyAboutEnable(isEnabled);
    }
}
