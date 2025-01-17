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
