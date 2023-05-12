package net.playeranalytics.plugin;

import java.io.File;
import java.io.InputStream;

public class ForgePluginInformation implements PluginInformation {

    private final File dataFolder;
    private final String version;

    public ForgePluginInformation(File dataFolder, String version) {
        this.dataFolder = dataFolder;
        this.version = version;
    }

    @Override
    public InputStream getResourceFromJar(String resource) {
        return this.getClass().getResourceAsStream("/" + resource);
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public String getVersion() {
        return version;
    }
}
