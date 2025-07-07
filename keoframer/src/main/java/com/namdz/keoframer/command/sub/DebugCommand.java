package com.namdz.keoframer.command.sub;

import com.namdz.keoframer.KeoFramer;
import com.namdz.keoframer.area.Area;
import com.namdz.keoframer.buff.BoosterInfo;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class DebugCommand extends SubCommand {
    public DebugCommand(KeoFramer plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "debug";
    }

    @Override
    public String getDescription() {
        return "Hiển thị thông tin gỡ lỗi cho bản thân.";
    }

    @Override
    public String getSyntax() {
        return "/kf debug";
    }

    @Override
    public String getPermission() {
        return "keoframer.admin.debug";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "player-only-command", "&cLệnh này chỉ dành cho người chơi.");
            return;
        }
        Player player = (Player) sender;
        sender.sendMessage(ChatColor.GOLD + "--- KeoFramer Debug Info ---");
        sender.sendMessage(ChatColor.AQUA + "Thông tin người chơi: " + ChatColor.WHITE + player.getName());

        Area area = plugin.getAreaManager().getAreaAt(player.getLocation());
        sender.sendMessage(
                ChatColor.AQUA + "Khu vực hiện tại: " + ChatColor.WHITE + (area != null ? area.getName() : "Không có"));

        if (area != null) {
            long cooldown = plugin.getCandyManager().getRemainingCooldownSeconds(player, area);
            sender.sendMessage(ChatColor.AQUA + "Cooldown còn lại: " + ChatColor.WHITE + cooldown + "s");
            double multiplier = plugin.getBuffManager().getEffectiveMultiplier(player, area.getCandyType());
            sender.sendMessage(ChatColor.AQUA + "Hệ số nhân kẹo (" + area.getCandyType() + "): " + ChatColor.WHITE + "x"
                    + String.format("%.1f", multiplier));
        }

        long reduction = TimeUnit.MILLISECONDS.toSeconds(plugin.getBuffManager().getTotalCooldownReduction(player));
        sender.sendMessage(ChatColor.AQUA + "Tổng giảm thời gian: " + ChatColor.WHITE + reduction + "s");

        BoosterInfo tempBooster = plugin.getBuffManager().getTemporaryBooster(player.getUniqueId());
        if (tempBooster != null) {
            long timeLeft = (tempBooster.getExpiryTimestamp() - System.currentTimeMillis()) / 1000;
            sender.sendMessage(ChatColor.AQUA + "Booster tạm thời: " + ChatColor.WHITE + "x"
                    + tempBooster.getMultiplier() + " (còn " + timeLeft + "s)");
        }

        sender.sendMessage(ChatColor.GOLD + "--- Thông tin Plugin ---");
        sender.sendMessage(ChatColor.AQUA + "Số khu vực đã tải: " + ChatColor.WHITE
                + plugin.getAreaManager().getAreaNames().size());
        sender.sendMessage(ChatColor.AQUA + "Số booster tạm thời hoạt động: " + ChatColor.WHITE
                + plugin.getBuffManager().getTemporaryBoosters().size());
    }
}