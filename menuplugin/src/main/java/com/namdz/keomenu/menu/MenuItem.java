package com.namdz.keomenu.menu;

import com.namdz.keomenu.KeoMenu;
import com.namdz.keomenu.util.ChatColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// MMOItems & MythicLib Imports
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.player.PlayerData;
import io.lumine.mythic.lib.api.item.NBTItem; // Quan trọng cho NBT tags của MMOItems

public class MenuItem {

    private final KeoMenu plugin;
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final int slot;
    private final List<String> actions;
    private final ConfigurationSection requirementsSection;
    private final int updateInterval; // Thêm updateInterval cho từng item

    public MenuItem(KeoMenu plugin, ConfigurationSection section) {
        this.plugin = plugin;
        this.material = Material.valueOf(section.getString("material", "STONE").toUpperCase());
        this.name = ChatColorUtil.translateColors(section.getString("name", "Default Item"));
        this.lore = section.getStringList("lore").stream()
                .map(ChatColorUtil::translateColors)
                .collect(Collectors.toList());
        this.slot = section.getInt("slot");
        this.actions = section.getStringList("actions");
        this.requirementsSection = section.getConfigurationSection("requirements");
        this.updateInterval = section.getInt("update_interval", 0); // Đọc update_interval riêng cho từng item
    }

