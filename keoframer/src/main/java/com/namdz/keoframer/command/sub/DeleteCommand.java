package com.namdz.keoframer.command.sub;

import com.namdz.keoframer.KeoFramer;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class DeleteCommand extends SubCommand {
    public DeleteCommand(KeoFramer plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public String getDescription() {
        return "Xóa một khu vực đã có.";
    }

    @Override
    public String getSyntax() {
        return "/kf delete <tên_khu_vực>";
    }

    @Override
    public String getPermission() {
        return "keoframer.admin.delete";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length != 1) {
            plugin.sendMessage(sender, "usage.delete", getSyntax());
            return;
        }
        String areaName = args[0];
        if (plugin.getAreaManager().deleteArea(areaName)) {
            plugin.sendMessage(sender, "area-deleted", "&aĐã xóa thành công khu vực '{area_name}'!", "{area_name}",
                    areaName);
        } else {
            plugin.sendMessage(sender, "area-not-found", "&cKhu vực '{area_name}' không tồn tại.", "{area_name}",
                    areaName);
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