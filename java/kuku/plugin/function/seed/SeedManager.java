package kuku.plugin.function.seed;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import kuku.plugin.function.FunctionPlugin;
import kuku.plugin.function.utils.LogUtils;

import java.util.logging.Logger;

/**
 * 地图种子查看管理器 - 简化版，所有玩家可用
 */
public class SeedManager {

    private final FunctionPlugin plugin;
    private final Logger logger;

    public SeedManager(FunctionPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * 启用模块
     */
    public void enable() {
        // 注册命令
        registerCommands();

        logger.info("§a种子查看模块已启用 - 所有玩家可用");
    }

    /**
     * 禁用模块
     */
    public void disable() {
        logger.info("§c种子查看模块已禁用");
    }

    /**
     * 重新加载
     */
    public void reload() {
        logger.info("§a种子查看模块已重新加载");
    }

    /**
     * 注册命令
     */
    private void registerCommands() {
        // 注册 /seed 命令
        plugin.getCommand("seed").setExecutor((sender, command, label, args) -> {
            handleSeedCommand(sender);
            return true;
        });
    }

    /**
     * 处理 /seed 命令 - 简化版，没有权限检查
     */
    private void handleSeedCommand(CommandSender sender) {
        World targetWorld = null;

        // 获取目标世界
        if (sender instanceof Player) {
            targetWorld = ((Player) sender).getWorld();
        } else {
            // 控制台默认使用主世界
            targetWorld = Bukkit.getWorlds().get(0);
            sender.sendMessage(ChatColor.YELLOW + "控制台默认显示主世界种子");
        }

        // 获取种子
        long seed = targetWorld.getSeed();

        // 格式化并发送消息
        String message = formatSeedMessage(seed, targetWorld);
        sender.sendMessage(message);

        // 记录日志（可选）
        logSeedUsage(sender, targetWorld, seed);
    }

    /**
     * 格式化种子消息
     */
    private String formatSeedMessage(long seed, World world) {
        // 简单美观的格式
        return ChatColor.translateAlternateColorCodes('&',
                "&6&l=== &e&l世界种子種子信息 &6&l===\n" +
                        "&6世界: &e" + world.getName() + "\n" +
                        "&6環境: &e" + getEnvironmentChineseName(world.getEnvironment()) + "\n" +
                        "&6種子碼: &a&l" + seed + "\n" +
                        "&6十六進制: &b0x" + Long.toHexString(seed) + "\n"
        );
    }

    /**
     * 获取环境的中文名称
     */
    private String getEnvironmentChineseName(World.Environment env) {
        switch (env) {
            case NORMAL: return "主世界";
            case NETHER: return "下界";
            case THE_END: return "末地";
            case CUSTOM: return "自定义世界";
            default: return "未知";
        }
    }

    /**
     * 记录种子使用日志
     */
    private void logSeedUsage(CommandSender sender, World world, long seed) {
        String playerName = sender instanceof Player ? sender.getName() : "控制台";
        LogUtils.logInfo(logger, "种子查看",
                playerName + " 查看了世界 " + world.getName() + " 的种子: " + seed);
    }

    /**
     * 检查模块是否启用（简化版始终返回true）
     */
    public boolean isEnabled() {
        return true;
    }
}