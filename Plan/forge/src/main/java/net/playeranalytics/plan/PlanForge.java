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
package net.playeranalytics.plan;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.commands.use.ColorScheme;
import com.djrapitops.plan.commands.use.Subcommand;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.gathering.ServerShutdownSave;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.settings.theme.PlanColorScheme;
import com.djrapitops.plan.utilities.java.ThreadContextClassLoaderSwap;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.google.common.base.Verify;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.network.NetworkConstants;
import net.playeranalytics.plan.commands.CommandManager;
import net.playeranalytics.plan.identification.properties.ForgeServerProperties;
import net.playeranalytics.plugin.ForgePlatformLayer;
import net.playeranalytics.plugin.PlatformAbstractionLayer;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.PluginLogger;
import org.slf4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Mod("plan")
public class PlanForge implements PlanPlugin {

    private PluginLogger pluginLogger;
    private Locale locale;
    private ServerShutdownSave serverShutdownSave;
    private PlanSystem system;
    private PlatformAbstractionLayer abstractionLayer;
    private RunnableFactory runnableFactory;
    private MinecraftServer server;
    private ErrorLogger errorLogger;
    private CommandManager commandManager;
    private String version;

    public PlanForge() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        version = ModLoadingContext.get().getActiveContainer().getModInfo().getOwningFile().versionString();

        MinecraftForge.EVENT_BUS.register(this);
    }


    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        if (event.getServer().isDedicatedServer()) {
            abstractionLayer = new ForgePlatformLayer(this);
            pluginLogger = abstractionLayer.getPluginLogger();
            runnableFactory = abstractionLayer.getRunnableFactory();
            server = event.getServer();

            onEnable();
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        if (event.getServer().isDedicatedServer()) {
            onDisable();
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        commandManager = new CommandManager(event.getDispatcher(), errorLogger);
    }

    @Override
    public InputStream getResource(String resource) {
        return getClass().getResourceAsStream("/" + resource);
    }

    @Override
    public ColorScheme getColorScheme() {
        return PlanColorScheme.create(system.getConfigSystem().getConfig(), pluginLogger);
    }

    @Override
    public PlanSystem getSystem() {
        return system;
    }

    @Override
    public void registerCommand(Subcommand command) {
        commandManager.registerRoot(command, runnableFactory);
    }

    @Override
    public void onEnable() {
        PlanForgeComponent component = DaggerPlanForgeComponent.builder()
                .plan(this)
                .abstractionLayer(abstractionLayer)
                .server(server)
                .serverProperties(new ForgeServerProperties(server))
                .build();

        try {
            system = ThreadContextClassLoaderSwap.performOperation(getClass().getClassLoader(), component::system);
            errorLogger = component.errorLogger();
            serverShutdownSave = component.serverShutdownSave();
            locale = system.getLocaleSystem().getLocale();
            system.enable();

            pluginLogger.info(locale.getString(PluginLang.ENABLED));
        } catch (AbstractMethodError e) {
            pluginLogger.error("Plugin ran into AbstractMethodError, server restart is required! This error is likely caused by updating the JAR without a restart.");
        } catch (EnableException e) {
            pluginLogger.error("----------------------------------------");
            pluginLogger.error("Error: " + e.getMessage());
            pluginLogger.error("----------------------------------------");
            pluginLogger.error("Plugin failed to initialize correctly. If this issue is caused by config settings you can use /plan reload");
            onDisable();
        } catch (Exception e) {
            String version = abstractionLayer.getPluginInformation().getVersion();
            pluginLogger.error(this.getClass().getSimpleName() + "-v" + version, e);
            pluginLogger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /plan reload");
            pluginLogger.error("This error should be reported at https://github.com/plan-player-analytics/Plan/issues");
            onDisable();
        }
        registerCommand(component.planCommand().build());
        if (system != null) {
            system.getProcessing().submitNonCritical(() -> system.getListenerSystem().callEnableEvent(this));
        }
    }

    @Override
    public void onDisable() {
        storeSessionsOnShutdown();
        runnableFactory.cancelAllKnownTasks();

        if (system != null) system.disable();

        pluginLogger.info(Locale.getStringNullSafe(locale, PluginLang.DISABLED));
    }

    private void storeSessionsOnShutdown() {
        if (serverShutdownSave != null) {
            Optional<Future<?>> complete = serverShutdownSave.performSave();
            if (complete.isPresent()) {
                try {
                    complete.get().get(4, TimeUnit.SECONDS); // wait for completion for 4s
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    pluginLogger.error("Failed to save sessions to database on shutdown: " + e.getCause().getMessage());
                } catch (TimeoutException e) {
                    pluginLogger.info(Locale.getStringNullSafe(locale, PluginLang.DISABLED_UNSAVED_SESSIONS_TIMEOUT));
                }
            }
        }
    }

    @Override
    public File getDataFolder() {
        return FMLPaths.GAMEDIR.get().resolve(FMLPaths.CONFIGDIR.get()).resolve("plan").toFile();
    }

    public String getVersion() {
        return version;
    }
}
