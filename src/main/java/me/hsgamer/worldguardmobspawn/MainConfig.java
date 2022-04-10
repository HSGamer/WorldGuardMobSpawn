package me.hsgamer.worldguardmobspawn;

import me.hsgamer.hscore.config.annotation.ConfigPath;

import java.util.Collections;
import java.util.List;

public interface MainConfig {
    @ConfigPath("enabled-worlds")
    default List<String> getEnabledWorlds() {
        return Collections.emptyList();
    }

    @ConfigPath("check-frequency")
    default int getCheckFrequency() {
        return 10;
    }
}
