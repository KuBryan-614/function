package kuku.plugin.function.monitor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;

/**
 * TPS监控模块
 * 提供 /ktps 命令查看服务器TPS和性能信息
 */
public class TPSChecker implements CommandExecutor {

    private final kuku.plugin.function.FunctionPlugin plugin;
    private final DecimalFormat decimalFormat;
    private long lastTickTime = System.currentTimeMillis();
    private int tickCount = 0;
    private double currentTPS = 20.0;

    public TPSChecker(kuku.plugin.function.FunctionPlugin plugin) {
        this.plugin = plugin;
        this.decimalFormat = new DecimalFormat("#0.00");

        // 启动TPS监控任务
        startTPSMonitor();
    }

    /**
     * 启动TPS监控
     */
    private void startTPSMonitor() {
        new BukkitRunnable() {
            @Override
            public void run() {
                tickCount++;

                // 每秒计算一次TPS
                if (tickCount % 20 == 0) {
                    long currentTime = System.currentTimeMillis();
                    long timeSpent = currentTime - lastTickTime;

                    // 计算TPS（理论上1000ms处理20个tick，实际时间/1000ms = 倍数）
                    currentTPS = Math.min(20.0, 20.0 * 1000.0 / Math.max(timeSpent, 1));

                    lastTickTime = currentTime;
                }
            }
        }.runTaskTimer(plugin, 1L, 1L); // 每个tick执行一次
    }

    /**
     * 获取当前TPS
     */
    public double getCurrentTPS() {
        return currentTPS;
    }

    /**
     * 获取格式化的TPS字符串
     */
    public String getFormattedTPS() {
        return decimalFormat.format(currentTPS);
    }

    /**
     * 获取TPS颜色标识
     */
    public ChatColor getTPSColor() {
        if (currentTPS >= 18.0) {
            return ChatColor.GREEN;      // 优秀：绿色
        } else if (currentTPS >= 15.0) {
            return ChatColor.YELLOW;     // 良好：黄色
        } else if (currentTPS >= 10.0) {
            return ChatColor.GOLD;       // 一般：橙色
        } else {
            return ChatColor.RED;        // 差：红色
        }
    }

    /**
     * 获取TPS状态描述
     */
    public String getTPSStatus() {
        if (currentTPS >= 18.0) {
            return "优秀";
        } else if (currentTPS >= 15.0) {
            return "良好";
        } else if (currentTPS >= 10.0) {
            return "一般";
        } else {
            return "较差";
        }
    }

    /**
     * 获取服务器性能信息
     */
    public String getPerformanceInfo() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = allocatedMemory - freeMemory;

        // 转换为MB
        double maxMB = maxMemory / (1024.0 * 1024.0);
        double usedMB = usedMemory / (1024.0 * 1024.0);

        // 计算内存使用百分比
        double memoryUsagePercent = (usedMemory * 100.0) / maxMemory;

        // 获取在线玩家数量
        int onlinePlayers = Bukkit.getOnlinePlayers().size();

        return ChatColor.GRAY + "内存使用: " +
                ChatColor.GOLD + decimalFormat.format(usedMB) + "MB" +
                ChatColor.GRAY + " / " +
                ChatColor.GOLD + decimalFormat.format(maxMB) + "MB" +
                ChatColor.GRAY + " (" + decimalFormat.format(memoryUsagePercent) + "%)" +
                ChatColor.GRAY + " | 在线玩家: " + ChatColor.GOLD + onlinePlayers;
    }

    /**
     * 处理/ktps命令
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 发送TPS信息
        ChatColor tpsColor = getTPSColor();

        sender.sendMessage(ChatColor.GRAY + "§m                                      ");
        sender.sendMessage(ChatColor.GOLD + "    ⚡ 服务器性能监控");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "    TPS状态: " + tpsColor + getFormattedTPS() +
                ChatColor.GRAY + " (" + tpsColor + getTPSStatus() + ChatColor.GRAY + ")");
        sender.sendMessage("");
        sender.sendMessage(getPerformanceInfo());
        sender.sendMessage("");

        // 如果TPS较低，显示警告信息
        if (currentTPS < 15.0) {
            sender.sendMessage(ChatColor.RED + "⚠ 警告：服务器TPS较低，可能会影响游戏体验！");
        }

        sender.sendMessage(ChatColor.GRAY + "§m                                      ");

        return true;
    }

    /**
     * 启用模块
     */
    public void enable() {
        // 注册命令
        plugin.getCommand("ktps").setExecutor(this);

        plugin.getLogger().info("§a[TPS监控] 模块已启用");
    }

    /**
     * 禁用模块
     */
    public void disable() {
        plugin.getLogger().info("§a[TPS监控] 模块已禁用");
    }

    /**
     * 重载模块
     */
    public void reload() {
        plugin.getLogger().info("§a[TPS监控] 模块配置已重载");
    }

    /**
     * 获取模块配置（简化版）
     */
    public TPSCheckerConfig getConfig() {
        return new TPSCheckerConfig(plugin);
    }

    /**
     * 检查模块是否启用
     */
    public boolean isEnabled() {
        return plugin.getPluginConfig().getBoolean("modules.tps", true) &&
                plugin.getPluginConfig().getBoolean("tps.enabled", true);
    }
}