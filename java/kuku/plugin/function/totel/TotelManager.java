package kuku.plugin.function.totel;

import kuku.plugin.function.utils.LogUtils;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.logging.Logger;

/**
 * 不死圖騰管理器 - 1.21.10 專用版本
 * 功能：不死圖騰在背包任何位置都能觸發效果
 */
public class TotelManager implements Listener {

    private final JavaPlugin plugin;
    private final Logger logger;
    private boolean enabled = false;

    // 系統前綴
    private static final String SYSTEM_PREFIX = "§8|§6系統§8| §f";

    public TotelManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * 啟用不死圖騰功能
     */
    public void enable() {
        if (enabled) return;

        enabled = true;

        // 註冊事件監聽器
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        LogUtils.logModuleEnable(logger, "不死圖騰");
        logger.info("§7功能：不死圖騰可以在背包任何位置觸發效果");
        logger.info("§7版本：1.21.10 (無需版本兼容處理)");
    }

    /**
     * 禁用不死圖騰功能
     */
    public void disable() {
        if (!enabled) return;

        enabled = false;
        LogUtils.logModuleDisable(logger, "不死圖騰");
    }

    /**
     * 重載配置
     */
    public void reload() {
        logger.info("§e不死圖騰功能已重載");
    }

    /**
     * 檢查是否為不死圖騰
     */
    private boolean isTotemOfUndying(ItemStack item) {
        if (item == null) return false;
        return item.getType() == Material.TOTEM_OF_UNDYING;
    }

    /**
     * 檢查玩家背包中是否有不死圖騰
     */
    private boolean hasTotemInInventory(Player player) {
        PlayerInventory inventory = player.getInventory();

        // 檢查所有背包格子（包括快捷欄和主背包）
        for (ItemStack item : inventory.getContents()) {
            if (isTotemOfUndying(item)) {
                return true;
            }
        }

        // 檢查副手
        ItemStack offHand = inventory.getItemInOffHand();
        return isTotemOfUndying(offHand);
    }

    /**
     * 消耗一個不死圖騰
     * @return 是否成功消耗
     */
    private boolean consumeTotem(Player player) {
        PlayerInventory inventory = player.getInventory();

        // 優先檢查主手和副手（保持原版邏輯）
        ItemStack mainHand = inventory.getItemInMainHand();
        if (isTotemOfUndying(mainHand)) {
            consumeItem(mainHand);
            inventory.setItemInMainHand(mainHand);
            return true;
        }

        ItemStack offHand = inventory.getItemInOffHand();
        if (isTotemOfUndying(offHand)) {
            consumeItem(offHand);
            inventory.setItemInOffHand(offHand);
            return true;
        }

        // 檢查快捷欄（0-8）
        for (int i = 0; i < 9; i++) {
            ItemStack item = inventory.getItem(i);
            if (isTotemOfUndying(item)) {
                consumeItem(item);
                inventory.setItem(i, item);
                return true;
            }
        }

        // 檢查主背包（9-35）
        for (int i = 9; i < 36; i++) {
            ItemStack item = inventory.getItem(i);
            if (isTotemOfUndying(item)) {
                consumeItem(item);
                inventory.setItem(i, item);
                return true;
            }
        }

        return false;
    }

    /**
     * 消耗物品
     */
    private void consumeItem(ItemStack item) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            item.setAmount(0);
            item.setType(Material.AIR);
        }
    }

    /**
     * 播放原版不死圖騰特效和動畫
     */
    private void playTotemEffects(Player player) {
        Location loc = player.getLocation();
        World world = player.getWorld();

        // 播放原版不死圖騰聲音
        world.playSound(loc, Sound.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);

        // 播放原版不死圖騰粒子效果
        // 1.21.10 中 Particle.TOTEM 已經包含完整的動畫
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            // 簡單地在玩家位置生成粒子，系統會自動播放完整動畫
            world.spawnParticle(
                    Particle.TOTEM_OF_UNDYING,
                    loc.getX(), loc.getY() + 1.0, loc.getZ(),
                    1,   // 只需要1個，系統會自動擴散
                    0.0, 0.0, 0.0,  // 無偏移
                    1.0  // 速度
            );
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        // 檢查模塊是否啟用
        if (!enabled) {
            return;
        }

        // 檢查是否為虛空傷害或自訂傷害（原版圖騰無法抵擋）
        DamageCause cause = event.getCause();
        if (cause == DamageCause.VOID || cause == DamageCause.CUSTOM || cause == DamageCause.SUICIDE) {
            return;
        }

        // 計算傷害後的生命值
        double damage = event.getFinalDamage();
        double health = player.getHealth();

        // 如果傷害不足以殺死玩家，不處理
        if (health - damage > 0.5) {
            return;
        }

        // 檢查玩家是否有不死圖騰
        if (!hasTotemInInventory(player)) {
            return;
        }

        // 取消傷害事件（防止玩家死亡）
        event.setCancelled(true);

        // 消耗一個不死圖騰
        if (!consumeTotem(player)) {
            return;
        }

        // 應用原版不死圖騰效果
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 900, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0));

        // 播放原版特效和動畫
        playTotemEffects(player);

        // 更新玩家生命值（設置為1顆心）
        player.setHealth(2.0);

        // 清除火焰效果（如果有的話）
        if (player.getFireTicks() > 0) {
            player.setFireTicks(0);
        }

        // 發送消息給玩家
        player.sendMessage(SYSTEM_PREFIX + "§a不死圖騰已觸發，拯救了你的生命！");
    }

    /**
     * 檢查是否啟用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 獲取插件實例
     */
    public JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * 測試用：手動觸發不死圖騰效果
     */
    public void testTotemEffect(Player player) {
        if (!enabled) {
            player.sendMessage("§c不死圖騰功能未啟用");
            return;
        }

        if (!hasTotemInInventory(player)) {
            player.sendMessage("§c你沒有不死圖騰");
            return;
        }

        if (consumeTotem(player)) {
            // 應用原版效果
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 900, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0));

            playTotemEffects(player);
            player.setHealth(2.0);
            player.sendMessage("§a已手動觸發不死圖騰效果");
        }
    }
}