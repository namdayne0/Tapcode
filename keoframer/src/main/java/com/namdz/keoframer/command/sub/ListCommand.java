package com.namdz.keoframer.command.sub;

import com.namdz.keoframer.KeoFramer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class ListCommand extends SubCommand {
    public ListCommand(KeoFramer plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "Xem danh sách tất cả các khu vực.";
    }

    @Override
    public String getSyntax() {
        return "/kf list";
    }

    @Override
    public String getPermission() {
        return "keoframer.admin.list";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        Set<String> areaNames = plugin.getAreaManager().getAreaNames();
        if (areaNames.isEmpty()) {
            plugin.sendMessage(sender, "area-list-empty", "&eChưa có khu vực nào được tạo.");
            return;
        }
        String prefix = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("message-prefix", "&b[KeoFramer]&r"));
        sender.sendMessage(prefix + " " + ChatColor.GREEN + "Danh sách các khu vực (" + areaNames.size() + "):");
        sender.sendMessage(ChatColor.AQUA + String.join(", ", areaNames));
    }
}