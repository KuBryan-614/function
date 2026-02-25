package kuku.plugin.function.nc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;

/**
 * 地獄座標計算指令
 */
public class NcCommand implements CommandExecutor {

    private final NcManager ncManager;

    public NcCommand(NcManager ncManager) {
        this.ncManager = ncManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 檢查模塊是否啟用
        if (!ncManager.isEnabled()) {
            sender.sendMessage("§c座標計算模塊未啟用!");
            return true;
        }

        // 檢查是否為玩家執行指令
        if (!(sender instanceof Player)) {
            String message = ncManager.getConfig().getConsoleErrorMessage();
            sender.sendMessage(message);
            return true;
        }

        Player player = (Player) sender;
        String playerName = player.getName();

        // 檢查冷卻時間
        if (ncManager.isOnCooldown(playerName)) {
            double remaining = ncManager.getRemainingCooldown(playerName);
            String message = ncManager.getConfig().getCooldownMessage()
                    .replace("{seconds}", String.format("%.1f", remaining));
            sendMessage(player, message);
            return true;
        }

        Location playerLoc = player.getLocation();
        World playerWorld = player.getWorld();

        // 檢查玩家所在的世界類型
        if (playerWorld.getEnvironment() != Environment.NORMAL &&
                playerWorld.getEnvironment() != Environment.NETHER) {
            String message = ncManager.getConfig().getInvalidWorldMessage();
            sendMessage(player, message);
            return true;
        }

        // 根據世界類型計算座標
        if (playerWorld.getEnvironment() == Environment.NORMAL) {
            // 主世界 -> 地獄座標
            displayMainToNether(player, playerLoc);
        } else {
            // 地獄 -> 主世界座標
            displayNetherToMain(player, playerLoc);
        }

        // 設置冷卻時間
        ncManager.setCooldown(playerName);

        return true;
    }

    /**
     * 發送消息（使用配置的前綴）
     */
    private void sendMessage(Player player, String message) {
        if (message == null || message.isEmpty()) return;

        if (ncManager.getConfig().usePrefix()) {
            player.sendMessage(ncManager.getConfig().getPrefix() + message);
        } else {
            player.sendMessage(message);
        }
    }

    /**
     * 顯示主世界到地獄的座標轉換
     */
    private void displayMainToNether(Player player, Location mainLoc) {
        double mainX = mainLoc.getX();
        double mainY = mainLoc.getY();
        double mainZ = mainLoc.getZ();

        // 計算地獄座標（除以8）
        double netherX = mainX / 8.0;
        double netherZ = mainZ / 8.0;
        // Y座標保持不變（地獄的高度限制為0-128）
        double netherY = Math.min(Math.max(mainY, 0), 128);

        // 獲取顯示精度
        int precision = ncManager.getConfig().getShowPrecision();
        String format = "%." + precision + "f";

        // 格式化座標
        String formattedMainX = String.format(format, mainX);
        String formattedMainY = String.format(format, mainY);
        String formattedMainZ = String.format(format, mainZ);

        String formattedNetherX = String.format(format, netherX);
        String formattedNetherY = String.format(format, netherY);
        String formattedNetherZ = String.format(format, netherZ);

        // 發送訊息給玩家
        NcConfig config = ncManager.getConfig();
        String separator = config.getSeparator();

        if (config.usePrefix()) {
            player.sendMessage(config.getPrefix() + config.getMainToNetherMessage());
        } else {
            player.sendMessage(config.getMainToNetherMessage());
        }

        player.sendMessage(separator);
        player.sendMessage(config.getMainCoordsMessage());
        player.sendMessage("§7X: §f" + formattedMainX + "  §7Y: §f" + formattedMainY + "  §7Z: §f" + formattedMainZ);
        player.sendMessage(separator);
        player.sendMessage(config.getNetherCoordsMessage());
        player.sendMessage("§7X: §f" + formattedNetherX + "  §7Y: §f" + formattedNetherY + "  §7Z: §f" + formattedNetherZ);
        player.sendMessage(separator);

        // 顯示提示
        if (config.showHints()) {
            player.sendMessage(config.getRatioHint());

            // 安全提示
            if (mainY < 10) {
                player.sendMessage(config.getLowYLevelWarning());
            }
        }
    }

    /**
     * 顯示地獄到主世界的座標轉換
     */
    private void displayNetherToMain(Player player, Location netherLoc) {
        double netherX = netherLoc.getX();
        double netherY = netherLoc.getY();
        double netherZ = netherLoc.getZ();

        // 計算主世界座標（乘以8）
        double mainX = netherX * 8.0;
        double mainZ = netherZ * 8.0;
        // Y座標保持不變
        double mainY = netherY;

        // 獲取顯示精度
        int precision = ncManager.getConfig().getShowPrecision();
        String format = "%." + precision + "f";

        // 格式化座標
        String formattedNetherX = String.format(format, netherX);
        String formattedNetherY = String.format(format, netherY);
        String formattedNetherZ = String.format(format, netherZ);

        String formattedMainX = String.format(format, mainX);
        String formattedMainY = String.format(format, mainY);
        String formattedMainZ = String.format(format, mainZ);

        // 發送訊息給玩家
        NcConfig config = ncManager.getConfig();
        String separator = config.getSeparator();

        if (config.usePrefix()) {
            player.sendMessage(config.getPrefix() + config.getNetherToMainMessage());
        } else {
            player.sendMessage(config.getNetherToMainMessage());
        }

        player.sendMessage(separator);
        player.sendMessage(config.getNetherCoordsMessage());
        player.sendMessage("§7X: §f" + formattedNetherX + "  §7Y: §f" + formattedNetherY + "  §7Z: §f" + formattedNetherZ);
        player.sendMessage(separator);
        player.sendMessage(config.getMainCoordsMessage());
        player.sendMessage("§7X: §f" + formattedMainX + "  §7Y: §f" + formattedMainY + "  §7Z: §f" + formattedMainZ);
        player.sendMessage(separator);

        // 顯示提示
        if (config.showHints()) {
            player.sendMessage(config.getRatioHint());

            // 安全提示
            if (config.showHints() && netherY < 30) {
                player.sendMessage(config.getSafeYLevelHint());
            }
        }
    }

    /**
     * 獲取指令說明
     */
    public static String getCommandDescription() {
        return "§7/nc §f- 計算當前位置的地獄/主世界對應座標";
    }
}