package kuku.plugin.function.rightclick;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import kuku.plugin.function.FunctionPlugin;
import kuku.plugin.function.utils.LogUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * 右键采收农作物模块管理器 - 简化版
 */
public class RightClickHarvestManager implements Listener {

    private final FunctionPlugin plugin;
    private final Logger logger;
    private final RightClickConfig config;
    private final Set<Material> crops = new HashSet<>();
    private final java.util.Map<java.util.UUID, Long> playerCooldowns = new java.util.HashMap<>();
    private final java.util.Set<org.bukkit.Location> processingBlocks = new java.util.HashSet<>();

    public RightClickHarvestManager(FunctionPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.config = new RightClickConfig(plugin);
        initializeCrops();
    }

    /**
     * 初始化农作物
     */
    private void initializeCrops() {
        // 初始化农作物
        for (String cropName : config.getCrops()) {
            try {
                Material material = Material.valueOf(cropName.toUpperCase());
                crops.add(material);
            } catch (IllegalArgumentException e) {
                LogUtils.logWarning(logger, "右键采收",
                        "无效的农作物类型: " + cropName);
            }
        }

        LogUtils.logInfo(logger, "右键采收",
                "初始化完成 - 农作物: " + crops.size() + " 种");
    }

    /**
     * 启用模块
     */
    public void enable() {
        if (!config.isEnabled()) {
            LogUtils.logModuleLoad(logger, "右键采收", false, "模块配置禁用");
            return;
        }

        try {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            LogUtils.logModuleLoad(logger, "右键采收", true, "空手采收农作物");
            LogUtils.logModuleEnable(logger, "右键采收");

            // 记录配置信息
            LogUtils.logFeatureStatus(logger, "显示消息", config.isShowMessage());
            LogUtils.logFeatureStatus(logger, "调试模式", config.isDebug());
            LogUtils.logFeatureStatus(logger, "自动重新种植", config.isAutoReplant());
            LogUtils.logFeatureStatus(logger, "掉落物品", config.isDropItems());
            LogUtils.logValueStatus(logger, "农作物数量", String.valueOf(crops.size()));

        } catch (Exception e) {
            LogUtils.logError(logger, "右键采收模块", "启用失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 禁用模块
     */
    public void disable() {
        LogUtils.logModuleDisable(logger, "右键采收");
        LogUtils.logInfo(logger, "右键采收", "模块已禁用");
    }

    /**
     * 重新加载配置
     */
    public void reload() {
        boolean previousEnabled = config.isEnabled();
        config.reload();

        // 重新初始化农作物
        crops.clear();
        initializeCrops();

        if (config.isEnabled() != previousEnabled) {
            LogUtils.logInfo(logger, "右键采收",
                    "模块状态变更: " + (previousEnabled ? "启用" : "禁用") + " -> " +
                            (config.isEnabled() ? "启用" : "禁用"));
        }

        LogUtils.logSuccess(logger, "右键采收", "配置已重载");
        LogUtils.logValueStatus(logger, "农作物数量", String.valueOf(crops.size()));
    }

    /**
     * 处理右键事件
     */
    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (!config.isEnabled()) return;

        // 只处理右键点击方块
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        // 只处理我们定义的农作物
        if (!crops.contains(clickedBlock.getType())) return;

        Player player = event.getPlayer();
        ItemStack handItem = player.getInventory().getItemInMainHand();

        // 如果玩家手里拿着任何东西，都不触发采收（让骨粉等正常工作）
        if (handItem != null && handItem.getType() != Material.AIR) {
            return;
        }

        // 阻止原版交互（比如收获小麦时弹出小麦物品）
        event.setCancelled(true);

        // 执行采收逻辑
        harvestCrop(clickedBlock, player);
    }

    /**
     * 采收农作物
     */
    private void harvestCrop(Block cropBlock, Player player) {
        // 1. 检查是不是可生长的作物
        if (!(cropBlock.getBlockData() instanceof Ageable)) {
            return;
        }

        Ageable ageable = (Ageable) cropBlock.getBlockData();

        // 2. 严格检查：只有完全成熟的才能被采收
        if (ageable.getAge() < ageable.getMaximumAge()) {
            return; // 未成熟？直接结束，不会生成任何物品。
        }

        // 3. 生成掉落物（直接掉在地上）
        if (config.isDropItems()) {
            Location location = cropBlock.getLocation();
            for (ItemStack drop : cropBlock.getDrops()) {
                cropBlock.getWorld().dropItemNaturally(location, drop.clone());
            }
        }

        // 4. 关键一步：立即重置为未成熟状态
        if (config.isAutoReplant()) {
            ageable.setAge(0); // 重置生长阶段为0
            cropBlock.setBlockData(ageable);

            // 甜浆果丛特殊处理
            if (cropBlock.getType() == Material.SWEET_BERRY_BUSH) {
                ageable.setAge(1);
                cropBlock.setBlockData(ageable);
            }
        }

        // 5. 至此，这个方块已经变回未成熟状态，玩家再怎么右键也不会满足上面的“成熟检查”。
    }

    /**
     * 检查模块是否启用
     */
    public boolean isEnabled() {
        return config.isEnabled();
    }

    /**
     * 获取配置实例
     */
    public RightClickConfig getConfig() {
        return config;
    }

    /**
     * 获取支持的农作物数量
     */
    public int getCropCount() {
        return crops.size();
    }
}