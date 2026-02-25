package kuku.plugin.function.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 計分板更新任務（極簡版 - 直接更新）
 */
public class ScoreboardTask extends BukkitRunnable {

    private final ScoreboardManager scoreboardManager;
    private final SimpleDateFormat timeFormat;
    private final SimpleDateFormat dateFormat;
    private final AtomicInteger titleAnimationIndex = new AtomicInteger(0);

    // 只保留最後更新時間來控制更新頻率
    private final Map<UUID, Long> lastUpdateTime = new ConcurrentHashMap<>();

    // 移動檢測：避免過度更新
    private final Map<UUID, Location> lastLocations = new ConcurrentHashMap<>();

    public ScoreboardTask(ScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;

        // 設定台灣時區的時間格式
        TimeZone taiwanTimeZone = TimeZone.getTimeZone("Asia/Taipei");

        // 12小時制格式
        this.timeFormat = new SimpleDateFormat("a hh:mm:ss");
        this.timeFormat.setTimeZone(taiwanTimeZone);

        // 日期格式
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.dateFormat.setTimeZone(taiwanTimeZone);
    }

    @Override
    public void run() {
        if (!scoreboardManager.isEnabled()) return;

        try {
            // === 1. 處理標題動畫 ===
            handleTitleAnimation();

            // === 2. 直接更新所有在線玩家 ===
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (shouldUpdatePlayer(player)) {
                    updatePlayerScoreboard(player);
                }
            }

            // === 3. 定期清理緩存（每分鐘） ===
            if (System.currentTimeMillis() % 60000 < 50) {
                cleanupCache();
            }

        } catch (Exception e) {
            scoreboardManager.getPlugin().getLogger().warning(
                    "計分板更新任務執行時出錯: " + e.getMessage()
            );
        }
    }

    /**
     * 檢查玩家是否需要更新
     */
    private boolean shouldUpdatePlayer(Player player) {
        if (player == null || !player.isOnline()) return false;

        // 檢查是否有計分板
        if (!scoreboardManager.hasScoreboard(player)) return false;

        // 檢查世界是否允許
        if (!scoreboardManager.getConfig().isWorldAllowed(player.getWorld().getName())) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // 獲取上次更新時間
        Long lastUpdate = lastUpdateTime.get(uuid);

        // 如果從未更新過，立即更新
        if (lastUpdate == null) {
            lastUpdateTime.put(uuid, now);
            lastLocations.put(uuid, player.getLocation());
            return true;
        }

        // 計算距離上次更新的時間
        long timeSinceLastUpdate = now - lastUpdate;

        // 基礎更新間隔：50ms（20 tick/秒）
        long baseInterval = 50;

        // 檢查玩家是否移動
        Location lastLocation = lastLocations.get(uuid);
        Location currentLocation = player.getLocation();

        boolean hasMoved = false;
        if (lastLocation != null) {
            // 檢查X或Z座標是否變化
            hasMoved = (currentLocation.getBlockX() != lastLocation.getBlockX() ||
                    currentLocation.getBlockZ() != lastLocation.getBlockZ());

            // 如果移動了，更新位置緩存
            if (hasMoved) {
                lastLocations.put(uuid, currentLocation);
            }
        }

        // 更新策略：
        // 1. 如果玩家移動了，且超過50ms，就更新
        // 2. 如果玩家沒移動，但超過200ms，也更新（保證其他信息如時間、血量等能更新）
        // 3. 永遠不超過3秒不更新（安全機制）

        if (timeSinceLastUpdate > 3000) {
            // 超過3秒，強制更新
            lastUpdateTime.put(uuid, now);
            return true;
        } else if (hasMoved && timeSinceLastUpdate > baseInterval) {
            // 移動了且超過50ms，更新
            lastUpdateTime.put(uuid, now);
            return true;
        } else if (!hasMoved && timeSinceLastUpdate > 200) {
            // 沒移動但超過200ms，更新（用於更新時間等）
            lastUpdateTime.put(uuid, now);
            return true;
        }

        return false;
    }

    /**
     * 更新玩家計分板
     */
    private void updatePlayerScoreboard(Player player) {
        try {
            scoreboardManager.updateScoreboard(player, getPlaceholders(player));
        } catch (Exception e) {
            scoreboardManager.getPlugin().getLogger().warning(
                    "更新玩家 " + player.getName() + " 的計分板時出錯: " + e.getMessage()
            );
        }
    }

    /**
     * 獲取佔位符映射
     */
    private Map<String, String> getPlaceholders(Player player) {
        Map<String, String> placeholders = new HashMap<>();

        // 玩家信息
        placeholders.put("player", player.getName());
        placeholders.put("displayname", player.getDisplayName());
        placeholders.put("biome", BiomeHelper.getPlayerBiomeName(player));

        // 血量（包含吸收血量）
        double health = player.getHealth();
        double absorption = player.getAbsorptionAmount();
        double maxHealth = player.getMaxHealth();

        String formattedHealth;
        if (absorption > 0) {
            ChatColor healthColor = getHealthColor(health / maxHealth);
            ChatColor absorptionColor = ChatColor.YELLOW;
            formattedHealth = String.format("%s%.1f%s(+%.1f)",
                    healthColor, health, absorptionColor, absorption);
        } else {
            ChatColor healthColor = getHealthColor(health / maxHealth);
            formattedHealth = String.format("%s%.1f", healthColor, health);
        }
        placeholders.put("health", formattedHealth);

        placeholders.put("food", String.valueOf(player.getFoodLevel()));

        // 位置信息
        placeholders.put("world", player.getWorld().getName());
        placeholders.put("x", String.valueOf(player.getLocation().getBlockX()));
        placeholders.put("y", String.valueOf(player.getLocation().getBlockY()));
        placeholders.put("z", String.valueOf(player.getLocation().getBlockZ()));

        // 伺服器信息
        placeholders.put("online", String.valueOf(Bukkit.getOnlinePlayers().size()));
        placeholders.put("max", String.valueOf(Bukkit.getMaxPlayers()));

        // 時間信息 - 台灣時間 12小時制
        Date now = new Date();
        TimeZone taiwanTimeZone = TimeZone.getTimeZone("Asia/Taipei");

        SimpleDateFormat timeFormat12h = new SimpleDateFormat("a hh:mm:ss");
        timeFormat12h.setTimeZone(taiwanTimeZone);
        String time12h = timeFormat12h.format(now);
        time12h = time12h.replace("AM", "上午").replace("PM", "下午");
        placeholders.put("time", "台灣時間: " + time12h);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(taiwanTimeZone);
        placeholders.put("date", dateFormat.format(now));

        // 玩家延遲
        int ping = player.getPing();
        if (ping < 0) ping = 0;
        placeholders.put("ping", String.valueOf(ping));

        // 伺服器性能
        try {
            double[] tps = Bukkit.getTPS();
            placeholders.put("tps", String.format("%.1f", tps[0]));
        } catch (Exception e) {
            placeholders.put("tps", "20.0");
        }

        return placeholders;
    }

    /**
     * 處理標題動畫
     */
    private void handleTitleAnimation() {
        if (!scoreboardManager.getConfig().isAnimationsEnabled()) return;

        // 增加動畫索引
        int currentIndex = titleAnimationIndex.incrementAndGet();

        // 通知計分板管理器更新動畫
        scoreboardManager.setTitleAnimationIndex(currentIndex);
    }

    /**
     * 根據血量百分比獲取顏色
     */
    private ChatColor getHealthColor(double healthPercentage) {
        if (healthPercentage >= 0.8) {
            return ChatColor.GREEN;      // 綠色：80-100%
        } else if (healthPercentage >= 0.6) {
            return ChatColor.YELLOW;     // 黃色：60-80%
        } else if (healthPercentage >= 0.4) {
            return ChatColor.GOLD;       // 金色：40-60%
        } else if (healthPercentage >= 0.2) {
            return ChatColor.RED;        // 紅色：20-40%
        } else {
            return ChatColor.DARK_RED;   // 深紅色：0-20%
        }
    }

    /**
     * 清理過期的緩存數據
     */
    private void cleanupCache() {
        // 清理離線玩家的數據
        lastUpdateTime.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
        lastLocations.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
    }

    // === 公開方法供事件監聽器調用 ===

    /**
     * 玩家移動時調用（可選，用於立即更新）
     */
    public void onPlayerMove(Player player) {
        if (player == null || !scoreboardManager.hasScoreboard(player)) return;

        // 立即更新位置緩存
        lastLocations.put(player.getUniqueId(), player.getLocation());

        // 可以選擇立即更新，或者等待下次tick更新
        // 這裡我們只是更新緩存，實際更新在run()中檢查
    }

    /**
     * 玩家屬性變化時調用（可選）
     */
    public void onPlayerStatChange(Player player) {
        if (player == null || !scoreboardManager.hasScoreboard(player)) return;

        // 可以立即更新，這裡我們只是重置更新時間，讓run()方法立刻更新
        lastUpdateTime.put(player.getUniqueId(), 0L);
    }

    /**
     * 獲取當前動畫索引
     */
    public int getTitleAnimationIndex() {
        return titleAnimationIndex.get();
    }
}