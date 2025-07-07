package com.namdz.keoframer.command.sub;

import com.namdz.keoframer.KeoFramer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddCandyCommand extends SubCommand {
    public AddCandyCommand(KeoFramer plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "addcandy";
    }

    @Override
    public String getDescription() {
        return "Thêm kẹo cho một người chơi.";
    }

    @Override
    public String getSyntax() {
        return "/kf addcandy <tên_player> <loại_kẹo> <số_lượng>";
    }

    @Override
    public String getPermission() {
        return "keoframer.admin.addcandy"; // Quyền chung, quyền chi tiết sẽ check bên dưới
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length != 3) {
            plugin.sendMessage(sender, "usage.addcandy", getSyntax());
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plugin.sendMessage(sender, "player-not-found", "&cNgười chơi '{player_name}' không tồn tại.",
                    "{player_name}", args[0]);
            return;
        }

        String candyType = args[1].toLowerCase();
        if (!plugin.getValidCandyTypes().contains(candyType)) {
            plugin.sendMessage(sender, "invalid-candy-type", "&cLoại kẹo '{type}' không hợp lệ!", "{type}", candyType);
            return;
        }

        if (!sender.hasPermission("keoframer.addcandy." + candyType) && !sender.hasPermission("keoframer.addcandy.*")) {
            plugin.sendMessage(sender, "no-permission", "&cBạn không có quyền thêm loại kẹo này.");
            return;
        }

        try {
            int amount = Integer.parseInt(args[2]);
            plugin.getCandyManager().addCandy(target.getUniqueId(), candyType, amount);
            plugin.sendMessage(sender, "candy-added", "&aĐã thêm {amount} {type} cho {player_name}.", "{amount}",
                    String.valueOf(amount), "{type}", plugin.getCandyDisplayName(candyType), "{player_name}",
                    target.getName());
            plugin.sendMessage(target, "candy-received-admin", "&aBạn đã nhận được {amount} {type} từ quản trị viên.",
                    "{amount}", String.valueOf(amount), "{type}", plugin.getCandyDisplayName(candyType));
        } catch (NumberFormatException e) {
            plugin.sendMessage(sender, "invalid-number", "&cSố lượng kẹo không hợp lệ.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], plugin.getValidCandyTypes(), new ArrayList<>());
        }
        return super.onTabComplete(sender, args);
    }
}