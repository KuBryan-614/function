package kuku.plugin.function.craftNewTable;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

/**
 * 合成事件处理器
 */
public class SlimeCraftListener implements Listener {

    private final SlimeCraftManager manager;
    private final CustomItemManager customItemManager;

    public SlimeCraftListener(SlimeCraftManager manager) {
        this.manager = manager;
        this.customItemManager = manager.getCustomItemManager();
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        Recipe recipe = event.getRecipe();

        if (recipe == null || inventory.getResult() == null) {
            return;
        }

        ItemStack[] matrix = inventory.getMatrix();

        // 检查是否为压缩雪球合成史莱姆核心的配方
        if (recipe.getResult().getType() == Material.EMERALD &&
                recipe.getResult().hasItemMeta() &&
                recipe.getResult().getItemMeta().getDisplayName().contains("史萊姆核心")) {

            // 检查所有材料是否为压缩雪球
            int compressedCount = 0;
            for (ItemStack item : matrix) {
                if (item != null) {
                    if (customItemManager.isCompressedSnowball(item)) {
                        compressedCount++;
                    } else {
                        // 如果有非压缩雪球的物品，取消合成
                        inventory.setResult(null);
                        return;
                    }
                }
            }

            // 必须是9个压缩雪球
            if (compressedCount == 9) {
                inventory.setResult(customItemManager.createSlimeCore());
            } else {
                inventory.setResult(null);
            }
        }

        // 检查是否为糖+史莱姆核心+绿色染料的合成
        else if (recipe.getResult().getType() == Material.SLIME_BALL) {
            boolean hasSugar = false;
            boolean hasSlimeCore = false;
            boolean hasDye = false;

            // 检查材料
            for (ItemStack item : matrix) {
                if (item != null) {
                    if (item.getType() == Material.SUGAR) {
                        hasSugar = true;
                    } else if (customItemManager.isSlimeCore(item)) {
                        hasSlimeCore = true;
                    } else if (item.getType() == Material.GREEN_DYE) {
                        hasDye = true;
                    } else {
                        // 有其他材料，取消合成
                        inventory.setResult(null);
                        return;
                    }
                }
            }

            // 必须三种材料都有
            if (hasSugar && hasSlimeCore && hasDye) {
                // 检查是否为垂直排列
                if (isVertical(matrix, Material.SUGAR, Material.GREEN_DYE)) {
                    inventory.setResult(null); // 垂直排列不允许
                } else {
                    inventory.setResult(new ItemStack(Material.SLIME_BALL,
                            manager.getConfig().getSlimeBallOutputAmount()));
                }
            } else {
                inventory.setResult(null);
            }
        }
    }

    /**
     * 检查是否为垂直排列
     */
    private boolean isVertical(ItemStack[] matrix, Material... materials) {
        // 检查每一列
        for (int col = 0; col < 3; col++) {
            int count = 0;
            for (int row = 0; row < 3; row++) {
                int index = row * 3 + col;
                ItemStack item = matrix[index];
                if (item != null) {
                    // 检查是否为指定材料
                    boolean isTargetMaterial = false;
                    for (Material material : materials) {
                        if (item.getType() == material) {
                            isTargetMaterial = true;
                            break;
                        }
                    }
                    if (isTargetMaterial) {
                        count++;
                    }
                }
            }
            // 如果一列有3个物品，则是垂直排列
            if (count == 3) {
                return true;
            }
        }
        return false;
    }
}