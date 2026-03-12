package com.pvptoggle.model;

import org.bukkit.Location;
import org.bukkit.World;

public class PvPZone {

    private final String name;
    private final String worldName;
    private final int x1;
    private final int y1;
    private final int z1;
    private final int x2;
    private final int y2;
    private final int z2;

    public record Corners(int x1, int y1, int z1, int x2, int y2, int z2) {}

    public PvPZone(String name, String worldName, Corners corners) {
        this.name = name;
        this.worldName = worldName;
        this.x1 = Math.min(corners.x1(), corners.x2());
        this.y1 = Math.min(corners.y1(), corners.y2());
        this.z1 = Math.min(corners.z1(), corners.z2());
        this.x2 = Math.max(corners.x1(), corners.x2());
        this.y2 = Math.max(corners.y1(), corners.y2());
        this.z2 = Math.max(corners.z1(), corners.z2());
    }

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
