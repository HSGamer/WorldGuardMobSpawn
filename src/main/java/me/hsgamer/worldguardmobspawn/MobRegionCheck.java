package me.hsgamer.worldguardmobspawn;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class MobRegionCheck extends BukkitRunnable {
    private final LivingEntity livingEntity;

    public MobRegionCheck(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
    }

    @Override
    public void run() {
        if (!livingEntity.isValid()) {
            cancel();
            return;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        StateFlag.State spawnState = query.queryState(BukkitAdapter.adapt(livingEntity.getLocation()), null, Flags.MOB_SPAWNING);
        if (spawnState == StateFlag.State.DENY) {
            Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(getClass()), livingEntity::remove);
            return;
        }

        if (livingEntity instanceof Mob) {
            Mob mob = (Mob) livingEntity;
            Entity target = mob.getTarget();
            if (target == null) {
                return;
            }
            StateFlag.State targetSpawnState = query.queryState(BukkitAdapter.adapt(target.getLocation()), null, Flags.MOB_SPAWNING);
            if (targetSpawnState == StateFlag.State.DENY) {
                Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(getClass()), () -> mob.setTarget(null));
            }
        }
    }
}
