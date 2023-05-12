package net.playeranalytics.plan.modules.forge;

import com.djrapitops.plan.identification.properties.ServerProperties;
import dagger.Module;
import dagger.Provides;
import net.minecraftforge.event.world.WorldEvent;
import net.playeranalytics.plan.identification.properties.ForgeServerProperties;

import javax.inject.Singleton;

@Module
public class ForgeServerPropertiesModule {

    @Provides
    @Singleton
    ServerProperties provideServerProperties(ForgeServerProperties serverProperties) {
        return serverProperties;
    }

}
