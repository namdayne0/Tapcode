package com.namdz.keomenu;

import com.namdz.keomenu.command.KeoMenuCommand;
import com.namdz.keomenu.hook.KeoFramerHook;
import com.namdz.keomenu.hook.PlaceholderAPIHook;
import com.namdz.keomenu.hook.VaultHook;
import com.namdz.keomenu.listener.MenuClickListener; // Đảm bảo là 'listener', không phải 'listeners'
import com.namdz.keomenu.menu.MenuManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class KeoMenu extends JavaPlugin {

    private MenuManager menuManager;
    private PlaceholderAPIHook placeholderAPIHook;
    private VaultHook vaultHook;
    private KeoFramerHook keoFramerHook;
    private int menuUpdateTaskId = -1; // Thêm dòng này

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.placeholderAPIHook = new PlaceholderAPIHook(this);
        this.vaultHook = new VaultHook(this);
        this.keoFramerHook = new KeoFramerHook(this);

        this.menuManager = new MenuManager(this);
        this.menuManager.loadAllMenus();

        // Đảm bảo import và khởi tạo đúng
        getServer().getPluginManager().registerEvents(new MenuClickListener(this), this);
        getCommand("keomenu").setExecutor(new KeoMenuCommand(this));
        getCommand("kmen").setExecutor(new KeoMenuCommand(this));

        // Bắt đầu tác vụ cập nhật menu
        this.menuManager.startMenuUpdateTask(); // Gọi tác vụ từ MenuManager

        if (placeholderAPIHook.isEnabled()) {
            getLogger().info("Tích hợp PlaceholderAPI thành công.");
        } else {
            getLogger().warning("Không tìm thấy PlaceholderAPI. Một số tính năng sẽ bị giới hạn.");
        }
        if (vaultHook.isEnabled()) {
            getLogger().info("Tích hợp Vault thành công.");
        } else {
            getLogger().warning("Không tìm thấy Vault. Các tính năng kinh tế sẽ không hoạt động.");
        }
        if (keoFramerHook.isEnabled()) {
            getLogger().info("Tích hợp KeoFramer thành công.");
        } else {
            getLogger().warning("Không tìm thấy KeoFramer. Các tính năng liên quan đến kẹo sẽ không hoạt hoạt động.");
        }

        getLogger().info("KeoMenu đã được bật!");
    }

    @Override
    public void onDisable() {
        // Hủy tác vụ cập nhật menu khi tắt plugin
        if (menuUpdateTaskId != -1) {
            getServer().getScheduler().cancelTask(menuUpdateTaskId);
        }
        getLogger().info("KeoMenu đã được tắt!");
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public PlaceholderAPIHook getPlaceholderAPIHook() {
        return placeholderAPIHook;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public KeoFramerHook getKeoFramerHook() {
        return keoFramerHook;
    }

    public int getMenuUpdateTaskId() { // Getter cho menuUpdateTaskId
        return menuUpdateTaskId;
    }

    public void setMenuUpdateTaskId(int menuUpdateTaskId) { // Setter cho menuUpdateTaskId
        this.menuUpdateTaskId = menuUpdateTaskId;
    }
}