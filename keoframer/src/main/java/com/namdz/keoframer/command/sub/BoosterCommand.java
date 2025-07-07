package com.namdz.keoframer.command.sub;

import com.namdz.keoframer.KeoFramer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class BoosterCommand extends SubCommand {
    public BoosterCommand(KeoFramer plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "booster";
    }

    @Override
    public String getDescription() {
        return "Tặng booster nhân kẹo tạm thời cho người chơi.";
    }

    @Override
    public String getSyntax() {
        return "/kf booster <player> <hệ_số> <thời_gian>";
    }

    @Override
    public String getPermission() {
        return "keoframer.admin.booster";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length != 3) {
            plugin.sendMessage(sender, "usage.booster", getSyntax());
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plugin.sendMessage(sender, "player-not-found", "&cNgười chơi '{player_name}' không tồn tại.",
                    "{player_name}", args[0]);
            return;
        }
        try {
            double multiplier = Double.parseDouble(args[1]);
            long durationMillis = plugin.getAreaManager().parseDelay(args[2]);
            if (durationMillis <= 0) {
                plugin.sendMessage(sender, "invalid-number", "&cThời gian phải lớn hơn 0.");
                return;
            }
            plugin.getBuffManager().addTemporaryBooster(target.getUniqueId(), multiplier, durationMillis);
            plugin.sendMessage(sender, "booster-given",
                    "&aĐã tặng booster {multiplier}x trong {duration} cho {player_name}.", "{multiplier}",
                    String.valueOf(multiplier), "{duration}", args[2], "{player_name}", target.getName());
            plugin.sendMessage(target, "booster-received", "&aBạn đã nhận được booster {multiplier}x trong {duration}!",
                    "{multiplier}", String.valueOf(multiplier), "{duration}", args[2]);
        } catch (Exception e) {
            plugin.sendMessage(sender, "invalid-number", "&cLỗi: Sai định dạng hệ số nhân hoặc thời gian.");
        }
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return super.onTabComplete(sender, args);
    }
}