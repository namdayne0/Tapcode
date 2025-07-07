package com.namdz.keomenu.util;

import org.bukkit.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatColorUtil {

    // Regex để tìm các mã màu hex (#RRGGBB)
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");

    /**
     * Dịch các mã màu (&) và mã màu hex (#RRGGBB) sang màu Minecraft.
     *
     * @param message Chuỗi cần dịch màu.
     * @return Chuỗi đã được dịch màu.
     */
    public static String translateColors(String message) {
        // Dịch mã màu hex trước
        Matcher matcher = HEX_COLOR_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            // Chuyển mã hex thành định dạng của ChatColor (ví dụ: §x§R§R§G§G§B§B)
            String hexColor = matcher.group(1);
            StringBuilder fancyColor = new StringBuilder("§x");
            for (char c : hexColor.toCharArray()) {
                fancyColor.append('§').append(c);
            }
            matcher.appendReplacement(buffer, fancyColor.toString());
        }
        matcher.appendTail(buffer);
        message = buffer.toString();

        // Dịch các mã màu truyền thống (&)
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}