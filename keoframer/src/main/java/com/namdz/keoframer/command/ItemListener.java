package com.namdz.keoframer.command;

import com.namdz.keoframer.KeoFramer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ItemListener implements Listener {

    private final KeoFramer plugin;

    public ItemListener(KeoFramer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack itemInHand = event.getItem();

        if (itemInHand == null || itemInHand.getType().isAir()) {
            return;
        }

        String wandItemId = plugin.getConfig().getString("wand-item", "BLAZE_ROD");
        Material wandMaterial = Material.matchMaterial(wandItemId);
        if (wandMaterial == null)
            wandMaterial = Material.BLAZE_ROD;

        if (itemInHand.getType() == wandMaterial && player.hasPermission("keoframer.admin")) {
            if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
                if (event.getClickedBlock() != null) {
                    handleWandClick(event, player, action);
                }
            }
        }
    }

    private void handleWandClick(PlayerInteractEvent event, Player player, Action action) {
        Location blockLocation = event.getClickedBlock().getLocation();
        event.setCancelled(true);
        String locStr = String.format("X: %d, Y: %d, Z: %d", blockLocation.getBlockX(), blockLocation.getBlockY(),
                blockLocation.getBlockZ());

        if (action == Action.LEFT_CLICK_BLOCK) {
            plugin.getPos1Selections().put(player.getUniqueId(), blockLocation);
            plugin.sendMessage(player, "pos1-selected", "&aĐã chọn vị trí 1: &e{location}", "{location}", locStr);
        } else {
            plugin.getPos2Selections().put(player.getUniqueId(), blockLocation);
            plugin.sendMessage(player, "pos2-selected", "&aĐã chọn vị trí 2: &e{location}", "{location}", locStr);
        }
    }
}
