package kuku.plugin.function.nc;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * 座標計算配置管理器
 * 使用獨立的 nc.yml 配置文件
 */
public class NcConfig {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public NcConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "nc.yml");
        loadConfig();
    }

    /**
     * 加載配置文件
     */
    public void loadConfig() {
        // 確保插件數據文件夾存在
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // 如果配置文件不存在，嘗試從JAR複製或創建新的
        if (!configFile.exists()) {
            plugin.getLogger().info("§e配置文件 nc.yml 不存在，正在創建...");

            try {
                // 檢查JAR中是否有默認配置文件
                InputStream inputStream = plugin.getResource("nc.yml");
                if (inputStream != null) {
                    // 從JAR複製到插件文件夾
                    Files.copy(inputStream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    plugin.getLogger().info("§a已從JAR複製默認配置文件");
                } else {
                    // JAR中沒有默認配置，創建新的配置文件
                    plugin.getLogger().info("§eJAR中未找到默認配置，創建新的配置文件");
                    createDefaultConfig();
                }
            } catch (IOException e) {
                plugin.getLogger().warning("§c複製配置文件時出錯: " + e.getMessage());
                plugin.getLogger().info("§e嘗試創建新的配置文件...");
                createDefaultConfig();
            }
        }

        // 加載配置
        config = YamlConfiguration.loadConfiguration(configFile);
        setupDefaults();
        saveConfig();
    }

    /**
     * 創建默認配置
     */
    private void createDefaultConfig() {
        try {
            if (!configFile.createNewFile()) {
                plugin.getLogger().warning("§c無法創建配置文件 nc.yml");
                return;
            }

            // 寫入默認配置
            config = YamlConfiguration.loadConfiguration(configFile);
            setupDefaults();
            saveConfig();
            plugin.getLogger().info("§a已創建默認配置文件 nc.yml");
        } catch (IOException e) {
            plugin.getLogger().severe("§c創建配置文件失敗: " + e.getMessage());
        }
    }

    /**
     * 重載配置文件
     */
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("§e座標計算配置已重載");
    }

    /**
     * 保存配置文件
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("無法保存 nc.yml: " + e.getMessage());
        }
    }

    /**
     * 設置默認配置
     */
    private void setupDefaults() {
        // 基本設置
        config.addDefault("enabled", true);
        config.addDefault("cooldown", 1.0); // 指令冷卻時間（秒）
        config.addDefault("show-precision", 2); // 座標顯示精度（小數位數）

        // 顯示設置
        config.addDefault("display.use-prefix", true);
        config.addDefault("display.prefix", "§8|§6系統§8| §f");
        config.addDefault("display.separator", "§7────────────────");
        config.addDefault("display.show-hints", true);

        // 消息設置
        config.addDefault("messages.main-to-nether", "§a地獄座標計算結果:");
        config.addDefault("messages.nether-to-main", "§a主世界座標計算結果:");
        config.addDefault("messages.main-coords", "§e現實世界座標 §7(主世界)");
        config.addDefault("messages.nether-coords", "§c地獄座標 §7(對應位置)");
        config.addDefault("messages.ratio-hint", "§7提示: 主世界1格 = 地獄8格");
        config.addDefault("messages.cooldown", "§c請等待 {seconds} 秒後再使用此指令");
        config.addDefault("messages.no-permission", "§c你沒有權限使用此指令");
        config.addDefault("messages.console-error", "§c只有玩家可以使用此指令");
        config.addDefault("messages.invalid-world", "§c此世界無法計算地獄座標!");

        // 安全提示
        config.addDefault("warnings.low-y-level", "§6⚠ 注意: 現實世界Y座標較低，地獄中可能遇到基岩層!");
        config.addDefault("warnings.safe-y-level", "§6ℹ 提示: 在地獄中建議保持在Y=30以上以避開熔岩湖");

        config.options().copyDefaults(true);
    }

    // =============== 獲取配置值的方法 ===============

    public boolean isEnabled() {
        return config.getBoolean("enabled", true);
    }

    public double getCooldown() {
        return config.getDouble("cooldown", 1.0);
    }

    public int getShowPrecision() {
        return config.getInt("show-precision", 2);
    }

    public boolean usePrefix() {
        return config.getBoolean("display.use-prefix", true);
    }

    public String getPrefix() {
        return config.getString("display.prefix", "§8|§6系統§8| §f");
    }

    public String getSeparator() {
        return config.getString("display.separator", "§7────────────────");
    }

    public boolean showHints() {
        return config.getBoolean("display.show-hints", true);
    }

    // 消息獲取
    public String getMainToNetherMessage() {
        return config.getString("messages.main-to-nether", "§a地獄座標計算結果:");
    }

    public String getNetherToMainMessage() {
        return config.getString("messages.nether-to-main", "§a主世界座標計算結果:");
    }

    public String getMainCoordsMessage() {
        return config.getString("messages.main-coords", "§e現實世界座標 §7(主世界)");
    }

    public String getNetherCoordsMessage() {
        return config.getString("messages.nether-coords", "§c地獄座標 §7(對應位置)");
    }

    public String getRatioHint() {
        return config.getString("messages.ratio-hint", "§7提示: 主世界1格 = 地獄8格");
    }

    public String getCooldownMessage() {
        return config.getString("messages.cooldown", "§c請等待 {seconds} 秒後再使用此指令");
    }

    public String getNoPermissionMessage() {
        return config.getString("messages.no-permission", "§c你沒有權限使用此指令");
    }

    public String getConsoleErrorMessage() {
        return config.getString("messages.console-error", "§c只有玩家可以使用此指令");
    }

    public String getInvalidWorldMessage() {
        return config.getString("messages.invalid-world", "§c此世界無法計算地獄座標!");
    }

    // 警告消息
    public String getLowYLevelWarning() {
        return config.getString("warnings.low-y-level", "§6⚠ 注意: 現實世界Y座標較低，地獄中可能遇到基岩層!");
    }

    public String getSafeYLevelHint() {
        return config.getString("warnings.safe-y-level", "§6ℹ 提示: 在地獄中建議保持在Y=30以上以避開熔岩湖");
    }

    /**
     * 獲取原始配置對象
     */
    public FileConfiguration getRawConfig() {
        return config;
    }

    /**
     * 獲取配置文件
     */
    public File getConfigFile() {
        return configFile;
    }
}