package com.namdz.keoframer.placeholder;

import com.namdz.keoframer.KeoFramer;
import com.namdz.keoframer.area.Area;
import com.namdz.keoframer.buff.BoosterInfo;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class KeoFramerPlaceholderExtension extends PlaceholderExpansion {

    private final KeoFramer plugin;

    public KeoFramerPlaceholderExtension(KeoFramer plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "keoframer";
    }

    @Override
    public @NotNull String getAuthor() {
        return "NamDz";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // Giữ cache để tối ưu hiệu năng
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        // Placeholder cho các loại kẹo cụ thể: %keoframer_candy_<tên_loại_kẹo>%
        if (params.startsWith("candy_")) {
            String candyType = params.substring(6);
            return String.valueOf(plugin.getCandyManager().getCandy(player.getUniqueId(), candyType));
        }

        // Lấy khu vực người chơi đang đứng (có thể là null)
        Area area = plugin.getAreaManager().getAreaAt(player.getLocation());

        switch (params.toLowerCase()) {
            case "cooldown":
                if (area == null) {
                    return "0"; // Nếu không trong khu vực, không có cooldown
                }
                return String.valueOf(plugin.getCandyManager().getRemainingCooldownSeconds(player, area));

            case "cooldown_reduction":
                long reductionMillis = plugin.getBuffManager().getTotalCooldownReduction(player);
                return String.valueOf(TimeUnit.MILLISECONDS.toSeconds(reductionMillis));

            case "booster_multiplier":
                // SỬA LỖI LOGIC TẠI ĐÂY
                if (area == null) {
                    // Nếu người chơi không ở trong khu vực nào, trả về thông báo
                    return "Vào khu vực để biết";
                }
                // Nếu có, tính toán hệ số nhân như bình thường
                double multiplier = plugin.getBuffManager().getEffectiveMultiplier(player, area.getCandyType());
                return String.format("%.1f", multiplier);

            case "booster_timeleft":
                BoosterInfo tempBooster = plugin.getBuffManager().getTemporaryBooster(player.getUniqueId());
                if (tempBooster != null) {
                    long timeLeftSeconds = (tempBooster.getExpiryTimestamp() - System.currentTimeMillis()) / 1000;
                    return timeLeftSeconds > 0 ? String.valueOf(timeLeftSeconds) : "0";
                }
                return "0";

            default:
                return null;
        }
    }
}