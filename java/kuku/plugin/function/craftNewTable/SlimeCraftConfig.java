package kuku.plugin.function.craftNewTable;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * 黏液球合成配置
 */
public class SlimeCraftConfig extends kuku.plugin.function.ModuleConfig {

    public SlimeCraftConfig(JavaPlugin plugin) {
        super(plugin, "slimecraft.yml");
        setupDefaults();
    }

    @Override
    protected void setupDefaults() {
        config.addDefault("enabled", true);
        config.addDefault("debug", false);

        // 雪球合成压缩雪球
        config.addDefault("recipes.snowball-to-compressed.enabled", true);
        config.addDefault("recipes.snowball-to-compressed.output-amount", 1);

        // 压缩雪球合成史莱姆核心
        config.addDefault("recipes.compressed-to-core.enabled", true);
        config.addDefault("recipes.compressed-to-core.output-amount", 1);

        // 糖 + 史莱姆核心 + 绿色染料 -> 黏液球
        config.addDefault("recipes.slimeball.enabled", true);
        config.addDefault("recipes.slimeball.output-amount", 1);

        // 黏液块分解
        config.addDefault("recipes.decompose.enabled", true);
        config.addDefault("recipes.decompose.output-amount", 9);

        config.options().copyDefaults(true);
        saveConfig();
    }

    /**
     * 检查雪球合成是否启用
     */
    public boolean isSnowballRecipeEnabled() {
        return config.getBoolean("recipes.snowball-to-compressed.enabled", true);
    }

    /**
     * 检查压缩雪球合成是否启用
     */
    public boolean isCompressedToCoreEnabled() {
        return config.getBoolean("recipes.compressed-to-core.enabled", true);
    }

    /**
     * 检查黏液球合成是否启用
     */
    public boolean isSlimeBallRecipeEnabled() {
        return config.getBoolean("recipes.slimeball.enabled", true);
    }

    /**
     * 检查黏液块分解是否启用
     */
    public boolean isSlimeBlockDecomposeEnabled() {
        return config.getBoolean("recipes.decompose.enabled", true);
    }

    /**
     * 获取黏液球输出数量
     */
    public int getSlimeBallOutputAmount() {
        return config.getInt("recipes.slimeball.output-amount", 1);
    }

    /**
     * 获取分解输出数量
     */
    public int getDecomposeOutputAmount() {
        return config.getInt("recipes.decompose.output-amount", 9);
    }

    /**
     * 是否开启调试模式
     */
    public boolean isDebug() {
        return config.getBoolean("debug", false);
    }
}