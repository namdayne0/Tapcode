package com.namdz.keomenu.hook;

import com.namdz.keomenu.KeoMenu;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
// Import API của KeoFramer nếu có. Ví dụ:
// import com.keoframer.api.KeoFramerAPI;

public class KeoFramerHook {

    private final KeoMenu plugin;
    private boolean enabled;
    // private KeoFramerAPI keoFramerAPI; // Nếu có API thật sự

    public KeoFramerHook(KeoMenu plugin) {
        this.plugin = plugin;
        // Kiểm tra xem plugin KeoFramer có được bật không
        this.enabled = Bukkit.getPluginManager().isPluginEnabled("KeoFramer"); // Thay "KeoFramer" bằng tên plugin của
                                                                               // bạn

        if (enabled) {
            // Nếu có API thật sự của KeoFramer, bạn sẽ khởi tạo nó ở đây
            // keoFramerAPI = KeoFramerAPI.getPlugin(); // Ví dụ
            plugin.getLogger().info("KeoFramer Hook đã được bật.");
        } else {
            plugin.getLogger().warning("KeoFramer không tìm thấy. Các tính năng liên quan đến kẹo sẽ không hoạt động.");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    // Các phương thức để tương tác với KeoFramer (ví dụ về hệ thống kẹo)
    // Bạn cần thay thế bằng API thực sự của KeoFramer nếu có.

    /**
     * Lấy số lượng kẹo của người chơi.
     * 
     * @param player    Người chơi.
     * @param candyType Loại kẹo.
     * @return Số lượng kẹo hiện có.
     */
    public int getCandy(OfflinePlayer player, String candyType) {
        if (enabled) {
            // Đây là nơi bạn sẽ gọi API của KeoFramer để lấy số kẹo
            // Ví dụ: return keoFramerAPI.getCandy(player, candyType);
            // Hiện tại là giả định:
            return 1000; // Trả về 1000 để testing
        }
        return 0;
    }

    /**
     * Thêm kẹo cho người chơi.
     * 
     * @param player    Người chơi.
     * @param candyType Loại kẹo.
     * @param amount    Số lượng cần thêm.
     */
    public void addCandy(OfflinePlayer player, String candyType, int amount) {
        if (enabled) {
            // keoFramerAPI.addCandy(player, candyType, amount); // Ví dụ
            plugin.getLogger().info("Thêm " + amount + " kẹo " + candyType + " cho " + player.getName() + " (Giả lập)");
        }
    }

    /**
     * Trừ kẹo của người chơi.
     * 
     * @param player    Người chơi.
     * @param candyType Loại kẹo.
     * @param amount    Số lượng cần trừ.
     * @return true nếu trừ thành công, false nếu không đủ.
     */
    public boolean takeCandy(OfflinePlayer player, String candyType, int amount) {
        if (enabled) {
            // return keoFramerAPI.takeCandy(player, candyType, amount); // Ví dụ
            if (getCandy(player, candyType) >= amount) { // Kiểm tra giả định
                plugin.getLogger()
                        .info("Trừ " + amount + " kẹo " + candyType + " từ " + player.getName() + " (Giả lập)");
                return true;
            }
        }
        return false;
    }
}