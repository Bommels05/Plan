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
package net.playeranalytics.plan.gathering;

import com.djrapitops.plan.gathering.ServerSensor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ForgeSensor implements ServerSensor<ServerLevel> {

    private final MinecraftServer server;

    @Inject
    public ForgeSensor(MinecraftServer server) {
        this.server = server;
    }


    @Override
    public boolean supportsDirectTPS() {
        return true;
    }

    @Override
    public int getOnlinePlayerCount() {
        return server.getPlayerCount();
    }

    @Override
    public double getTPS() {
        //Returns the ticks per second of the last 100 ticks
        double totalTickLength = 0;
        int count = 0;
        for (long tickLength : server.tickTimes) {
            if (tickLength == 0) continue; // Ignore uninitialized values in array
            totalTickLength += Math.max(tickLength, TimeUnit.MILLISECONDS.toNanos(50));
            count++;
        }
        if (count == 0) {
            return -1;
        } else {
            return TimeUnit.SECONDS.toNanos(1) / (totalTickLength / count);
        }
    }

    @Override
    public Iterable<ServerLevel> getWorlds() {
        return server.getAllLevels();
    }

    @Override
    public int getChunkCount(ServerLevel world) {
        return world.getChunkSource().getLoadedChunksCount();
    }

    @Override
    public int getEntityCount(ServerLevel world) {
        int entities = 0;
        for (Entity ignored : world.getAllEntities()) {
            entities++;
        }

        return entities;
    }

    @Override
    public List<String> getOnlinePlayerNames() {
        return List.of(server.getPlayerNames());
    }
}
