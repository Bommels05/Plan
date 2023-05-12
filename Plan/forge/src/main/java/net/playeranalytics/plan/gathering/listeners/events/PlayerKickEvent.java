package net.playeranalytics.plan.gathering.listeners.events;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerKickEvent extends PlayerEvent {

    public PlayerKickEvent(Player player) {
        super(player);
    }
}
