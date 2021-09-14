package me.hsgamer.worldguardmobspawn;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.hsgamer.hscore.bukkit.baseplugin.BasePlugin;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;

import java.util.HashSet;
import java.util.Set;

public final class WorldGuardMobSpawn extends BasePlugin implements Listener {
    private final MainConfig mainConfig = new MainConfig(this);
    private final Set<Mob> taggedMobs = new HashSet<>();

    @Override
    public void enable() {
        mainConfig.setup();
        registerListener(this);
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Mob)) {
            return;
        }
        Mob mob = (Mob) entity;

        World world = event.getLocation().getWorld();
        if (world == null || !mainConfig.getEnabledWorlds().contains(world.getName())) {
            return;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        StateFlag.State spawnState = query.queryState(BukkitAdapter.adapt(mob.getLocation()), null, Flags.MOB_SPAWNING);
        if (spawnState != StateFlag.State.ALLOW) {
            return;
        }

        long time = mainConfig.getCheckFrequency();
        new MobRegionCheck(mob).runTaskTimerAsynchronously(this, time, time);
    }

    @EventHandler
    public void onMobUnload(EntitiesUnloadEvent event) {
        taggedMobs.removeIf(entity -> !entity.isValid());
    }

    @EventHandler
    public void onMobLoad(EntitiesLoadEvent event) {
        if (!mainConfig.getEnabledWorlds().contains(event.getWorld().getName())) {
            return;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        long time = mainConfig.getCheckFrequency();
        event.getEntities()
                .parallelStream()
                .filter(Mob.class::isInstance)
                .map(Mob.class::cast)
                .filter(entity -> !taggedMobs.contains(entity))
                .filter(entity -> {
                    RegionQuery query = container.createQuery();
                    StateFlag.State spawnState = query.queryState(BukkitAdapter.adapt(entity.getLocation()), null, Flags.MOB_SPAWNING);
                    return spawnState == StateFlag.State.ALLOW;
                })
                .sequential()
                .forEach(entity -> {
                    taggedMobs.add(entity);
                    new MobRegionCheck(entity).runTaskTimerAsynchronously(this, time, time);
                });
    }
}
