package kuku.plugin.function.half;

import kuku.plugin.function.FunctionPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 半砖单格破坏配置类 (更新版)
 * 适配最新的 HalfSlabManager 功能
 */
public class HalfSlabConfig {

    private final FunctionPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public HalfSlabConfig(FunctionPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "half.yml");
        loadConfig();
    }

    /**
     * 加载配置
     */
    private void loadConfig() {
        if (!configFile.exists()) {
            plugin.saveResource("half.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        setDefaults();
        saveConfig();
    }

    /**
     * 设置默认值 (已移除冷却相关项)
     */
    private void setDefaults() {
        // 基本设置
        config.addDefault("enabled", true);
        config.addDefault("show-message", true);
        config.addDefault("play-effects", true);
        config.addDefault("drop-items", true);

        // 功能设置
        config.addDefault("raytrace-distance", 6.0);
        config.addDefault("particle-count", 20);
        // 注意：cooldown-ticks 已移除

        // 排除的材料列表
        config.addDefault("excluded-materials", Arrays.asList(
                "BARRIER",
                "BEDROCK",
                "COMMAND_BLOCK"
        ));

        // 支持的单层半砖类型列表 (用于更精确的控制)
        // 留空则表示支持所有半砖，仅遵循排除列表
        config.addDefault("supported-slabs", Arrays.asList());

        // 权限设置 (已移除 bypass-cooldown)
        config.addDefault("permissions.enabled", true);
        config.addDefault("permissions.required", "kuku.halfslab.use");

        config.options().copyDefaults(true);
    }

    /**
     * 保存配置
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("无法保存半砖模块配置: " + e.getMessage());
        }
    }

    /**
     * 重载配置
     */
    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        // 注意：不再调用 setDefaults()，以保留用户已修改的配置
        // setDefaults();
        // saveConfig();
    }

    // ============= 以下为 Getter 方法 =============

    /**
     * 检查模块是否启用 (简化版，仅检查本文件)
     * 如果希望由主配置控制，请修改此逻辑
     */
    public boolean isEnabled() {
        // 从主配置中获取模块开关，默认为 true
        boolean moduleEnabledInMainConfig = plugin.getPluginConfig().getBoolean("modules.half", true);

        // 从本配置中获取详细开关
        boolean moduleEnabledInLocalConfig = config.getBoolean("enabled", true);

        // 两者都需为 true，模块才启用
        return moduleEnabledInMainConfig && moduleEnabledInLocalConfig;
    }

    public boolean isShowMessage() {
        return config.getBoolean("show-message", true);
    }

    public boolean isPlayEffects() {
        return config.getBoolean("play-effects", true);
    }

    public boolean isDropItems() {
        return config.getBoolean("drop-items", true);
    }

    public double getRayTraceDistance() {
        // 限制一个合理的范围
        double distance = config.getDouble("raytrace-distance", 6.0);
        return Math.max(1.0, Math.min(distance, 20.0));
    }

    public int getParticleCount() {
        // 限制一个合理的范围
        int count = config.getInt("particle-count", 20);
        return Math.max(0, Math.min(count, 100));
    }

    // 注意：getCooldownTicks() 方法已移除
    // 注意：getBypassCooldownPermission() 方法已移除

    public List<String> getExcludedMaterials() {
        return config.getStringList("excluded-materials");
    }

    public List<String> getSupportedSlabs() {
        return config.getStringList("supported-slabs");
    }

    public boolean isPermissionEnabled() {
        return config.getBoolean("permissions.enabled", true);
    }

    public String getRequiredPermission() {
        return config.getString("permissions.required", "kuku.halfslab.use");
    }
}