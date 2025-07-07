package com.namdz.keoframer.candy;

import com.namdz.keoframer.KeoFramer;
import com.namdz.keoframer.area.Area;
import com.namdz.keoframer.buff.BuffManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CandyManager {

    private final KeoFramer plugin;
    private final BuffManager buffManager;
    private final Map<UUID, Map<String, Integer>> playerCandies = new HashMap<>();
    private final Map<UUID, Map<String, Long>> playerCooldowns = new HashMap<>();
    private File candiesFile;
    private FileConfiguration candiesConfig;

    public CandyManager(KeoFramer plugin) {
        this.plugin = plugin;
        this.buffManager = plugin.getBuffManager();
        this.candiesFile = new File(plugin.getDataFolder(), "candies.yml");
    }

    public void tryGiveCandy(Player player, Area area) {
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long effectiveCooldown = getEffectiveCooldown(player, area);

        playerCooldowns.putIfAbsent(uuid, new HashMap<>());
        Map<String, Long> cooldowns = playerCooldowns.get(uuid);
        long lastGiveTime = cooldowns.getOrDefault(area.getName(), 0L);

        if (currentTime >= lastGiveTime + effectiveCooldown) {
            cooldowns.put(area.getName(), currentTime);

            double chance = Math.random() * 100;
            if (chance < area.getSuccessRate()) {
                String candyType = area.getCandyType();
                double multiplier = buffManager.getEffectiveMultiplier(player, candyType);
                int finalCandyAmount = (int) Math.round(area.getCandyAmount() * multiplier);

                addCandy(uuid, candyType, finalCandyAmount);

                String displayName = plugin.getCandyDisplayName(candyType);
                String message = ChatColor.GREEN + "+ " + finalCandyAmount + " " + displayName + ChatColor.GRAY + " (x"
                        + String.format("%.1f", multiplier) + ")";
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
            } else {
                String failMessage = plugin.getConfig().getString("messages.candy-fail", "&cKhông may mắn lần này!");
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent(ChatColor.translateAlternateColorCodes('&', failMessage)));
            }
        }
    }

    public long getRemainingCooldownSeconds(Player player, Area area) {
        if (player == null || area == null)
            return 0;
        UUID uuid = player.getUniqueId();
        playerCooldowns.putIfAbsent(uuid, new HashMap<>());
        Map<String, Long> cooldowns = playerCooldowns.get(uuid);
        long lastGiveTime = cooldowns.getOrDefault(area.getName(), 0L);
        long effectiveCooldown = getEffectiveCooldown(player, area);
        long deadline = lastGiveTime + effectiveCooldown;
        long remainingMillis = deadline - System.currentTimeMillis();
        return remainingMillis > 0 ? TimeUnit.MILLISECONDS.toSeconds(remainingMillis) + 1 : 0;
    }

    private long getEffectiveCooldown(Player player, Area area) {
        long baseCooldown = area.getCandyDelayMillis();
        long reduction = buffManager.getTotalCooldownReduction(player);
        return Math.max(100L, baseCooldown - reduction);
    }

    public void addCandy(UUID uuid, String type, int amount) {
        playerCandies.putIfAbsent(uuid, new HashMap<>());
        Map<String, Integer> balances = playerCandies.get(uuid);
        balances.put(type.toLowerCase(), balances.getOrDefault(type.toLowerCase(), 0) + amount);
    }

    public boolean takeCandy(UUID uuid, String type, int amount) {
        if (getCandy(uuid, type) >= amount) {
            addCandy(uuid, type, -amount);
            return true;
        }
        return false;
    }

    public int getCandy(UUID uuid, String type) {
        return playerCandies.getOrDefault(uuid, Collections.emptyMap()).getOrDefault(type.toLowerCase(), 0);
    }

    public Map<String, Integer> getAllCandies(UUID uuid) {
        return playerCandies.getOrDefault(uuid, Collections.emptyMap());
    }

    public void loadCandies() {
        playerCandies.clear();
        if (!candiesFile.exists()) {
            try {
                candiesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        candiesConfig = YamlConfiguration.loadConfiguration(candiesFile);
        ConfigurationSection mainSection = candiesConfig.getConfigurationSection("players");
        if (mainSection == null)
            return;

        for (String uuidStr : mainSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection candySection = mainSection.getConfigurationSection(uuidStr);
                if (candySection != null) {
                    Map<String, Integer> balances = new HashMap<>();
                    for (String candyType : candySection.getKeys(false)) {
                        balances.put(candyType, candySection.getInt(candyType));
                    }
                    playerCandies.put(uuid, balances);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("UUID không hợp lệ trong candies.yml: " + uuidStr);
            }
        }
    }

    public void saveCandies() {
        try {
            candiesConfig = new YamlConfiguration();
            for (Map.Entry<UUID, Map<String, Integer>> playerEntry : playerCandies.entrySet()) {
                String uuidStr = playerEntry.getKey().toString();
                for (Map.Entry<String, Integer> candyEntry : playerEntry.getValue().entrySet()) {
                    if (candyEntry.getValue() > 0) {
                        candiesConfig.set("players." + uuidStr + "." + candyEntry.getKey(), candyEntry.getValue());
                    }
                }
            }
            candiesConfig.save(candiesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
