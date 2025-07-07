package com.namdz.keoframer.command;

import com.namdz.keoframer.KeoFramer;
import com.namdz.keoframer.command.sub.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeoFramerCommand implements TabExecutor {

    private final KeoFramer plugin;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public KeoFramerCommand(KeoFramer plugin) {
        this.plugin = plugin;

        // Đăng ký tất cả các lệnh con tại đây
        registerSubCommand(new AddCandyCommand(plugin));
        registerSubCommand(new BoosterCommand(plugin));
        registerSubCommand(new CheckCandyCommand(plugin));
        registerSubCommand(new CreateCommand(plugin));
        registerSubCommand(new DebugCommand(plugin));
        registerSubCommand(new DeleteCommand(plugin));
        registerSubCommand(new EnchantCommand(plugin));
        registerSubCommand(new InfoCommand(plugin));
        registerSubCommand(new ListCommand(plugin));
        registerSubCommand(new PapiCommand(plugin));
        registerSubCommand(new Pos1Command(plugin));
        registerSubCommand(new Pos2Command(plugin));
        registerSubCommand(new ReloadCommand(plugin));
        registerSubCommand(new SetCommand(plugin));
        registerSubCommand(new UprangeCommand(plugin));
        registerSubCommand(new WandCommand(plugin));
        // Đăng ký lệnh help cuối cùng
        registerSubCommand(new HelpCommand(plugin, this.subCommands));
    }

    private void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("kfreload")) {
            subCommands.get("reload").perform(sender, new String[0]);
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            subCommands.get("help").perform(sender, new String[0]);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            plugin.sendMessage(sender, "unknown-command", "&cLệnh không xác định. Dùng /kf help.");
            return true;
        }

        if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
            plugin.sendMessage(sender, "no-permission", "&cBạn không có quyền sử dụng lệnh này.");
            return true;
        }

        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, args.length - 1);

        subCommand.perform(sender, subArgs);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> availableCommands = subCommands.values().stream()
                    .filter(sub -> sub.getPermission() == null || sender.hasPermission(sub.getPermission()))
                    .map(SubCommand::getName)
                    .collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[0], availableCommands, new ArrayList<>());
        }

        if (args.length > 1) {
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null
                    && (subCommand.getPermission() == null || sender.hasPermission(subCommand.getPermission()))) {
                String[] subArgs = new String[args.length - 1];
                System.arraycopy(args, 1, subArgs, 0, args.length - 1);
                return subCommand.onTabComplete(sender, subArgs);
            }
        }

        return Collections.emptyList();
    }
}