package com.namdz.keomenu.menu;

import com.namdz.keoframer.api.KeoFramerAPI;
import org.bukkit.entity.Player;

public class Requirement {
    private final String type;
    private final String value;
    private final String failMessage;

    public Requirement(String type, String value, String failMessage) {
        this.type = type;
        this.value = value;
        this.failMessage = failMessage;
    }

    public boolean isMet(Player player, KeoFramerAPI keoFramerAPI) {
        switch (type) {
            case "money":
                try {
                    double requiredMoney = Double.parseDouble(value);
                    // Ở đây bạn cần tích hợp với Vault API để kiểm tra tiền
                    // Hiện tại chỉ là ví dụ giả định
                    // if (VaultHook.getEconomy().has(player, requiredMoney)) { return true; }
                    player.sendMessage("Requirement tiền chưa được triển khai hoàn chỉnh."); // Gỡ bỏ dòng này khi tích
                                                                                             // hợp Vault
                    return true; // Tạm thời trả về true
                } catch (NumberFormatException e) {
                    return false;
                }
            case "candy_keo_do":
            case "candy_keo_xanh":
            case "candy_keo_vang":
                String candyType = type.replace("candy_", "");
                try {
                    int requiredCandy = Integer.parseInt(value);
                    return keoFramerAPI.getCandy(player, candyType) >= requiredCandy;
                } catch (NumberFormatException e) {
                    return false;
                }
            default:
                return true;
        }
    }

    public String getFailMessage() {
        return failMessage;
    }
}