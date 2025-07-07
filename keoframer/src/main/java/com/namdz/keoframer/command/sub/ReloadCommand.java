package com.namdz.keoframer.command.sub;

import com.namdz.keoframer.KeoFramer;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubCommand {
    public ReloadCommand(KeoFramer plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Tải lại các file cấu hình của plugin.";
    }

    @Override
    public String getSyntax() {
        return "/kf reload";
    }

    @Override
    public String getPermission() {
        return "keoframer.admin.reload";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        plugin.reloadPlugin();
        plugin.sendMessage(sender, "plugin-reloaded", "&aĐã tải lại cấu hình và dữ liệu!");
    }
}