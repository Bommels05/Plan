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
package net.playeranalytics.plugin;

import net.playeranalytics.plan.PlanForge;
import net.playeranalytics.plugin.scheduling.ForgeRunnableFactory;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.ForgeListeners;
import net.playeranalytics.plugin.server.ForgePluginLogger;
import net.playeranalytics.plugin.server.Listeners;
import net.playeranalytics.plugin.server.PluginLogger;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;

public class ForgePlatformLayer implements PlatformAbstractionLayer {

    private final PluginLogger pluginLogger;
    private final PluginInformation pluginInformation;
    private final RunnableFactory runnableFactory;
    private final Listeners listeners;

    public ForgePlatformLayer(PlanForge planForge) {
        pluginLogger = new ForgePluginLogger(LogManager.getLogger("Plan"));
        pluginInformation = new ForgePluginInformation(planForge.getDataFolder(), planForge.getVersion());
        runnableFactory = new ForgeRunnableFactory();
        listeners = new ForgeListeners();
    }

    @Override
    public PluginLogger getPluginLogger() {
        return pluginLogger;
    }

    @Override
    public Listeners getListeners() {
        return listeners;
    }

    @Override
    public RunnableFactory getRunnableFactory() {
        return runnableFactory;
    }

    @Override
    public PluginInformation getPluginInformation() {
        return pluginInformation;
    }
}
