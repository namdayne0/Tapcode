package com.namdz.keoframer.api;

import com.namdz.keoframer.KeoFramer;
import com.namdz.keoframer.area.Area;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class KeoFramerAPI {

    private final KeoFramer plugin;

    public KeoFramerAPI(KeoFramer plugin) {
        this.plugin = plugin;
    }

    /**
     * Lấy số lượng của một loại kẹo cụ thể của người chơi.
     */
    public int getCandy(Player player, String candyType) {
        return plugin.getCandyManager().getCandy(player.getUniqueId(), candyType);
    }

    /**
     * Lấy tất cả các loại kẹo và số lượng của người chơi.
     */
    public Map<String, Integer> getAllCandies(Player player) {
        return plugin.getCandyManager().getAllCandies(player.getUniqueId());
    }

    /**
     * Thêm kẹo cho người chơi.
     */
    public void addCandy(Player player, String candyType, int amount) {
        plugin.getCandyManager().addCandy(player.getUniqueId(), candyType, amount);
    }

    /**
     * Lấy đi kẹo của người chơi.
     */
    public boolean takeCandy(Player player, String candyType, int amount) {
        return plugin.getCandyManager().takeCandy(player.getUniqueId(), candyType, amount);
    }

    /**
     * Kiểm tra xem một vị trí có nằm trong một khu vực của KeoFramer không.
     */
    public Area getAreaAt(Location location) {
        return plugin.getAreaManager().getAreaAt(location);
    }

    /**
     * Lấy hệ số nhân kẹo cuối cùng mà người chơi đang được hưởng tại một khu vực.
     */
    public double getEffectiveMultiplier(Player player, Area area) {
        if (area == null) {
            return 1.0;
        }
        return plugin.getBuffManager().getEffectiveMultiplier(player, area.getCandyType());
    }

    // --- CÁC PHƯƠNG THỨC MỚI ĐƯỢC THÊM ---

    /**
     * Lấy danh sách ID của tất cả các loại kẹo đã được đăng ký trong config.yml.
     * 
     * @return Một List<String> chứa các ID (ví dụ: ["keo_do", "keo_xanh"]).
     */
    public List<String> getRegisteredCandyTypes() {
        return plugin.getValidCandyTypes();
    }

    /**
     * Lấy tên hiển thị đã được định dạng mã màu của một loại kẹo.
     * 
     * @param candyId ID của loại kẹo (vd: "keo_do").
     * @return Tên hiển thị (ví dụ: "&cKẹo Đỏ").
     */
    public String getCandyDisplayName(String candyId) {
        return plugin.getCandyDisplayName(candyId);
    }
}