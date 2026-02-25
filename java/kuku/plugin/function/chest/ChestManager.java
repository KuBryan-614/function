package kuku.plugin.function.chest;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.Inventory;
import kuku.plugin.function.utils.LogUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * 儲物箱管理器 - 允許上方有完整方塊時開啟儲物箱
 */
public class ChestManager implements Listener {

    private final JavaPlugin plugin;
    private final Logger logger;
    private final ChestConfig config;
    private boolean enabled;

    // 監聽的方塊類型 - 1.21.10 版本所有容器類方塊
    private static final Set<Material> DEFAULT_CHEST_TYPES = new HashSet<>();

    static {
        // 傳統儲物箱類
        DEFAULT_CHEST_TYPES.add(Material.CHEST);               // 儲物箱
        DEFAULT_CHEST_TYPES.add(Material.TRAPPED_CHEST);       // 陷阱箱
        DEFAULT_CHEST_TYPES.add(Material.ENDER_CHEST);         // 終界箱

        // 1.14+ 新增的容器
        DEFAULT_CHEST_TYPES.add(Material.BARREL);              // 木桶
        DEFAULT_CHEST_TYPES.add(Material.SHULKER_BOX);         // 界伏盒
        DEFAULT_CHEST_TYPES.add(Material.WHITE_SHULKER_BOX);
        DEFAULT_CHEST_TYPES.add(Material.ORANGE_SHULKER_BOX);
        DEFAULT_CHEST_TYPES.add(Material.MAGENTA_SHULKER_BOX);
        DEFAULT_CHEST_TYPES.add(Material.LIGHT_BLUE_SHULKER_BOX);
        DEFAULT_CHEST_TYPES.add(Material.YELLOW_SHULKER_BOX);
        DEFAULT_CHEST_TYPES.add(Material.LIME_SHULKER_BOX);
        DEFAULT_CHEST_TYPES.add(Material.PINK_SHULKER_BOX);
        DEFAULT_CHEST_TYPES.add(Material.GRAY_SHULKER_BOX);
        DEFAULT_CHEST_TYPES.add(Material.LIGHT_GRAY_SHULKER_BOX);
        DEFAULT_CHEST_TYPES.add(Material.CYAN_SHULKER_BOX);
        DEFAULT_CHEST_TYPES.add(Material.PURPLE_SHULKER_BOX);
        DEFAULT_CHEST_TYPES.add(Material.BLUE_SHULKER_BOX);
        DEFAULT_CHEST_TYPES.add(Material.BROWN_SHULKER_BOX);
        DEFAULT_CHEST_TYPES.add(Material.GREEN_SHULKER_BOX);
        DEFAULT_CHEST_TYPES.add(Material.RED_SHULKER_BOX);
        DEFAULT_CHEST_TYPES.add(Material.BLACK_SHULKER_BOX);

        // 1.17+ 新增的容器
        DEFAULT_CHEST_TYPES.add(Material.DISPENSER);           // 發射器
        DEFAULT_CHEST_TYPES.add(Material.DROPPER);             // 投擲器
        DEFAULT_CHEST_TYPES.add(Material.HOPPER);              // 漏斗

        // 1.20+ 新增的容器
        DEFAULT_CHEST_TYPES.add(Material.DECORATED_POT);       // 飾紋陶罐
        DEFAULT_CHEST_TYPES.add(Material.CRAFTER);             // 合成器（1.21新增）

        // 1.21.10 新增的容器類型
        DEFAULT_CHEST_TYPES.add(Material.COPPER_BULB);         // 銅燈泡（可互動）
        DEFAULT_CHEST_TYPES.add(Material.EXPOSED_COPPER_BULB);
        DEFAULT_CHEST_TYPES.add(Material.WEATHERED_COPPER_BULB);
        DEFAULT_CHEST_TYPES.add(Material.OXIDIZED_COPPER_BULB);

        // 其他可儲物的方塊
        DEFAULT_CHEST_TYPES.add(Material.FURNACE);             // 熔爐
        DEFAULT_CHEST_TYPES.add(Material.BLAST_FURNACE);       // 高爐
        DEFAULT_CHEST_TYPES.add(Material.SMOKER);              // 煙燻爐

        DEFAULT_CHEST_TYPES.add(Material.BREWING_STAND);       // 釀造台
        DEFAULT_CHEST_TYPES.add(Material.LECTERN);             // 講台（可放書）
        DEFAULT_CHEST_TYPES.add(Material.CARTOGRAPHY_TABLE);   // 製圖台
        DEFAULT_CHEST_TYPES.add(Material.LOOM);                // 織布機
        DEFAULT_CHEST_TYPES.add(Material.SMITHING_TABLE);      // 鍛造台
        DEFAULT_CHEST_TYPES.add(Material.GRINDSTONE);          // 砂輪
        DEFAULT_CHEST_TYPES.add(Material.STONECUTTER);         // 切石機

        // 1.21.10 可能新增的其他容器（根據實際版本）
        DEFAULT_CHEST_TYPES.add(Material.CHISELED_BOOKSHELF);  // 雕紋書櫃
        DEFAULT_CHEST_TYPES.add(Material.BEEHIVE);             // 蜂箱
        DEFAULT_CHEST_TYPES.add(Material.BEE_NEST);            // 蜂窩
        DEFAULT_CHEST_TYPES.add(Material.RESPAWN_ANCHOR);      // 重生錨
    }

