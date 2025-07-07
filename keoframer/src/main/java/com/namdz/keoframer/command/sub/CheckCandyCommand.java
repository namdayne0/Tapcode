package com.namdz.keoframer.command.sub;

import com.namdz.keoframer.KeoFramer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CheckCandyCommand extends SubCommand {
    public CheckCandyCommand(KeoFramer plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "checkcandy";
    }

    @Override
    public String getDescription() {
        return "Kiểm tra số kẹo của người chơi.";
    }

    @Override
    public String getSyntax() {
        return "/kf checkcandy <tên_player> [loại_kẹo]";
    }

    @Override
    public String getPermission() {
        return "keoframer.admin.checkcandy";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 1) {
            plugin.sendMessage(sender, "usage.checkcandy", getSyntax());
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plugin.sendMessage(sender, "player-not-found", "&cNgười chơi '{player_name}' không tồn tại.",
                    "{player_name}", args[0]);
            return;
        }

        if (args.length == 2) {
            String candyType = args[1].toLowerCase();
            if (!plugin.getValidCandyTypes().contains(candyType)) {
                plugin.sendMessage(sender, "invalid-candy-type", "&cLoại kẹo '{type}' không hợp lệ!", "{type}",
                        candyType);
                return;
            }
            int balance = plugin.getCandyManager().getCandy(target.getUniqueId(), candyType);
            plugin.sendMessage(sender, "candy-balance-specific", "&e{player} có {amount} {type}.", "{player}",
                    target.getName(), "{amount}", String.valueOf(balance), "{type}",
                    plugin.getCandyDisplayName(candyType));
        } else {
            Map<String, Integer> allBalances = plugin.getCandyManager().getAllCandies(target.getUniqueId());
            if (allBalances.isEmpty()) {
                plugin.sendMessage(sender, "candy-balance-empty", "&e{player} không có kẹo nào.", "{player}",
                        target.getName());
                return;
            }
            sender.sendMessage(ChatColor.AQUA + "--- Bảng kẹo của " + target.getName() + " ---");
            allBalances.forEach((type, amount) -> sender
                    .sendMessage(plugin.getCandyDisplayName(type) + ": " + ChatColor.WHITE + amount));
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