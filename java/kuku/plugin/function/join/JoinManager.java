package kuku.plugin.function.join;

import kuku.plugin.function.utils.LogUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import java.util.logging.Logger;
import java.util.List;

/**
 * 加入功能管理器
 * 使用獨立的 join.yml 配置文件
 */
public class JoinManager {

    private final JavaPlugin plugin;
    private final Logger logger;
    private final JoinConfig config;
    private final JoinListener listener;
    private boolean enabled = false;

    // 系統前綴
    private static final String SYSTEM_PREFIX = "§8|§6系統§8| §f";

    public JoinManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.config = new JoinConfig(plugin);
        this.listener = new JoinListener(this);

        // 註冊事件監聽器
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    /**
     * 啟用加入功能
     */
    public void enable() {
        if (enabled) {
            return;
        }

        enabled = true;

        LogUtils.logModuleEnable(logger, "加入");
        logModuleStatus();
    }

    /**
     * 禁用加入功能
     */
    public void disable() {
        if (!enabled) {
            return;
        }

        enabled = false;
        LogUtils.logModuleDisable(logger, "加入");
    }

    /**
     * 重載配置
     */
    public void reload() {
        config.reloadConfig();
        logger.info("§e加入模塊配置已重載");
        logModuleStatus();
    }

    /**
     * 記錄模塊狀態
     */
    private void logModuleStatus() {
        if (!config.isEnabled()) {
            logger.info("§7注意: 模塊在配置中被禁用 (join.yml)");
            return;
        }

        LogUtils.startModuleStatus(logger);
        LogUtils.logFeatureStatus(logger, "歡迎消息", config.isWelcomeMessageEnabled());
        LogUtils.logFeatureStatus(logger, "退出消息", config.isQuitMessageEnabled());
        LogUtils.logFeatureStatus(logger, "個人歡迎", config.isPersonalWelcomeEnabled());
        LogUtils.logFeatureStatus(logger, "首次加入", config.isFirstJoinEnabled());
        LogUtils.logFeatureStatus(logger, "音效效果", config.isSoundEffectsEnabled());
    }

    /**
     * 手動發送歡迎消息給玩家
     * 這是一個API方法，供其他插件或代碼調用
     */
    public void sendWelcomeToPlayer(Player player) {
        if (!enabled || !config.isEnabled()) return;

        // 簡單的佔位符替換，加上系統前綴
        for (String line : config.getPersonalWelcomeMessages()) {
            String formattedLine = SYSTEM_PREFIX + line
                    .replace("{player}", player.getName())
                    .replace("{online}", String.valueOf(plugin.getServer().getOnlinePlayers().size()))
                    .replace("{max}", String.valueOf(plugin.getServer().getMaxPlayers()))
                    .replace("{world}", player.getWorld().getName());
            player.sendMessage(formattedLine);
        }

        logger.info("已手動發送歡迎消息給玩家: " + player.getName());
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
    public JoinConfig getConfig() {
        return config;
    }

    /**
     * 獲取插件實例
     */
    public JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * 獲取事件監聽器
     */
    public JoinListener getListener() {
        return listener;
    }
}