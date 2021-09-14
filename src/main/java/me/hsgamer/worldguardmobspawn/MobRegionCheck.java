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
import org.bukkit.scheduler.BukkitRunnable;

public class MobRegionCheck extends BukkitRunnable {
    private final WorldGuardMobSpawn instance;
    private final LivingEntity livingEntity;

    public MobRegionCheck(WorldGuardMobSpawn instance, LivingEntity livingEntity) {
        this.instance = instance;
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
            Bukkit.getScheduler().runTask(instance, livingEntity::remove);
            return;
        }

        if (!(livingEntity instanceof Mob)) {
            return;
        }
        Mob mob = (Mob) livingEntity;
        Entity target = mob.getTarget();
        if (target == null) {
            return;
        }
        StateFlag.State targetSpawnState = query.queryState(BukkitAdapter.adapt(target.getLocation()), null, Flags.MOB_SPAWNING);
        if (targetSpawnState == StateFlag.State.DENY) {
            Bukkit.getScheduler().runTask(instance, () -> mob.setTarget(null));
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        instance.deleteTaggedEntity(livingEntity);
    }
}
