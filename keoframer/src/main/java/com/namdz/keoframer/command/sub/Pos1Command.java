package com.namdz.keoframer.command.sub;

import com.namdz.keoframer.KeoFramer;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Pos1Command extends SubCommand {
    public Pos1Command(KeoFramer plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "pos1";
    }

    @Override
    public String getDescription() {
        return "Đặt điểm chọn thứ nhất tại vị trí đang đứng.";
    }

    @Override
    public String getSyntax() {
        return "/kf pos1";
    }

    @Override
    public String getPermission() {
        return "keoframer.admin.pos";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "player-only-command", "&cLệnh này chỉ dành cho người chơi.");
            return;
        }
        Player player = (Player) sender;
        Location loc = player.getLocation().getBlock().getLocation();
        plugin.getPos1Selections().put(player.getUniqueId(), loc);
        String locStr = String.format("X: %d, Y: %d, Z: %d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        plugin.sendMessage(sender, "pos1-selected", "&aĐã chọn vị trí 1: &e{location}", "{location}", locStr);
    }
}