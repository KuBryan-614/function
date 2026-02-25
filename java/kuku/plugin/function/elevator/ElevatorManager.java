package kuku.plugin.function.elevator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;

import kuku.plugin.function.FunctionPlugin;
import kuku.plugin.function.utils.LogUtils;

import java.util.logging.Logger;

public class ElevatorManager {

    private final FunctionPlugin plugin;
    private final Logger logger;
    private final FileConfiguration config;

    private ElevatorListener elevatorListener;
    private boolean enabled = false;

    public ElevatorManager(FunctionPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.config = plugin.getConfig();
    }

    /**
     * 啟用電梯模塊
     */
    public void enable() {
        try {
            // 檢查模塊是否啟用
            if (!config.getBoolean("elevator.enabled", true)) {
                logger.info("§e[電梯] 模塊已禁用");
                return;
            }

            // 獲取基本配置
            int range = config.getInt("elevator.range", 10);
            long cooldown = config.getLong("elevator.cooldown", 500);
            boolean soundEnabled = config.getBoolean("elevator.sound-enabled", true);
            boolean jumpUp = config.getBoolean("elevator.jump-up", true);
            boolean sneakDown = config.getBoolean("elevator.sneak-down", true);
            boolean sneakClickInfo = config.getBoolean("elevator.sneak-click-info", true);
            boolean showMessage = config.getBoolean("elevator.show-message", true);
            boolean safetyCheck = config.getBoolean("elevator.safety-check", true);

            // 創建並註冊監聽器
            elevatorListener = new ElevatorListener(plugin, cooldown, range,
                    jumpUp, sneakDown, sneakClickInfo,
                    showMessage, soundEnabled, safetyCheck);
            PluginManager pm = Bukkit.getPluginManager();
            pm.registerEvents(elevatorListener, plugin);

            enabled = true;

            // 輸出啟用信息
            logger.info("§a[電梯] 模塊已啟用");
            logger.info("§e[電梯] 搜索範圍: " + range + "格");
            logger.info("§e[電梯] 冷卻時間: " + cooldown + "ms");
            logger.info("§e[電梯] 跳躍上樓: " + (jumpUp ? "啟用" : "禁用"));
            logger.info("§e[電梯] 蹲下下樓: " + (sneakDown ? "啟用" : "禁用"));
            logger.info("§e[電梯] 蹲下左鍵顯示樓層: " + (sneakClickInfo ? "啟用" : "禁用"));
            logger.info("§e[電梯] 安全檢查: " + (safetyCheck ? "啟用" : "禁用"));
            logger.info("§a[電梯] 電梯標誌: §f[電梯]");
            logger.info("§a[電梯] 結構: §f告示牌 + 羊毛");
            logger.info("§a[電梯] 權限: §f所有玩家皆可使用");

        } catch (Exception e) {
            logger.severe("§c[電梯] 啟用模塊時發生錯誤:");
            e.printStackTrace();
            enabled = false;
        }
    }

    /**
     * 禁用電梯模塊
     */
    public void disable() {
        try {
            if (elevatorListener != null) {
                HandlerList.unregisterAll(elevatorListener);
                elevatorListener = null;
            }

            enabled = false;
            logger.info("§e[電梯] 模塊已禁用");

        } catch (Exception e) {
            logger.severe("§c[電梯] 禁用模塊時發生錯誤:");
            e.printStackTrace();
        }
    }

    /**
     * 重載電梯模塊
     */
    public void reload() {
        logger.info("§e[電梯] 正在重載模塊...");

        // 禁用當前模塊
        disable();

        // 重新加載配置
        plugin.reloadConfig();

        // 重新啟用模塊
        enable();

        logger.info("§a[電梯] 模塊重載完成");
    }

    /**
     * 檢查模塊是否啟用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 獲取模塊狀態
     */
    public String getStatus() {
        if (!enabled) {
            return "§c電梯模塊未啟用";
        }

        int range = config.getInt("elevator.range", 10);
        long cooldown = config.getLong("elevator.cooldown", 500);
        boolean jumpUp = config.getBoolean("elevator.jump-up", true);
        boolean sneakDown = config.getBoolean("elevator.sneak-down", true);
        boolean sneakClickInfo = config.getBoolean("elevator.sneak-click-info", true);
        boolean safetyCheck = config.getBoolean("elevator.safety-check", true);

        return ChatColor.GOLD + "=== 電梯模塊狀態 ===\n" +
                ChatColor.YELLOW + "狀態: §a啟用\n" +
                ChatColor.YELLOW + "跳躍上樓: §f" + (jumpUp ? "啟用" : "禁用") + "\n" +
                ChatColor.YELLOW + "蹲下下樓: §f" + (sneakDown ? "啟用" : "禁用") + "\n" +
                ChatColor.YELLOW + "蹲下左鍵顯示樓層: §f" + (sneakClickInfo ? "啟用" : "禁用") + "\n" +
                ChatColor.YELLOW + "安全檢查: §f" + (safetyCheck ? "啟用" : "禁用") + "\n" +
                ChatColor.YELLOW + "搜索範圍: §f" + range + "格\n" +
                ChatColor.YELLOW + "冷卻時間: §f" + cooldown + "毫秒\n" +
                ChatColor.YELLOW + "電梯標誌: §f[電梯]\n" +
                ChatColor.YELLOW + "結構: §f告示牌 + 羊毛\n" +
                ChatColor.YELLOW + "權限: §f所有玩家皆可使用";
    }

