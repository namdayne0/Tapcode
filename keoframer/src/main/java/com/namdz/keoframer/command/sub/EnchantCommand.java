package com.namdz.keoframer.command.sub;

import com.namdz.keoframer.KeoFramer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnchantCommand extends SubCommand {
    public EnchantCommand(KeoFramer plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "enchant";
    }

    @Override
    public String getDescription() {
        return "Phù phép các thuộc tính đặc biệt lên vật phẩm.";
    }

    @Override
    public String getSyntax() {
        return "/kf enchant <loại> <giá_trị...>";
    }

    @Override
    public String getPermission() {
        return "keoframer.admin.enchant";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "player-only-command", "&cLệnh này chỉ dành cho người chơi.");
            return;
        }
        if (args.length < 2) {
            sendHelpMessage(sender);
            return;
        }
        Player player = (Player) sender;
        String enchantType = args[0].toLowerCase();
        String[] enchantArgs = Arrays.copyOfRange(args, 1, args.length);
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            plugin.sendMessage(sender, "enchant-fail-hand", "&cBạn phải cầm một vật phẩm trên tay.");
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        switch (enchantType) {
            case "access":
                if (enchantArgs.length != 1) {
                    sendHelpMessage(sender);
                    return;
                }
                String areaName = enchantArgs[0];
                if (plugin.getAreaManager().getAreaByName(areaName) == null) {
                    plugin.sendMessage(sender, "area-not-found", "&cKhu vực '{area_name}' không tồn tại.",
                            "{area_name}", areaName);
                    return;
                }
                meta.getPersistentDataContainer().set(KeoFramer.AREA_ACCESS_KEY, PersistentDataType.STRING, areaName);
                lore.add(ChatColor.GRAY + "Quyền vào khu: " + ChatColor.YELLOW + areaName);
                break;
            case "cooldown":
                if (enchantArgs.length != 1) {
                    sendHelpMessage(sender);
                    return;
                }
                long reductionMillis = plugin.getAreaManager().parseDelay(enchantArgs[0]);
                if (reductionMillis <= 0) {
                    plugin.sendMessage(sender, "invalid-number", "&cThời gian giảm phải lớn hơn 0.");
                    return;
                }
                meta.getPersistentDataContainer().set(plugin.getBuffManager().COOLDOWN_REDUCTION_KEY,
                        PersistentDataType.LONG, reductionMillis);
                lore.add(ChatColor.BLUE + "Giảm thời gian chờ: " + ChatColor.AQUA + enchantArgs[0]);
                break;
            case "booster":
                if (enchantArgs.length != 2) {
                    sendHelpMessage(sender);
                    return;
                }
                String candyType = enchantArgs[0].toLowerCase();
                if (!plugin.getValidCandyTypes().contains(candyType)) {
                    plugin.sendMessage(sender, "invalid-candy-type", "&cLoại kẹo '{type}' không hợp lệ!", "{type}",
                            candyType);
                    return;
                }
                try {
                    double multiplier = Double.parseDouble(enchantArgs[1]);
                    NamespacedKey specificBoosterKey = new NamespacedKey(plugin, "kf_booster_" + candyType);
                    meta.getPersistentDataContainer().set(specificBoosterKey, PersistentDataType.DOUBLE, multiplier);
                    lore.add(ChatColor.GOLD + "Nhân " + plugin.getCandyDisplayName(candyType) + ": " + ChatColor.YELLOW
                            + "x" + multiplier);
                } catch (NumberFormatException e) {
                    plugin.sendMessage(sender, "invalid-number", "&cHệ số nhân phải là một con số (ví dụ: 1.5).");
                    return;
                }
                break;
            case "cumulative": // KHÔI PHỤC LỆNH
                if (enchantArgs.length < 2) {
                    sendHelpMessage(sender);
                    return;
                }
                try {
                    double bonus = Double.parseDouble(enchantArgs[0]);
                    String[] candyTypes = Arrays.copyOfRange(enchantArgs, 1, enchantArgs.length);
                    if (candyTypes.length == 0) {
                        sendHelpMessage(sender);
                        return;
                    }

                    List<String> affectedCandyNames = new ArrayList<>();
                    for (String cType : candyTypes) {
                        if (plugin.getValidCandyTypes().contains(cType.toLowerCase())) {
                            NamespacedKey key = new NamespacedKey(plugin, "kf_cumulative_" + cType.toLowerCase());
                            meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, bonus);
                            affectedCandyNames.add(plugin.getCandyDisplayName(cType));
                        }
                    }
                    if (!affectedCandyNames.isEmpty()) {
                        lore.add(ChatColor.GREEN + "Buff cộng dồn: " + ChatColor.YELLOW + "+" + bonus);
                        lore.add(ChatColor.GRAY + "  └ Áp dụng cho: " + String.join(", ", affectedCandyNames));
                    }
                } catch (NumberFormatException e) {
                    plugin.sendMessage(sender, "invalid-number", "&cHệ số bonus phải là một con số (ví dụ: 0.5).");
                    return;
                }
                break;
            default:
                sendHelpMessage(sender);
                return;
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        plugin.sendMessage(sender, "enchant-success", "&aĐã phù phép vật phẩm thành công!");
    }

    private void sendHelpMessage(CommandSender sender) {
        plugin.sendMessage(sender, "help.enchant", "&6Các loại enchant: access, cooldown, booster, cumulative");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("access", "cooldown", "booster", "cumulative"),
                    new ArrayList<>());
        }
        if (args.length >= 2) {
            String enchantType = args[0].toLowerCase();
            if (enchantType.equals("access")) {
                if (args.length == 2)
                    return StringUtil.copyPartialMatches(args[1], plugin.getAreaManager().getAreaNames(),
                            new ArrayList<>());
            } else if (enchantType.equals("booster")) {
                if (args.length == 2)
                    return StringUtil.copyPartialMatches(args[1], plugin.getValidCandyTypes(), new ArrayList<>());
            } else if (enchantType.equals("cumulative")) {
                if (args.length >= 2)
                    return StringUtil.copyPartialMatches(args[args.length - 1], plugin.getValidCandyTypes(),
                            new ArrayList<>());
            }
        }
        return super.onTabComplete(sender, args);
    }
}