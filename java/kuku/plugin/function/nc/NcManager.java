package kuku.plugin.function.nc;

import kuku.plugin.function.utils.LogUtils;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;

/**
 * 座標計算管理器
 */
public class NcManager {

    private final JavaPlugin plugin;
    private final Logger logger;
    private final NcConfig config;
    private boolean enabled = false;

    // 冷卻時間管理
    private final Map<String, Long> cooldowns = new HashMap<>();

    public NcManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.config = new NcConfig(plugin);
    }

    /**
     * 啟用座標計算功能
     */
    public void enable() {
        if (enabled) return;

        enabled = true;

        // 註冊指令
        NcCommand ncCommand = new NcCommand(this);
        plugin.getCommand("nc").setExecutor(ncCommand);

        LogUtils.logModuleEnable(logger, "座標計算");
        logModuleStatus();
    }

    /**
     * 禁用座標計算功能
     */
    public void disable() {
        if (!enabled) return;

        enabled = false;
        LogUtils.logModuleDisable(logger, "座標計算");

        // 清理冷卻時間記錄
        cooldowns.clear();
    }

    /**
     * 重載配置
     */
    public void reload() {
        config.reloadConfig();
        logger.info("§e座標計算配置已重載");
    }

    /**
     * 記錄模塊狀態
     */
    private void logModuleStatus() {
        if (!config.isEnabled()) {
            logger.info("§7注意: 模塊在配置中被禁用 (nc.yml)");
            return;
        }

        LogUtils.startModuleStatus(logger);
        LogUtils.logFeatureStatus(logger, "座標計算功能", config.isEnabled());
        LogUtils.logValueStatus(logger, "指令冷卻", config.getCooldown() + "秒");
        LogUtils.logValueStatus(logger, "座標精度", config.getShowPrecision() + "位小數");
        LogUtils.logFeatureStatus(logger, "顯示提示", config.showHints());
    }

    /**
     * 檢查玩家是否在冷卻中
     */
    public boolean isOnCooldown(String playerName) {
        if (!cooldowns.containsKey(playerName)) return false;

        long currentTime = System.currentTimeMillis();
        long lastUseTime = cooldowns.get(playerName);
        double cooldownSeconds = config.getCooldown();

        return (currentTime - lastUseTime) < (cooldownSeconds * 1000);
    }

    /**
     * 獲取剩餘冷卻時間
     */
    public double getRemainingCooldown(String playerName) {
        if (!cooldowns.containsKey(playerName)) return 0;

        long currentTime = System.currentTimeMillis();
        long lastUseTime = cooldowns.get(playerName);
        double cooldownSeconds = config.getCooldown();
        double elapsed = (currentTime - lastUseTime) / 1000.0;

        return Math.max(0, cooldownSeconds - elapsed);
    }

    /**
     * 設置冷卻時間
     */
    public void setCooldown(String playerName) {
        cooldowns.put(playerName, System.currentTimeMillis());
    }

    /**
     * 檢查是否啟用
     */
    public boolean isEnabled() {
        return enabled && config.isEnabled();
    }

    /**
     * 獲取配置管理器
     */
    public NcConfig getConfig() {
        return config;
    }

    /**
     * 獲取插件實例
     */
    public JavaPlugin getPlugin() {
        return plugin;
    }
}