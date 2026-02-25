package kuku.plugin.function.scoreboard;

import kuku.plugin.function.utils.LogUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.logging.Logger;

/**
 * 計分板管理器
 */
public class ScoreboardManager {

    private final JavaPlugin plugin;
    private final Logger logger;
    private final ScoreboardConfig config;
    private final ScoreboardListener listener;
    private final ScoreboardTask updateTask;

    private boolean enabled = false;
    private int titleAnimationIndex = 0;

    private final Map<UUID, List<String>> scoreboardEntries = new HashMap<>();

    // 存儲玩家的計分板
    private final Map<UUID, Scoreboard> playerScoreboards;
    private final Map<UUID, Objective> playerObjectives;

    // 添加缓存映射
    private final Map<UUID, ScoreboardCache> scoreboardCache = new HashMap<>();
    // 系統前綴
    private static final String SYSTEM_PREFIX = "§8|§6系統§8| §f";

    public ScoreboardManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.config = new ScoreboardConfig(plugin);
        this.listener = new ScoreboardListener(this);
        this.updateTask = new ScoreboardTask(this);

        this.playerScoreboards = new HashMap<>();
        this.playerObjectives = new HashMap<>();

        // 註冊事件監聽器
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    /**
     * 啟用計分板功能
     */
    public void enable() {
        if (enabled) {
            return;
        }

        enabled = true;

        // 強制使用快速更新（1 tick = 0.05秒）
        int updateInterval = 2;

        updateTask.runTaskTimer(plugin, 0L, updateInterval);

        LogUtils.logModuleEnable(logger, "計分板");
        logModuleStatus();

        // 啟用後立即為所有在線玩家創建計分板
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (config.isWorldAllowed(player.getWorld().getName())) {
                    if (!hasScoreboard(player)) {
                        createScoreboard(player);
                    }
                }
            }
        }, 5L);  // 0.25秒後執行
    }

    /**
     * 禁用計分板功能
     */
    public void disable() {
        if (!enabled) {
            return;
        }

        enabled = false;

        // 停止更新任務
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
        }

        // 移除所有玩家的計分板（確保清理資源）
        logger.info("開始清理所有玩家的計分板資源...");
        int cleanedCount = 0;

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            try {
                removeScoreboard(player);
                cleanedCount++;
            } catch (Exception e) {
                logger.warning("清理玩家 " + player.getName() + " 的計分板時出錯: " + e.getMessage());
            }
        }

        // 額外清理：檢查是否有遺漏的引用
        if (!playerObjectives.isEmpty()) {
            logger.warning("發現 " + playerObjectives.size() + " 個未清理的Objective引用");
            playerObjectives.clear();
        }

        if (!playerScoreboards.isEmpty()) {
            logger.warning("發現 " + playerScoreboards.size() + " 個未清理的Scoreboard引用");
            playerScoreboards.clear();
        }

        if (scoreboardEntries != null && !scoreboardEntries.isEmpty()) {
            logger.warning("發現 " + scoreboardEntries.size() + " 個未清理的條目引用");
            scoreboardEntries.clear();
        }

        logger.info("計分板模塊已禁用，清理了 " + cleanedCount + " 個玩家的計分板資源");
    }

    /**
     * 重載配置
     */
    public void reload() {
        config.reloadConfig();

        // 重新設置所有玩家的計分板
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (hasScoreboard(player)) {
                removeScoreboard(player);
                if (config.isWorldAllowed(player.getWorld().getName())) {
                    createScoreboard(player);
                }
            }
        }

        logger.info("§e計分板配置已重載");
    }

    /**
     * 記錄模塊狀態
     */
    private void logModuleStatus() {
        if (!config.isEnabled()) {
            logger.info("§7注意: 模塊在配置中被禁用 (scoreboard.yml)");
            return;
        }

        LogUtils.startModuleStatus(logger);
        LogUtils.logFeatureStatus(logger, "計分板功能", config.isEnabled());
        LogUtils.logValueStatus(logger, "更新間隔", config.getUpdateInterval() + " ticks");

        if (config.isAllWorlds()) {
            LogUtils.logFeatureStatus(logger, "世界限制", true);
        } else {
            LogUtils.logFeatureStatus(logger, "世界限制", false);
            LogUtils.logValueStatus(logger, "白名單世界", config.getWorldWhitelist().size() + "個");
        }

        LogUtils.logFeatureStatus(logger, "動畫效果", config.isAnimationsEnabled());

        // 可選：顯示計分板行數
        int lineCount = config.getLines().size();
        if (lineCount > 0) {
            LogUtils.logValueStatus(logger, "顯示行數", lineCount + "行");
        }
    }

    /**
     * 為玩家創建計分板
     */
    public void createScoreboard(Player player) {
        if (!enabled || !config.isEnabled()) return;

        try {
            UUID uuid = player.getUniqueId();
            String playerName = player.getName();

            // 先移除現有的
            removeScoreboard(player);

            // 檢查世界是否允許
            if (!config.isWorldAllowed(player.getWorld().getName())) {
                return;
            }

            // 創建新的計分板
            Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
            String objectiveName = "kuku_" + uuid.toString().substring(0, 8);

            Objective objective = scoreboard.registerNewObjective(objectiveName, "dummy", getAnimatedTitle());
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            // 應用計分板
            player.setScoreboard(scoreboard);

            // 保存引用
            playerScoreboards.put(uuid, scoreboard);
            playerObjectives.put(uuid, objective);
            scoreboardEntries.put(uuid, new ArrayList<>());

            // 創建後立即更新一次
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && hasScoreboard(player)) {
                    updateScoreboard(player, new HashMap<>());
                }
            }, 1L); // 1 tick 後更新

        } catch (Exception e) {
            logger.warning("為玩家 " + player.getName() + " 創建計分板時出錯: " + e.getMessage());
        }
    }

    // ✅ 修改 removeScoreboard 方法
    public void removeScoreboard(Player player) {
        if (player == null) return;

        UUID uuid = player.getUniqueId();
        String playerName = player.getName();

        try {
            // === 第一步：獲取並取消註冊Objective ===
            Objective objective = playerObjectives.get(uuid);
            if (objective != null) {
                try {
                    // 重要：必須調用unregister()來釋放資源
                    if (!objective.isModifiable()) {
                        // 如果Objective已經不可修改，強制取消註冊
                        objective.unregister();
                    } else {
                        objective.unregister();
                    }
                    logger.fine("取消註冊玩家 " + playerName + " 的Objective: " + objective.getName());
                } catch (IllegalStateException e) {
                    // Objective可能已經被取消註冊，這是正常情況
                    logger.fine("玩家 " + playerName + " 的Objective已經被取消註冊");
                }
            }

            // === 第二步：恢復玩家到主計分板 ===
            try {
                player.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
                logger.fine("恢復玩家 " + playerName + " 到主計分板");
            } catch (Exception e) {
                logger.warning("恢復玩家 " + playerName + " 到主計分板時出錯: " + e.getMessage());
            }

            // === 第三步：清理所有緩存 ===
            playerScoreboards.remove(uuid);
            playerObjectives.remove(uuid);
            scoreboardEntries.remove(uuid);

            scoreboardCache.remove(uuid);

            logger.fine("完成移除玩家 " + playerName + " 的計分板資源");

        } catch (Exception e) {
            logger.warning("移除玩家 " + player.getName() + " 的計分板時出錯: " + e.getMessage());
        }
    }

    /**
     * 更新玩家的計分板內容
     */
    public void updateScoreboard(Player player, Map<String, String> placeholders) {
        if (!enabled || !config.isEnabled()) return;

        Objective objective = playerObjectives.get(player.getUniqueId());
        if (objective == null) return;

        UUID playerId = player.getUniqueId();
        String playerName = player.getName();

        try {
            // 获取或创建缓存
            ScoreboardCache cache = scoreboardCache.computeIfAbsent(playerId, k -> new ScoreboardCache());
            Scoreboard scoreboard = objective.getScoreboard();

            // === 1. 处理标题（只有变化时才更新） ===
            String currentTitle = getAnimatedTitle(); // 获取当前帧的动画标题
            if (!currentTitle.equals(cache.getLastTitle())) {
                objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', currentTitle));
                cache.setLastTitle(currentTitle);
            }

            // === 2. 处理内容行 ===
            List<String> lines = config.getLines();
            if (lines.isEmpty()) return;

            Set<String> usedEntriesThisUpdate = new HashSet<>(); // 本次更新使用的唯一标识符
            Map<Integer, String> currentEntries = new HashMap<>(); // 本次更新的 [行索引 -> 唯一标识符]

            // 生成当前帧的所有行
            for (int i = 0; i < lines.size(); i++) {
                String rawLine = lines.get(i);
                String processedLine = rawLine;

                // 替换占位符 (复用你原有的逻辑)
                for (Map.Entry<String, String> placeholder : placeholders.entrySet()) {
                    String key = placeholder.getKey();
                    String value = placeholder.getValue();

                    if (key.equals("ping") && processedLine.contains("{ping}")) {
                        try {
                            int ping = Integer.parseInt(value);
                            ChatColor color = getPingColor(ping);
                            processedLine = processedLine.replace("{ping}", color + value);
                        } catch (NumberFormatException e) {
                            processedLine = processedLine.replace("{ping}", value);
                        }
                    } else if (key.equals("health") && processedLine.contains("{health}")) {
                        processedLine = processedLine.replace("{health}", value);
                    } else {
                        processedLine = processedLine.replace("{" + key + "}", value);
                    }
                }

                processedLine = ChatColor.translateAlternateColorCodes('&', processedLine);
                if (processedLine.length() > 48) {
                    processedLine = processedLine.substring(0, 45) + "...";
                }

                // 生成该行的唯一标识符（复用你优化后的逻辑，但确保稳定）
                String uniqueEntry = generateStableEntryId(processedLine, i);
                currentEntries.put(i, uniqueEntry);

                // === 差分更新关键逻辑 ===
                String lastLine = cache.getLastLine(i);
                String lastEntry = cache.getLastEntry(i);

                // 情况A：该行内容发生了变化（或首次设置）
                if (!processedLine.equals(lastLine)) {
                    // 先移除旧的记分项（如果存在且标识符不同）
                    if (lastEntry != null && !lastEntry.equals(uniqueEntry)) {
                        scoreboard.resetScores(lastEntry);
                    }
                    // 设置新的分数
                    int scoreValue = lines.size() - i - 1;
                    objective.getScore(uniqueEntry).setScore(scoreValue);
                    // 更新缓存
                    cache.setLastLine(i, processedLine);
                    cache.setLastEntry(i, uniqueEntry);
                }
                // 情况B：行内容没变，但需要确保分数存在（防止意外丢失）
                else if (lastEntry != null) {
                    // 内容未变化，通常无需操作。此处可确保分数值，但通常不必。
                    // objective.getScore(lastEntry).setScore(lines.size() - i - 1);
                }

                usedEntriesThisUpdate.add(uniqueEntry);
            }

            // === 3. 清理已不存在的旧行 ===
            // 遍历缓存中的旧标识符，如果本次更新没用到，就移除
            for (Map.Entry<Integer, String> oldEntry : cache.getLastEntries().entrySet()) {
                String oldEntryId = oldEntry.getValue();
                if (oldEntryId != null && !usedEntriesThisUpdate.contains(oldEntryId)) {
                    scoreboard.resetScores(oldEntryId);
                    // 清理该行缓存
                    cache.setLastLine(oldEntry.getKey(), null);
                    cache.setLastEntry(oldEntry.getKey(), null);
                }
            }

            // 更新缓存时间
            cache.setLastUpdateTime(System.currentTimeMillis());

        } catch (Exception e) {
            logger.warning("更新玩家 " + playerName + " 的计分板时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateStableEntryId(String lineContent, int lineIndex) {
        // 使用一个简单的、基于行索引的不可见后缀，确保同一行位置标识符稳定
        char[] colors = {'k', 'l', 'm', 'n', 'o'}; // 使用不会改变渲染的格式码
        int colorIdx = lineIndex % colors.length;
        // 后缀格式：§k§l§r (不可见，但能保证唯一性和稳定性)
        return lineContent + "§" + colors[colorIdx] + "§r";
    }

    // 輔助方法：創建唯一的計分板條目
    private String makeUniqueScoreEntry(String content, int lineIndex, Set<String> usedEntries) {
        // 如果內容已經唯一，直接返回
        if (!usedEntries.contains(content)) {
            return content;
        }

        // 內容重複，需要添加唯一標識
        // 使用不可見的格式代碼來確保唯一性
        char[] formatCodes = {'k', 'l', 'm', 'n', 'o', 'r'};

        for (int attempt = 1; attempt <= 10; attempt++) {
            // 創建唯一後綴
            StringBuilder suffix = new StringBuilder();

            // 添加多個不可見的格式代碼
            for (int j = 0; j < attempt; j++) {
                int codeIndex = (lineIndex + j) % formatCodes.length;
                suffix.append("§").append(formatCodes[codeIndex]);
            }

            suffix.append("§r"); // 重置格式

            String uniqueContent = content + suffix.toString();

            if (!usedEntries.contains(uniqueContent)) {
                return uniqueContent;
            }
        }

        // 如果10次嘗試都失敗，使用更強的唯一性保證
        return content + "§k§r§" + lineIndex + "§r";
    }

    // 修改 ScoreboardManager.java 的 updateScoreboard 方法
    private String getUniqueScoreEntry(String content, int lineNumber) {
        // 為每行生成唯一標識符
        // Bukkit計分板要求每行必須是唯一的
        char[] colors = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        int colorIndex = lineNumber % colors.length;

        // 使用不可見的顏色代碼確保唯一性
        return "§" + colors[colorIndex] + content + "§r";
    }

    /**
     * 獲取動畫標題
     */
    private String getAnimatedTitle() {
        if (config.isAnimationsEnabled()) {
            List<String> frames = config.getTitleAnimationFrames();
            if (!frames.isEmpty()) {
                int index = titleAnimationIndex % frames.size();
                return frames.get(index);
            }
        }
        return config.getTitle();
    }

    /**
     * 獲取唯一條目（避免重複行）
     */
    private String getUniqueEntry(String line, int index) {
        // 使用顏色代碼和索引來確保唯一性
        String colorCode = ChatColor.values()[index % ChatColor.values().length].toString();
        return colorCode + line + ChatColor.RESET + "§" + index;
    }

    /**
     * 根據延遲值獲取顏色
     */
    private ChatColor getPingColor(int ping) {
        if (ping < 50) {
            return ChatColor.GREEN;      // 綠色：優秀 (<50ms)
        } else if (ping < 100) {
            return ChatColor.YELLOW;     // 黃色：良好 (50-100ms)
        } else if (ping < 200) {
            return ChatColor.GOLD;       // 金色：普通 (100-200ms)
        } else if (ping < 300) {
            return ChatColor.RED;        // 紅色：較差 (200-300ms)
        } else {
            return ChatColor.DARK_RED;   // 深紅色：很差 (>300ms)
        }
    }

    /**
     * 檢查玩家是否有計分板
     */
    public boolean hasScoreboard(Player player) {
        return playerScoreboards.containsKey(player.getUniqueId());
    }

    /**
     * 檢查是否啟用
     */
    public boolean isEnabled() {
        return enabled && config.isEnabled();
    }

    /**
     * 獲取配置管理器
     */
    public ScoreboardConfig getConfig() {
        return config;
    }

    /**
     * 獲取插件實例
     */
    public JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * 設置標題動畫索引
     */
    public void setTitleAnimationIndex(int index) {
        this.titleAnimationIndex = index;
    }

    /**
     * 增加動畫索引
     */
    public void incrementAnimationIndex() {
        titleAnimationIndex++;
        if (titleAnimationIndex >= 10000) {
            titleAnimationIndex = 0;
        }
    }

    public ScoreboardTask getUpdateTask() {
        return updateTask;
    }

    /**
     * 手動為玩家刷新計分板
     */
    public void refreshScoreboard(Player player) {
        if (!enabled || !config.isEnabled()) return;

        if (hasScoreboard(player)) {
            createScoreboard(player);
            player.sendMessage(SYSTEM_PREFIX + "§a計分板已刷新!");
        }
    }
}