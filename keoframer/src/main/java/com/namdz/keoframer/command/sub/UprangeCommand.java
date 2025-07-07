package com.namdz.keoframer.command.sub;

import com.namdz.keoframer.KeoFramer;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class UprangeCommand extends SubCommand {
    public UprangeCommand(KeoFramer plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "uprange";
    }

    @Override
    public String getDescription() {
        return "Cập nhật lại kích thước của một khu vực.";
    }

    @Override
    public String getSyntax() {
        return "/kf uprange <tên_khu_vực>";
    }

    @Override
    public String getPermission() {
        return "keoframer.admin.uprange";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "player-only-command", "&cLệnh này chỉ dành cho người chơi.");
            return;
        }
        if (args.length != 1) {
            plugin.sendMessage(sender, "usage.uprange", getSyntax());
            return;
        }

        Player player = (Player) sender;
        Location pos1 = plugin.getPos1Selections().get(player.getUniqueId());
        Location pos2 = plugin.getPos2Selections().get(player.getUniqueId());

        if (pos1 == null || pos2 == null) {
            plugin.sendMessage(sender, "must-select-positions", "&cBạn phải chọn đủ 2 vị trí bằng /kf wand trước!");
            return;
        }

        String areaName = args[0];
        if (plugin.getAreaManager().updateAreaBounds(areaName, pos1, pos2)) {
            plugin.sendMessage(sender, "area-upranged", "&aĐã cập nhật lại kích thước cho khu vực '{area}'.", "{area}",
                    areaName);
        } else {
            plugin.sendMessage(sender, "area-not-found", "&cKhu vực '{area}' không tồn tại.", "{area}", areaName);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], plugin.getAreaManager().getAreaNames(), new ArrayList<>());
        }
        return super.onTabComplete(sender, args);
    }
}