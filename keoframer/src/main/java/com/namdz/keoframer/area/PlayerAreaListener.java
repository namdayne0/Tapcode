package com.namdz.keoframer.area; // SỬA LỖI: Đặt đúng package là "area"

import com.namdz.keoframer.KeoFramer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class PlayerAreaListener implements Listener {

    private final KeoFramer plugin;
    private final AreaManager areaManager;

    public PlayerAreaListener(KeoFramer plugin, AreaManager areaManager) {
        this.plugin = plugin;
        this.areaManager = areaManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }

        Player player = event.getPlayer();
        Area targetArea = areaManager.getAreaAt(event.getTo());

        if (targetArea != null && targetArea.getPermission() != null && !targetArea.getPermission().isEmpty()) {
            if (!hasAccess(player, targetArea)) {
                String noPermMsg = plugin.getConfig().getString("messages.no-permission-area",
                        "&cBạn không có quyền vào khu vực này!");
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent(ChatColor.translateAlternateColorCodes('&', noPermMsg)));

                Vector pushBackVector = event.getFrom().toVector().subtract(event.getTo().toVector()).normalize();
                player.setVelocity(pushBackVector.multiply(0.5).setY(0.1));
            }
        }
    }

    private boolean hasAccess(Player player, Area area) {
        if (player.hasPermission(area.getPermission())) {
            return true;
        }
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() != Material.AIR && itemInHand.hasItemMeta()) {
            ItemMeta meta = itemInHand.getItemMeta();
            if (meta != null
                    && meta.getPersistentDataContainer().has(KeoFramer.AREA_ACCESS_KEY, PersistentDataType.STRING)) {
                String itemArea = meta.getPersistentDataContainer().get(KeoFramer.AREA_ACCESS_KEY,
                        PersistentDataType.STRING);
                return area.getName().equals(itemArea);
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        switch (event.getCause()) {
            case ENTITY_ATTACK:
            case ENTITY_SWEEP_ATTACK:
            case PROJECTILE:
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                Player player = (Player) event.getEntity();
                Area area = areaManager.getAreaAt(player.getLocation());
                if (area != null && area.isNoPush()) {
                    event.setCancelled(true);
                }
                break;
            default:
                break;
        }
    }
}