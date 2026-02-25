package kuku.plugin.function.easybox;

import kuku.plugin.function.FunctionPlugin;
import kuku.plugin.function.utils.LogUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;

import java.util.*;

public class EasyBoxManager implements Listener {

    private final FunctionPlugin plugin;
    private final EasyBoxConfig config;
    private final Map<UUID, ItemStack> openedBoxes = new HashMap<>();

    public EasyBoxManager(FunctionPlugin plugin) {
        this.plugin = plugin;
        this.config = new EasyBoxConfig(plugin);
    }

    public void enable() {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(this, plugin);
        LogUtils.logSuccess(plugin.getLogger(), "EasyBox",
                "模塊已啟用 - 極簡界伏盒功能");
    }

    public void disable() {
        openedBoxes.clear();
    }

    public void reload() {
        config.reload();
    }

    public EasyBoxConfig getConfig() {
        return config;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!config.isModuleEnabled() || !config.isEnabled()) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // 檢查是否手持界伏盒
        if (!isShulkerBox(item)) return;

        // 檢查是否右鍵空氣
        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            event.setCancelled(true);

            // 更新Lore
            updateItemLore(item);

            // 打開界伏盒
            openShulkerBox(player, item);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        UUID uuid = player.getUniqueId();

        // 保存界伏盒內容並更新Lore
        if (openedBoxes.containsKey(uuid)) {
            ItemStack box = openedBoxes.get(uuid);
            saveBoxContents(box, event.getInventory());
            updateItemLore(box);
            openedBoxes.remove(uuid);
        }
    }

    private void openShulkerBox(Player player, ItemStack shulkerBox) {
        // 創建界伏盒庫存
        Inventory boxInventory = Bukkit.createInventory(
                player,
                27,
                ChatColor.translateAlternateColorCodes('&', config.getInventoryTitle())
        );

        // 載入界伏盒內容
        ItemStack[] contents = getShulkerBoxContents(shulkerBox);
        if (contents != null) {
            boxInventory.setContents(contents);
        }

        // 記錄打開的界伏盒
        openedBoxes.put(player.getUniqueId(), shulkerBox);

        // 打開庫存
        player.openInventory(boxInventory);
    }

    private void saveBoxContents(ItemStack shulkerBox, Inventory inventory) {
        if (!isShulkerBox(shulkerBox) || !shulkerBox.hasItemMeta()) return;

        ItemMeta meta = shulkerBox.getItemMeta();
        if (meta instanceof BlockStateMeta) {
            BlockStateMeta blockMeta = (BlockStateMeta) meta;
            if (blockMeta.getBlockState() instanceof org.bukkit.block.ShulkerBox) {
                org.bukkit.block.ShulkerBox shulker = (org.bukkit.block.ShulkerBox) blockMeta.getBlockState();
                shulker.getInventory().setContents(inventory.getContents());
                blockMeta.setBlockState(shulker);
                shulkerBox.setItemMeta(blockMeta);
            }
        }
    }

    private ItemStack[] getShulkerBoxContents(ItemStack shulkerBox) {
        if (!isShulkerBox(shulkerBox) || !shulkerBox.hasItemMeta()) return null;

        ItemMeta meta = shulkerBox.getItemMeta();
        if (meta instanceof BlockStateMeta) {
            BlockStateMeta blockMeta = (BlockStateMeta) meta;
            if (blockMeta.getBlockState() instanceof org.bukkit.block.ShulkerBox) {
                org.bukkit.block.ShulkerBox shulker = (org.bukkit.block.ShulkerBox) blockMeta.getBlockState();
                return shulker.getInventory().getContents();
            }
        }
        return null;
    }

    private void updateItemLore(ItemStack shulkerBox) {
        if (!config.isLoreEnabled()) return;
        if (!isShulkerBox(shulkerBox) || !shulkerBox.hasItemMeta()) return;

        ItemMeta meta = shulkerBox.getItemMeta();
        if (meta == null) return;

        List<String> lore = new ArrayList<>();

        // 獲取界伏盒內容
        ItemStack[] contents = getShulkerBoxContents(shulkerBox);

        if (contents != null) {
            // 統計物品
            Map<String, Integer> itemCounts = new LinkedHashMap<>();
            int totalItems = 0;
            int totalSlots = 0;

            for (ItemStack item : contents) {
                if (item != null && !item.getType().isAir()) {
                    totalSlots++;
                    totalItems += item.getAmount();

                    String itemName = getItemDisplayName(item);
                    itemCounts.put(itemName, itemCounts.getOrDefault(itemName, 0) + item.getAmount());
                }
            }

            // 添加標題
            lore.add(ChatColor.translateAlternateColorCodes('&',
                    config.getLoreTitle()
                            .replace("%total%", String.valueOf(totalItems))
                            .replace("%slots%", String.valueOf(totalSlots))
                            .replace("%max%", "27")));

            // 添加物品列表（限制行數）
            int lineCount = 0;
            for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
                if (lineCount >= config.getMaxLoreLines()) {
                    lore.add(ChatColor.GRAY + "...");
                    break;
                }

                String line = config.getLoreFormat()
                        .replace("%item%", entry.getKey())
                        .replace("%amount%", String.valueOf(entry.getValue()));
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
                lineCount++;
            }

            if (itemCounts.isEmpty()) {
                lore.add(ChatColor.translateAlternateColorCodes('&', config.getEmptyMessage()));
            }
        } else {
            lore.add(ChatColor.translateAlternateColorCodes('&', config.getEmptyMessage()));
        }

        meta.setLore(lore);
        shulkerBox.setItemMeta(meta);
    }

    private String getItemDisplayName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return ChatColor.stripColor(item.getItemMeta().getDisplayName());
        }

        // 簡單的英文名稱轉換
        String name = item.getType().toString().toLowerCase();
        name = name.replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    private boolean isShulkerBox(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;

        String materialName = item.getType().name();
        return materialName.contains("SHULKER_BOX") && !materialName.contains("LEGACY");
    }
}