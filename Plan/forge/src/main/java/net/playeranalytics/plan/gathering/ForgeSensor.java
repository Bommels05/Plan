package net.playeranalytics.plan.gathering;

import com.djrapitops.plan.gathering.ServerSensor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import javax.inject.Inject;
import java.util.List;

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
        return server.getAverageTickTime();
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
        for (Entity entity : world.getAllEntities()) {
            entities++;
        }

        return entities;
    }

    @Override
    public List<String> getOnlinePlayerNames() {
        return List.of(server.getPlayerNames());
    }
}
