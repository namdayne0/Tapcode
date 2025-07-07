package com.namdz.keomenu.menu;

import com.namdz.keomenu.KeoMenu;
import com.namdz.keomenu.util.ChatColorUtil; // Sửa từ utils thành util
// import com.namdz.keomenu.utils.PlaceholderUtil; // Xóa dòng này, thay bằng PlaceholderAPIHook
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects; // Thêm import này

public class CustomMenu {

    private final KeoMenu plugin;
    private final String id;
    private final String title;
    private final int rows;
    private final List<String> openCommands;
    private final String permission;
    private final Map<Integer, MenuItem> items; // Lưu MenuItem theo slot
    private Inventory inventory;

    public CustomMenu(KeoMenu plugin, String id, ConfigurationSection section) {
        this.plugin = plugin;
        this.id = id;
        this.title = ChatColorUtil.translateColors(section.getString("menu_title", "Default Menu"));
        this.rows = section.getInt("rows", 3);
        this.openCommands = section.getStringList("open_commands");
        this.permission = section.getString("permission", null); // Có thể null
        this.items = new HashMap<>();
        this.inventory = Bukkit.createInventory(null, rows * 9, title);

        ConfigurationSection itemsSection = section.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(key);
                    ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                    // Lỗi: method loadFromConfig
                    // Sửa thành:
                    MenuItem menuItem = new MenuItem(plugin, itemSection); // Tạo MenuItem mới bằng constructor
                    this.items.put(slot, menuItem);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Slot không hợp lệ trong menu " + id + ": " + key);
                }
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getRows() {
        return rows;
    }

    public List<String> getOpenCommands() {
        return openCommands;
    }

    public String getPermission() {
        return permission;
    }

    public Map<Integer, MenuItem> getItems() {
        return items;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public MenuItem getItemInSlot(int slot) {
        return items.get(slot);
    }

    // Phương thức để mở menu cho người chơi
    public void open(Player player) {
        // Tạo một Inventory mới mỗi khi mở để tránh vấn đề concurrent modification
        // và để placeholders có thể cập nhật
        Inventory playerInventory = Bukkit.createInventory(null, rows * 9, title);

        for (Map.Entry<Integer, MenuItem> entry : items.entrySet()) {
            int slot = entry.getKey();
            MenuItem menuItem = entry.getValue();
            // Xây dựng item với placeholders đã được xử lý cho người chơi hiện tại
            playerInventory.setItem(slot, menuItem.build(player));
        }
        player.openInventory(playerInventory);
        this.inventory = playerInventory; // Cập nhật Inventory đang được mở
        plugin.getMenuManager().setPlayerOpenMenu(player, this); // Theo dõi menu đang mở của người chơi
    }

    // Thêm phương thức này để cập nhật menu sau các hành động
    public void updateMenuForPlayer(Player player) {
        if (!player.isOnline() || !player.getOpenInventory().getTitle().equals(this.title)) {
            // Đảm bảo người chơi vẫn online và đang mở đúng menu này
            return;
        }

        Inventory currentInventory = player.getOpenInventory().getTopInventory();
        for (Map.Entry<Integer, MenuItem> entry : items.entrySet()) {
            int slot = entry.getKey();
            MenuItem menuItem = entry.getValue();
            currentInventory.setItem(slot, menuItem.build(player)); // Cập nhật lại item
        }
    }
}