package me.hsgamer.worldguardmobspawn;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.hsgamer.hscore.bukkit.baseplugin.BasePlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.EntitiesLoadEvent;

import java.util.HashSet;
import java.util.Set;

public final class WorldGuardMobSpawn extends BasePlugin implements Listener {
    private final MainConfig mainConfig = new MainConfig(this);
    private final Set<LivingEntity> taggedEntities = new HashSet<>();

    @Override
    public void enable() {
        mainConfig.setup();
        registerListener(this);
    }

    public MainConfig getMainConfig() {
        return mainConfig;
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

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        StateFlag.State spawnState = query.queryState(BukkitAdapter.adapt(location), null, Flags.MOB_SPAWNING);
        if (spawnState != StateFlag.State.ALLOW) {
            return;
        }

        startTask(event.getEntity());
    }

    @EventHandler
    public void onMobLoad(EntitiesLoadEvent event) {
        if (!mainConfig.getEnabledWorlds().contains(event.getWorld().getName())) {
            return;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        event.getEntities()
                .parallelStream()
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .filter(entity -> !taggedEntities.contains(entity))
                .filter(entity -> {
                    RegionQuery query = container.createQuery();
                    StateFlag.State spawnState = query.queryState(BukkitAdapter.adapt(entity.getLocation()), null, Flags.MOB_SPAWNING);
                    return spawnState == StateFlag.State.ALLOW;
                })
                .sequential()
                .forEach(entity -> {
                    taggedEntities.add(entity);
                    startTask(entity);
                });
    }
}
