package kuku.plugin.function;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * 模块配置基础类
 */
public abstract class ModuleConfig {

    protected final JavaPlugin plugin;
    protected final String fileName;
    protected File configFile;
    protected FileConfiguration config;

    public ModuleConfig(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.configFile = new File(plugin.getDataFolder(), fileName);
        loadConfig();
    }

    /**
     * 加载配置
     */
    protected void loadConfig() {
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * 保存配置
     */
    protected void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "无法保存配置文件 " + fileName, e);
        }
    }

    /**
     * 重载配置
     */
    public void reload() {
        loadConfig();
    }

    /**
     * 检查模块是否启用
     */
    public boolean isEnabled() {
        return config.getBoolean("enabled", true);
    }

    /**
     * 获取配置实例
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * 设置默认值（子类实现）
     */
    protected abstract void setupDefaults();
}