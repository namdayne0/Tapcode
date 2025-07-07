package com.namdz.keoframer.command.sub;

import com.namdz.keoframer.KeoFramer;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetParticleCommand extends SubCommand {
    public SetParticleCommand(KeoFramer plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "setparticle";
    }

    @Override
    public String getDescription() {
        return "Cài đặt hiệu ứng hạt cho một khu vực.";
    }

    @Override
    public String getSyntax() {
        return "/kf setparticle <tên_kv> <loại|none> [style] [data]";
    }

    @Override
    public String getPermission() {
        return "keoframer.admin.setparticle";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.sendMessage(sender, "usage.setparticle", getSyntax());
            return;
        }
        String areaName = args[0];
        String[] particleValues = Arrays.copyOfRange(args, 1, args.length);

        try {
            if (plugin.getAreaManager().setParticle(areaName, particleValues)) {
                plugin.sendMessage(sender, "particle-updated", "&aĐã cập nhật particle cho khu vực '{area_name}'.",
                        "{area_name}", areaName);
            } else {
                plugin.sendMessage(sender, "area-not-found", "&cKhu vực '{area_name}' không tồn tại.", "{area_name}",
                        areaName);
            }
        } catch (Exception e) {
            plugin.sendMessage(sender, "invalid-particle-data",
                    "&cLỗi: Dữ liệu particle không hợp lệ (sai tên hoặc style).");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], plugin.getAreaManager().getAreaNames(), new ArrayList<>());
        }
        if (args.length == 2) {
            List<String> particleNames = new ArrayList<>();
            for (Particle p : Particle.values())
                particleNames.add(p.name());
            particleNames.add("none");
            return StringUtil.copyPartialMatches(args[1], particleNames, new ArrayList<>());
        }
        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], Arrays.asList("aura", "fountain", "explode"),
                    new ArrayList<>());
        }
        if (args.length == 4) {
            List<String> materialNames = new ArrayList<>();
            for (Material m : Material.values()) {
                if (m.isBlock() || m.isItem())
                    materialNames.add(m.name());
            }
            return StringUtil.copyPartialMatches(args[3], materialNames, new ArrayList<>());
        }
        return super.onTabComplete(sender, args);
    }
}