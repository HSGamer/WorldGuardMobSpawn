package me.hsgamer.worldguardmobspawn;

import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.hsgamer.hscore.bukkit.simpleplugin.SimplePlugin;
import me.hsgamer.hscore.config.proxy.ConfigGenerator;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.codemc.worldguardwrapper.flag.WrappedState;

import java.util.HashSet;
import java.util.Set;

public final class WorldGuardMobSpawn extends SimplePlugin implements Listener {
    private final MainConfig mainConfig = ConfigGenerator.newInstance(MainConfig.class, new BukkitConfig(this, "config.yml"), true, true);
    private final Set<LivingEntity> taggedEntities = new HashSet<>();

    @Override
    public void enable() {
        registerListener(this);
    }

    public void deleteTaggedEntity(LivingEntity livingEntity) {
        taggedEntities.remove(livingEntity);
    }

    private void startTask(LivingEntity livingEntity) {
        long time = mainConfig.getCheckFrequency();
        new MobRegionCheck(this, livingEntity).runTaskTimerAsynchronously(this, time, time);
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        Location location = event.getLocation();
        World world = location.getWorld();
        if (world == null || !mainConfig.getEnabledWorlds().contains(world.getName())) {
            return;
        }

        WrappedState spawnState = Flags.queryMobSpawningFlag(location);
        if (spawnState != WrappedState.ALLOW) {
            return;
        }

        startTask(event.getEntity());
    }

    @EventHandler
    public void onMobLoad(EntitiesLoadEvent event) {
        if (!mainConfig.getEnabledWorlds().contains(event.getWorld().getName())) {
            return;
        }

        event.getEntities()
                .parallelStream()
                .filter(LivingEntity.class::isInstance)
                .filter(entity -> !(entity instanceof HumanEntity))
                .map(LivingEntity.class::cast)
                .filter(entity -> !taggedEntities.contains(entity))
                .filter(entity -> Flags.queryMobSpawningFlag(entity.getLocation()) == WrappedState.ALLOW)
                .forEach(entity -> {
                    taggedEntities.add(entity);
                    startTask(entity);
                });
    }
}
