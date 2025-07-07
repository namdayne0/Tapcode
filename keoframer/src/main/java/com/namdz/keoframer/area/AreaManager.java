package com.namdz.keoframer.area;

import com.namdz.keoframer.KeoFramer;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AreaManager {

    private final KeoFramer plugin;
    private final Map<String, Area> areas = new HashMap<>();
    private final File areasFile;

    public AreaManager(KeoFramer plugin) {
        this.plugin = plugin;
        this.areasFile = new File(plugin.getDataFolder(), "areas.yml");
    }

    public void createArea(String name, Location pos1, Location pos2, String candyType, int candyAmount, String delay) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(areasFile);
        String path = name.toLowerCase();
        config.set(path + ".world", pos1.getWorld().getName());
        config.set(path + ".pos1", String.format("%d,%d,%d", pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ()));
        config.set(path + ".pos2", String.format("%d,%d,%d", pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ()));
        config.set(path + ".candy-type", candyType);
        config.set(path + ".candy-amount", candyAmount);
        config.set(path + ".candy-delay", delay);
        config.set(path + ".success-rate", 100.0);
        config.set(path + ".permission", "keoframer.area." + name.toLowerCase());
        config.set(path + ".no-push", false);
        config.set(path + ".check-y", true);
        saveAndReload(config, name);
    }

    public boolean deleteArea(String name) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(areasFile);
        String path = name.toLowerCase();
        if (!config.contains(path))
            return false;
        config.set(path, null);
        return saveAndReload(config, name);
    }

    public boolean updateAreaBounds(String name, Location pos1, Location pos2) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(areasFile);
        String path = name.toLowerCase();
        if (!config.contains(path))
            return false;
        config.set(path + ".pos1", String.format("%d,%d,%d", pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ()));
        config.set(path + ".pos2", String.format("%d,%d,%d", pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ()));
        return saveAndReload(config, name);
    }

    public boolean setSimpleProperty(String areaName, String property, String value) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(areasFile);
        String path = areaName.toLowerCase();
        if (!config.contains(path))
            return false;

        switch (property.toLowerCase()) {
            case "candy-amount":
                config.set(path + ".candy-amount", Integer.parseInt(value));
                break;
            case "candy-type":
                config.set(path + ".candy-type", value);
                break;
            case "delay":
                config.set(path + ".candy-delay", value);
                break;
            case "permission":
                config.set(path + ".permission", value.equalsIgnoreCase("none") ? null : value);
                break;
            case "nopush":
                config.set(path + ".no-push", Boolean.parseBoolean(value));
                break;
            case "checky":
                config.set(path + ".check-y", Boolean.parseBoolean(value));
                break;
            case "success-rate":
                config.set(path + ".success-rate", Double.parseDouble(value));
                break;
            default:
                return false;
        }
        return saveAndReload(config, areaName);
    }

    public boolean setParticle(String areaName, String... values) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(areasFile);
        String path = areaName.toLowerCase();
        if (!config.contains(path))
            return false;

        if (values[0].equalsIgnoreCase("none")) {
            config.set(path + ".particle", null);
        } else {
            String particlePath = path + ".particle.";
            config.set(particlePath + "type", values[0].toUpperCase());
            config.set(particlePath + "style", (values.length > 1) ? values[1] : "aura");
            if (values.length > 2)
                config.set(particlePath + "particle-data", values[2]);
        }

        return saveAndReload(config, areaName);
    }

    private boolean saveAndReload(FileConfiguration config, String areaName) {
        try {
            config.save(areasFile);
            loadAreas();
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Không thể lưu thay đổi cho khu vực '" + areaName + "'!");
            return false;
        }
    }

    public void loadAreas() {
        areas.clear();
        if (!areasFile.exists()) {
            plugin.getLogger().warning("File areas.yml không tồn tại!");
            return;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(areasFile);
        ConfigurationSection areasSection = config.getConfigurationSection("");
        if (areasSection == null)
            return;

        for (String key : areasSection.getKeys(false)) {
            try {
                String worldName = config.getString(key + ".world");
                org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
                if (bukkitWorld == null) {
                    plugin.getLogger()
                            .warning("Thế giới '" + worldName + "' cho khu vực '" + key + "' không hợp lệ. Bỏ qua...");
                    continue;
                }
                String[] pos1Coords = config.getString(key + ".pos1").split(",");
                String[] pos2Coords = config.getString(key + ".pos2").split(",");
                BlockVector3 pos1 = BlockVector3.at(Integer.parseInt(pos1Coords[0]), Integer.parseInt(pos1Coords[1]),
                        Integer.parseInt(pos1Coords[2]));
                BlockVector3 pos2 = BlockVector3.at(Integer.parseInt(pos2Coords[0]), Integer.parseInt(pos2Coords[1]),
                        Integer.parseInt(pos2Coords[2]));

                String candyType = config.getString(key + ".candy-type", "default_candy");
                int candyAmount = config.getInt(key + ".candy-amount", 1);
                String delayStr = config.getString(key + ".candy-delay", "10s");
                long delayMillis = parseDelay(delayStr);
                double successRate = config.getDouble(key + ".success-rate", 100.0);
                String permission = config.getString(key + ".permission");
                boolean noPush = config.getBoolean(key + ".no-push", false);
                boolean checkY = config.getBoolean(key + ".check-y", true);

                ParticleData particleData = null;
                if (config.isConfigurationSection(key + ".particle")) {
                    try {
                        String particlePath = key + ".particle.";
                        Particle type = Particle
                                .valueOf(config.getString(particlePath + "type", "FLAME").toUpperCase());
                        String style = config.getString(particlePath + "style", "aura");
                        String data = config.getString(particlePath + "particle-data");
                        particleData = new ParticleData(type, style, data);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Tên particle không hợp lệ trong khu vực '" + key + "'.");
                    }
                }
                Area area = new Area(key, bukkitWorld, pos1, pos2, candyType, candyAmount, delayMillis, successRate,
                        permission, noPush, particleData, checkY);
                areas.put(key, area);
            } catch (Exception e) {
                plugin.getLogger().severe("Lỗi khi tải khu vực '" + key + "': " + e.getMessage());
                e.printStackTrace();
            }
        }
        plugin.getLogger().info("Đã tải " + areas.size() + " khu vực.");
    }

    public long parseDelay(String delayStr) {
        if (delayStr == null || delayStr.isEmpty())
            return 10000L;
        Pattern timePattern = Pattern.compile("(\\d+)(ms|s|m|h|d)");
        Matcher matcher = timePattern.matcher(delayStr.toLowerCase());
        if (matcher.matches()) {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            switch (unit) {
                case "ms":
                    return value;
                case "s":
                    return TimeUnit.SECONDS.toMillis(value);
                case "m":
                    return TimeUnit.MINUTES.toMillis(value);
                case "h":
                    return TimeUnit.HOURS.toMillis(value);
                case "d":
                    return TimeUnit.DAYS.toMillis(value);
            }
        }
        plugin.getLogger()
                .warning("Định dạng thời gian không hợp lệ: '" + delayStr + "'. Sử dụng '10s', '5m', '1h', '500ms'.");
        return 10000L;
    }

    public Area getAreaAt(Location location) {
        for (Area area : areas.values()) {
            if (area.isInArea(location)) {
                return area;
            }
        }
        return null;
    }

    public Area getAreaByName(String name) {
        return areas.get(name.toLowerCase());
    }

    public Set<String> getAreaNames() {
        return areas.keySet();
    }
}