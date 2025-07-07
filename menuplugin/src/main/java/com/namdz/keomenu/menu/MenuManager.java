package com.namdz.keomenu.menu;

import com.namdz.keomenu.KeoMenu;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID; // Import UUID

public class MenuManager {

    private final KeoMenu plugin;
    private final Map<String, CustomMenu> loadedMenus; // Lưu các menu đã tải
    // Map để theo dõi menu nào người chơi đang mở
    private final Map<UUID, CustomMenu> playerOpenMenus;

    public MenuManager(KeoMenu plugin) {
        this.plugin = plugin;
        this.loadedMenus = new HashMap<>();
        this.playerOpenMenus = new HashMap<>(); // Khởi tạo map theo dõi
    }

    public void loadAllMenus() {
        loadedMenus.clear(); // Xóa các menu cũ trước khi tải lại
        playerOpenMenus.clear(); // Xóa luôn danh sách menu đang mở của người chơi khi reload

        File menusFolder = new File(plugin.getDataFolder(), "menus");
        if (!menusFolder.exists()) {
            menusFolder.mkdirs();
        }

        File[] menuFiles = menusFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (menuFiles != null) {
            for (File file : menuFiles) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                String menuId = config.getString("menu_id");
                if (menuId == null || menuId.isEmpty()) {
                    plugin.getLogger().warning("Menu file " + file.getName() + " thiếu 'menu_id'. Bỏ qua.");
                    continue;
                }
                CustomMenu menu = new CustomMenu(plugin, menuId, config);
                loadedMenus.put(menuId, menu);
                plugin.getLogger().info("Đã tải menu: " + menuId);
            }
        } else {
            plugin.getLogger().warning("Không tìm thấy file menu nào trong thư mục 'menus'.");
        }
    }

    public void openMenu(Player player, String menuId) {
        CustomMenu menu = loadedMenus.get(menuId);
        if (menu != null) {
            // Kiểm tra quyền hạn của menu
            if (menu.getPermission() != null && !menu.getPermission().isEmpty()) {
                if (!player.hasPermission(menu.getPermission())) {
                    player.sendMessage("§cBạn không có quyền để mở menu này.");
                    return;
                }
            }
            menu.open(player); // Gọi phương thức open trên CustomMenu
            plugin.getLogger().info(player.getName() + " đã mở menu: " + menuId);
        } else {
            player.sendMessage("§cMenu '" + menuId + "' không tồn tại.");
            plugin.getLogger()
                    .warning("Người chơi " + player.getName() + " đã cố gắng mở menu không tồn tại: " + menuId);
        }
    }

    // ========================================================================
    // Các phương thức theo dõi menu đang mở của người chơi
    // ========================================================================

    public CustomMenu getPlayerOpenMenu(Player player) {
        return playerOpenMenus.get(player.getUniqueId());
    }

    public void setPlayerOpenMenu(Player player, CustomMenu menu) {
        playerOpenMenus.put(player.getUniqueId(), menu);
    }

    public void removePlayerOpenMenu(Player player) {
        playerOpenMenus.remove(player.getUniqueId());
    }

    // ========================================================================
    // Logic cập nhật menu theo update_interval
    // ========================================================================

    public void startMenuUpdateTask() {
        // Dừng tác vụ cũ nếu có để tránh trùng lặp khi reload
        if (plugin.getServer().getScheduler().isCurrentlyRunning(getPlugin().getMenuUpdateTaskId())) {
            plugin.getServer().getScheduler().cancelTask(getPlugin().getMenuUpdateTaskId());
        }

        // Bắt đầu một async task để cập nhật menu
        // Thực hiện mỗi 1 tick (hoặc một khoảng thời gian nhỏ hơn nếu muốn cập nhật
        // mượt hơn)
        // Việc này nên được định nghĩa trong Main class để có thể hủy bỏ
        plugin.setMenuUpdateTaskId(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            // Duyệt qua tất cả người chơi đang mở menu của plugin
            for (Map.Entry<UUID, CustomMenu> entry : playerOpenMenus.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null && player.isOnline()) {
                    CustomMenu menu = entry.getValue();
                    // Duyệt qua các item trong menu để tìm item có update_interval
                    // Đây là cách tối ưu hơn so với việc update toàn bộ menu
                    for (Map.Entry<Integer, MenuItem> itemEntry : menu.getItems().entrySet()) {
                        MenuItem menuItem = itemEntry.getValue();
                        if (menuItem.getUpdateInterval() > 0) {
                            // Kiểm tra xem đã đến lúc cập nhật item này chưa
                            // Bạn cần một cơ chế để theo dõi thời gian cập nhật của từng item
                            // Ví dụ: Lưu thời gian lần cuối cập nhật trong MenuItem hoặc Map riêng
                            // Cho mục đích đơn giản, ta sẽ chỉ update khi interval là 1 tick
                            // hoặc bạn cần thêm Map<UUID, Map<Integer, Long>> lastUpdateTime
                            // để theo dõi thời gian cập nhật cho từng item của từng người chơi

                            // For simplicity: just update if it's supposed to update every tick (20 ticks =
                            // 1 sec)
                            // A more robust solution would track last update time per player per item
                            if (menuItem.getUpdateInterval() == 1
                                    || (Bukkit.getCurrentTick() % menuItem.getUpdateInterval() == 0)) {
                                // Cần chạy trên Main Thread để cập nhật Inventory
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    // Kiểm tra lại xem người chơi còn đang mở menu đó không
                                    if (player.getOpenInventory().getTitle().equals(menu.getTitle())) {
                                        player.getOpenInventory().getTopInventory().setItem(menuItem.getSlot(),
                                                menuItem.build(player));
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }, 1L, 1L).getTaskId()); // Bắt đầu sau 1 tick, lặp lại mỗi 1 tick
    }
}