    public int getSlot() {
        return slot;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public ItemStack build(Player player) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(plugin.getPlaceholderAPIHook().setPlaceholders(player, name));
            List<String> processedLore = lore.stream()
                    .map(line -> plugin.getPlaceholderAPIHook().setPlaceholders(player, line))
                    .collect(Collectors.toList());
            meta.setLore(processedLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean checkRequirements(Player player) {
        if (requirementsSection == null) {
            return true;
        }

        // Kiểm tra yêu cầu tiền (Vault)
        if (requirementsSection.contains("money")) {
            double requiredMoney = requirementsSection.getDouble("money.amount");
            String message = ChatColorUtil
                    .translateColors(requirementsSection.getString("money.message", "&cBạn không đủ tiền!"));

            if (!plugin.getVaultHook().has(player, requiredMoney)) {
                player.sendMessage(message);
                return false;
            }
        }

        // Kiểm tra yêu cầu kẹo (KeoFramer API)
        if (requirementsSection.contains("candy") && plugin.getKeoFramerHook() != null
                && plugin.getKeoFramerHook().isEnabled()) {
            ConfigurationSection candyReqs = requirementsSection.getConfigurationSection("candy");
            if (candyReqs != null) {
                for (String candyType : candyReqs.getKeys(false)) {
                    ConfigurationSection candyItemReq = candyReqs.getConfigurationSection(candyType);
                    if (candyItemReq != null) {
                        int requiredAmount = candyItemReq.getInt("amount");
                        String message = ChatColorUtil.translateColors(
                                candyItemReq.getString("message", "&cBạn không đủ kẹo " + candyType + "!"));

                        int playerCandy = plugin.getKeoFramerHook().getCandy(player, candyType);
                        if (playerCandy < requiredAmount) {
                            player.sendMessage(message);
                            return false;
                        }
                    }
                }
            }
        }

        // Kiểm tra yêu cầu MMOItems
        if (requirementsSection.contains("mmoitems")) {
            ConfigurationSection mmoItemsReqs = requirementsSection.getConfigurationSection("mmoitems");
            if (mmoItemsReqs != null) {
                // ĐẢM BẢO MMOITEMS ĐÃ ĐƯỢC TẢI
                if (!Bukkit.getPluginManager().isPluginEnabled("MMOItems") || MMOItems.plugin == null) {
                    plugin.getLogger().warning("MMOItems API không có sẵn nhưng yêu cầu MMOItems đã được sử dụng tại "
                            + requirementsSection.getCurrentPath() + ". Bỏ qua yêu cầu.");
                    player.sendMessage("§cLỗi: Hệ thống MMOItems không hoạt động hoặc chưa sẵn sàng.");
                    return false;
                }

                for (String itemIdKey : mmoItemsReqs.getKeys(false)) {
                    String[] parts = itemIdKey.split("\\.");
                    if (parts.length != 2) {
                        plugin.getLogger().warning("Định dạng MMOItem ID không hợp lệ trong yêu cầu: " + itemIdKey
                                + " tại " + mmoItemsReqs.getCurrentPath());
                        player.sendMessage("§cLỗi cấu hình MMOItem: ID không hợp lệ. Vui lòng kiểm tra console.");
                        return false;
                    }
                    final String itemType = parts[0];
                    final String itemId = parts[1];

                    ConfigurationSection itemReq = mmoItemsReqs.getConfigurationSection(itemIdKey);
                    if (itemReq != null) {
                        int requiredAmount = itemReq.getInt("amount");
                        String message = ChatColorUtil.translateColors(itemReq.getString("message",
                                "&cBạn không đủ " + requiredAmount + " " + itemType + " " + itemId + "!"));

                        // Kiểm tra xem Type và MMOItem có tồn tại trong cấu hình MMOItems không
                        Type mmoItemType = Type.get(itemType);
                        if (mmoItemType == null) {
                            plugin.getLogger()
                                    .warning("MMOItem Type không tìm thấy trong cấu hình MMOItems: " + itemType
                                            + " cho ID " + itemId + " tại " + mmoItemsReqs.getCurrentPath()
                                            + ". Vui lòng kiểm tra lại MMOItems.");
                            player.sendMessage("§cLỗi: Loại item tùy chỉnh không tồn tại. Vui lòng báo quản trị viên.");
                            return false;
                        }
                        MMOItem mmoItemDefinition = MMOItems.plugin.getMMOItem(mmoItemType, itemId);
                        if (mmoItemDefinition == null) {
                            plugin.getLogger()
                                    .warning("MMOItem không tìm thấy trong cấu hình MMOItems: Type=" + itemType
                                            + ", ID=" + itemId + " tại " + mmoItemsReqs.getCurrentPath()
                                            + ". Vui lòng kiểm tra lại MMOItems.");
                            player.sendMessage(
                                    "§cLỗi: Item tùy chỉnh không tồn tại hoặc ID sai. Vui lòng báo quản trị viên.");
                            return false;
                        }

                        // Đếm số lượng MMOItem trong kho đồ của người chơi
                        int playerAmount = 0;
                        for (ItemStack itemStack : player.getInventory().getContents()) {
                            if (itemStack != null && itemStack.hasItemMeta()) {
                                NBTItem nbtItem = NBTItem.get(itemStack);

                                // Kiểm tra các tag MMOITEMS_ITEM_TYPE và MMOITEMS_ITEM_ID
                                if (nbtItem.hasTag("MMOITEMS_ITEM_TYPE") && nbtItem.hasTag("MMOITEMS_ITEM_ID")) {
                                    if (nbtItem.getString("MMOITEMS_ITEM_TYPE").equals(itemType) &&
                                            nbtItem.getString("MMOITEMS_ITEM_ID").equals(itemId)) {
                                        playerAmount += itemStack.getAmount();
                                    }
                                }
                            }
                        }

                        if (playerAmount < requiredAmount) {
                            player.sendMessage(message);
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public void executeActions(Player player) {
        boolean allActionsSuccessful = true; // Biến cờ để theo dõi thành công của các hành động

        for (String action : actions) {
            action = action.trim(); // Đảm bảo không có khoảng trắng thừa

            if (action.startsWith("[open] ")) {
                String menuId = action.substring("[open] ".length()).trim();
                plugin.getMenuManager().openMenu(player, menuId);
            } else if (action.equals("[close]")) {
                player.closeInventory();
            } else if (action.startsWith("[message] ")) {
                String message = action.substring("[message] ".length()).trim();
                player.sendMessage(
                        plugin.getPlaceholderAPIHook().setPlaceholders(player, ChatColorUtil.translateColors(message)));
            } else if (action.startsWith("[command] ")) {
                String command = action.substring("[command] ".length()).trim();
                Bukkit.dispatchCommand(player, plugin.getPlaceholderAPIHook().setPlaceholders(player, command));
            } else if (action.startsWith("[console_command] ")) {
                String command = action.substring("[console_command] ".length()).trim();
                // Đối với console_command, PlaceholderAPI cần được xử lý cẩn thận nếu player là
                // null
                // Tuy nhiên, ở đây player luôn có, nên có thể dùng player để xử lý placeholder
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        plugin.getPlaceholderAPIHook().setPlaceholders(player, command));
            } else if (action.startsWith("[add_money] ")) {
                try {
                    double amount = Double.parseDouble(action.substring("[add_money] ".length()).trim());
                    if (plugin.getVaultHook().isEnabled()) {
                        plugin.getVaultHook().depositPlayer(player, amount);
                    } else {
                        plugin.getLogger().warning("Vault không được bật nhưng hành động add_money được sử dụng.");
                    }
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Số tiền không hợp lệ trong hành động add_money: " + action);
                }
            } else if (action.startsWith("[take_money] ")) {
                try {
                    double amount = Double.parseDouble(action.substring("[take_money] ".length()).trim());
                    if (plugin.getVaultHook().isEnabled()) {
                        if (plugin.getVaultHook().has(player, amount)) { // Kiểm tra lần nữa trước khi trừ
                            plugin.getVaultHook().withdrawPlayer(player, amount);
                        } else {
                            // Điều này không nên xảy ra nếu requirements đã được kiểm tra, nhưng là một lớp
                            // bảo vệ.
                            player.sendMessage("§cBạn không đủ tiền để thực hiện hành động này.");
                            allActionsSuccessful = false;
                        }
                    } else {
                        plugin.getLogger().warning("Vault không được bật nhưng hành động take_money được sử dụng.");
                    }
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Số tiền không hợp lệ trong hành động take_money: " + action);
                }
            } else if (action.startsWith("[add_candy] ") && plugin.getKeoFramerHook() != null
                    && plugin.getKeoFramerHook().isEnabled()) {
                String[] parts = action.substring("[add_candy] ".length()).trim().split(" ");
                if (parts.length == 2) {
                    String candyType = parts[0];
                    try {
                        int amount = Integer.parseInt(parts[1]);
                        plugin.getKeoFramerHook().addCandy(player, candyType, amount);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Số lượng kẹo không hợp lệ trong hành động add_candy: " + action);
                    }
                } else {
                    plugin.getLogger().warning("Định dạng lệnh add_candy không hợp lệ: " + action);
                }
            } else if (action.startsWith("[take_candy] ") && plugin.getKeoFramerHook() != null
                    && plugin.getKeoFramerHook().isEnabled()) {
                String[] parts = action.substring("[take_candy] ".length()).trim().split(" ");
                if (parts.length == 2) {
                    String candyType = parts[0];
                    try {
                        int amount = Integer.parseInt(parts[1]);
                        // Kiểm tra lần nữa trước khi trừ
                        if (plugin.getKeoFramerHook().getCandy(player, candyType) >= amount) {
                            plugin.getKeoFramerHook().takeCandy(player, candyType, amount);
                        } else {
                            // Điều này không nên xảy ra nếu requirements đã được kiểm tra
                            player.sendMessage("§cBạn không đủ kẹo để thực hiện hành động này.");
                            allActionsSuccessful = false;
                        }
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Số lượng kẹo không hợp lệ trong hành động take_candy: " + action);
                    }
                } else {
                    plugin.getLogger().warning("Định dạng lệnh take_candy không hợp lệ: " + action);
                }
            } else if (action.startsWith("[give_mmoitem] ")) {
                String[] parts = action.substring("[give_mmoitem] ".length()).trim().split(" ");
                if (parts.length == 3) {
                    final String itemType = parts[0];
                    final String itemId = parts[1];
                    try {
                        final int amount = Integer.parseInt(parts[2]);
                        giveMMOItem(player, itemType, itemId, amount);
                    } catch (NumberFormatException e) {
                        plugin.getLogger()
                                .warning("Số lượng MMOItem không hợp lệ trong hành động give_mmoitem: " + action);
                        player.sendMessage("§cLỗi: Số lượng item cấp không hợp lệ.");
                    }
                } else {
                    plugin.getLogger().warning("Định dạng lệnh give_mmoitem không hợp lệ: " + action);
                    player.sendMessage("§cLỗi: Định dạng lệnh cấp item không hợp lệ.");
                }
            } else if (action.startsWith("[take_mmoitem] ")) {
                String[] parts = action.substring("[take_mmoitem] ".length()).trim().split(" ");
                if (parts.length == 3) {
                    final String itemType = parts[0];
                    final String itemId = parts[1];
                    try {
                        final int amount = Integer.parseInt(parts[2]);
                        if (!takeMMOItem(player, itemType, itemId, amount)) {
                            allActionsSuccessful = false; // Đánh dấu thất bại nếu không trừ đủ
                        }
                    } catch (NumberFormatException e) {
                        plugin.getLogger()
                                .warning("Số lượng MMOItem không hợp lệ trong hành động take_mmoitem: " + action);
                        player.sendMessage("§cLỗi: Số lượng item trừ không hợp lệ.");
                    }
                } else {
                    plugin.getLogger().warning("Định dạng lệnh take_mmoitem không hợp lệ: " + action);
                    player.sendMessage("§cLỗi: Định dạng lệnh trừ item không hợp lệ.");
                }
            } else if (action.startsWith("[trade_mmoitem] ")) {
                String tradeContent = action.substring("[trade_mmoitem] ".length()).trim();
                String[] individualCommands = tradeContent.split(";");

                boolean currentTradeSuccess = true;
                List<Runnable> giveActionsQueue = new ArrayList<>(); // Hàng đợi các hành động give

                // Bước 1: Thực hiện tất cả các lệnh take và thu thập các lệnh give
                for (String cmd : individualCommands) {
                    cmd = cmd.trim();
                    if (cmd.startsWith("take_mmoitem ")) {
                        String[] parts = cmd.substring("take_mmoitem ".length()).trim().split(" ");
                        if (parts.length == 3) {
                            final String takeItemType = parts[0];
                            final String takeItemId = parts[1];
                            try {
                                final int takeAmount = Integer.parseInt(parts[2]);
                                // Thực hiện trừ item. Nếu thất bại, dừng ngay và đánh dấu không thành công
                                if (!takeMMOItem(player, takeItemType, takeItemId, takeAmount)) {
                                    currentTradeSuccess = false;
                                    break; // Thoát vòng lặp con
                                }
                            } catch (NumberFormatException e) {
                                plugin.getLogger().warning(
                                        "Số lượng MMOItem không hợp lệ trong lệnh take_mmoitem của trade: " + cmd);
                                player.sendMessage("§cLỗi: Số lượng item trao đổi không hợp lệ.");
                                currentTradeSuccess = false;
                                break;
                            }
                        } else {
                            plugin.getLogger().warning("Định dạng lệnh take_mmoitem không hợp lệ trong trade: " + cmd);
                            player.sendMessage("§cLỗi: Định dạng lệnh trừ item trong giao dịch không hợp lệ.");
                            currentTradeSuccess = false;
                            break;
                        }
                    } else if (cmd.startsWith("give_mmoitem ")) {
                        // Thêm vào hàng đợi để thực hiện sau
                        final String giveCmd = cmd; // Make effectively final for lambda
                        giveActionsQueue.add(() -> {
                            String[] giveParts = giveCmd.substring("give_mmoitem ".length()).trim().split(" ");
                            if (giveParts.length == 3) {
                                final String giveItemType = giveParts[0];
                                final String giveItemId = giveParts[1];
                                try {
                                    final int giveAmount = Integer.parseInt(giveParts[2]);
                                    giveMMOItem(player, giveItemType, giveItemId, giveAmount);
                                } catch (NumberFormatException e) {
                                    plugin.getLogger()
                                            .warning("Số lượng MMOItem không hợp lệ trong lệnh give_mmoitem của trade: "
                                                    + giveCmd);
                                    player.sendMessage("§cLỗi: Số lượng item cấp trong giao dịch không hợp lệ.");
                                }
                            } else {
                                plugin.getLogger()
                                        .warning("Định dạng lệnh give_mmoitem không hợp lệ trong trade: " + giveCmd);
                                player.sendMessage("§cLỗi: Định dạng lệnh cấp item trong giao dịch không hợp lệ.");
                            }
                        });
                    } else {
                        plugin.getLogger().warning(
                                "Lệnh con không hợp lệ trong [trade_mmoitem]: " + cmd + " trong hành động: " + action);
                        player.sendMessage("§cLỗi: Lệnh giao dịch không hợp lệ.");
                        currentTradeSuccess = false;
                        break;
                    }
                }

                // Bước 2: Nếu tất cả các lệnh take thành công, thực hiện các lệnh give
                if (currentTradeSuccess) {
                    giveActionsQueue.forEach(Runnable::run);
                } else {
                    allActionsSuccessful = false; // Đánh dấu rằng chuỗi hành động cha không thành công
                }
            } else {
                plugin.getLogger().warning("Hành động không xác định: " + action);
            }
        }
        // Có thể thêm logic ở đây nếu allActionsSuccessful là false và bạn muốn xử lý
        // đặc biệt.
    }

    // ====================================================================================================
    // HÀM HỖ TRỢ MMOITEMS
    // ====================================================================================================

    /**
     * Trừ một lượng MMOItem cụ thể từ kho đồ của người chơi.
     *
     * @param player   Người chơi.
     * @param itemType Loại MMOItem (ví dụ: SWORD, MATERIAL).
     * @param itemId   ID của MMOItem (ví dụ: LONG_SWORD).
     * @param amount   Số lượng cần trừ.
     * @return true nếu trừ thành công đủ số lượng, false nếu không đủ hoặc có lỗi.
     */
    private boolean takeMMOItem(Player player, String itemType, String itemId, int amount) {
        if (!Bukkit.getPluginManager().isPluginEnabled("MMOItems") || MMOItems.plugin == null) {
            plugin.getLogger().warning("MMOItems API không có sẵn khi trừ item: " + itemType + "." + itemId);
            player.sendMessage("§cLỗi: Hệ thống MMOItems không hoạt động.");
            return false;
        }

        Type mmoItemType = Type.get(itemType);
        if (mmoItemType == null) {
            plugin.getLogger().warning("MMOItem Type không tìm thấy để trừ: " + itemType + " cho ID " + itemId
                    + ". Vui lòng kiểm tra lại cấu hình MMOItems.");
            player.sendMessage("§cLỗi: Loại item tùy chỉnh không tồn tại.");
            return false;
        }
        MMOItem mmoItemDefinition = MMOItems.plugin.getMMOItem(mmoItemType, itemId);
        if (mmoItemDefinition == null) {
            plugin.getLogger().warning("MMOItem không tìm thấy để trừ: Type=" + itemType + ", ID=" + itemId
                    + ". Vui lòng kiểm tra lại cấu hình MMOItems.");
            player.sendMessage("§cLỗi: Item tùy chỉnh không tồn tại hoặc ID sai.");
            return false;
        }

        int removedCount = 0;
        // Duyệt ngược để tránh IndexOutOfBoundsException khi xóa item và đảm bảo duyệt
        // hết kho đồ
        for (int i = player.getInventory().getSize() - 1; i >= 0 && removedCount < amount; i--) {
            ItemStack currentItem = player.getInventory().getItem(i);
            if (currentItem != null && currentItem.hasItemMeta()) {
                NBTItem nbtItem = NBTItem.get(currentItem);

                if (nbtItem.hasTag("MMOITEMS_ITEM_TYPE") && nbtItem.hasTag("MMOITEMS_ITEM_ID")) {
                    if (nbtItem.getString("MMOITEMS_ITEM_TYPE").equals(itemType) &&
                            nbtItem.getString("MMOITEMS_ITEM_ID").equals(itemId)) {

                        int take = Math.min(currentItem.getAmount(), amount - removedCount);
                        currentItem.setAmount(currentItem.getAmount() - take);
                        // Cập nhật lại slot trong inventory. Nếu amount về 0, nó sẽ tự động xóa
                        // itemStack
                        player.getInventory().setItem(i, currentItem);
                        removedCount += take;
                    }
                }
            }
        }

        // Kiểm tra xem đã trừ đủ số lượng yêu cầu chưa
        if (removedCount < amount) {
            player.sendMessage(
                    "§cBạn không đủ " + amount + " " + itemType + "." + itemId + " để thực hiện hành động này.");
            return false;
        }
        return true;
    }

    /**
     * Cấp một lượng MMOItem cụ thể cho người chơi.
     *
     * @param player   Người chơi.
     * @param itemType Loại MMOItem (ví dụ: SWORD, MATERIAL).
     * @param itemId   ID của MMOItem (ví dụ: LONG_SWORD).
     * @param amount   Số lượng cần cấp.
     */
    private void giveMMOItem(Player player, String itemType, String itemId, int amount) {
        if (!Bukkit.getPluginManager().isPluginEnabled("MMOItems") || MMOItems.plugin == null) {
            plugin.getLogger().warning("MMOItems API không có sẵn khi cấp item: " + itemType + "." + itemId);
            player.sendMessage("§cLỗi: Hệ thống MMOItems không hoạt động.");
            return;
        }

        Type mmoItemType = Type.get(itemType);
        if (mmoItemType == null) {
            plugin.getLogger().warning("MMOItem Type không tìm thấy để cấp: " + itemType + " cho ID " + itemId
                    + ". Vui lòng kiểm tra lại cấu hình MMOItems.");
            player.sendMessage("§cLỗi: Loại item tùy chỉnh không tồn tại.");
            return;
        }
        MMOItem mmoItemDefinition = MMOItems.plugin.getMMOItem(mmoItemType, itemId);
        if (mmoItemDefinition != null) {
            // Sử dụng itemStack của MMOItems, không tạo ItemStack mới
            for (int i = 0; i < amount; i++) {
                player.getInventory().addItem(mmoItemDefinition.newBuilder().build());
            }
        } else {
            plugin.getLogger().warning("MMOItem không tìm thấy để cấp: Type=" + itemType + ", ID=" + itemId
                    + ". Vui lòng kiểm tra lại cấu hình MMOItems.");
            player.sendMessage("§cLỗi: Item tùy chỉnh không tồn tại hoặc ID sai.");
        }
    }
}