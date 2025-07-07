package com.namdz.keoframer.buff;

import com.namdz.keoframer.KeoFramer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BuffManager {

    private final KeoFramer plugin;
    public final NamespacedKey COOLDOWN_REDUCTION_KEY;

    private final Map<UUID, BoosterInfo> temporaryBoosters = new ConcurrentHashMap<>();

    public BuffManager(KeoFramer plugin) {
        this.plugin = plugin;
        this.COOLDOWN_REDUCTION_KEY = new NamespacedKey(plugin, "kf_cooldown_reduction");
    }

    public void addTemporaryBooster(UUID playerUuid, double multiplier, long durationMillis) {
        temporaryBoosters.put(playerUuid, new BoosterInfo(multiplier, durationMillis));
    }

    public BoosterInfo getTemporaryBooster(UUID playerUuid) {
        BoosterInfo booster = temporaryBoosters.get(playerUuid);
        if (booster != null && booster.isExpired()) {
            temporaryBoosters.remove(playerUuid);
            return null;
        }
        return booster;
    }

    public Map<UUID, BoosterInfo> getTemporaryBoosters() {
        temporaryBoosters.entrySet().removeIf(entry -> entry.getValue().isExpired());
        return temporaryBoosters;
    }

    private List<ItemStack> getEquippedItems(Player player) {
        List<ItemStack> equippedItems = new ArrayList<>(Arrays.asList(player.getInventory().getArmorContents()));
        equippedItems.add(player.getInventory().getItemInMainHand());
        equippedItems.add(player.getInventory().getItemInOffHand());
        return equippedItems;
    }

    public long getTotalCooldownReduction(Player player) {
        long totalReduction = 0;
        for (ItemStack item : getEquippedItems(player)) {
            if (item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.getPersistentDataContainer().has(COOLDOWN_REDUCTION_KEY, PersistentDataType.LONG)) {
                    totalReduction += meta.getPersistentDataContainer().get(COOLDOWN_REDUCTION_KEY,
                            PersistentDataType.LONG);
                }
            }
        }
        return totalReduction;
    }

    public double getEffectiveMultiplier(Player player, String candyType) {
        double multiplicativeBonus = 1.0;
        double additiveBonus = 0.0;

        List<ItemStack> equippedItems = getEquippedItems(player);

        // --- TÍNH TOÁN CÁC BUFF NHÂN (LẤY CÁI CAO NHẤT) ---
        BoosterInfo tempBooster = getTemporaryBooster(player.getUniqueId());
        if (tempBooster != null) {
            multiplicativeBonus = Math.max(multiplicativeBonus, tempBooster.getMultiplier());
        }

        ConfigurationSection permSection = plugin.getConfig().getConfigurationSection("booster-permissions");
        if (permSection != null) {
            for (String perm : permSection.getKeys(false)) {
                if (player.hasPermission(perm)) {
                    multiplicativeBonus = Math.max(multiplicativeBonus, permSection.getDouble(perm, 1.0));
                }
            }
        }

        NamespacedKey multiplicativeKey = new NamespacedKey(plugin, "kf_booster_" + candyType);
        for (ItemStack item : equippedItems) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.getPersistentDataContainer().has(multiplicativeKey, PersistentDataType.DOUBLE)) {
                    multiplicativeBonus = Math.max(multiplicativeBonus,
                            meta.getPersistentDataContainer().get(multiplicativeKey, PersistentDataType.DOUBLE));
                }
            }
        }

        // --- TÍNH TOÁN CÁC BUFF CỘNG DỒN ---
        NamespacedKey cumulativeKey = new NamespacedKey(plugin, "kf_cumulative_" + candyType);
        for (ItemStack item : equippedItems) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.getPersistentDataContainer().has(cumulativeKey, PersistentDataType.DOUBLE)) {
                    additiveBonus += meta.getPersistentDataContainer().get(cumulativeKey, PersistentDataType.DOUBLE);
                }
            }
        }

        return (1.0 + additiveBonus) * multiplicativeBonus;
    }
}
