package com.namdz.keoframer.command.sub;

import com.namdz.keoframer.KeoFramer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class WandCommand extends SubCommand {
    public WandCommand(KeoFramer plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "wand";
    }

    @Override
    public String getDescription() {
        return "Nhận đũa thần để chọn khu vực.";
    }

    @Override
    public String getSyntax() {
        return "/kf wand";
    }

    @Override
    public String getPermission() {
        return "keoframer.admin.wand";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "player-only-command", "&cLệnh này chỉ dành cho người chơi.");
            return;
        }
        Player player = (Player) sender;
        String wandItemId = plugin.getConfig().getString("wand-item", "BLAZE_ROD");
        Material wandMaterial = Material.matchMaterial(wandItemId);
        if (wandMaterial == null)
            wandMaterial = Material.BLAZE_ROD;

        ItemStack wand = new ItemStack(wandMaterial);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "KeoFramer Wand");
            meta.setLore(Arrays.asList(ChatColor.YELLOW + "Chuột trái: Chọn vị trí 1",
                    ChatColor.YELLOW + "Chuột phải: Chọn vị trí 2"));
            wand.setItemMeta(meta);
        }

        player.getInventory().addItem(wand);
        plugin.sendMessage(sender, "wand-received", "&aBạn đã nhận được đũa chọn khu vực!");
    }
}