package com.namdz.keomenu.command;

import com.namdz.keomenu.KeoMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.namdz.keomenu.util.ChatColorUtil; // Import đúng đường dẫn

public class KeoMenuCommand implements CommandExecutor {

    private final KeoMenu plugin;

    public KeoMenuCommand(KeoMenu plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColorUtil.translateColors("&cChỉ người chơi mới có thể sử dụng lệnh này."));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (player.hasPermission("keomenu.reload")) {
                plugin.reloadConfig();
                plugin.getMenuManager().loadAllMenus(); // Tải lại tất cả menu
                player.sendMessage(ChatColorUtil.translateColors("&aKeoMenu đã được tải lại thành công!"));
            } else {
                player.sendMessage(ChatColorUtil.translateColors("&cBạn không có quyền để tải lại plugin."));
            }
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("open")) {
            if (player.hasPermission("keomenu.open") || player.hasPermission("keomenu.open." + args[1])) {
                String menuId = args[1];
                plugin.getMenuManager().openMenu(player, menuId);
            } else {
                player.sendMessage(ChatColorUtil.translateColors("&cBạn không có quyền để mở menu này."));
            }
            return true;
        }

        player.sendMessage(ChatColorUtil.translateColors("&a--- KeoMenu Help ---"));
        player.sendMessage(ChatColorUtil.translateColors("&a/keomenu reload &7- Tai lai cau hinh plugin."));
        player.sendMessage(ChatColorUtil.translateColors("&a/keomenu open <menu_id> &7- Mo mot menu cu the."));
        return true;
    }
}