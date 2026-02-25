package kuku.plugin.function.chest;

import org.bukkit.plugin.java.JavaPlugin;
import kuku.plugin.function.FunctionPlugin;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 儲物箱管理器配置
 */
public class ChestConfig {

    private final JavaPlugin plugin;

    /**
     * 構造函數
     */
    public ChestConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 是否允許玩家潛行時強制開啟容器
     */
    public boolean isAllowOpenWhenSneaking() {
        return getConfig().getBoolean("chest.allowOpenWhenSneaking", false);
    }

    /**
     * 是否允許上方有方塊時開啟
     */
    public boolean isAllowOpenWithBlockAbove() {
        return getConfig().getBoolean("chest.allowOpenWithBlockAbove", true);
    }

    /**
     * 是否允許界伏盒有上方方塊時開啟
     */
    public boolean isAllowShulkerBoxes() {
        return getConfig().getBoolean("chest.allowShulkerBoxes", true);
    }

    /**
     * 是否處理雙儲物箱
     */
    public boolean isHandleDoubleChests() {
        return getConfig().getBoolean("chest.handleDoubleChests", true);
    }

    /**
     * 是否顯示調試信息
     */
    public boolean isShowDebugMessages() {
        return getConfig().getBoolean("chest.showDebugMessages", false) ||
                getConfig().getBoolean("settings.debug", false);
    }

    /**
     * 是否包含特殊容器（蜂箱、重生錨等）
     */
    public boolean isIncludeSpecialContainers() {
        return getConfig().getBoolean("chest.includeSpecialContainers", true);
    }

    /**
     * 是否包含工作台容器（熔爐、釀造台等）
     */
    public boolean isIncludeWorkstationContainers() {
        return getConfig().getBoolean("chest.includeWorkstationContainers", true);
    }

    /**
     * 獲取自定義儲物箱類型
     */
    public Set<String> getCustomChestTypes() {
        List<String> list = getConfig().getStringList("chest.customChestTypes");
        return new HashSet<>(list);
    }

    /**
     * 檢查是否啟用
     */
    public boolean isEnabled() {
        return getConfig().getBoolean("chest.enabled", true);
    }

    /**
     * 是否進行安全檢查
     */
    public boolean isEnableSafetyCheck() {
        return getConfig().getBoolean("chest.enableSafetyCheck", false);
    }

    /**
     * 重載配置（空實現）
     */
    public void reload() {
        // 配置由主插件統一重載
    }

    /**
     * 獲取配置實例
     */
    private org.bukkit.configuration.file.FileConfiguration getConfig() {
        if (plugin instanceof FunctionPlugin) {
            return ((FunctionPlugin) plugin).getPluginConfig();
        }
        return plugin.getConfig();
    }
}