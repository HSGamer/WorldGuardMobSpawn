package me.hsgamer.worldguardmobspawn;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class WorldCheck extends BukkitRunnable {
    private final Set<Mob> taggedMobs = new HashSet<>();
    private final WorldGuardMobSpawn instance;
    private final World world;

    public WorldCheck(WorldGuardMobSpawn instance, World world) {
        this.instance = instance;
        this.world = world;
    }

    @Override
    public void run() {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        long time = instance.getMainConfig().getCheckFrequency();
        taggedMobs.removeIf(Entity::isDead);
        world.getLivingEntities()
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
                    new MobRegionCheck(entity).runTaskTimerAsynchronously(JavaPlugin.getProvidingPlugin(getClass()), time, time);
                });
    }
}
