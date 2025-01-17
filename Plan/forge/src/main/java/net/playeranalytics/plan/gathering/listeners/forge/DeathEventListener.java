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

import com.djrapitops.plan.delivery.formatting.EntityNameFormatter;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.gathering.domain.PlayerKill;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.processing.processors.player.MobKillProcessor;
import com.djrapitops.plan.processing.processors.player.PlayerKillProcessor;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.playeranalytics.plan.gathering.listeners.ForgeListener;

import javax.inject.Inject;
import java.util.Optional;

public class DeathEventListener implements ForgeListener {

    private final Processing processing;
    private final ErrorLogger errorLogger;
    private final ServerInfo serverInfo;

    private boolean isEnabled = false;
    private boolean wasRegistered = false;

    @Inject
    public DeathEventListener(ServerInfo serverInfo, Processing processing, ErrorLogger errorLogger) {
        this.serverInfo = serverInfo;
        this.processing = processing;
        this.errorLogger = errorLogger;
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKilled(LivingDeathEvent event) {
        if (!this.isEnabled) {
            return;
        }

        long time = System.currentTimeMillis();
        Entity victim = event.getEntity();
        Entity killer = event.getSource().getEntity();

        if (victim instanceof ServerPlayer) {
            // Process Death
            SessionCache.getCachedSession(victim.getUUID()).ifPresent(ActiveSession::addDeath);
        }

        try {
            Optional<ServerPlayer> foundKiller = getCause(killer);
            if (foundKiller.isEmpty()) {
                return;
            }

            ServerPlayer player = foundKiller.get();

            Runnable processor = victim instanceof ServerPlayer
                    ? new PlayerKillProcessor(getKiller(player), getVictim((ServerPlayer) victim), serverInfo.getServerIdentifier(), findWeapon(player), time)
                    : new MobKillProcessor(player.getUUID());
            processing.submitCritical(processor);
        } catch (Exception | NoSuchMethodError e) {
            errorLogger.error(e, ErrorContext.builder().related(getClass(), victim, killer).build());
        }
    }

    private PlayerKill.Killer getKiller(ServerPlayer killer) {
        return new PlayerKill.Killer(killer.getUUID(), killer.getName().getString());
    }

    private PlayerKill.Victim getVictim(ServerPlayer victim) {
        return new PlayerKill.Victim(victim.getUUID(), victim.getName().getString());
    }

    public Optional<ServerPlayer> getCause(Entity killer) {
        if (killer instanceof ServerPlayer player) return Optional.of(player);
        if (killer instanceof TamableAnimal tamed) return getOwner(tamed);
        if (killer instanceof Projectile projectile) return getShooter(projectile);
        return Optional.empty();
    }

    public String findWeapon(Entity killer) {
        if (killer instanceof ServerPlayer player) return getItemInHand(player);

        // Projectile, EnderCrystal and all other causes that are not known yet
        return new EntityNameFormatter().apply(killer.getType().getDescription().getString());
    }

    private String getItemInHand(ServerPlayer killer) {
        ItemStack itemInHand = killer.getMainHandItem();
        return itemInHand.getItem().getName(itemInHand).getString();
    }

    private Optional<ServerPlayer> getShooter(Projectile projectile) {
        Entity source = projectile.getOwner();
        if (source instanceof ServerPlayer player) {
            return Optional.of(player);
        }

        return Optional.empty();
    }

    private Optional<ServerPlayer> getOwner(TamableAnimal tameable) {
        if (!tameable.isTame()) {
            return Optional.empty();
        }

        Entity owner = tameable.getOwner();
        if (owner instanceof ServerPlayer player) {
            return Optional.of(player);
        }

        return Optional.empty();
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
