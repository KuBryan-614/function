package kuku.plugin.function.elevator;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.block.Action;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ElevatorListener implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Long> lastUsed = new HashMap<>();
    private final long cooldownMs;
    private final int searchRange;
    private final boolean jumpUp;
    private final boolean sneakDown;
    private final boolean sneakClickInfo;
    private final boolean showMessage;
    private final boolean soundEnabled;
    private final boolean safetyCheck;

    public ElevatorListener(JavaPlugin plugin, long cooldownMs, int searchRange,
                            boolean jumpUp, boolean sneakDown, boolean sneakClickInfo,
                            boolean showMessage, boolean soundEnabled, boolean safetyCheck) {
        this.plugin = plugin;
        this.cooldownMs = cooldownMs;
        this.searchRange = searchRange;
        this.jumpUp = jumpUp;
        this.sneakDown = sneakDown;
        this.sneakClickInfo = sneakClickInfo;
        this.showMessage = showMessage;
        this.soundEnabled = soundEnabled;
        this.safetyCheck = safetyCheck;
    }

    // ========== 1. 處理跳躍上樓 ==========
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!jumpUp) return;

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        // 檢測玩家是否跳躍
        boolean isOnGround = player.isOnGround();

        // 檢查玩家是否剛跳起來
        if (!isOnGround && player.getVelocity().getY() > 0.1) {
            // 檢查玩家腳下的方塊（站在羊毛上）
            Block standingBlock = player.getLocation().subtract(0, 0.1, 0).getBlock();

            // 檢查是否站在電梯羊毛上
            Block elevatorSign = findElevatorSignAbove(standingBlock);
            if (elevatorSign != null) {
                // 觸發向上移動
                handleElevatorMove(player, elevatorSign, true);
            }
        }
    }

    // ========== 2. 處理蹲下下樓 ==========
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
        if (!sneakDown) return;

        Player player = e.getPlayer();

        // 只處理開始蹲下的瞬間
        if (!e.isSneaking()) return;

        // 檢查玩家腳下的方塊（站在羊毛上）
        Block standingBlock = player.getLocation().subtract(0, 0.1, 0).getBlock();

        // 檢查是否站在電梯羊毛上
        Block elevatorSign = findElevatorSignAbove(standingBlock);
        if (elevatorSign != null) {
            // 觸發向下移動
            handleElevatorMove(player, elevatorSign, false);
        }
    }

    // ========== 3. 處理蹲下左鍵點擊告示牌顯示樓層 ==========
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!sneakClickInfo) return;

        // 只處理左鍵點擊方塊事件
        if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = e.getPlayer();

        // 檢查玩家是否蹲下
        if (!player.isSneaking()) return;

        Block clickedBlock = e.getClickedBlock();
        if (clickedBlock == null) return;

        // 檢查是否為告示牌
        if (!(clickedBlock.getState() instanceof Sign)) return;

        Sign sign = (Sign) clickedBlock.getState();

        // 檢查是否為電梯告示牌
        if (!sign.getLine(0).trim().equalsIgnoreCase("[電梯]")) return;

        // 檢查下方是否為羊毛
        Block woolBelow = clickedBlock.getRelative(0, -1, 0);
        if (!woolBelow.getType().name().endsWith("_WOOL")) return;

        // 防止破壞告示牌
        e.setCancelled(true);

        // 檢查冷卻時間
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (lastUsed.containsKey(uuid)) {
            long timeSinceLastUse = now - lastUsed.get(uuid);
            if (timeSinceLastUse < cooldownMs) {
                // 不顯示訊息，避免打擾玩家
                return;
            }
        }

        // 更新冷卻時間
        lastUsed.put(uuid, now);

        // 顯示所有樓層信息
        showAllFloorInfo(player, clickedBlock);
    }

    /**
     * 查找羊毛上方的電梯告示牌
     */
    private Block findElevatorSignAbove(Block standingBlock) {
        // 使用新的平台檢測器
        Block signBlock = WoolPlatform.findElevatorSignOnPlatform(standingBlock);

        if (signBlock == null) {
            return null;
        }

        // 檢查是否為電梯告示牌
        if (!(signBlock.getState() instanceof Sign)) {
            return null;
        }

        Sign sign = (Sign) signBlock.getState();
        if (!sign.getLine(0).trim().equalsIgnoreCase("[電梯]")) {
            return null;
        }

        return signBlock;
    }

    /**
     * 處理電梯移動（跳躍上樓或蹲下下樓）
     */
    private void handleElevatorMove(Player player, Block elevatorSign, boolean moveUp) {
        // 檢查冷卻時間
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (lastUsed.containsKey(uuid)) {
            long timeSinceLastUse = now - lastUsed.get(uuid);
            if (timeSinceLastUse < cooldownMs) {
                return;
            }
        }

        // 檢查平台下方是否有足夠空間
        if (!checkPlatformSafety(elevatorSign, moveUp)) {
            if (showMessage) {
                player.sendMessage(ChatColor.RED + "[電梯] " + (moveUp ? "上方" : "下方") + "空間不足，無法使用電梯");
            }
            return;
        }

        // 更新冷卻時間
        lastUsed.put(uuid, now);

        // 尋找目標樓層
        Block targetFloor = findTargetFloor(elevatorSign, moveUp);

        if (targetFloor == null) {
            String direction = moveUp ? "上方" : "下方";
            if (showMessage) {
                player.sendMessage(ChatColor.RED + "[電梯] 找不到" + direction + "樓層");
            }
            return;
        }

        // 傳送到目標樓層
        if (teleportToFloor(player, targetFloor)) {
            // 播放音效
            if (soundEnabled) {
                playElevatorSound(player, moveUp);
            }

            // 顯示樓層訊息
            if (showMessage) {
                sendFloorMessage(player, targetFloor, moveUp);
            }
        }
    }

    /**
     * 檢查平台安全（檢查整個平台的空間）
     */
    private boolean checkPlatformSafety(Block signBlock, boolean moveUp) {
        if (!safetyCheck) return true;

        // 找出平台的所有羊毛方塊
        Block woolBelow = signBlock.getRelative(0, -1, 0);
        Set<Block> platformBlocks = new HashSet<>();
        WoolPlatform.findConnectedWoolBlocks(woolBelow, platformBlocks, 9);

        // 檢查每個羊毛方塊上方的空間
        for (Block block : platformBlocks) {
            Block checkSpace = block.getRelative(0, moveUp ? 1 : -1, 0);
            if (checkSpace != null && !isPassable(checkSpace)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 顯示所有樓層信息（蹲下左鍵點擊時觸發）
     */
    private void showAllFloorInfo(Player player, Block clickedSign) {
        // 獲取所有樓層
        List<Block> allFloors = findAllFloors(clickedSign);

        if (allFloors.isEmpty()) {
            player.sendMessage(ChatColor.RED + "[電梯] 未找到任何樓層");
            return;
        }

        // 排序樓層（按Y軸）
        allFloors.sort(Comparator.comparingInt(Block::getY));

        // 找到當前樓層索引
        int currentIndex = -1;
        for (int i = 0; i < allFloors.size(); i++) {
            if (allFloors.get(i).equals(clickedSign)) {
                currentIndex = i;
                break;
            }
        }

        // 獲取點擊的告示牌信息
        Sign sign = (Sign) clickedSign.getState();
        String currentFloorName = sign.getLine(1).trim();
        String currentFloorDesc = sign.getLine(2).trim();

        if (currentFloorName.isEmpty()) {
            currentFloorName = "Y=" + clickedSign.getY();
        }

        // 顯示標題
        player.sendMessage(ChatColor.GOLD + "═══════ [ 電梯樓層總覽 ] ═══════");
        player.sendMessage(ChatColor.YELLOW + "電梯編號: " + ChatColor.WHITE +
                clickedSign.getX() + ", " + clickedSign.getZ());
        player.sendMessage(ChatColor.YELLOW + "總樓層數: " + ChatColor.WHITE + allFloors.size() + " 層");

        // 顯示當前樓層
        player.sendMessage(ChatColor.GREEN + "● 當前樓層 (" + (currentIndex + 1) + "/" + allFloors.size() + "):");
        player.sendMessage(ChatColor.WHITE + "  " + currentFloorName);
        if (!currentFloorDesc.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "  " + currentFloorDesc);
        }
        player.sendMessage(ChatColor.DARK_GRAY + "  座標: Y=" + clickedSign.getY());

        // 顯示所有樓層列表
        player.sendMessage(ChatColor.YELLOW + "├─ 樓層列表 ──");

        for (int i = 0; i < allFloors.size(); i++) {
            Block floor = allFloors.get(i);
            Sign floorSign = (Sign) floor.getState();

            String floorName = floorSign.getLine(1).trim();
            if (floorName.isEmpty()) floorName = "Y=" + floor.getY();

            String floorDesc = floorSign.getLine(2).trim();

            // 判斷是否為當前樓層
            if (i == currentIndex) {
                player.sendMessage(ChatColor.GREEN + "├ " + (i + 1) + ". " + floorName +
                        ChatColor.DARK_GREEN + " ← 當前");
                if (!floorDesc.isEmpty()) {
                    player.sendMessage(ChatColor.GRAY + "│   " + floorDesc);
                }
            } else {
                // 計算樓層差距
                int floorDiff = floor.getY() - clickedSign.getY();
                String diffText = floorDiff > 0 ?
                        ChatColor.GREEN + "(上" + floorDiff + "層)" :
                        ChatColor.RED + "(下" + Math.abs(floorDiff) + "層)";

                player.sendMessage(ChatColor.WHITE + "├ " + (i + 1) + ". " + floorName + " " +
                        ChatColor.DARK_GRAY + "Y=" + floor.getY() + " " + diffText);

                if (!floorDesc.isEmpty()) {
                    player.sendMessage(ChatColor.GRAY + "│   " + floorDesc);
                }
            }
        }

        player.sendMessage(ChatColor.YELLOW + "└─────────────");

        // 顯示操作提示
        player.sendMessage(ChatColor.GOLD + "操作說明:");
        player.sendMessage(ChatColor.WHITE + "  • 站在電梯上 " + ChatColor.GREEN + "跳躍" +
                ChatColor.WHITE + " → 上樓");
        player.sendMessage(ChatColor.WHITE + "  • 站在電梯上 " + ChatColor.RED + "蹲下" +
                ChatColor.WHITE + " → 下樓");
        player.sendMessage(ChatColor.WHITE + "  • 蹲下 + " + ChatColor.YELLOW + "左鍵點擊告示牌" +
                ChatColor.WHITE + " → 顯示樓層訊息");

        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════");

        // 播放提示音效
        if (soundEnabled) {
            playInfoSound(player);
        }
    }

    /**
     * 尋找目標樓層
     */
    private Block findTargetFloor(Block currentSign, boolean moveUp) {
        List<Block> allFloors = findAllFloors(currentSign);
        if (allFloors.isEmpty()) return null;

        // 排序樓層（按Y軸）
        allFloors.sort(Comparator.comparingInt(Block::getY));

        int currentIndex = -1;
        for (int i = 0; i < allFloors.size(); i++) {
            if (allFloors.get(i).equals(currentSign)) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1) return null;

        if (moveUp) {
            // 向上移動：找下一個更高的樓層
            for (int i = currentIndex + 1; i < allFloors.size(); i++) {
                Block floor = allFloors.get(i);
                if (isValidTeleportLocation(floor)) {
                    return floor;
                }
            }
        } else {
            // 向下移動：找下一個更低的樓層
            for (int i = currentIndex - 1; i >= 0; i--) {
                Block floor = allFloors.get(i);
                if (isValidTeleportLocation(floor)) {
                    return floor;
                }
            }
        }

        return null;
    }

    /**
     * 查找所有電梯樓層
     */
    private List<Block> findAllFloors(Block currentSign) {
        List<Block> floors = new ArrayList<>();
        org.bukkit.World world = currentSign.getWorld();

        int x = currentSign.getX();
        int z = currentSign.getZ();

        // ===== 計算搜尋高度 =====
        int minWorldY = world.getMinHeight();

        // 預設：需要兩格空間（腳 + 頭）
        int maxWorldY = world.getMaxHeight() - 2;

        // Nether 特例：允許搜尋到最頂基岩層
        if (world.getEnvironment() == org.bukkit.World.Environment.NETHER) {
            maxWorldY = world.getMaxHeight();
        }

        int minY = Math.max(minWorldY, currentSign.getY() - searchRange);
        int maxY = Math.min(maxWorldY, currentSign.getY() + searchRange);

        // ===== 掃描樓層 =====
        for (int y = minY; y <= maxY; y++) {
            Block checkBlock = world.getBlockAt(x, y, z);

            // 必須是告示牌
            if (!(checkBlock.getState() instanceof Sign)) continue;

            Sign sign = (Sign) checkBlock.getState();

            // 第一行必須是 [電梯]
            if (!sign.getLine(0).trim().equalsIgnoreCase("[電梯]")) continue;

            // 下方必須是羊毛
            Block woolBelow = checkBlock.getRelative(0, -1, 0);
            if (!woolBelow.getType().name().endsWith("_WOOL")) continue;

            floors.add(checkBlock);
        }

        return floors;
    }

    /**
     * 檢查是否為有效的傳送位置
     */
    private boolean isValidTeleportLocation(Block signBlock) {
        // 檢查上方是否有足夠空間（兩格）
        Block space1 = signBlock.getRelative(0, 1, 0);
        Block space2 = signBlock.getRelative(0, 2, 0);

        return isPassable(space1) && isPassable(space2);
    }

    /**
     * 檢查方塊是否可通過
     */
    private boolean isPassable(Block block) {
        Material type = block.getType();
        return type == Material.AIR ||
                type == Material.CAVE_AIR ||
                type == Material.VOID_AIR ||
                type == Material.WATER ||
                type == Material.LAVA ||
                block.isPassable();
    }

    /**
     * 傳送到樓層
     */
    private boolean teleportToFloor(Player player, Block targetSign) {
        try {
            // 找出目標樓層的平台
            Block targetWool = targetSign.getRelative(0, -1, 0);
            Set<Block> platformBlocks = new HashSet<>();
            WoolPlatform.findConnectedWoolBlocks(targetWool, platformBlocks, 9);

            if (platformBlocks.isEmpty()) {
                return false;
            }

            // 找出平台的中心點
            int minX = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxZ = Integer.MIN_VALUE;
            int y = targetWool.getY();

            for (Block block : platformBlocks) {
                int x = block.getX();
                int z = block.getZ();
                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
                if (z < minZ) minZ = z;
                if (z > maxZ) maxZ = z;
            }

            // 計算平台中心座標
            double centerX = (minX + maxX + 1) / 2.0;
            double centerZ = (minZ + maxZ + 1) / 2.0;

            Location targetLoc = new Location(targetSign.getWorld(), centerX, y + 1.0, centerZ);

            // 保持玩家原有的朝向
            targetLoc.setYaw(player.getLocation().getYaw());
            targetLoc.setPitch(player.getLocation().getPitch());

            // 安全檢查
            if (safetyCheck && !isSafeLocation(targetLoc)) {
                if (showMessage) {
                    player.sendMessage(ChatColor.RED + "[電梯] 目標位置不安全");
                }
                return false;
            }

            // 執行傳送
            player.teleport(targetLoc);
            return true;

        } catch (Exception e) {
            plugin.getLogger().warning("傳送玩家時發生錯誤: " + e.getMessage());
            if (showMessage) {
                player.sendMessage(ChatColor.RED + "[電梯] 傳送失敗");
            }
            return false;
        }
    }

    /**
     * 檢查位置是否安全
     */
    private boolean isSafeLocation(Location loc) {
        World world = loc.getWorld();

        Block feet = loc.getBlock();
        Block head = loc.clone().add(0, 1, 0).getBlock();
        Block ground = feet.getRelative(0, -1, 0);

        // 腳下必須是實體（基岩 OK）
        if (!ground.getType().isSolid()) return false;

        // Nether 頂部特例
        if (world.getEnvironment() == World.Environment.NETHER) {
            if (feet.getY() >= world.getMaxHeight()) {
                return true;
            }
        }

        // 一般世界
        return feet.getType().isAir() && head.getType().isAir();
    }

    /**
     * 發送樓層訊息（傳送時顯示）
     */
    private void sendFloorMessage(Player player, Block signBlock, boolean movedUp) {
        if (!(signBlock.getState() instanceof Sign)) return;

        Sign sign = (Sign) signBlock.getState();
        String floorName = sign.getLine(1).trim();    // 第2行：樓層名稱
        String floorDesc = sign.getLine(2).trim();    // 第3行：樓層描述

        // 如果樓層名稱為空，使用默認格式
        if (floorName.isEmpty()) {
            floorName = "Y=" + signBlock.getY();
        }

        String direction = movedUp ? "上樓" : "下樓";
        ChatColor directionColor = movedUp ? ChatColor.GREEN : ChatColor.RED;

        player.sendMessage(ChatColor.GOLD + "⚡ [電梯] " + directionColor + direction);
        player.sendMessage(ChatColor.YELLOW + "樓層: " + ChatColor.WHITE + floorName);

        if (!floorDesc.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "  " + floorDesc);
        }
    }

    /**
     * 播放電梯音效（傳送時）
     */
    private void playElevatorSound(Player player, boolean moveUp) {
        try {
            Sound sound;
            if (moveUp) {
                // 上樓音效 - 更高音調
                sound = Sound.valueOf("BLOCK_NOTE_BLOCK_BELL");
                player.playSound(player.getLocation(), sound, 0.8f, 1.5f);
            } else {
                // 下樓音效 - 更低音調
                sound = Sound.valueOf("BLOCK_NOTE_BLOCK_BELL");
                player.playSound(player.getLocation(), sound, 0.8f, 0.8f);
            }
        } catch (Exception e) {
            try {
                // 備用音效
                Sound sound = Sound.valueOf("ENTITY_EXPERIENCE_ORB_PICKUP");
                player.playSound(player.getLocation(), sound, 0.5f, moveUp ? 1.2f : 0.8f);
            } catch (Exception ignored) {}
        }
    }

    /**
     * 播放提示音效（顯示樓層信息時）
     */
    private void playInfoSound(Player player) {
        try {
            Sound sound = Sound.valueOf("BLOCK_NOTE_BLOCK_PLING");
            player.playSound(player.getLocation(), sound, 0.5f, 1.0f);
        } catch (Exception e) {
            try {
                Sound sound = Sound.valueOf("NOTE_PLING");
                player.playSound(player.getLocation(), sound, 0.5f, 1.0f);
            } catch (Exception ignored) {}
        }
    }
}