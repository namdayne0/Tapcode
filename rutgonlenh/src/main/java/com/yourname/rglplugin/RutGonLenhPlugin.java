package com.yourname.rglplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RutGonLenhPlugin extends JavaPlugin implements CommandExecutor, TabCompleter {

    private File customConfigFile;
    private FileConfiguration customConfig;
    private CommandMap commandMap;
    private final Map<String, CustomAliasCommand> registeredAliasCommands = new java.util.HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("RutGonLenhPlugin đã được bật!");

        // Khởi tạo và load commands.yml
        setupCustomConfig();

        // Lấy CommandMap thông qua Reflection
        try {
            Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (CommandMap) commandMapField.get(getServer().getPluginManager());
            getLogger().info("Đã truy cập CommandMap thành công.");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            getLogger().severe("Không thể truy cập CommandMap: " + e.getMessage());
            getLogger().severe("Plugin sẽ không hoạt động đúng cách nếu không có quyền truy cập vào CommandMap.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Tải các lệnh rút gọn
        loadCommands();

        // Đăng ký lệnh /rgl cho chính plugin này
        PluginCommand rglCommand = getCommand("rgl");
        if (rglCommand != null) {
            rglCommand.setExecutor(this);
            rglCommand.setTabCompleter(this);
            getLogger().info("Lệnh /rgl đã được đăng ký thành công.");
        } else {
            getLogger().warning("Lệnh /rgl không được định nghĩa trong plugin.yml. Hãy kiểm tra lại!");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("RutGonLenhPlugin đã được tắt!");
        // Hủy đăng ký tất cả các lệnh alias khi tắt plugin
        unregisterAllCustomCommands();
    }

    private void setupCustomConfig() {
        customConfigFile = new File(getDataFolder(), "commands.yml");
        if (!customConfigFile.exists()) {
            // Copy default commands.yml từ resources nếu chưa tồn tại
            saveResource("commands.yml", false);
            getLogger().info("Tạo file commands.yml mẫu.");
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
    }

    public void reloadCustomConfig() {
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
        getLogger().info("commands.yml đã được tải lại.");
        unregisterAllCustomCommands(); // Hủy đăng ký các lệnh cũ
        loadCommands(); // Tải lại các lệnh mới
    }

    private void loadCommands() {
        if (customConfig == null) {
            getLogger().warning("commands.yml chưa được tải! Không thể load commands.");
            return;
        }

        getLogger().info("Đang tải các lệnh rút gọn từ commands.yml...");
        Set<String> aliases = customConfig.getKeys(false); // Lấy tất cả các key cấp cao nhất

        if (aliases.isEmpty()) {
            getLogger().info("Không có lệnh rút gọn nào trong commands.yml.");
            return;
        }

        for (String alias : aliases) {
            ConfigurationSection section = customConfig.getConfigurationSection(alias);
            if (section == null) {
                getLogger().warning("Lỗi cấu hình cho alias: " + alias + ". Bỏ qua.");
                continue;
            }

            String originalCommand = section.getString("original-command");
            String usageType = section.getString("use");

            if (originalCommand == null || usageType == null) {
                getLogger().warning("Alias '" + alias + "' thiếu 'original-command' hoặc 'use'. Bỏ qua.");
                continue;
            }

            // Đăng ký lệnh tùy chỉnh
            registerCustomCommand(alias, originalCommand, usageType);
        }
        getLogger().info("Đã tải xong các lệnh rút gọn.");
    }

    private void registerCustomCommand(String alias, String originalCommand, String usageType) {
        // Tạo một đối tượng CustomAliasCommand mới và đăng ký nó
        CustomAliasCommand customCmd = new CustomAliasCommand(
                alias, // Tên lệnh alias
                "Lệnh rút gọn cho: " + originalCommand, // Mô tả
                "/" + alias, // Cú pháp sử dụng
                Arrays.asList(alias) // Alias cho lệnh rút gọn (có thể là chính nó)
        );
        customCmd.setOriginalCommand(originalCommand);
        customCmd.setUsageType(usageType);
        customCmd.setExecutor(new CustomCommandExecutor(originalCommand, usageType));
        customCmd.setTabCompleter(new CustomTabCompleter(originalCommand)); // Đăng ký Tab Completer

        // Đăng ký vào CommandMap. Tên plugin làm fallback prefix.
        commandMap.register(this.getDescription().getName(), customCmd);
        registeredAliasCommands.put(alias.toLowerCase(), customCmd); // Lưu để dễ dàng hủy đăng ký

        getLogger().info("Đã đăng ký lệnh rút gọn: /" + alias + " -> " + originalCommand);
    }

    private void unregisterAllCustomCommands() {
        for (Map.Entry<String, CustomAliasCommand> entry : registeredAliasCommands.entrySet()) {
            // Gỡ đăng ký lệnh khỏi CommandMap
            entry.getValue().unregister(commandMap);
            getLogger().info("Đã hủy đăng ký lệnh: " + entry.getKey());
        }
        registeredAliasCommands.clear(); // Xóa khỏi danh sách theo dõi
        // commandMap.clearCommands(); // Dòng này đã bị comment/bỏ đi để tránh lỗi mất
        // lệnh
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("rgl")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("rglplugin.admin")) { // Kiểm tra quyền
                    reloadCustomConfig();
                    sender.sendMessage("§aPlugin RutGonLenh đã được tải lại thành công.");
                    return true;
                } else {
                    sender.sendMessage("§cBạn không có quyền sử dụng lệnh này.");
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("rgl")) {
            if (args.length == 1) {
                return Arrays.asList("reload").stream()
                        .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return null;
    }

    // --- Các lớp trợ giúp ---

    /**
     * Lớp Command tùy chỉnh để đăng ký các lệnh rút gọn động.
     * Kế thừa từ org.bukkit.command.Command để có thể đăng ký vào CommandMap.
     */
    private static class CustomAliasCommand extends Command {
        private String originalCommand;
        private String usageType;
        private CommandExecutor executor;
        private TabCompleter tabCompleter; // Biến này là 'tabCompleter' với 'C' hoa

        protected CustomAliasCommand(String name, String description, String usageMessage, List<String> aliases) {
            super(name, description, usageMessage, aliases);
        }

        // Setter cho lệnh gốc và loại sử dụng
        public void setOriginalCommand(String originalCommand) {
            this.originalCommand = originalCommand;
        }

        public void setUsageType(String usageType) {
            this.usageType = usageType;
        }

        // Setter cho CommandExecutor
        public void setExecutor(CommandExecutor executor) {
            this.executor = executor;
        }

        // Setter cho TabCompleter
        public void setTabCompleter(TabCompleter tabCompleter) {
            this.tabCompleter = tabCompleter;
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            if (executor != null) {
                return executor.onCommand(sender, this, commandLabel, args);
            }
            return false;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args)
                throws IllegalArgumentException {
            if (tabCompleter != null) {
                return tabCompleter.onTabComplete(sender, this, alias, args); // Đã sửa từ 'tabcompleter' thành
                                                                              // 'tabCompleter'
            }
            return null;
        }
    }

    /**
     * Xử lý việc thực thi lệnh rút gọn khi người chơi gõ lệnh.
     */
    private class CustomCommandExecutor implements CommandExecutor {
        private final String originalCommand;
        private final String usageType;

        public CustomCommandExecutor(String originalCommand, String usageType) {
            this.originalCommand = originalCommand;
            this.usageType = usageType;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            String finalCommand = originalCommand;

            // Thay thế placeholder %player%
            if (sender instanceof Player) {
                finalCommand = finalCommand.replace("%player%", ((Player) sender).getName());
            } else {
                // Nếu lệnh được gửi từ console và có %player%, sẽ thay bằng "console"
                finalCommand = finalCommand.replace("%player%", "console");
            }

            // Nối các đối số từ lệnh rút gọn vào cuối lệnh gốc
            StringBuilder argsBuilder = new StringBuilder();
            if (args.length > 0) {
                for (String arg : args) {
                    argsBuilder.append(" ").append(arg);
                }
                finalCommand += argsBuilder.toString();
            }

            // Thực thi lệnh tùy theo loại sử dụng
            if (usageType.equalsIgnoreCase("console")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            } else if (usageType.equalsIgnoreCase("player")) {
                if (sender instanceof Player) {
                    Bukkit.dispatchCommand(sender, finalCommand);
                } else {
                    sender.sendMessage("§cLệnh này chỉ có thể được thực hiện bởi người chơi.");
                }
            } else {
                sender.sendMessage("§cLỗi cấu hình: Loại sử dụng '" + usageType
                        + "' không hợp lệ. Phải là 'console' hoặc 'player'.");
            }
            return true;
        }
    }

    /**
     * Xử lý Tab Completion cho lệnh rút gọn.
     * Cố gắng gọi Tab Completer của lệnh gốc nếu có.
     */
    private class CustomTabCompleter implements TabCompleter {
        private final String originalCommandBase; // Phần tên lệnh gốc (ví dụ: "tp" từ "tp %player%")
        private final String originalCommandFullPath; // Lưu toàn bộ chuỗi lệnh gốc (ví dụ: "tp %player%")

        public CustomTabCompleter(String originalCommand) {
            this.originalCommandFullPath = originalCommand;
            this.originalCommandBase = originalCommand.split(" ")[0]; // Lấy phần đầu tiên làm tên lệnh gốc
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            // Lấy lệnh gốc từ CommandMap của Bukkit
            Command originalCmd = commandMap.getCommand(originalCommandBase);

            if (originalCmd == null) {
                return new ArrayList<>(); // Lệnh gốc không tồn tại
            }

            // Xây dựng effectiveArgsForOriginalCommand: kết hợp các đối số cố định của lệnh
            // gốc
            // (nếu có) với các đối số mà người dùng đã nhập.
            List<String> combinedArgs = new ArrayList<>();
            String[] fullOriginalCmdSplit = originalCommandFullPath.split(" ");

            // Thêm các đối số cố định của lệnh gốc (nếu có sau tên lệnh cơ sở)
            if (fullOriginalCmdSplit.length > 1) {
                combinedArgs.addAll(Arrays.asList(fullOriginalCmdSplit).subList(1, fullOriginalCmdSplit.length));
            }
            // Thêm các đối số từ lệnh alias mà người dùng đang gõ
            combinedArgs.addAll(Arrays.asList(args));

            // Chuyển List về mảng String để truyền cho Tab Completer của lệnh gốc
            String[] effectiveArgsForOriginalCommand = combinedArgs.toArray(new String[0]);

            // Gọi tab complete của lệnh gốc
            if (originalCmd instanceof PluginCommand) {
                PluginCommand pluginOriginalCmd = (PluginCommand) originalCmd;
                if (pluginOriginalCmd.getTabCompleter() != null) {
                    return pluginOriginalCmd.getTabCompleter().onTabComplete(sender, pluginOriginalCmd,
                            originalCommandBase, effectiveArgsForOriginalCommand);
                }
            }
            // Fallback nếu không có TabCompleter cụ thể hoặc nếu nó không phải
            // PluginCommand
            try {
                // Thử gọi tabComplete mặc định của Command
                return originalCmd.tabComplete(sender, originalCommandBase, effectiveArgsForOriginalCommand);
            } catch (Exception e) {
                // Lỗi khi gọi tabComplete mặc định (có thể do phiên bản API không hỗ trợ)
                getLogger().warning("Lỗi khi cố gắng gọi TabCompleter mặc định cho lệnh " + originalCommandBase + ": "
                        + e.getMessage());
            }

            // Cuối cùng, nếu mọi thứ thất bại, thử gợi ý tên người chơi nếu có vẻ phù hợp
            if (effectiveArgsForOriginalCommand.length > 0) {
                String currentArg = effectiveArgsForOriginalCommand[effectiveArgsForOriginalCommand.length - 1];
                if (currentArg.isEmpty() || originalCommandFullPath.contains("%player%")) {
                    List<String> playerNames = new ArrayList<>();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getName().toLowerCase().startsWith(currentArg.toLowerCase())) {
                            playerNames.add(player.getName());
                        }
                    }
                    return playerNames;
                }
            }

            return new ArrayList<>(); // Trả về danh sách rỗng nếu không có gợi ý nào
        }
    }
}