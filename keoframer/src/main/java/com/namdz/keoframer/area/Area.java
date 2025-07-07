package com.namdz.keoframer.area;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Location;
import org.bukkit.World;

public class Area {

    private final String name;
    private final CuboidRegion region;
    private final World world;
    private final String candyType;
    private final int candyAmount;
    private final long candyDelayMillis;
    private final double successRate;
    private final String permission;
    private final boolean noPush;
    private final ParticleData particleData;
    private final boolean checkY;

    public Area(String name, World world, BlockVector3 pos1, BlockVector3 pos2, String candyType, int candyAmount, long candyDelayMillis, double successRate, String permission, boolean noPush, ParticleData particleData, boolean checkY) {
        this.name = name;
        this.world = world;
        this.region = new CuboidRegion(BukkitAdapter.adapt(world), pos1, pos2);
        this.candyType = candyType;
        this.candyAmount = candyAmount;
        this.candyDelayMillis = candyDelayMillis;
        this.successRate = successRate;
        this.permission = permission;
        this.noPush = noPush;
        this.particleData = particleData;
        this.checkY = checkY;
        
        this.region.setPos1(this.region.getMinimumPoint());
        this.region.setPos2(this.region.getMaximumPoint());
    }

    public boolean isInArea(Location loc) {
        if (!loc.getWorld().equals(this.world)) {
            return false;
        }
        
        int checkX = loc.getBlockX();
        int checkY = loc.getBlockY();
        int checkZ = loc.getBlockZ();

        if (this.checkY) {
            // Chế độ 3D: Kiểm tra cả block người chơi đang đứng và block dưới chân họ
            return region.contains(BlockVector3.at(checkX, checkY, checkZ)) || 
                   region.contains(BlockVector3.at(checkX, checkY - 1, checkZ));
        } else {
            // SỬA LỖI LOGIC 2D TẠI ĐÂY:
            // Kiểm tra X, Z thủ công và Y phải lớn hơn hoặc bằng đáy
            return checkX >= region.getMinimumPoint().getX() && checkX <= region.getMaximumPoint().getX()
                && checkZ >= region.getMinimumPoint().getZ() && checkZ <= region.getMaximumPoint().getZ()
                && checkY >= region.getMinimumPoint().getY();
        }
    }
    
    public Location getCenter() {
        double centerX = (region.getMinimumPoint().getX() + region.getMaximumPoint().getX() + 1) / 2.0;
        double centerY = (region.getMinimumPoint().getY() + region.getMaximumPoint().getY() + 1) / 2.0;
        double centerZ = (region.getMinimumPoint().getZ() + region.getMaximumPoint().getZ() + 1) / 2.0;
        return new Location(world, centerX, centerY, centerZ);
    }

    public String getName() { return name; }
    public CuboidRegion getRegion() { return region; }
    public World getWorld() { return world; }
    public String getCandyType() { return candyType; }
    public int getCandyAmount() { return candyAmount; }
    public long getCandyDelayMillis() { return candyDelayMillis; }
    public double getSuccessRate() { return successRate; }
    public String getPermission() { return permission; }
    public boolean isNoPush() { return noPush; }
    public ParticleData getParticleData() { return particleData; }
    public boolean isCheckY() { return checkY; }
}