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

import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.gathering.cache.NicknameCache;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.StoreNicknameTransaction;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.playeranalytics.plan.gathering.listeners.ForgeListener;

import javax.inject.Inject;
import java.util.UUID;

public class ChatListener implements ForgeListener {

    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final NicknameCache nicknameCache;
    private final ErrorLogger errorLogger;

    private boolean isEnabled = false;
    private boolean wasRegistered = false;


    @Inject
    public ChatListener(ServerInfo serverInfo, DBSystem dbSystem, NicknameCache nicknameCache, ErrorLogger errorLogger) {
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.nicknameCache = nicknameCache;
        this.errorLogger = errorLogger;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChat(ServerChatEvent event) {
        if (!this.isEnabled) {
            return;
        }
        try {
            actOnChatEvent(event.getPlayer());
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event.getPlayer()).build());
        }
    }

    private void actOnChatEvent(ServerPlayer player) {
        long time = System.currentTimeMillis();
        UUID uuid = player.getUUID();
        String displayName = player.getDisplayName().getString();

        dbSystem.getDatabase().executeTransaction(new StoreNicknameTransaction(uuid, new Nickname(displayName, time, serverInfo.getServerUUID()), (playerUUID, name) -> nicknameCache.getDisplayName(playerUUID).map(name::equals).orElse(false)));
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
