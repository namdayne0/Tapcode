package com.namdz.keoframer.command.sub;

import com.namdz.keoframer.KeoFramer;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class CreateCommand extends SubCommand {
    public CreateCommand(KeoFramer plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Tạo một khu vực mới.";
    }

    @Override
    public String getSyntax() {
        return "/kf create <tên> <loại_kẹo> <số_lượng> <delay>";
    }

    @Override
    public String getPermission() {
        return "keoframer.admin.create";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "player-only-command", "&cLệnh này chỉ dành cho người chơi.");
            return;
        }
        if (args.length != 4) {
            plugin.sendMessage(sender, "usage.create", getSyntax());
            return;
        }
        Player player = (Player) sender;
        String areaName = args[0];
        String candyType = args[1].toLowerCase();

        if (!plugin.getValidCandyTypes().contains(candyType)) {
            plugin.sendMessage(sender, "invalid-candy-type", "&cLoại kẹo '{type}' không hợp lệ!", "{type}", candyType);
            return;
        }

        try {
            int candyAmount = Integer.parseInt(args[2]);
            String delay = args[3];
            Location pos1 = plugin.getPos1Selections().get(player.getUniqueId());
            Location pos2 = plugin.getPos2Selections().get(player.getUniqueId());

            if (pos1 == null || pos2 == null) {
                plugin.sendMessage(sender, "must-select-positions", "&cBạn phải chọn đủ 2 vị trí bằng /kf wand trước!");
                return;
            }
            if (plugin.getAreaManager().getAreaByName(areaName) != null) {
                plugin.sendMessage(sender, "area-already-exists", "&cTên khu vực '{area_name}' đã tồn tại!",
                        "{area_name}", areaName);
                return;
            }

            plugin.getAreaManager().createArea(areaName, pos1, pos2, candyType, candyAmount, delay);
            plugin.sendMessage(sender, "area-created", "&aĐã tạo thành công khu vực '{area_name}'!", "{area_name}",
                    areaName);

            plugin.getPos1Selections().remove(player.getUniqueId());
            plugin.getPos2Selections().remove(player.getUniqueId());
        } catch (NumberFormatException e) {
            plugin.sendMessage(sender, "invalid-number", "&cSố kẹo phải là một con số!");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) { // Gợi ý loại kẹo
            return plugin.getValidCandyTypes();
        }
        return super.onTabComplete(sender, args);
    }
}