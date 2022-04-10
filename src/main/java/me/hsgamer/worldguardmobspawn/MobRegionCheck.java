package me.hsgamer.worldguardmobspawn;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.scheduler.BukkitRunnable;
import org.codemc.worldguardwrapper.flag.WrappedState;

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

        WrappedState spawnState = Flags.queryMobSpawningFlag(livingEntity.getLocation());
        if (spawnState == WrappedState.DENY) {
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
        WrappedState targetSpawnState = Flags.queryMobSpawningFlag(target.getLocation());
        if (targetSpawnState == WrappedState.DENY) {
            Bukkit.getScheduler().runTask(instance, () -> mob.setTarget(null));
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        instance.deleteTaggedEntity(livingEntity);
    }
}
