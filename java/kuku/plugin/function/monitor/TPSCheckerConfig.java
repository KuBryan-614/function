package kuku.plugin.function.monitor;

import kuku.plugin.function.FunctionPlugin;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * TPS监控模块配置
 */
public class TPSCheckerConfig {

    private final FunctionPlugin plugin;
    private final FileConfiguration config;

    public TPSCheckerConfig(FunctionPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getPluginConfig();
    }

    /**
     * 检查模块是否启用
     */
    public boolean isEnabled() {
        return config.getBoolean("tps.enabled", true);
    }

    /**
     * 是否显示详细性能信息
     */
    public boolean isDetailedInfo() {
        return config.getBoolean("tps.detailed-info", true);
    }

    /**
     * 是否在TPS低时发送警告
     */
    public boolean showWarning() {
        return config.getBoolean("tps.show-warning", true);
    }

    /**
     * 低TPS阈值
     */
    public double getLowTpsThreshold() {
        return config.getDouble("tps.low-tps-threshold", 15.0);
    }

    /**
     * 高TPS阈值
     */
    public double getHighTpsThreshold() {
        return config.getDouble("tps.high-tps-threshold", 18.0);
    }

    /**
     * 命令权限
     */
    public String getCommandPermission() {
        return config.getString("tps.command-permission", "kuku.command.tps");
    }

    /**
     * 是否自动广播TPS过低警告
     */
    public boolean isAutoBroadcast() {
        return config.getBoolean("tps.auto-broadcast", false);
    }

    /**
     * 自动广播间隔（秒）
     */
    public int getBroadcastInterval() {
        return config.getInt("tps.broadcast-interval", 300);
    }

    /**
     * 自动广播的TPS阈值
     */
    public double getBroadcastTpsThreshold() {
        return config.getDouble("tps.broadcast-tps-threshold", 10.0);
    }
}