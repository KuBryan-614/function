package kuku.plugin.function.easybox;

import org.bukkit.configuration.file.FileConfiguration;
import kuku.plugin.function.FunctionPlugin;

public class EasyBoxConfig {

    private final FunctionPlugin plugin;
    private FileConfiguration config;

    public EasyBoxConfig(FunctionPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        config = plugin.getConfig();
    }

    // 檢查模塊是否啟用
    public boolean isModuleEnabled() {
        return config.getBoolean("modules.easybox", true);
    }

    // 檢查功能是否啟用
    public boolean isEnabled() {
        return config.getBoolean("easybox.enabled", true);
    }

    // Lore相關設置
    public boolean isLoreEnabled() {
        return config.getBoolean("easybox.lore-enabled", true);
    }

    public String getLoreTitle() {
        return config.getString("easybox.lore-title", "&7[ &e內容 &7] &8(%total% 個物品)");
    }

    public String getLoreFormat() {
        return config.getString("easybox.lore-format", " &7• %item% &8x%amount%");
    }

    public int getMaxLoreLines() {
        return config.getInt("easybox.max-lore-lines", 6);
    }

    public String getEmptyMessage() {
        return config.getString("easybox.empty-message", " &7• 空的");
    }

    // 界面設置
    public String getInventoryTitle() {
        return config.getString("easybox.inventory-title", "&8[&6界伏盒&8]");
    }
}