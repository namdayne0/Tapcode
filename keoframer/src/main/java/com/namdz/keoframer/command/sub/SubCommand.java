// Đặt file này tại: src/main/java/com/namdz/keoframer/command/sub/SubCommand.java

package com.namdz.keoframer.command.sub;

import com.namdz.keoframer.KeoFramer;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public abstract class SubCommand {

    protected final KeoFramer plugin;

    public SubCommand(KeoFramer plugin) {
        this.plugin = plugin;
    }

    /**
     * Tên của lệnh con (ví dụ: "create", "list").
     */
    public abstract String getName();

    /**
     * Mô tả ngắn gọn về chức năng của lệnh.
     */
    public abstract String getDescription();

    /**
     * Cú pháp sử dụng lệnh.
     */
    public abstract String getSyntax();

    /**
     * Quyền hạn cần thiết để sử dụng lệnh này.
     * Trả về null nếu không cần quyền.
     */
    public abstract String getPermission();

    /**
     * Logic chính khi lệnh được thực thi.
     */
    public abstract void perform(CommandSender sender, String[] args);

    /**
     * Logic gợi ý lệnh (tab-complete), không bắt buộc.
     */
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
