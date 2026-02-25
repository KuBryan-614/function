package kuku.plugin.function.rightclick;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import kuku.plugin.function.FunctionPlugin;
import kuku.plugin.function.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * 右键采收配置类 - 简化版
 */
public class RightClickConfig {

    private final FunctionPlugin plugin;
    private final Logger logger;
    private File configFile;
    private FileConfiguration config;

    public RightClickConfig(FunctionPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        setupConfig();
    }

    /**
     * 设置配置文件
     */
    private void setupConfig() {
        // 确保数据文件夹存在
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // 创建配置文件
        configFile = new File(plugin.getDataFolder(), "rightclick.yml");

        // 如果配置文件不存在，创建默认配置
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                LogUtils.logFileCreate(logger, "rightclick.yml");
            } catch (IOException e) {
                LogUtils.logError(logger, "右键采收配置", "创建配置文件失败: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 加载配置
        config = YamlConfiguration.loadConfiguration(configFile);

        // 设置默认值
        setupDefaults();

        // 保存配置
        saveConfig();
    }

    /**
     * 设置默认配置值
     */
    private void setupDefaults() {
        // 基本设置
        config.addDefault("enabled", true);
        config.addDefault("show-message", true);
        config.addDefault("debug", false);

        // 支持的农作物列表
        List<String> defaultCrops = Arrays.asList(
                "WHEAT", "POTATOES", "CARROTS", "BEETROOTS",
                "NETHER_WART", "COCOA", "SWEET_BERRY_BUSH"
        );
        config.addDefault("crops", defaultCrops);

        // 特殊设置
        config.addDefault("auto-replant", true);
        config.addDefault("drop-items", true);

        // 复制默认值
        config.options().copyDefaults(true);
    }

    /**
     * 保存配置
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            LogUtils.logError(logger, "右键采收配置", "保存配置文件失败: " + e.getMessage());
        }
    }

    /**
     * 重新加载配置
     */
    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        setupDefaults(); // 确保新添加的默认值被设置
        saveConfig();
        LogUtils.logSuccess(logger, "右键采收配置", "配置文件已重载");
    }

    /**
     * 检查模块是否启用
     */
    public boolean isEnabled() {
        return config.getBoolean("enabled", true);
    }

    /**
     * 检查是否显示消息
     */
    public boolean isShowMessage() {
        return config.getBoolean("show-message", true);
    }

    /**
     * 获取调试模式状态
     */
    public boolean isDebug() {
        return config.getBoolean("debug", false);
    }

    /**
     * 获取支持的农作物列表
     */
    public List<String> getCrops() {
        return config.getStringList("crops");
    }

    /**
     * 检查是否自动重新种植
     */
    public boolean isAutoReplant() {
        return config.getBoolean("auto-replant", true);
    }

    /**
     * 检查是否掉落物品
     */
    public boolean isDropItems() {
        return config.getBoolean("drop-items", true);
    }

    /**
     * 获取配置文件实例
     */
    public FileConfiguration getConfig() {
        return config;
    }
}