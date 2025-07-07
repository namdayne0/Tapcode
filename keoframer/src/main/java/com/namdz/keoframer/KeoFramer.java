package com.namdz.keoframer;

import com.namdz.keoframer.api.KeoFramerAPI;
import com.namdz.keoframer.area.AreaManager;
import com.namdz.keoframer.area.ParticleScheduler;
import com.namdz.keoframer.area.PlayerAreaListener;
import com.namdz.keoframer.buff.BuffManager;
import com.namdz.keoframer.candy.CandyManager;
import com.namdz.keoframer.candy.CandyScheduler;
import com.namdz.keoframer.command.ItemListener;
import com.namdz.keoframer.command.KeoFramerCommand;
import com.namdz.keoframer.placeholder.KeoFramerPlaceholderExtension;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class KeoFramer extends JavaPlugin {

    public static NamespacedKey AREA_ACCESS_KEY;
    private AreaManager areaManager;
    private CandyManager candyManager;
    private BuffManager buffManager;
    private KeoFramerAPI api; // Thêm trường mới cho API
    private BukkitTask particleTask;
    private BukkitTask candyTask;
    private final Map<UUID, Location> pos1Selections = new HashMap<>();
    private final Map<UUID, Location> pos2Selections = new HashMap<>();
    private final Map<String, String> candyTypeDisplayNames = new HashMap<>();

    @Override
    public void onEnable() {
        AREA_ACCESS_KEY = new NamespacedKey(this, "area_access");

        saveDefaultConfig();
        loadCustomConfig();
        saveResource("areas.yml", false);

        this.areaManager = new AreaManager(this);
        this.buffManager = new BuffManager(this);
        this.candyManager = new CandyManager(this);
        this.api = new KeoFramerAPI(this); // Khởi tạo API

        this.areaManager.loadAreas();
        this.candyManager.loadCandies();

        TabExecutor commandHandler = new KeoFramerCommand(this);
        getCommand("kf").setExecutor(commandHandler);
        getCommand("kf").setTabCompleter(commandHandler);
        getCommand("kfreload").setExecutor(commandHandler);

        getServer().getPluginManager().registerEvents(new PlayerAreaListener(this, areaManager), this);
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new KeoFramerPlaceholderExtension(this).register();
            getLogger().info("Đã kết nối với PlaceholderAPI.");
        }

        this.particleTask = new ParticleScheduler(areaManager).runTaskTimer(this, 0L, 20L);
        this.candyTask = new CandyScheduler(this, areaManager, candyManager).runTaskTimer(this, 0L, 20L);
        getLogger().info("KeoFramer v" + getDescription().getVersion() + " đã được bật!");
    }

    @Override
    public void onDisable() {
        if (this.particleTask != null)
            this.particleTask.cancel();
        if (this.candyTask != null)
            this.candyTask.cancel();
        if (this.candyManager != null)
            this.candyManager.saveCandies();
        getLogger().info("KeoFramer đã được tắt!");
    }

    public void reloadPlugin() {
        if (this.candyManager != null)
            this.candyManager.saveCandies();
        if (this.particleTask != null)
            this.particleTask.cancel();
        if (this.candyTask != null)
            this.candyTask.cancel();

        reloadConfig();
        loadCustomConfig();
        saveResource("areas.yml", false);

        areaManager.loadAreas();
        candyManager.loadCandies();

        this.particleTask = new ParticleScheduler(areaManager).runTaskTimer(this, 0L, 20L);
        this.candyTask = new CandyScheduler(this, areaManager, candyManager).runTaskTimer(this, 0L, 20L);
    }

    private void loadCustomConfig() {
        candyTypeDisplayNames.clear();
        ConfigurationSection candySection = getConfig().getConfigurationSection("candy-types");
        if (candySection != null) {
            for (String candyId : candySection.getKeys(false)) {
                String displayName = candySection.getString(candyId + ".display-name", candyId);
                candyTypeDisplayNames.put(candyId.toLowerCase(), displayName);
            }
        }
    }

    public List<String> getValidCandyTypes() {
        return new ArrayList<>(candyTypeDisplayNames.keySet());
    }

    public String getCandyDisplayName(String candyId) {
        return ChatColor.translateAlternateColorCodes('&',
                candyTypeDisplayNames.getOrDefault(candyId.toLowerCase(), candyId));
    }

    public void sendMessage(CommandSender sender, String key, String defaultValue, String... replacements) {
        String message = getConfig().getString("messages." + key, defaultValue);
        String prefix = getConfig().getString("message-prefix", "&b[KeoFramer]&r");
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " " + message));
    }

    /**
     * Phương thức công khai để các plugin khác gọi đến và lấy API.
     */
    public KeoFramerAPI getApi() {
        return api;
    }

    public Map<UUID, Location> getPos1Selections() {
        return pos1Selections;
    }

    public Map<UUID, Location> getPos2Selections() {
        return pos2Selections;
    }

    public AreaManager getAreaManager() {
        return areaManager;
    }

    public CandyManager getCandyManager() {
        return candyManager;
    }

    public BuffManager getBuffManager() {
        return buffManager;
    }
}
