package com.namdz.keoframer.command.sub;

import com.namdz.keoframer.KeoFramer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class PapiCommand extends SubCommand {
    public PapiCommand(KeoFramer plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "papi";
    }

    @Override
    public String getDescription() {
        return "Hiển thị danh sách các placeholder của plugin.";
    }

    @Override
    public String getSyntax() {
        return "/kf papi";
    }

    @Override
    public String getPermission() {
        return "keoframer.admin.papi";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.GOLD + "--- Danh sách Placeholder của KeoFramer ---");
        sender.sendMessage(ChatColor.YELLOW + "%keoframer_candy_<tên_loại_kẹo>%" + ChatColor.GRAY
                + " - Số lượng một loại kẹo cụ thể.");
        sender.sendMessage(ChatColor.YELLOW + "%keoframer_cooldown%" + ChatColor.GRAY + " - Thời gian chờ còn lại.");
        sender.sendMessage(
                ChatColor.YELLOW + "%keoframer_cooldown_reduction%" + ChatColor.GRAY + " - Tổng số giây được giảm.");
        sender.sendMessage(
                ChatColor.YELLOW + "%keoframer_booster_multiplier%" + ChatColor.GRAY + " - Hệ số nhân kẹo hiện tại.");
        sender.sendMessage(ChatColor.YELLOW + "%keoframer_booster_timeleft%" + ChatColor.GRAY
                + " - Thời gian còn lại của booster tạm thời.");
    }
}