package com.namdz.keoframer.command.sub;

import com.namdz.keoframer.KeoFramer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class HelpCommand extends SubCommand {

    // Lớp này cần truy cập vào danh sách các lệnh con khác
    private final Map<String, SubCommand> subCommands;

    public HelpCommand(KeoFramer plugin, Map<String, SubCommand> subCommands) {
        super(plugin);
        this.subCommands = subCommands;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Hiển thị danh sách tất cả các lệnh.";
    }

    @Override
    public String getSyntax() {
        return "/kf help";
    }

    @Override
    public String getPermission() {
        return null; // Không cần quyền đặc biệt
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        String prefix = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("message-prefix", "&b[KeoFramer]&r"));
        String version = plugin.getDescription().getVersion();

        sender.sendMessage(prefix + ChatColor.AQUA + "--- KeoFramer Help (v" + version + ") ---");

        subCommands.values().stream()
                .filter(sub -> sub.getPermission() == null || sender.hasPermission(sub.getPermission()))
                .sorted((s1, s2) -> s1.getName().compareTo(s2.getName()))
                .forEach(sub -> {
                    sender.sendMessage(
                            ChatColor.YELLOW + sub.getSyntax() + ChatColor.GRAY + " - " + sub.getDescription());
                });
    }
}