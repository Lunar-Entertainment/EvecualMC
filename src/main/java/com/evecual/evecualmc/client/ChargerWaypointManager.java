package com.evecual.evecualmc.client;

import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ChargerWaypointManager {
    private static final Set<BlockPos> WAYPOINTS = new CopyOnWriteArraySet<>();

    public static void addWaypoint(BlockPos pos) {
        WAYPOINTS.add(pos.toImmutable());
    }

    public static void removeWaypoint(BlockPos pos) {
        WAYPOINTS.remove(pos);
    }

    public static void clear() {
        WAYPOINTS.clear();
    }

    public static Set<BlockPos> getWaypoints() {
        return Collections.unmodifiableSet(WAYPOINTS);
    }
}
