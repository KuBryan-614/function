package kuku.plugin.function.elevator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.World;
import java.util.HashSet;
import java.util.Set;

/**
 * 羊毛平台檢測器
 */
public class WoolPlatform {

    /**
     * 檢查給定的方塊是否在一個有效的羊毛平台上
     * 並返回該平台的告示牌方塊
     *
     * @param startBlock 開始檢測的方塊
     * @return 平台上的告示牌方塊，如果無效則返回null
     */
    public static Block findElevatorSignOnPlatform(Block startBlock) {
        // 檢查起始方塊是否為羊毛
        if (!isWool(startBlock.getType())) {
            return null;
        }

        int startX = startBlock.getX();
        int startY = startBlock.getY();
        int startZ = startBlock.getZ();
        World world = startBlock.getWorld();

        // 找出相連的羊毛方塊
        Set<Block> platformBlocks = new HashSet<>();
        findConnectedWoolBlocks(startBlock, platformBlocks, 25); // 限制最大數量避免無限遞歸

        // 檢查平台大小是否為 1x1, 2x2, 或 3x3
        int size = platformBlocks.size();
        if (size != 1 && size != 4 && size != 9) {
            return null;
        }

        // 檢查是否為矩形平台
        if (!isRectangularPlatform(platformBlocks)) {
            return null;
        }

        // 找出平台上方 (Y+1) 的告示牌
        return findSignAbovePlatform(platformBlocks, world);
    }

    /**
     * 找出所有相連的羊毛方塊
     */
    public static void findConnectedWoolBlocks(Block block, Set<Block> visited, int maxDepth) {
        if (visited.size() >= maxDepth) return;

        if (!isWool(block.getType()) || visited.contains(block)) {
            return;
        }

        visited.add(block);

        // 檢查周圍八個方向的方塊（同一高度）
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;

                Block neighbor = block.getRelative(dx, 0, dz);
                if (isWool(neighbor.getType()) && !visited.contains(neighbor)) {
                    findConnectedWoolBlocks(neighbor, visited, maxDepth);
                }
            }
        }
    }

    /**
     * 檢查平台是否為矩形
     */
    private static boolean isRectangularPlatform(Set<Block> blocks) {
        if (blocks.isEmpty()) return false;

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;

        // 找出平台邊界
        for (Block block : blocks) {
            int x = block.getX();
            int z = block.getZ();
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (z < minZ) minZ = z;
            if (z > maxZ) maxZ = z;
        }

        int width = maxX - minX + 1;
        int depth = maxZ - minZ + 1;

        // 檢查尺寸是否為 1x1, 2x2, 或 3x3
        if ((width != depth) || (width != 1 && width != 2 && width != 3)) {
            return false;
        }

        // 檢查矩形區域內的所有方塊是否都是羊毛
        World world = blocks.iterator().next().getWorld();
        int y = blocks.iterator().next().getY();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                Block checkBlock = world.getBlockAt(x, y, z);
                if (!isWool(checkBlock.getType())) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 在平台上方尋找告示牌
     */
    private static Block findSignAbovePlatform(Set<Block> platformBlocks, World world) {
        if (platformBlocks.isEmpty()) return null;

        // 找出平台邊界
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        int y = platformBlocks.iterator().next().getY();

        for (Block block : platformBlocks) {
            int x = block.getX();
            int z = block.getZ();
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (z < minZ) minZ = z;
            if (z > maxZ) maxZ = z;
        }

        // 在平台上方 (Y+1) 的區域內尋找告示牌
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                Block block = world.getBlockAt(x, y + 1, z);
                if (block.getState() instanceof org.bukkit.block.Sign) {
                    return block;
                }
            }
        }

        return null;
    }

    /**
     * 檢查材質是否為羊毛
     */
    private static boolean isWool(Material material) {
        return material.name().endsWith("_WOOL");
    }
}