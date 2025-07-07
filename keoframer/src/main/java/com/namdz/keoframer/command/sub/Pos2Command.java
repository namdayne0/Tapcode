package com.namdz.keoframer.command.sub;

import com.namdz.keoframer.KeoFramer;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Pos2Command extends SubCommand {
    public Pos2Command(KeoFramer plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "pos2";
    }

    @Override
    public String getDescription() {
        return "Đặt điểm chọn thứ hai tại vị trí đang đứng.";
    }

    @Override
    public String getSyntax() {
        return "/kf pos2";
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
        plugin.getPos2Selections().put(player.getUniqueId(), loc);
        String locStr = String.format("X: %d, Y: %d, Z: %d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        plugin.sendMessage(sender, "pos2-selected", "&aĐã chọn vị trí 2: &e{location}", "{location}", locStr);
    }
}