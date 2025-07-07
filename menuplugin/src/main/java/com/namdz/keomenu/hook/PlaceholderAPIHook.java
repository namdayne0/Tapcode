package com.namdz.keomenu.hook;

import com.namdz.keomenu.KeoMenu;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class PlaceholderAPIHook {

    private final KeoMenu plugin;
    private boolean enabled;

    public PlaceholderAPIHook(KeoMenu plugin) {
        this.plugin = plugin;
        this.enabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (enabled) {
            plugin.getLogger().info("PlaceholderAPI Hook đã được bật.");
        } else {
            plugin.getLogger().warning("PlaceholderAPI không tìm thấy. Các placeholder sẽ không hoạt động.");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String setPlaceholders(OfflinePlayer player, String text) {
        if (enabled) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }
}