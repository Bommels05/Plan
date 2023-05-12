package net.playeranalytics.plugin;

import net.playeranalytics.plan.PlanForge;
import net.playeranalytics.plugin.scheduling.ForgeRunnableFactory;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.ForgePluginLogger;
import net.playeranalytics.plugin.server.Listeners;
import net.playeranalytics.plugin.server.PluginLogger;
import org.slf4j.Logger;

public class ForgePlatformLayer implements PlatformAbstractionLayer {

    private final PluginLogger pluginLogger;
    private final PluginInformation pluginInformation;
    private final RunnableFactory runnableFactory;
    private final Listeners listeners;

    public ForgePlatformLayer(PlanForge planForge) {
        pluginLogger = new ForgePluginLogger(planForge.getLogger());
        pluginInformation = new ForgePluginInformation(planForge.getDataFolder(), planForge.getVersion());
        runnableFactory = new ForgeRunnableFactory();
        listeners = null;
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
