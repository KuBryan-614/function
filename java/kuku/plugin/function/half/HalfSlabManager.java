package kuku.plugin.function.half;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector; // 明确的 Bukkit Vector 导入
import org.bukkit.util.RayTraceResult;
import kuku.plugin.function.FunctionPlugin;
import kuku.plugin.function.utils.LogUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 半砖单格破坏管理器 (Spigot API 版本)
 * 功能: 潜行时破坏双层半砖只破坏其中一格
 */
public class HalfSlabManager implements Listener {

    private final FunctionPlugin plugin;
    private HalfSlabConfig config;
    private boolean enabled = false;
    private final Map<Material, Material> slabMaterialCache = new HashMap<>();

    // 存储玩家上次操作时间，用于防止快速连续点击导致的问题（非冷却）
    private final Map<UUID, Long> lastOperationTime = new HashMap<>();
    private static final long MIN_OPERATION_INTERVAL = 100L; // 100毫秒

    public HalfSlabManager(FunctionPlugin plugin) {
        this.plugin = plugin;
        this.config = new HalfSlabConfig(plugin);
    }

    /**
     * 启用模块
     */
    public void enable() {
        if (enabled) return;

        if (!config.isEnabled()) {
            LogUtils.logModuleLoad(plugin.getLogger(), "半砖单格破坏", false, "模块禁用");
            return;
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        enabled = true;
        LogUtils.logModuleLoad(plugin.getLogger(), "半砖单格破坏", true, "已启用 (Spigot API)");
    }

    /**
     * 禁用模块
     */
    public void disable() {
        if (!enabled) return;
        BlockBreakEvent.getHandlerList().unregister(this);
        slabMaterialCache.clear();
        lastOperationTime.clear();
        enabled = false;
        LogUtils.logModuleLoad(plugin.getLogger(), "半砖单格破坏", false, "已禁用");
    }

    /**
     * 重载模块配置
     */
    public void reload() {
        config.reload();
        slabMaterialCache.clear();

        if (config.isEnabled() && !enabled) {
            enable();
        } else if (!config.isEnabled() && enabled) {
            disable();
        }
        plugin.getLogger().info("§a[半砖单格破坏] 配置已重载");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!enabled) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();

        // 权限检查
        if (config.isPermissionEnabled() && !player.hasPermission(config.getRequiredPermission())) {
            return;
        }

        // 检查是否为潜行状态和半砖
        if (!player.isSneaking() || !isValidSlab(block)) {
            return;
        }

        Slab slab = (Slab) block.getBlockData();
        if (slab.getType() != Slab.Type.DOUBLE) {
            return;
        }

        // 防止快速连续操作（非冷却，仅为防止刷物品/卡顿）
        long currentTime = System.currentTimeMillis();
        UUID playerId = player.getUniqueId();

        if (lastOperationTime.containsKey(playerId)) {
            long timeSinceLastOp = currentTime - lastOperationTime.get(playerId);
            if (timeSinceLastOp < MIN_OPERATION_INTERVAL) {
                return;
            }
        }
        lastOperationTime.put(playerId, currentTime);

        // 取消原事件并开始处理
        event.setCancelled(true);
        Slab.Type halfToBreak = getTargetedHalf(player, block);

        if (halfToBreak != null) {
            processSlabBreak(player, block, slab, halfToBreak);
        }
    }

    /**
     * 处理半砖破坏逻辑
     */
    private void processSlabBreak(Player player, Block block, Slab slab, Slab.Type halfToBreak) {
        // 更新方块为剩余的一半
        Slab.Type remainingHalf = (halfToBreak == Slab.Type.TOP) ? Slab.Type.BOTTOM : Slab.Type.TOP;
        slab.setType(remainingHalf);
        block.setBlockData(slab);

        // 播放效果和给予掉落物
        playBreakEffects(block, player);
        dropSlabItem(block, player);

        // 发送提示消息
        if (config.isShowMessage()) {
            String side = halfToBreak == Slab.Type.TOP ? "上半部分" : "下半部分";
            player.sendActionBar(ChatColor.GREEN + "已破坏半砖的" + side);
        }
    }