    private Set<Material> cachedChestTypes;

    /**
     * 構造函數
     */
    public ChestManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.config = new ChestConfig(plugin);
        this.enabled = false;
        this.cachedChestTypes = new HashSet<>(DEFAULT_CHEST_TYPES);

        // 初始化時從配置讀取自定義類型
        updateCachedChestTypes();
    }

    private boolean supportsOpeningWithBlockAbove(Material material) {
        // 界伏盒有特殊邏輯，因為它們是實體不是方塊
        if (material.name().contains("SHULKER_BOX")) {
            return config.isAllowShulkerBoxes();
        }

        // 其他容器都支持
        return true;
    }

    /**
     * 啟用管理器
     */
    public void enable() {
        if (enabled) return;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        enabled = true;

        LogUtils.logModuleEnable(logger, "儲物箱管理器");
    }

    /**
     * 禁用管理器
     */
    public void disable() {
        if (!enabled) return;

        // 取消註冊事件監聽器
        PlayerInteractEvent.getHandlerList().unregister(this);
        enabled = false;

        LogUtils.logModuleDisable(logger, "儲物箱管理器");
    }

    /**
     * 重載配置
     */
    public void reload() {
        // 更新緩存的儲物箱類型
        updateCachedChestTypes();

        // 記錄重載
        LogUtils.logSuccess(logger, "儲物箱管理器", "配置已重載");

        // 顯示詳細的配置信息（如果啟用調試）
        if (config.isShowDebugMessages()) {
            LogUtils.logDebug(logger, "儲物箱管理器",
                    "配置詳情: allowOpenWithBlockAbove=" + config.isAllowOpenWithBlockAbove() +
                            ", allowOpenWhenSneaking=" + config.isAllowOpenWhenSneaking() +  // 新增
                            ", handleDoubleChests=" + config.isHandleDoubleChests() +
                            ", 監聽的儲物箱類型數量=" + cachedChestTypes.size());

            // 列出所有監聽的儲物箱類型
            StringBuilder types = new StringBuilder("監聽類型: ");
            for (Material material : cachedChestTypes) {
                types.append(material.name()).append(", ");
            }
            if (cachedChestTypes.size() > 0) {
                types.delete(types.length() - 2, types.length()); // 移除最後的逗號和空格
            }
            LogUtils.logDebug(logger, "儲物箱管理器", types.toString());
        }
    }

    /**
     * 更新緩存的儲物箱類型
     */
    private void updateCachedChestTypes() {
        // 重置為空
        cachedChestTypes = new HashSet<>();

        // 添加基本容器
        addBasicContainers();

        // 根據配置添加特殊容器
        if (config.isIncludeSpecialContainers()) {
            addSpecialContainers();
        }

        // 根據配置添加工作台容器
        if (config.isIncludeWorkstationContainers()) {
            addWorkstationContainers();
        }

        // 添加自定義類型
        addCustomContainers();

        // 記錄日誌
        if (config.isShowDebugMessages()) {
            LogUtils.logDebug(logger, "儲物箱管理器",
                    "已加載 " + cachedChestTypes.size() + " 種容器類型");
        }
    }

    /**
     * 添加基本容器
     */
    private void addBasicContainers() {
        // 傳統儲物箱類
        cachedChestTypes.add(Material.CHEST);
        cachedChestTypes.add(Material.TRAPPED_CHEST);
        cachedChestTypes.add(Material.ENDER_CHEST);
        cachedChestTypes.add(Material.BARREL);

        // 界伏盒（根據配置）
        if (config.isAllowShulkerBoxes()) {
            cachedChestTypes.add(Material.SHULKER_BOX);
            cachedChestTypes.add(Material.WHITE_SHULKER_BOX);
            cachedChestTypes.add(Material.ORANGE_SHULKER_BOX);
            cachedChestTypes.add(Material.MAGENTA_SHULKER_BOX);
            cachedChestTypes.add(Material.LIGHT_BLUE_SHULKER_BOX);
            cachedChestTypes.add(Material.YELLOW_SHULKER_BOX);
            cachedChestTypes.add(Material.LIME_SHULKER_BOX);
            cachedChestTypes.add(Material.PINK_SHULKER_BOX);
            cachedChestTypes.add(Material.GRAY_SHULKER_BOX);
            cachedChestTypes.add(Material.LIGHT_GRAY_SHULKER_BOX);
            cachedChestTypes.add(Material.CYAN_SHULKER_BOX);
            cachedChestTypes.add(Material.PURPLE_SHULKER_BOX);
            cachedChestTypes.add(Material.BLUE_SHULKER_BOX);
            cachedChestTypes.add(Material.BROWN_SHULKER_BOX);
            cachedChestTypes.add(Material.GREEN_SHULKER_BOX);
            cachedChestTypes.add(Material.RED_SHULKER_BOX);
            cachedChestTypes.add(Material.BLACK_SHULKER_BOX);
        }
    }

    /**
     * 添加特殊容器
     */
    private void addSpecialContainers() {
        // 1.21.10 特殊容器
        cachedChestTypes.add(Material.COPPER_BULB);
        cachedChestTypes.add(Material.EXPOSED_COPPER_BULB);
        cachedChestTypes.add(Material.WEATHERED_COPPER_BULB);
        cachedChestTypes.add(Material.OXIDIZED_COPPER_BULB);

        cachedChestTypes.add(Material.DECORATED_POT);
        cachedChestTypes.add(Material.CRAFTER);
        cachedChestTypes.add(Material.CHISELED_BOOKSHELF);
        cachedChestTypes.add(Material.BEEHIVE);
        cachedChestTypes.add(Material.BEE_NEST);
        cachedChestTypes.add(Material.RESPAWN_ANCHOR);
    }

    /**
     * 添加工作台容器
     */
    private void addWorkstationContainers() {
        cachedChestTypes.add(Material.DISPENSER);
        cachedChestTypes.add(Material.DROPPER);
        cachedChestTypes.add(Material.HOPPER);
        cachedChestTypes.add(Material.FURNACE);
        cachedChestTypes.add(Material.BLAST_FURNACE);
        cachedChestTypes.add(Material.SMOKER);
        cachedChestTypes.add(Material.BREWING_STAND);
        cachedChestTypes.add(Material.LECTERN);
        cachedChestTypes.add(Material.CARTOGRAPHY_TABLE);
        cachedChestTypes.add(Material.LOOM);
        cachedChestTypes.add(Material.SMITHING_TABLE);
        cachedChestTypes.add(Material.GRINDSTONE);
        cachedChestTypes.add(Material.STONECUTTER);
    }

    /**
     * 添加自定義容器
     */
    private void addCustomContainers() {
        Set<String> customTypes = config.getCustomChestTypes();
        for (String typeName : customTypes) {
            try {
                Material material = Material.valueOf(typeName.toUpperCase());
                cachedChestTypes.add(material);

                if (config.isShowDebugMessages()) {
                    LogUtils.logDebug(logger, "儲物箱管理器",
                            "添加自定義容器類型: " + typeName);
                }
            } catch (IllegalArgumentException e) {
                LogUtils.logWarning(logger, "儲物箱管理器",
                        "無效的材質名稱: " + typeName);
            }
        }
    }

    /**
     * 獲取配置
     */
    public ChestConfig getConfig() {
        return config;
    }

    /**
     * 檢查是否啟用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 處理玩家互動事件
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 檢查是否為右鍵點擊方塊
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // 檢查點擊的方塊是否為容器
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        Material blockType = clickedBlock.getType();

        if (!isChestType(blockType)) {
            return;
        }

        // 檢查是否支持有上方方塊時開啟
        if (!supportsOpeningWithBlockAbove(blockType)) {
            return;
        }

        // 檢查功能是否啟用
        if (!config.isAllowOpenWithBlockAbove()) {
            return;
        }

        Player player = event.getPlayer();

        // 新增：檢查玩家是否正在嘗試放置方塊
        if (isPlayerTryingToPlaceBlock(event, player)) {
            // 玩家正在嘗試放置方塊，不干預原版行為
            return;
        }

        // 檢查是否是雙儲物箱
        boolean isDoubleChest = false;
        Block otherChestPart = null;
        boolean isDoubleChestPart = false;

        if (config.isHandleDoubleChests() &&
                (blockType == Material.CHEST || blockType == Material.TRAPPED_CHEST)) {

            if (clickedBlock.getBlockData() instanceof Chest) {
                Chest chestData = (Chest) clickedBlock.getBlockData();
                Chest.Type chestType = chestData.getType();

                // 如果是雙儲物箱的一部分
                if (chestType != Chest.Type.SINGLE) {
                    isDoubleChestPart = true;

                    // 找到雙儲物箱的另一部分
                    otherChestPart = getOtherChestPart(clickedBlock, chestData);

                    // 如果是有效的雙儲物箱
                    if (otherChestPart != null) {
                        Material otherType = otherChestPart.getType();
                        if (otherType == Material.CHEST || otherType == Material.TRAPPED_CHEST) {
                            isDoubleChest = true;
                        }
                    }
                }
            }
        }

        // 檢查點擊的箱子是否被遮擋
        boolean clickedHasBlockAbove = hasBlockAbove(clickedBlock);

        // 如果是雙儲物箱，檢查另一部分是否被遮擋
        boolean otherHasBlockAbove = false;
        if (isDoubleChest && otherChestPart != null) {
            otherHasBlockAbove = hasBlockAbove(otherChestPart);
        }

        // 檢查是否需要強制開啟
        boolean shouldForceOpen = false;

        if (isDoubleChestPart) {
            // 雙儲物箱的特殊處理：只要有一部分被遮擋，整個雙儲物箱都無法正常開啟
            // 因此，如果點擊的部分被遮擋，或者另一部分被遮擋，都需要強制開啟
            shouldForceOpen = clickedHasBlockAbove || otherHasBlockAbove;

            if (config.isShowDebugMessages() && shouldForceOpen) {
                LogUtils.logDebug(logger, "儲物箱管理器",
                        "雙儲物箱檢測到遮擋 - 點擊部分: " + (clickedHasBlockAbove ? "遮擋" : "正常") +
                                ", 另一部分: " + (otherHasBlockAbove ? "遮擋" : "正常"));
            }
        } else {
            // 單個容器：只檢查點擊的部分
            shouldForceOpen = clickedHasBlockAbove;
        }

        if (shouldForceOpen) {
            // 如果啟用了安全檢查
            if (config.isEnableSafetyCheck() && !canContainerBeOpenedSafely(clickedBlock, blockType)) {
                if (config.isShowDebugMessages()) {
                    LogUtils.logDebug(logger, "儲物箱管理器",
                            "容器 " + blockType.name() + " 處於不安全狀態，不強制開啟");
                }
                return;
            }

            // 對於某些特殊方塊，需要特殊處理
            handleSpecialContainers(event, clickedBlock, blockType);

            // 強制取消事件（防止原版機制阻止開啟）
            event.setCancelled(true);

            // 重要：直接打開容器界面！
            openContainerForPlayer(player, clickedBlock);

            if (config.isShowDebugMessages()) {
                LogUtils.logDebug(logger, "儲物箱管理器",
                        "已強制開啟" + (isDoubleChest ? "雙" : "") +
                                "儲物箱: " + blockType.name());
            }

            // 播放開啟音效（可選）
            if (config.isShowDebugMessages()) {
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.0f);
            }
        }
    }

    /**
     * 檢查玩家是否正在嘗試放置方塊
     */
    private boolean isPlayerTryingToPlaceBlock(PlayerInteractEvent event, Player player) {
        // 如果玩家潛行
        if (player.isSneaking()) {
            var itemInHand = event.getItem();

            // 檢查手中是否有物品
            if (itemInHand != null) {
                Material material = itemInHand.getType();

                // 檢查是否是容器類方塊（漏斗、發射器、投擲器等）
                if (isContainerBlock(material)) {
                    if (config.isShowDebugMessages()) {
                        LogUtils.logDebug(logger, "儲物箱管理器",
                                "玩家 " + player.getName() + " 潛行手持容器方塊，不干預放置行為: " + material.name());
                    }
                    return true;
                }

                // 檢查是否是普通方塊（非空氣、非工具等）
                if (material.isBlock()) {
                    if (config.isShowDebugMessages()) {
                        LogUtils.logDebug(logger, "儲物箱管理器",
                                "玩家 " + player.getName() + " 潛行手持方塊，不干預放置行為: " + material.name());
                    }
                    return true;
                }
            }

            // 新增配置檢查：是否允許在潛行時強制開啟
            if (!config.isAllowOpenWhenSneaking()) {
                if (config.isShowDebugMessages()) {
                    LogUtils.logDebug(logger, "儲物箱管理器",
                            "玩家 " + player.getName() + " 潛行中且配置不允許強制開啟");
                }
                return true;
            }
        }

        return false;
    }

    /**
     * 檢查材質是否為可放置的容器方塊
     */
    private boolean isContainerBlock(Material material) {
        // 這些是玩家手持時可以放置在箱子上的容器方塊
        switch (material) {
            case HOPPER:
            case DISPENSER:
            case DROPPER:
            case FURNACE:
            case BLAST_FURNACE:
            case SMOKER:
            case BARREL:
            case CHEST:
            case TRAPPED_CHEST:
            case SHULKER_BOX:
            case WHITE_SHULKER_BOX:
            case ORANGE_SHULKER_BOX:
            case MAGENTA_SHULKER_BOX:
            case LIGHT_BLUE_SHULKER_BOX:
            case YELLOW_SHULKER_BOX:
            case LIME_SHULKER_BOX:
            case PINK_SHULKER_BOX:
            case GRAY_SHULKER_BOX:
            case LIGHT_GRAY_SHULKER_BOX:
            case CYAN_SHULKER_BOX:
            case PURPLE_SHULKER_BOX:
            case BLUE_SHULKER_BOX:
            case BROWN_SHULKER_BOX:
            case GREEN_SHULKER_BOX:
            case RED_SHULKER_BOX:
            case BLACK_SHULKER_BOX:
            case CRAFTER:           // 合成器
            case BREWING_STAND:     // 釀造台
            case LECTERN:           // 講台
            case CHISELED_BOOKSHELF: // 雕紋書櫃
            case BEEHIVE:           // 蜂箱
            case BEE_NEST:          // 蜂窩
            case RESPAWN_ANCHOR:    // 重生錨
                return true;
            default:
                return false;
        }
    }

    /**
     * 檢查方塊上方是否有遮擋
     */
    private boolean hasBlockAbove(Block block) {
        Block blockAbove = block.getRelative(BlockFace.UP);
        Material aboveType = blockAbove.getType();

        // 檢查上方方塊是否為空氣或透明方塊
        boolean hasBlockAbove = !aboveType.isAir() && !isTransparentBlock(aboveType);

        if (config.isShowDebugMessages() && hasBlockAbove) {
            LogUtils.logDebug(logger, "儲物箱管理器",
                    "方塊 " + block.getType().name() + " 上方有遮擋: " + aboveType.name());
        }

        return hasBlockAbove;
    }

    /**
     * 檢查方塊是否為透明方塊（兼容性方法）
     */
    private boolean isTransparentBlock(Material material) {
        // 快速檢查：空氣和流體總是透明
        if (material.isAir() ||
                material == Material.WATER ||
                material == Material.LAVA ||
                material.name().contains("WATER") ||
                material.name().contains("LAVA")) {
            return true;
        }

        String name = material.name().toUpperCase();

        // 玻璃類
        if (name.contains("GLASS") || name.contains("STAINED_GLASS")) {
            return true;
        }

        // 植物、裝飾品、紅石元件等
        if (name.contains("VINE") ||
                name.contains("LEAVES") ||
                name.contains("FLOWER") ||
                name.contains("GRASS") ||
                name.contains("FERN") ||
                name.contains("SAPLING") ||
                name.contains("TORCH") ||
                name.contains("REDSTONE") ||
                name.contains("RAIL") ||
                name.contains("SIGN") ||
                name.contains("BANNER") ||
                name.contains("CARPET") ||
                name.contains("SLAB") ||
                name.contains("STAIRS") ||
                name.contains("FENCE_GATE") ||
                name.contains("TRAPDOOR") ||
                name.contains("DOOR") ||
                name.contains("BUTTON") ||
                name.contains("LEVER") ||
                name.contains("PRESSURE_PLATE") ||
                name.contains("BED") ||
                name.contains("BANNER") ||
                name.contains("CHORUS") ||
                name.contains("COBWEB")) {
            return true;
        }

        // 特定透明方塊
        switch (material) {
            case AIR:
            case WATER:
            case LAVA:
            case GLASS:
            case TINTED_GLASS:
            case ICE:
            case PACKED_ICE:
            case BLUE_ICE:
            case FROSTED_ICE:
            case BARRIER:
            case TORCH:
            case WALL_TORCH:
            case REDSTONE_TORCH:
            case REDSTONE_WALL_TORCH:
            case LANTERN:
            case SOUL_LANTERN:
            case SEA_LANTERN:
            case END_ROD:
            case BEACON:
            case CONDUIT:
            case COBWEB:
            case CHORUS_PLANT:
            case CHORUS_FLOWER:
                return true;
            default:
                return false;
        }
    }

    /**
     * 為玩家打開容器
     */
    private void openContainerForPlayer(Player player, Block containerBlock) {
        try {
            // 獲取容器狀態
            BlockState state = containerBlock.getState();

            if (state instanceof Container) {
                Container container = (Container) state;
                Inventory inventory = container.getInventory();

                // 打開容器界面
                player.openInventory(inventory);

                if (config.isShowDebugMessages()) {
                    LogUtils.logDebug(logger, "儲物箱管理器",
                            "成功為玩家 " + player.getName() + " 開啟容器: " + containerBlock.getType().name());
                }
            } else {
                LogUtils.logWarning(logger, "儲物箱管理器",
                        "方塊不是容器類型: " + containerBlock.getType().name());
            }
        } catch (Exception e) {
            LogUtils.logWarning(logger, "儲物箱管理器",
                    "無法開啟容器 " + containerBlock.getType().name() + ": " + e.getMessage());
        }
    }

    /**
     * 處理特殊容器
     */
    private void handleSpecialContainers(PlayerInteractEvent event, Block block, Material type) {
        try {
            // 對於某些特殊容器可能需要額外處理
            switch (type) {
                case DECORATED_POT:
                    // 飾紋陶罐可能需要特殊處理
                    // 例如：檢查是否被刷子清理過
                    break;

                case CRAFTER:
                    // 合成器可能有特殊邏輯
                    // 例如：檢查是否為自動合成模式
                    break;

                case BEEHIVE:
                case BEE_NEST:
                    // 蜂箱/蜂窩需要檢查是否有蜜蜂
                    // 可以檢查方塊實體數據
                    break;

                case RESPAWN_ANCHOR:
                    // 重生錨需要檢查充能等級
                    // 充能等級大於0時才允許互動
                    break;

                case CHISELED_BOOKSHELF:
                    // 雕紋書櫃可以檢查書的數量
                    break;

                case LECTERN:
                    // 講台可以檢查是否有書
                    break;

                default:
                    // 其他容器使用默認處理
                    break;
            }
        } catch (Exception e) {
            if (config.isShowDebugMessages()) {
                LogUtils.logDebug(logger, "儲物箱管理器",
                        "處理特殊容器時發生錯誤: " + e.getMessage());
            }
        }
    }

    /**
     * 檢查容器在上方有方塊的情況下是否可以安全開啟
     * 注意：這個方法只在處理「上方有方塊」的特殊情況時調用
     */
    private boolean canContainerBeOpenedSafely(Block block, Material type) {
        try {
            // 特殊容器的安全檢查（只針對我們要強制開啟的情況）
            switch (type) {
                case RESPAWN_ANCHOR:
                    // 重生錨在末地或地獄開啟充能狀態會爆炸
                    // 這是危險操作，我們不應該強制開啟
                    String worldName = block.getWorld().getName().toLowerCase();
                    if (worldName.contains("the_end") || worldName.contains("nether")) {
                        // 檢查充能等級（簡化版）
                        // 實際應該檢查方塊狀態，但這裡我們保守一點
                        return false;
                    }
                    break;

                case BEEHIVE:
                case BEE_NEST:
                    // 如果蜂箱有蜜蜂，強制開啟可能會激怒蜜蜂
                    // 我們不應該強制開啟有蜜蜂的蜂箱
                    // 注意：這裡需要檢查方塊實體數據
                    break;

                case FURNACE:
                case BLAST_FURNACE:
                case SMOKER:
                    // 熔爐類通常是安全的，可以開啟
                    break;

                default:
                    // 大多數容器都是安全的
                    return true;
            }

            return true;
        } catch (Exception e) {
            if (config.isShowDebugMessages()) {
                LogUtils.logDebug(logger, "儲物箱管理器",
                        "檢查容器安全狀態時發生錯誤: " + e.getMessage());
            }
            // 發生錯誤時保守一點，不強制開啟
            return false;
        }
    }

    /**
     * 檢查是否為儲物箱類型
     */
    private boolean isChestType(Material material) {
        return cachedChestTypes.contains(material);
    }

    /**
     * 獲取雙儲物箱的另一部分
     */
    private Block getOtherChestPart(Block chestBlock, Chest chestData) {
        try {
            Chest.Type type = chestData.getType();
            BlockFace facing = chestData.getFacing();

            if (type == Chest.Type.LEFT) {
                // 左側箱子，右側是另一部分
                return chestBlock.getRelative(getRightDirection(facing));
            } else if (type == Chest.Type.RIGHT) {
                // 右側箱子，左側是另一部分
                return chestBlock.getRelative(getLeftDirection(facing));
            }
        } catch (Exception e) {
            if (config.isShowDebugMessages()) {
                LogUtils.logDebug(logger, "儲物箱管理器",
                        "獲取雙儲物箱另一部分時發生錯誤: " + e.getMessage());
            }
        }

        return null;
    }

    /**
     * 獲取右側方向
     */
    private BlockFace getRightDirection(BlockFace facing) {
        switch (facing) {
            case NORTH: return BlockFace.WEST;
            case SOUTH: return BlockFace.EAST;
            case EAST: return BlockFace.NORTH;
            case WEST: return BlockFace.SOUTH;
            default: return facing;
        }
    }

    /**
     * 獲取左側方向
     */
    private BlockFace getLeftDirection(BlockFace facing) {
        switch (facing) {
            case NORTH: return BlockFace.EAST;
            case SOUTH: return BlockFace.WEST;
            case EAST: return BlockFace.SOUTH;
            case WEST: return BlockFace.NORTH;
            default: return facing;
        }
    }
}