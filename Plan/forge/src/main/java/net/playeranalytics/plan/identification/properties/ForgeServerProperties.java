package net.playeranalytics.plan.identification.properties;

import com.djrapitops.plan.identification.properties.ServerProperties;
import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.LauncherVersion;

import java.util.function.Supplier;

public class ForgeServerProperties extends ServerProperties {


    public ForgeServerProperties(MinecraftServer server) {
        super(server.getServerModName(), server.getPort(), server.getServerVersion(), LauncherVersion.getVersion(), server::getLocalIp, server.getMaxPlayers());
    }
}