    /**
     * 根据玩家准心判断要破坏哪一半（改进版）
     */
    private Slab.Type getTargetedHalf(Player player, Block slabBlock) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection(); // 这里使用 org.bukkit.util.Vector

        RayTraceResult rayTrace = slabBlock.getWorld().rayTraceBlocks(
                eyeLocation,
                direction,
                config.getRayTraceDistance(),
                FluidCollisionMode.NEVER,
                true
        );

        if (rayTrace == null || rayTrace.getHitBlock() == null ||
                !rayTrace.getHitBlock().equals(slabBlock)) {
            return Slab.Type.TOP; // 默认破坏上半部分
        }

        BlockFace hitFace = rayTrace.getHitBlockFace();
        Vector hitPosition = rayTrace.getHitPosition(); // 这里使用 org.bukkit.util.Vector

        // 根据击中面精确判断
        switch (hitFace) {
            case UP:
                return Slab.Type.TOP;
            case DOWN:
                return Slab.Type.BOTTOM;
            case NORTH:
            case SOUTH:
            case EAST:
            case WEST:
                // 对于垂直面，根据Y坐标判断
                double relativeY = hitPosition.getY() - slabBlock.getY();
                return relativeY > 0.5 ? Slab.Type.TOP : Slab.Type.BOTTOM;
            default:
                return Slab.Type.TOP; // 默认
        }
    }

    /**
     * 检查方块是否为有效半砖
     */
    private boolean isValidSlab(Block block) {
        if (!(block.getBlockData() instanceof Slab)) {
            return false;
        }

        Material material = block.getType();
        String materialName = material.name();

        // 检查排除列表
        if (config.getExcludedMaterials().contains(materialName)) {
            return false;
        }

        // 检查支持列表
        List<String> supported = config.getSupportedSlabs();
        return supported.isEmpty() || supported.contains(materialName);
    }

    /**
     * 播放破坏效果（使用 Spigot SoundGroup API 确保音效准确）
     */
    private void playBreakEffects(Block block, Player player) {
        try {
            if (!config.isPlayEffects()) return;

            Location location = block.getLocation().add(0.5, 0.5, 0.5);
            Material material = block.getType();

            // 1. 播放方块破坏粒子
            block.getWorld().spawnParticle(
                    Particle.BLOCK,
                    location,
                    config.getParticleCount(),
                    0.3, 0.3, 0.3,
                    0.1,
                    block.getBlockData()
            );

            // 2. 使用 Spigot SoundGroup API 获取准确的原版破坏音效
            // 这是解决音效不匹配问题的核心
            SoundGroup soundGroup = material.createBlockData().getSoundGroup();
            Sound breakSound = soundGroup.getBreakSound();

            // 播放音效（使用原版音效组中的音量和音高）
            block.getWorld().playSound(
                    location,
                    breakSound,
                    soundGroup.getVolume(),
                    soundGroup.getPitch()
            );

            // 3. 发送方块破坏动画（STEP_SOUND 效果）
            player.playEffect(block.getLocation(), Effect.STEP_SOUND, material);

        } catch (Exception e) {
            plugin.getLogger().warning("播放半砖破坏效果时发生错误: " + e.getMessage());
            // 即使出错也不影响主要功能
        }
    }

    /**
     * 给予半砖掉落物
     */
    private void dropSlabItem(Block block, Player player) {
        try {
            if (!config.isDropItems() || player.getGameMode() == GameMode.CREATIVE) {
                return;
            }

            Material slabMaterial = getSingleSlabMaterial(block.getType());
            if (slabMaterial != null && slabMaterial.isItem()) {
                ItemStack itemToDrop = new ItemStack(slabMaterial, 1);
                block.getWorld().dropItemNaturally(
                        block.getLocation().add(0.5, 0.5, 0.5),
                        itemToDrop
                );
            }
        } catch (Exception e) {
            plugin.getLogger().warning("给予半砖掉落物时发生错误: " + e.getMessage());
        }
    }

    /**
     * 将双层半砖材料转换为单层半砖材料（支持多种命名格式）
     */
    private Material getSingleSlabMaterial(Material doubleSlab) {
        // 检查缓存
        if (slabMaterialCache.containsKey(doubleSlab)) {
            return slabMaterialCache.get(doubleSlab);
        }

        Material singleSlab = null;
        String name = doubleSlab.name();

        // 情况1：已经是单层半砖（直接返回）
        if (name.endsWith("_SLAB") && !name.contains("DOUBLE")) {
            singleSlab = doubleSlab;
        }
        // 情况2：带有 DOUBLE 前缀的旧格式
        else if (name.startsWith("DOUBLE_")) {
            String baseName = name.substring(7); // 移除 "DOUBLE_"
            singleSlab = findSlabVariant(baseName);
        }
        // 情况3：其他可能的双半砖名称
        else {
            // 尝试移除可能的 "DOUBLE" 部分
            String cleanedName = name
                    .replace("DOUBLE_", "")
                    .replace("_DOUBLE", "");
            singleSlab = findSlabVariant(cleanedName);
        }

        // 缓存结果（即使为 null 也缓存，避免重复查找）
        slabMaterialCache.put(doubleSlab, singleSlab);
        return singleSlab;
    }

    /**
     * 查找半砖变体
     */
    private Material findSlabVariant(String baseName) {
        // 优先尝试直接匹配
        Material directMatch = Material.getMaterial(baseName);
        if (directMatch != null) {
            return directMatch;
        }

        // 尝试添加 _SLAB 后缀
        if (!baseName.endsWith("_SLAB")) {
            Material withSlabSuffix = Material.getMaterial(baseName + "_SLAB");
            if (withSlabSuffix != null) {
                return withSlabSuffix;
            }
        }

        // 尝试常见的半砖命名模式
        String[] commonPrefixes = {"", "SMOOTH_", "POLISHED_", "CUT_", "WAXED_"};
        String[] commonSuffixes = {"", "_SLAB"};

        for (String prefix : commonPrefixes) {
            for (String suffix : commonSuffixes) {
                String variantName = prefix + baseName + suffix;
                Material variant = Material.getMaterial(variantName);
                if (variant != null) {
                    return variant;
                }
            }
        }

        return null;
    }

    /**
     * 检查模块是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 获取配置实例
     */
    public HalfSlabConfig getConfig() {
        return config;
    }

    /**
     * 清理缓存
     */
    public void clearCaches() {
        slabMaterialCache.clear();
        lastOperationTime.clear();
    }

    /**
     * 为开发者提供的调试方法
     */
    public void debugSoundInfo(Player player, Block block) {
        if (!player.isOp()) return;

        try {
            Material material = block.getType();
            SoundGroup soundGroup = material.createBlockData().getSoundGroup();

            player.sendMessage(ChatColor.GOLD + "=== 方块音效调试信息 ===");
            player.sendMessage(ChatColor.YELLOW + "方块: " + ChatColor.WHITE + material.name());
            player.sendMessage(ChatColor.YELLOW + "破坏音效: " + ChatColor.WHITE + soundGroup.getBreakSound().name());
            player.sendMessage(ChatColor.YELLOW + "音量: " + ChatColor.WHITE + soundGroup.getVolume());
            player.sendMessage(ChatColor.YELLOW + "音高: " + ChatColor.WHITE + soundGroup.getPitch());

            // 测试音效
            player.playSound(player.getLocation(), soundGroup.getBreakSound(), soundGroup.getVolume(), soundGroup.getPitch());

        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "调试错误: " + e.getMessage());
        }
    }
}