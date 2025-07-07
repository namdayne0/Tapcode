package com.namdz.keomenu.hook;

import com.namdz.keomenu.KeoMenu;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    private final KeoMenu plugin;
    private Economy econ = null;
    private boolean enabled;

    public VaultHook(KeoMenu plugin) {
        this.plugin = plugin;
        this.enabled = setupEconomy();
        if (enabled) {
            plugin.getLogger().info("Vault Hook (Economy) đã được bật.");
        } else {
            plugin.getLogger().warning("Vault (Economy) không tìm thấy. Các tính năng kinh tế sẽ không hoạt động.");
        }
    }

    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Economy getEconomy() {
        return econ;
    }

    public boolean has(OfflinePlayer player, double amount) {
        if (enabled) {
            return econ.has(player, amount);
        }
        return false;
    }

    public void withdrawPlayer(OfflinePlayer player, double amount) {
        if (enabled) {
            econ.withdrawPlayer(player, amount);
        }
    }

    public void depositPlayer(OfflinePlayer player, double amount) {
        if (enabled) {
            econ.depositPlayer(player, amount);
        }
    }
}