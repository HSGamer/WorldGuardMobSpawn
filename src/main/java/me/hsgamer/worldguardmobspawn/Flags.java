package me.hsgamer.worldguardmobspawn;

import org.bukkit.Location;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;

public final class Flags {
    private static IWrappedFlag<WrappedState> mobSpawningFlag;

    private Flags() {
        // EMPTY
    }

    public static IWrappedFlag<WrappedState> getMobSpawningFlag() {
        if (mobSpawningFlag == null) {
            mobSpawningFlag = WorldGuardWrapper.getInstance().getFlag("mob-spawning", WrappedState.class).orElse(null);
            if (mobSpawningFlag == null) {
                throw new IllegalArgumentException("Could not find mob-spawning flag");
            }
        }
        return mobSpawningFlag;
    }

    public static WrappedState queryMobSpawningFlag(Location location) {
        return WorldGuardWrapper.getInstance().queryFlag(null, location, getMobSpawningFlag()).orElse(WrappedState.ALLOW);
    }
}
