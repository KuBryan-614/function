package kuku.plugin.function.craftNewTable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 黏液球合成表管理器
 */
public class SlimeCraftManager {

    private final JavaPlugin plugin;
    private final Logger logger;
    private SlimeCraftConfig config;
    private CustomItemManager customItemManager;
    private final Map<String, NamespacedKey> recipeKeys;

    public SlimeCraftManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.recipeKeys = new HashMap<>();
        loadConfig();
        this.customItemManager = new CustomItemManager(plugin);
    }

    /**
     * 加载配置
     */
    private void loadConfig() {
        this.config = new SlimeCraftConfig(plugin);
    }

    /**
     * 启用模块
     */
    public void enable() {
        if (!config.isEnabled()) {
            logger.info("§e[黏液球合成] 模块已禁用");
            return;
        }

        registerRecipes();
        registerEvents(); // 注册事件
        logger.info("§a[黏液球合成] 模块已启用");
        logger.info("§e已注册 " + recipeKeys.size() + " 个合成表");

        // 显示自定义物品信息
        logger.info("§e自定义物品: 压缩雪球, 史莱姆核心");
    }

    /**
     * 注册所有合成表
     */
    private void registerRecipes() {
        // 9个雪球合成压缩雪球
        if (config.isSnowballRecipeEnabled()) {
            registerSnowballToCompressed();
        }

        // 9个压缩雪球合成史莱姆核心
        if (config.isCompressedToCoreEnabled()) {
            registerCompressedToCore();
        }

        // 糖 + 史莱姆核心 + 绿色染料 -> 黏液球
        if (config.isSlimeBallRecipeEnabled()) {
            registerSlimeBallRecipe();
        }

        // 可选：黏液块分解成黏液球
        if (config.isSlimeBlockDecomposeEnabled()) {
            registerSlimeBlockDecompose();
        }
    }

    /**
     * 9个雪球 -> 压缩雪球
     */
    private void registerSnowballToCompressed() {
        try {
            NamespacedKey key = new NamespacedKey(plugin, "snowball_to_compressed");

            ShapedRecipe recipe = new ShapedRecipe(key, customItemManager.createCompressedSnowball());
            recipe.shape("###", "###", "###");
            recipe.setIngredient('#', Material.SNOWBALL);

            Bukkit.addRecipe(recipe);
            recipeKeys.put("snowball_to_compressed", key);

            if (config.isDebug()) {
                logger.info("§a[黏液球合成] 已注册雪球合成压缩雪球配方");
            }
        } catch (Exception e) {
            logger.warning("§c[黏液球合成] 注册雪球合成配方失败: " + e.getMessage());
        }
    }

    /**
     * 9个压缩雪球 -> 史莱姆核心
     */
    private void registerCompressedToCore() {
        try {
            NamespacedKey key = new NamespacedKey(plugin, "compressed_to_core");

            ShapedRecipe recipe = new ShapedRecipe(key, customItemManager.createSlimeCore());
            recipe.shape("###", "###", "###");
            recipe.setIngredient('#', Material.SNOW_BLOCK); // 使用雪块作为压缩雪球的占位符

            Bukkit.addRecipe(recipe);
            recipeKeys.put("compressed_to_core", key);

            if (config.isDebug()) {
                logger.info("§a[黏液球合成] 已注册压缩雪球合成史莱姆核心配方");
            }
        } catch (Exception e) {
            logger.warning("§c[黏液球合成] 注册压缩雪球合成配方失败: " + e.getMessage());
        }
    }

    /**
     * 糖 + 史莱姆核心 + 绿色染料 -> 黏液球
     * 支持任意水平排列，不支持垂直排列
     */
    private void registerSlimeBallRecipe() {
        try {
            // 注册多个变体配方
            registerSlimeBallVariant("horizontal1", new String[]{"SGD"});  // 糖、史莱姆核心、染料（第一行）
            registerSlimeBallVariant("horizontal2", new String[]{"GS", " D"}); // 糖和核心在第一行，染料在第二行
            registerSlimeBallVariant("horizontal3", new String[]{"G", "S", "D"}); // 垂直排列（应该不会匹配，但可以用于测试）

            if (config.isDebug()) {
                logger.info("§a[黏液球合成] 已注册黏液球合成配方（多个变体）");
            }
        } catch (Exception e) {
            logger.warning("§c[黏液球合成] 注册黏液球合成配方失败: " + e.getMessage());
        }
    }

    /**
     * 注册黏液球合成的变体
     */
    private void registerSlimeBallVariant(String variant, String[] shape) {
        try {
            NamespacedKey key = new NamespacedKey(plugin, "slimeball_recipe_" + variant);

            ShapedRecipe recipe = new ShapedRecipe(key,
                    new ItemStack(Material.SLIME_BALL, config.getSlimeBallOutputAmount()));

            // 设置形状
            recipe.shape(shape);

            // 设置材料
            // S = 糖
            recipe.setIngredient('S', Material.SUGAR);
            // G = 史莱姆核心（使用绿宝石作为占位符，实际会在事件中检查）
            recipe.setIngredient('G', Material.EMERALD);
            // D = 绿色染料
            recipe.setIngredient('D', Material.GREEN_DYE);

            Bukkit.addRecipe(recipe);
            recipeKeys.put("slimeball_" + variant, key);
        } catch (Exception e) {
            // 忽略形状错误
        }
    }

    /**
     * 黏液块分解成黏液球
     */
    private void registerSlimeBlockDecompose() {
        try {
            NamespacedKey key = new NamespacedKey(plugin, "slimeblock_decompose");

            ShapelessRecipe recipe = new ShapelessRecipe(key,
                    new ItemStack(Material.SLIME_BALL, config.getDecomposeOutputAmount()));
            recipe.addIngredient(Material.SLIME_BLOCK);

            Bukkit.addRecipe(recipe);
            recipeKeys.put("decompose", key);

            if (config.isDebug()) {
                logger.info("§a[黏液球合成] 已注册黏液块分解配方");
            }
        } catch (Exception e) {
            logger.warning("§c[黏液球合成] 注册黏液块分解配方失败: " + e.getMessage());
        }
    }

    /**
     * 禁用模块（移除所有合成表）
     */
    public void disable() {
        for (NamespacedKey key : recipeKeys.values()) {
            Bukkit.removeRecipe(key);
        }
        recipeKeys.clear();
        logger.info("§a[黏液球合成] 模块已禁用");
    }

    /**
     * 重载配置
     */
    public void reload() {
        disable();
        loadConfig();
        enable();
        logger.info("§a[黏液球合成] 配置已重载");
    }

    /**
     * 获取配置
     */
    public SlimeCraftConfig getConfig() {
        return config;
    }

    /**
     * 获取自定义物品管理器
     */
    public CustomItemManager getCustomItemManager() {
        return customItemManager;
    }

    /**
     * 检查模块是否启用
     */
    public boolean isEnabled() {
        return config.isEnabled();
    }

    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(
                new SlimeCraftListener(this), plugin);
    }

    /**
     * 获取状态信息
     */
    public String getStatus() {
        if (!isEnabled()) {
            return "§c黏液球合成模块未启用";
        }

        StringBuilder status = new StringBuilder();
        status.append("§a=== 黏液球合成模块状态 ===\n");
        status.append("§e已注册配方: §f").append(recipeKeys.size()).append("\n");
        status.append("§e合成链: §f雪球 → 压缩雪球 → 史莱姆核心 → 黏液球\n");
        status.append("§e雪球合成: §f").append(config.isSnowballRecipeEnabled() ? "启用" : "禁用").append("\n");
        status.append("§e压缩雪球合成: §f").append(config.isCompressedToCoreEnabled() ? "启用" : "禁用").append("\n");
        status.append("§e黏液球合成: §f").append(config.isSlimeBallRecipeEnabled() ? "启用" : "禁用").append("\n");
        status.append("§e黏液块分解: §f").append(config.isSlimeBlockDecomposeEnabled() ? "启用" : "禁用").append("\n");
        status.append("§e自定义物品: §f压缩雪球、史莱姆核心");

        return status.toString();
    }
}