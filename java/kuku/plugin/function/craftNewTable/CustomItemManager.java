package kuku.plugin.function.craftNewTable;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

/**
 * 自定义物品管理器
 */
public class CustomItemManager {

    private final JavaPlugin plugin;
    private final NamespacedKey compressedSnowballKey;
    private final NamespacedKey slimeCoreKey;

    public CustomItemManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.compressedSnowballKey = new NamespacedKey(plugin, "compressed_snowball");
        this.slimeCoreKey = new NamespacedKey(plugin, "slime_core");
    }

    /**
     * 创建压缩雪球
     */
    public ItemStack createCompressedSnowball() {
        ItemStack item = new ItemStack(Material.SNOW_BLOCK);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.AQUA + "壓縮雪球");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "由9個雪球壓縮而成",
                ChatColor.GRAY + "可用於合成史萊姆核心"
        ));

        // 添加自定义NBT标签
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(compressedSnowballKey, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * 创建史莱姆核心
     */
    public ItemStack createSlimeCore() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GREEN + "史萊姆核心");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "由9個壓縮雪球合成",
                ChatColor.GRAY + "可用於合成黏液球",
                ChatColor.YELLOW + "核心能量：100%"
        ));

        // 添加自定义NBT标签
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(slimeCoreKey, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * 检查是否为压缩雪球
     */
    public boolean isCompressedSnowball(ItemStack item) {
        if (item == null || item.getType() != Material.SNOW_BLOCK) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(compressedSnowballKey, PersistentDataType.BYTE);
    }

    /**
     * 检查是否为史莱姆核心
     */
    public boolean isSlimeCore(ItemStack item) {
        if (item == null || item.getType() != Material.EMERALD) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(slimeCoreKey, PersistentDataType.BYTE);
    }

    /**
     * 获取自定义物品名称
     */
    public String getItemName(ItemStack item) {
        if (item == null) return null;

        if (isCompressedSnowball(item)) return "壓縮雪球";
        if (isSlimeCore(item)) return "史萊姆核心";

        return null;
    }

    public NamespacedKey getCompressedSnowballKey() {
        return compressedSnowballKey;
    }

    public NamespacedKey getSlimeCoreKey() {
        return slimeCoreKey;
    }
}