package com.namdz.keoframer.candy;

import com.namdz.keoframer.KeoFramer;
import com.namdz.keoframer.area.Area;
import com.namdz.keoframer.area.AreaManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class CandyScheduler extends BukkitRunnable {

    private final KeoFramer plugin;
    private final AreaManager areaManager;
    private final CandyManager candyManager;

    public CandyScheduler(KeoFramer plugin, AreaManager areaManager, CandyManager candyManager) {
        this.plugin = plugin;
        this.areaManager = areaManager;
        this.candyManager = candyManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Area area = areaManager.getAreaAt(player.getLocation());
            if (area != null) {
                if (!hasAccess(player, area)) {
                    continue;
                }
                candyManager.tryGiveCandy(player, area);
            }
        }
    }

    private boolean hasAccess(Player player, Area area) {
        String permission = area.getPermission();
        if (permission == null || permission.isEmpty()) {
            return true;
        }
        if (player.hasPermission(permission)) {
            return true;
        }
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() != Material.AIR && itemInHand.hasItemMeta()) {
            ItemMeta meta = itemInHand.getItemMeta();
            if (meta.getPersistentDataContainer().has(KeoFramer.AREA_ACCESS_KEY, PersistentDataType.STRING)) {
                String itemArea = meta.getPersistentDataContainer().get(KeoFramer.AREA_ACCESS_KEY,
                        PersistentDataType.STRING);
                return area.getName().equals(itemArea);
            }
        }
        return false;
    }
}