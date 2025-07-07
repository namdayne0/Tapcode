package com.namdz.keoframer.area;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public class ParticleData {

    private final Particle type;
    private final String style;
    private final String data;

    public ParticleData(Particle type, String style, String data) {
        this.type = type;
        this.style = style != null ? style.toLowerCase() : "aura"; // Mặc định là aura
        this.data = data;
    }

    public void spawn(World world, Location location) {
        int count;
        double speed;
        double offsetX, offsetY, offsetZ;

        switch (style) {
            case "fountain":
                count = 15;
                speed = 0.5;
                offsetX = 0.5;
                offsetY = 1;
                offsetZ = 0.5;
                break;
            case "explode":
                count = 50;
                speed = 1;
                offsetX = 1.5;
                offsetY = 1.5;
                offsetZ = 1.5;
                break;
            case "aura":
            default:
                count = 3;
                speed = 0; // Tốc độ = 0 để hạt không bay đi
                offsetX = 0.5;
                offsetY = 1.0;
                offsetZ = 0.5;
                break;
        }

        Object particleData = null;
        if (data != null && type.getDataType() != Void.class) {
            Material material = Material.matchMaterial(data.toUpperCase());
            if (material != null) {
                if (type.getDataType() == org.bukkit.block.data.BlockData.class) {
                    particleData = material.createBlockData();
                } else if (type.getDataType() == ItemStack.class) {
                    particleData = new ItemStack(material);
                }
            } else {
                return;
            }
        }

        world.spawnParticle(type, location, count, offsetX, offsetY, offsetZ, speed, particleData);
    }

    @Override
    public String toString() {
        String dataString = (data != null) ? ", Data: " + data : "";
        return String.format("Type: %s, Style: %s%s", type.name(), style, dataString);
    }
}