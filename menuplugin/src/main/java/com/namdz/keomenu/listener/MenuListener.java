package com.namdz.keomenu.listeners;

import com.namdz.keomenu.KeoMenu;
import com.namdz.keomenu.menu.CustomMenu;
import com.namdz.keomenu.menu.MenuItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class MenuListener implements Listener {

    private final KeoMenu plugin;

    public MenuListener(KeoMenu plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        InventoryHolder holder = event.getInventory().getHolder();

        // Kiểm tra xem inventory có phải là của CustomMenu không
        if (holder instanceof CustomMenu) {
            event.setCancelled(true); // Ngăn người chơi lấy hoặc đặt đồ trong menu

            CustomMenu currentMenu = (CustomMenu) holder;
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType().isAir()) {
                return; // Click vào chỗ trống
            }

            int slot = event.getRawSlot();
            // Lấy MenuItem từ map items của CustomMenu
            MenuItem menuItem = currentMenu.getItems().get(slot);

            if (menuItem != null) {
                // Kiểm tra các yêu cầu trước khi thực hiện hành động
                if (menuItem.checkRequirements(player)) {
                    menuItem.executeActions(player);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof CustomMenu) {
            // Logic xử lý khi đóng menu (nếu cần).
            // Ví dụ: hủy các BukkitRunnable cụ thể cho người chơi này nếu bạn theo dõi
            // chúng.
            // Hiện tại, BukkitRunnable trong CustomMenu.open() sẽ tự hủy khi người chơi
            // đóng inventory.
        }
    }
}