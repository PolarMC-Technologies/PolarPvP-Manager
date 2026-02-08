package com.pvptoggle.model;

import org.bukkit.Location;
import org.bukkit.World;

// AABB region in a world that forces PvP for everyone inside.
public class PvPZone {

    private final String name;
    private final String worldName;
    private final int x1;
    private final int y1;
    private final int z1;
    private final int x2;
    private final int y2;
    private final int z2;

    public record Corners(int ax, int ay, int az, int bx, int by, int bz) {}

    public PvPZone(String name, String worldName, Corners corners) {
        this.name = name;
        this.worldName = worldName;
        this.x1 = Math.min(corners.ax(), corners.bx());
        this.y1 = Math.min(corners.ay(), corners.by());
        this.z1 = Math.min(corners.az(), corners.bz());
        this.x2 = Math.max(corners.ax(), corners.bx());
        this.y2 = Math.max(corners.ay(), corners.by());
        this.z2 = Math.max(corners.az(), corners.bz());
    }

    /** Checks whether the given location is within this zone. */
    public boolean contains(Location location) {
        if (location == null) return false;
        World world = location.getWorld();
        if (world == null) return false;
        if (!world.getName().equals(worldName)) return false;
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return x >= x1 && x <= x2
            && y >= y1 && y <= y2
            && z >= z1 && z <= z2;
    }

    public String getName()      { return name; }
    public String getWorldName() { return worldName; }
    public int getX1() { return x1; }
    public int getY1() { return y1; }
    public int getZ1() { return z1; }
    public int getX2() { return x2; }
    public int getY2() { return y2; }
    public int getZ2() { return z2; }
}
