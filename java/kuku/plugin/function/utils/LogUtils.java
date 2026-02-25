package kuku.plugin.function.utils;

import org.bukkit.ChatColor;
import java.util.logging.Logger;

/**
 * 日誌工具類 - 統一格式化插件日誌輸出
 */
public class LogUtils {

    private LogUtils() {
        // 工具類，不需要實例化
    }

    /**
     * 記錄模塊加載狀態
     */
    public static void logModuleLoad(Logger logger, String moduleName, boolean loaded, String configType) {
        if (loaded) {
            logger.info("§a✓ " + moduleName + " 模塊已加載 (" + configType + ")");
        } else {
            logger.info("§7- " + moduleName + " 模塊已跳過 (配置禁用)");
        }
    }

    /**
     * 記錄模塊啟用狀態
     */
    public static void logModuleEnable(Logger logger, String moduleName) {
        logger.info("§a" + moduleName + " 模塊已啟用");
    }

    /**
     * 記錄模塊禁用狀態
     */
    public static void logModuleDisable(Logger logger, String moduleName) {
        logger.info("§c" + moduleName + " 模塊已禁用");
    }

    /**
     * 開始記錄模塊狀態
     */
    public static void startModuleStatus(Logger logger) {
        logger.info("§7模塊狀態:");
    }

    /**
     * 記錄功能開關狀態
     */
    public static void logFeatureStatus(Logger logger, String featureName, boolean enabled) {
        String status = enabled ? "§a✓" : "§c✗";
        logger.info("§7- " + featureName + ": " + status);
    }

    /**
     * 記錄數值狀態
     */
    public static void logValueStatus(Logger logger, String name, String value) {
        logger.info("§7- " + name + ": §f" + value);
    }

    /**
     * 記錄數值狀態（帶顏色）
     */
    public static void logValueStatus(Logger logger, String name, String value, ChatColor color) {
        logger.info("§7- " + name + ": " + color + value);
    }

    /**
     * 記錄分隔線
     */
    public static void logSeparator(Logger logger) {
        logger.info("§7══════════════════════════════");
    }

    /**
     * 記錄插件啟用
     */
    public static void logPluginEnable(Logger logger, String pluginName, String version, int moduleCount) {
        logger.info("§a══════════════════════════════");
        logger.info("§a" + pluginName + " 已啟用!");
        logger.info("§a版本: §f" + version);
        logger.info("§a已加載 " + moduleCount + " 個模塊");
        logger.info("§a══════════════════════════════");
    }

    /**
     * 記錄插件禁用
     */
    public static void logPluginDisable(Logger logger, String pluginName, int moduleCount) {
        logger.info("§c" + pluginName + " 已禁用!");
        logger.info("§c已卸載 " + moduleCount + " 個模塊");
    }

    /**
     * 記錄錯誤
     */
    public static void logError(Logger logger, String moduleName, String error) {
        logger.severe("§c✗ " + moduleName + " 加載失敗: " + error);
    }

    /**
     * 記錄調試信息
     */
    public static void logDebug(Logger logger, String moduleName, String message) {
        logger.info("§e[Debug] " + moduleName + ": " + message);
    }

    /**
     * 記錄調試信息（帶詳細信息）
     */
    public static void logDebug(Logger logger, String moduleName, String message, Object... args) {
        String formattedMessage = String.format(message, args);
        logger.info("§e[Debug] " + moduleName + ": " + formattedMessage);
    }

    /**
     * 記錄警告信息
     */
    public static void logWarning(Logger logger, String moduleName, String warning) {
        logger.warning("§e⚠ " + moduleName + ": " + warning);
    }

    public static void logFileCreate(Logger logger, String fileName) {
        logger.info("§a已創建配置文件: §f" + fileName);
    }

    public static void logInfo(Logger logger, String moduleName, String message) {
        logger.info("§7" + moduleName + ": " + message);
    }

    /**
     * 記錄一般信息（帶顏色）
     */
    public static void logInfo(Logger logger, String moduleName, String message, ChatColor color) {
        logger.info(color + moduleName + ": " + message);
    }

    /**
     * 记录TPS信息
     */
    public static void logTPSInfo(Logger logger, double tps) {
        String color;
        if (tps >= 18.0) {
            color = "§2"; // 绿色
        } else if (tps >= 15.0) {
            color = "§e"; // 黄色
        } else if (tps >= 10.0) {
            color = "§6"; // 橙色
        } else {
            color = "§c"; // 红色
        }

        logger.info("§7[TPS监控] 当前TPS: " + color + String.format("%.2f", tps));
    }

    /**
     * 記錄成功信息
     */
    public static void logSuccess(Logger logger, String moduleName, String message) {
        logger.info("§a✓ " + moduleName + ": " + message);
    }
}