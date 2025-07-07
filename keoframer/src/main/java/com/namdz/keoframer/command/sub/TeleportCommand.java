package com.namdz.keoframer.command.sub;

import com.namdz.keoframer.KeoFramer;
import com.namdz.keoframer.area.Area;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class TeleportCommand extends SubCommand {
    public TeleportCommand(KeoFramer plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "tp";
    }

    @Override
    public String getDescription() {
        return "Dịch chuyển đến trung tâm một khu vực.";
    }

    @Override
    public String getSyntax() {
        return "/kf tp <tên_khu_vực>";
    }

    @Override
    public String getPermission() {
        return "keoframer.admin.tp";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "player-only-command", "&cLệnh này chỉ dành cho người chơi.");
            return;
        }
        if (args.length < 1) {
            plugin.sendMessage(sender, "usage.tp", getSyntax());
            return;
        }
        Player player = (Player) sender;
        String areaName = args[0];
        Area area = plugin.getAreaManager().getAreaByName(areaName);
        if (area == null) {
            plugin.sendMessage(sender, "area-not-found", "&cKhu vực '{area_name}' không tồn tại.", "{area_name}",
                    areaName);
            return;
        }
        player.teleport(area.getCenter());
        plugin.sendMessage(sender, "teleported-to-area", "&aĐã dịch chuyển bạn đến trung tâm khu vực '{area_name}'.",
                "{area_name}", areaName);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], plugin.getAreaManager().getAreaNames(), new ArrayList<>());
        }
        return super.onTabComplete(sender, args);
    }
}