    public void create2x2ExampleElevator(org.bukkit.entity.Player player) {
        try {
            org.bukkit.Location loc = player.getLocation().getBlock().getLocation();

            // 建立 2x2 羊毛平台
            for (int dx = 0; dx <= 1; dx++) {
                for (int dz = 0; dz <= 1; dz++) {
                    org.bukkit.Location woolLoc = loc.clone().add(dx, 0, dz);
                    org.bukkit.block.Block woolBlock = woolLoc.getBlock();
                    woolBlock.setType(org.bukkit.Material.BLUE_WOOL);
                }
            }

            // 在平台角落放置告示牌（示範告示牌可以在任意位置）
            org.bukkit.block.Block signBlock = loc.clone().add(0, 1, 0).getBlock();
            signBlock.setType(org.bukkit.Material.OAK_SIGN);

            if (signBlock.getState() instanceof org.bukkit.block.Sign) {
                org.bukkit.block.Sign sign = (org.bukkit.block.Sign) signBlock.getState();
                sign.setLine(0, "[電梯]");
                sign.setLine(1, "2x2示例");
                sign.setLine(2, "平台大小: 2x2");
                sign.setLine(3, "");
                sign.update();

                player.sendMessage("§a2x2 示範電梯已創建！");
                player.sendMessage("§e注意: 告示牌可以在 2x2 平台上的任何位置");
            }

        } catch (Exception e) {
            player.sendMessage("§c創建示範電梯時發生錯誤: " + e.getMessage());
        }
    }

    /**
     * 創建示範電梯
     */
    public void createExampleElevator(org.bukkit.entity.Player player) {
        try {
            org.bukkit.Location loc = player.getLocation().getBlock().getLocation();

            // 建立 3x3 羊毛平台
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    org.bukkit.Location woolLoc = loc.clone().add(dx, 0, dz);
                    org.bukkit.block.Block woolBlock = woolLoc.getBlock();
                    woolBlock.setType(org.bukkit.Material.WHITE_WOOL);
                }
            }

            // 在平台中心放置告示牌（可以改為任意位置）
            org.bukkit.block.Block signBlock = loc.clone().add(0, 1, 0).getBlock();
            signBlock.setType(org.bukkit.Material.OAK_SIGN);

            // 設置告示牌文字
            if (signBlock.getState() instanceof org.bukkit.block.Sign) {
                org.bukkit.block.Sign sign = (org.bukkit.block.Sign) signBlock.getState();
                sign.setLine(0, "[電梯]");
                sign.setLine(1, "1樓");
                sign.setLine(2, "地面");
                sign.setLine(3, "3x3平台");
                sign.update();

                // 確保上方有空間
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        for (int dy = 1; dy <= 3; dy++) {
                            loc.clone().add(dx, dy, dz).getBlock().setType(org.bukkit.Material.AIR);
                        }
                    }
                }

                player.sendMessage("§a示範電梯已創建！");
                player.sendMessage("§e電梯類型: §f3x3 羊毛平台");
                player.sendMessage("§e告示牌第1行: §f[電梯]");
                player.sendMessage("§e告示牌第2行: §f1樓");
                player.sendMessage("§e告示牌第3行: §f地面");
                player.sendMessage("§e告示牌第4行: §f3x3平台");
                player.sendMessage("§e平台大小: §f3x3 (支持 1x1, 2x2, 3x3)");
                player.sendMessage("§e使用方法:");
                player.sendMessage("§f  • 站在平台上 §a跳躍 §f→ 上樓");
                player.sendMessage("§f  • 站在平台上 §a蹲下 §f→ 下樓");
                player.sendMessage("§f  • 蹲下 + §e左鍵點擊告示牌 §f→ 顯示所有樓層");
                player.sendMessage("§a注意: 告示牌可以放在平台上的任何位置");
            }

        } catch (Exception e) {
            player.sendMessage("§c創建示範電梯時發生錯誤: " + e.getMessage());
        }
    }
}