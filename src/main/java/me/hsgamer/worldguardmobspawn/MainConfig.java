package me.hsgamer.worldguardmobspawn;

import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.hsgamer.hscore.config.annotated.AnnotatedConfig;
import me.hsgamer.hscore.config.annotation.ConfigPath;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;

public class MainConfig extends AnnotatedConfig {
    @ConfigPath("enabled-worlds")
    private List<String> enabledWorlds = Collections.emptyList();
    @ConfigPath("check-frequency")
    private long checkFrequency = 10;

    public MainConfig(Plugin plugin) {
        super(new BukkitConfig(plugin, "config.yml"));
    }

    public List<String> getEnabledWorlds() {
        return enabledWorlds;
    }

    public long getCheckFrequency() {
        return checkFrequency;
    }
}
