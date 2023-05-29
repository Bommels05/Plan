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
package net.playeranalytics.plan.gathering.listeners.events;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Event;

import java.net.SocketAddress;

public class PlayerLoginEvent extends Event {
    private final SocketAddress address;
    private final GameProfile profile;
    private final Component banReason;

    public PlayerLoginEvent(SocketAddress address, GameProfile profile, Component banReason) {
        this.address = address;
        this.profile = profile;
        this.banReason = banReason;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public GameProfile getProfile() {
        return profile;
    }

    public Component getBanReason() {
        return banReason;
    }

    public boolean isBanned() {
        return banReason != null;
    }
}
