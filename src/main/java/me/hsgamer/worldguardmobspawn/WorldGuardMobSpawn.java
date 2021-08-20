package me.hsgamer.worldguardmobspawn;

import me.hsgamer.hscore.bukkit.baseplugin.BasePlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.Objects;

public final class WorldGuardMobSpawn extends BasePlugin implements Listener {
    private final MainConfig mainConfig = new MainConfig(this);

    @Override
    public void enable() {
        mainConfig.setup();
        registerListener(this);
    }

    @Override
    public void postEnable() {
        mainConfig.getEnabledWorlds().stream()
                .map(Bukkit::getWorld)
                .filter(Objects::nonNull)
                .map(world -> new WorldCheck(this, world))
                .forEach(runnable -> runnable.runTaskTimer(this, mainConfig.getCheckFrequency(), mainConfig.getCheckFrequency()));
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }
}
