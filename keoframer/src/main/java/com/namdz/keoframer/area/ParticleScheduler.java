package com.namdz.keoframer.area;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleScheduler extends BukkitRunnable {

    private final AreaManager areaManager;

    public ParticleScheduler(AreaManager areaManager) {
        this.areaManager = areaManager;
    }

    @Override
    public void run() {
        // Lặp qua tất cả người chơi đang online
        for (Player player : Bukkit.getOnlinePlayers()) {
            Area area = areaManager.getAreaAt(player.getLocation());

            // Nếu người chơi đang ở trong một khu vực và khu vực đó có cài đặt particle
            if (area != null && area.getParticleData() != null) {
                ParticleData particleData = area.getParticleData();
                // Spawn particle tại vị trí của người chơi
                particleData.spawn(player.getWorld(), player.getLocation());
            }
        }
    }
}