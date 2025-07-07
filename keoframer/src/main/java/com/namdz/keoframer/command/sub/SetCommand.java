package com.namdz.keoframer.command.sub;

import com.namdz.keoframer.KeoFramer;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetCommand extends SubCommand {

    private final List<String> PROPERTIES = Arrays.asList("candy-type", "candy-amount", "delay", "permission", "nopush",
            "checky", "success-rate");

    public SetCommand(KeoFramer plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getDescription() {
        return "Chỉnh sửa một thuộc tính của khu vực.";
    }

    @Override
    public String getSyntax() {
        return "/kf set <tên_kv> <thuộc_tính> <giá_trị>";
    }

    @Override
    public String getPermission() {
        return "keoframer.admin.set";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length != 3) {
            plugin.sendMessage(sender, "usage.set", getSyntax());
            return;
        }
        String areaName = args[0];
        String property = args[1].toLowerCase();
        String value = args[2];
        try {
            if (!PROPERTIES.contains(property)) {
                plugin.sendMessage(sender, "invalid-property", "&cThuộc tính '{property}' không hợp lệ.", "{property}",
                        property);
                return;
            }
            if (property.equals("candy-type") && !plugin.getValidCandyTypes().contains(value.toLowerCase())) {
                plugin.sendMessage(sender, "invalid-candy-type", "&cLoại kẹo '{type}' không hợp lệ!", "{type}", value);
                return;
            }

            if (plugin.getAreaManager().setSimpleProperty(areaName, property, value)) {
                plugin.sendMessage(sender, "area-property-updated", "&aĐã cập nhật '{property}' cho '{area_name}'.",
                        "{property}", property, "{area_name}", areaName);
            } else {
                plugin.sendMessage(sender, "area-not-found", "&cKhu vực '{area_name}' không tồn tại.", "{area_name}",
                        areaName);
            }
        } catch (NumberFormatException e) {
            plugin.sendMessage(sender, "invalid-number", "&cGiá trị số không hợp lệ.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], plugin.getAreaManager().getAreaNames(), new ArrayList<>());
        }
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], PROPERTIES, new ArrayList<>());
        }
        if (args.length == 3) {
            String property = args[1].toLowerCase();
            if (property.equals("nopush") || property.equals("checky")) {
                return StringUtil.copyPartialMatches(args[2], Arrays.asList("true", "false"), new ArrayList<>());
            }
            if (property.equals("candy-type")) {
                return StringUtil.copyPartialMatches(args[2], plugin.getValidCandyTypes(), new ArrayList<>());
            }
        }
        return super.onTabComplete(sender, args);
    }
}