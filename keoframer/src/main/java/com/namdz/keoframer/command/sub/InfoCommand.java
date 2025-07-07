package com.namdz.keoframer.command.sub;

import com.namdz.keoframer.KeoFramer;
import com.namdz.keoframer.area.Area;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class InfoCommand extends SubCommand {
    public InfoCommand(KeoFramer plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "Xem thông tin chi tiết của một khu vực.";
    }

    @Override
    public String getSyntax() {
        return "/kf info <tên_khu_vực>";
    }

    @Override
    public String getPermission() {
        return "keoframer.admin.info";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 1) {
            plugin.sendMessage(sender, "usage.info", getSyntax());
            return;
        }
        String areaName = args[0];
        Area area = plugin.getAreaManager().getAreaByName(areaName);
        if (area == null) {
            plugin.sendMessage(sender, "area-not-found", "&cKhu vực '{area_name}' không tồn tại.", "{area_name}",
                    areaName);
            return;
        }
        sender.sendMessage(ChatColor.AQUA + "--- Thông tin Khu vực: " + ChatColor.YELLOW + area.getName()
                + ChatColor.AQUA + " ---");
        sender.sendMessage(ChatColor.GREEN + "Thế giới: " + ChatColor.WHITE + area.getWorld().getName());
        sender.sendMessage(ChatColor.GREEN + "Chế độ: " + ChatColor.WHITE
                + (area.isCheckY() ? "3D (Giới hạn chiều cao)" : "2D (Sàn / Cột dọc)"));
        sender.sendMessage(ChatColor.GREEN + "Loại kẹo: " + plugin.getCandyDisplayName(area.getCandyType()));
        sender.sendMessage(ChatColor.GREEN + "Kẹo nhận: " + ChatColor.WHITE + area.getCandyAmount());
        sender.sendMessage(ChatColor.GREEN + "Thời gian chờ: " + ChatColor.WHITE
                + TimeUnit.MILLISECONDS.toSeconds(area.getCandyDelayMillis()) + " giây");
        sender.sendMessage(ChatColor.GREEN + "Tỉ lệ thành công: " + ChatColor.WHITE + area.getSuccessRate() + "%");
        sender.sendMessage(ChatColor.GREEN + "Quyền yêu cầu: " + ChatColor.WHITE
                + (area.getPermission() != null ? area.getPermission() : "Không có"));
        sender.sendMessage(ChatColor.GREEN + "Chống đẩy: " + ChatColor.WHITE + (area.isNoPush() ? "Bật" : "Tắt"));
        sender.sendMessage(ChatColor.GREEN + "Particle: " + ChatColor.WHITE
                + (area.getParticleData() != null ? area.getParticleData().toString() : "Không có"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], plugin.getAreaManager().getAreaNames(), new ArrayList<>());
        }
        return super.onTabComplete(sender, args);
    }
}