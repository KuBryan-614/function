package kuku.plugin.function.clearlag;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import kuku.plugin.function.utils.LogUtils;

import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.IOException;

public class ClearLagManager {

    private final JavaPlugin plugin;
    private long cleanInterval;
    private long countdownTime;
    private boolean enabled;
    private boolean showActionBar;
    private boolean broadcastMessages;
    private int taskId;
    private int actionBarTaskId;

    // 玩家设置：分开存储聊天栏和ActionBar设置
    private Map<Player, Boolean> playerChatNotifications;
    private Map<Player, Boolean> playerActionBarNotifications;

    private ClearLagConfig config;

    // 独立配置文件
    private File clearConfigFile;
    private FileConfiguration clearConfig;

    public ClearLagManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playerChatNotifications = new HashMap<>();
        this.playerActionBarNotifications = new HashMap<>();
        this.config = new ClearLagConfig();

        // 初始化配置文件
        setupConfigFile();
    }

    /**
     * 设置配置文件
     */
    private void setupConfigFile() {
        // 确保 configs 文件夹存在
        File configsFolder = new File(plugin.getDataFolder(), "configs");
        if (!configsFolder.exists()) {
            configsFolder.mkdirs();
        }

        // 创建 clear.yml 文件
        clearConfigFile = new File(configsFolder, "clear.yml");

        // 如果文件不存在，创建默认配置
        if (!clearConfigFile.exists()) {
            createDefaultConfig();
        }

        // 加载配置文件
        clearConfig = YamlConfiguration.loadConfiguration(clearConfigFile);
    }

    /**
     * 创建默认配置文件
     */
    private void createDefaultConfig() {
        try {
            // 创建默认配置内容
            String defaultConfig = "# ============================================\n" +
                    "#            清理掉落物模塊設定檔\n" +
                    "#            Clear Lag Module Config\n" +
                    "#        版本: 1.0.0 - 獨立配置文件\n" +
                    "# ============================================\n\n" +
                    "# 🧹 基本設定\n" +
                    "basic:\n" +
                    "  enabled: true              # 模組啟用狀態：主開關\n" +
                    "  clean-interval: 300        # 清理間隔：秒（5分鐘）\n" +
                    "  countdown-time: 10         # 倒計時時間：秒（清理前倒數）\n" +
                    "  show-actionbar: true       # 顯示動作欄：在物品欄上方顯示倒計時\n" +
                    "  broadcast-messages: true   # 廣播訊息：清理時發送遊戲內廣播\n\n" +
                    "# 🔒 物品保護設定\n" +
                    "protection:\n" +
                    "  protect-named-items: false      # 保護命名物品：不清理有自定義名稱的物品\n" +
                    "  protect-enchanted-items: false  # 保護附魔物品：不清理有附魔的物品\n" +
                    "  min-items-to-clear: 0           # 最小清理數量：物品數量少於此值則不清理\n" +
                    "  max-clear-per-cycle: 0          # 每輪最大清理量：0表示無限制\n\n" +
                    "# 💬 消息格式設定\n" +
                    "messages:\n" +
                    "  # 系統前綴設定\n" +
                    "  prefix:\n" +
                    "    enabled: true                 # 啟用前綴顯示\n" +
                    "    format: \"|系統| 垃圾車: \"     # 前綴格式\n" +
                    "    colors:\n" +
                    "      bracket: \"&6\"               # 括號顏色\n" +
                    "      system: \"&e\"                # 系統文字顏色\n" +
                    "      colon: \"&7\"                 # 冒號顏色\n\n" +
                    "  # 具體消息內容\n" +
                    "  content:\n" +
                    "    # 啟用/禁用消息\n" +
                    "    enabled: \"&a✓ &e清理掉落物模塊已啟用！\"\n" +
                    "    disabled: \"&c✗ &e清理掉落物模塊已停用！\"\n" +
                    "    reloaded: \"&a✓ &e配置已重載！\"\n" +
                    "    \n" +
                    "    # 清理消息\n" +
                    "    starting: \"&c⚠ &e將在 {time}&e 秒後清理掉落物！\"\n" +
                    "    countdown: \"&c清理倒計時: {time}秒\"\n" +
                    "    cleared: \"&a✓ &e已清理 {count}&e 個掉落物！\"\n" +
                    "    manual-clear: \"&e管理員手動啟動了清理程序...\"\n" +
                    "    \n" +
                    "    # 玩家設置消息\n" +
                    "    chat-on: \"&e清理聊天欄提示已&a開啟\"\n" +
                    "    chat-off: \"&e清理聊天欄提示已&c關閉\"\n" +
                    "    actionbar-on: \"&e清理物品欄上方提示已&a開啟\"\n" +
                    "    actionbar-off: \"&e清理物品欄上方提示已&c關閉\"\n" +
                    "    \n" +
                    "    # 命令反饋\n" +
                    "    no-permission: \"&c你沒有權限使用此命令！\"\n" +
                    "    player-only: \"&c只有玩家可以使用此命令！\"\n" +
                    "    invalid-number: \"&c請輸入有效的數字！\"\n" +
                    "    interval-too-short: \"&c清理間隔不能小於10秒！\"\n" +
                    "    countdown-too-short: \"&c倒計時時間不能小於1秒！\"\n" +
                    "    \n" +
                    "    # 狀態消息\n" +
                    "    status-chat: \"&e聊天欄提示: {status}\"\n" +
                    "    status-actionbar: \"&e物品欄上方提示: {status}\"\n" +
                    "    status-enabled: \"&a已開啟\"\n" +
                    "    status-disabled: \"&c已關閉\"\n\n" +
                    "# ⚙️ 指令設定\n" +
                    "commands:\n" +
                    "  # 指令別名\n" +
                    "  aliases:\n" +
                    "    - \"clearlag\"\n" +
                    "    - \"cl\"\n" +
                    "    - \"lagclear\"\n\n" +
                    "# 🔄 自動保存設定\n" +
                    "auto-save:\n" +
                    "  enabled: true                 # 啟用自動保存\n" +
                    "  interval: 300                 # 保存間隔（秒）\n\n";

            // 写入文件
            clearConfig = new YamlConfiguration();
            clearConfig.loadFromString(defaultConfig);
            clearConfig.save(clearConfigFile);

            LogUtils.logSuccess(plugin.getLogger(), "清理掉落物", "已创建默认配置文件");
        } catch (Exception e) {
            LogUtils.logError(plugin.getLogger(), "清理掉落物配置文件", "创建失败: " + e.getMessage());
        }
    }

    public void enable() {
        // 加载配置
        loadConfig();

        if (!config.isEnabled()) {
            LogUtils.logModuleLoad(plugin.getLogger(), "清理掉落物", false, "模块禁用");
            return;
        }

        // 启动定时任务
        startTasks();

        // 注册命令
        plugin.getCommand("clearlag").setExecutor(new ClearLagCommand(this));
        plugin.getCommand("clearlag").setTabCompleter(new ClearLagTabCompleter());

        // 发送启用消息
        if (config.isBroadcastMessages()) {
            Bukkit.broadcastMessage(getFormattedMessage(config.getMessages().getEnabled()));
        }

        LogUtils.logModuleLoad(plugin.getLogger(), "清理掉落物", true,
                "清理间隔: " + cleanInterval + "秒, 倒计时: " + countdownTime + "秒");
    }

    public void disable() {
        stopTasks();

        // 清除所有玩家的 ActionBar
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendActionBar(player, "");
        }

        // 发送禁用消息
        if (config.isBroadcastMessages()) {
            Bukkit.broadcastMessage(getFormattedMessage(config.getMessages().getDisabled()));
        }

        // 保存配置文件
        saveConfig();

        LogUtils.logModuleDisable(plugin.getLogger(), "清理掉落物");
    }

    public void reload() {
        stopTasks();

        // 重新加载配置文件
        clearConfig = YamlConfiguration.loadConfiguration(clearConfigFile);
        loadConfig();

        if (config.isEnabled()) {
            startTasks();
        }

        if (config.isBroadcastMessages()) {
            Bukkit.broadcastMessage(getFormattedMessage(config.getMessages().getReloaded()));
        }

        LogUtils.logSuccess(plugin.getLogger(), "清理掉落物", "配置已重载");
    }

    /**
     * 保存配置到文件
     */
    private void saveConfig() {
        try {
            clearConfig.set("basic.enabled", config.isEnabled());
            clearConfig.set("basic.clean-interval", config.getCleanInterval());
            clearConfig.set("basic.countdown-time", config.getCountdownTime());
            clearConfig.set("basic.show-actionbar", config.isShowActionBar());
            clearConfig.set("basic.broadcast-messages", config.isBroadcastMessages());

            // 保存保护设置
            clearConfig.set("protection.protect-named-items", config.isProtectNamedItems());
            clearConfig.set("protection.protect-enchanted-items", config.isProtectEnchantedItems());
            clearConfig.set("protection.min-items-to-clear", config.getMinItemsToClear());
            clearConfig.set("protection.max-clear-per-cycle", config.getMaxClearPerCycle());

            // 保存消息设置
            clearConfig.set("messages.prefix.format", stripColor(config.getMessages().getPrefix()));
            clearConfig.set("messages.content.enabled", stripColor(config.getMessages().getEnabled()));
            clearConfig.set("messages.content.disabled", stripColor(config.getMessages().getDisabled()));
            clearConfig.set("messages.content.reloaded", stripColor(config.getMessages().getReloaded()));
            clearConfig.set("messages.content.starting", stripColor(config.getMessages().getStarting()));
            clearConfig.set("messages.content.countdown", stripColor(config.getMessages().getCountdown()));
            clearConfig.set("messages.content.cleared", stripColor(config.getMessages().getCleared()));
            clearConfig.set("messages.content.manual-clear", stripColor(config.getMessages().getManualClear()));
            clearConfig.set("messages.content.chat-on", stripColor(config.getMessages().getChatOn()));
            clearConfig.set("messages.content.chat-off", stripColor(config.getMessages().getChatOff()));
            clearConfig.set("messages.content.actionbar-on", stripColor(config.getMessages().getActionBarOn()));
            clearConfig.set("messages.content.actionbar-off", stripColor(config.getMessages().getActionBarOff()));
            clearConfig.set("messages.content.status-chat", stripColor(config.getMessages().getStatusChat()));
            clearConfig.set("messages.content.status-actionbar", stripColor(config.getMessages().getStatusActionBar()));
            clearConfig.set("messages.content.status-enabled", stripColor(config.getMessages().getStatusEnabled()));
            clearConfig.set("messages.content.status-disabled", stripColor(config.getMessages().getStatusDisabled()));

            clearConfig.save(clearConfigFile);
        } catch (IOException e) {
            LogUtils.logError(plugin.getLogger(), "清理掉落物配置文件", "保存失败: " + e.getMessage());
        }
    }

    private void loadConfig() {
        // 从独立配置文件读取
        config.setEnabled(clearConfig.getBoolean("basic.enabled", true));
        config.setCleanInterval(clearConfig.getLong("basic.clean-interval", 300));
        config.setCountdownTime(clearConfig.getLong("basic.countdown-time", 10));
        config.setShowActionBar(clearConfig.getBoolean("basic.show-actionbar", true));
        config.setBroadcastMessages(clearConfig.getBoolean("basic.broadcast-messages", true));

        // 保护设置
        config.setProtectNamedItems(clearConfig.getBoolean("protection.protect-named-items", false));
        config.setProtectEnchantedItems(clearConfig.getBoolean("protection.protect-enchanted-items", false));
        config.setMinItemsToClear(clearConfig.getInt("protection.min-items-to-clear", 0));
        config.setMaxClearPerCycle(clearConfig.getInt("protection.max-clear-per-cycle", 0));

        // 消息设置
        ClearLagMessages messages = new ClearLagMessages();
        messages.setPrefix(formatColor(clearConfig.getString("messages.prefix.format", "|系統| 垃圾車: ")));
        messages.setEnabled(formatColor(clearConfig.getString("messages.content.enabled", "&a✓ &e清理掉落物模塊已啟用！")));
        messages.setDisabled(formatColor(clearConfig.getString("messages.content.disabled", "&c✗ &e清理掉落物模塊已停用！")));
        messages.setReloaded(formatColor(clearConfig.getString("messages.content.reloaded", "&a✓ &e配置已重載！")));
        messages.setStarting(formatColor(clearConfig.getString("messages.content.starting", "&c⚠ &e將在 {time}&e 秒後清理掉落物！")));
        messages.setCountdown(formatColor(clearConfig.getString("messages.content.countdown", "&c清理倒計時: {time}秒")));
        messages.setCleared(formatColor(clearConfig.getString("messages.content.cleared", "&a✓ &e已清理 {count}&e 個掉落物！")));
        messages.setManualClear(formatColor(clearConfig.getString("messages.content.manual-clear", "&e管理員手動啟動了清理程序...")));
        messages.setChatOn(formatColor(clearConfig.getString("messages.content.chat-on", "&e清理聊天欄提示已&a開啟")));
        messages.setChatOff(formatColor(clearConfig.getString("messages.content.chat-off", "&e清理聊天欄提示已&c關閉")));
        messages.setActionBarOn(formatColor(clearConfig.getString("messages.content.actionbar-on", "&e清理物品欄上方提示已&a開啟")));
        messages.setActionBarOff(formatColor(clearConfig.getString("messages.content.actionbar-off", "&e清理物品欄上方提示已&c關閉")));
        messages.setStatusChat(formatColor(clearConfig.getString("messages.content.status-chat", "&e聊天欄提示: {status}")));
        messages.setStatusActionBar(formatColor(clearConfig.getString("messages.content.status-actionbar", "&e物品欄上方提示: {status}")));
        messages.setStatusEnabled(formatColor(clearConfig.getString("messages.content.status-enabled", "&a已開啟")));
        messages.setStatusDisabled(formatColor(clearConfig.getString("messages.content.status-disabled", "&c已關閉")));

        config.setMessages(messages);

        this.enabled = config.isEnabled();
        this.cleanInterval = config.getCleanInterval();
        this.countdownTime = config.getCountdownTime();
        this.showActionBar = config.isShowActionBar();
        this.broadcastMessages = config.isBroadcastMessages();
    }

    /**
     * 格式化消息颜色
     */
    private String formatColor(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * 移除颜色代码
     */
    private String stripColor(String message) {
        return ChatColor.stripColor(message);
    }

    /**
     * 获取带前缀的格式化消息
     */
    public String getFormattedMessage(String message) {
        if (config.getMessages().getPrefix() != null) {
            return config.getMessages().getPrefix() + message;
        }
        return message;
    }

    /**
     * 格式化消息（替换变量）
     */
    public String getFormattedMessage(String message, Map<String, String> variables) {
        String formatted = message;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            formatted = formatted.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return getFormattedMessage(formatted);
    }

    private void startTasks() {
        if (!enabled) return;

        // 清理任务
        taskId = new BukkitRunnable() {
            @Override
            public void run() {
                startCountdown();
            }
        }.runTaskTimer(plugin, cleanInterval * 20L, cleanInterval * 20L).getTaskId();
    }

    private void startCountdown() {
        if (broadcastMessages) {
            // 发送倒计时开始消息（只发送给开启了聊天栏提示的玩家）
            Map<String, String> vars = new HashMap<>();
            vars.put("time", String.valueOf(countdownTime));
            String startingMessage = getFormattedMessage(
                    config.getMessages().getStarting(),
                    vars
            );

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (getPlayerChatNotification(player)) {
                    player.sendMessage(startingMessage);
                }
            }
        }

        // 启动倒计时 ActionBar 显示
        final int[] remainingTime = {(int) countdownTime};

        actionBarTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (remainingTime[0] <= 0) {
                    clearItems();
                    this.cancel();
                    return;
                }

                // 发送 ActionBar 给所有在线玩家（只发送给开启了ActionBar提示的玩家）
                if (showActionBar) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (getPlayerActionBarNotification(player)) {
                            sendActionBar(player, createCountdownMessage(remainingTime[0]));
                        }
                    }
                }

                // 倒计时提示（只发送给开启了聊天栏提示的玩家）
                if (broadcastMessages && remainingTime[0] <= 5) {
                    Map<String, String> vars = new HashMap<>();
                    vars.put("time", String.valueOf(remainingTime[0]));
                    String countdownMessage = getFormattedMessage(
                            config.getMessages().getCountdown(),
                            vars
                    );

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (getPlayerChatNotification(player)) {
                            player.sendMessage(countdownMessage);
                        }
                    }
                }

                remainingTime[0]--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }

    private String createCountdownMessage(int seconds) {
        // 创建进度条
        int progressBarLength = 20;
        int progress = (int) ((double) seconds / countdownTime * progressBarLength);
        StringBuilder progressBar = new StringBuilder();

        progressBar.append(ChatColor.RED);
        for (int i = 0; i < progressBarLength; i++) {
            if (i < progress) {
                progressBar.append("█");
            } else {
                progressBar.append("░");
            }
        }

        // 根据剩余时间改变颜色
        ChatColor timeColor;
        if (seconds > 5) {
            timeColor = ChatColor.YELLOW;
        } else {
            timeColor = ChatColor.RED;
        }

        // ActionBar 不添加前缀
        return ChatColor.YELLOW + "清理倒计时: " + timeColor +
                String.format("%02d", seconds) + ChatColor.YELLOW + "秒 " +
                progressBar.toString();
    }

    private void sendActionBar(Player player, String message) {
        player.sendActionBar(message);
    }

    private boolean isProtected(Item item) {
        if (config.isProtectNamedItems() && item.getItemStack().hasItemMeta()
                && item.getItemStack().getItemMeta().hasDisplayName()) {
            return true;
        }

        if (config.isProtectEnchantedItems() && item.getItemStack().hasItemMeta()
                && item.getItemStack().getItemMeta().hasEnchants()) {
            return true;
        }

        // 检查最小数量限制
        if (config.getMinItemsToClear() > 0 &&
                item.getItemStack().getAmount() < config.getMinItemsToClear()) {
            return true;
        }

        return false;
    }

    private void stopTasks() {
        if (taskId != 0) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = 0;
        }
        if (actionBarTaskId != 0) {
            Bukkit.getScheduler().cancelTask(actionBarTaskId);
            actionBarTaskId = 0;
        }
    }

    private void clearItems() {
        int removedItems = 0;
        int maxClear = config.getMaxClearPerCycle();

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item) {
                    Item item = (Item) entity;

                    if (isProtected(item)) {
                        continue;
                    }

                    item.remove();
                    removedItems++;

                    if (maxClear > 0 && removedItems >= maxClear) {
                        break;
                    }
                }
                if (maxClear > 0 && removedItems >= maxClear) {
                    break;
                }
            }
            if (maxClear > 0 && removedItems >= maxClear) {
                break;
            }
        }

        if (broadcastMessages) {
            // 发送清理完成消息（只发送给开启了聊天栏提示的玩家）
            Map<String, String> vars = new HashMap<>();
            vars.put("count", String.valueOf(removedItems));
            String clearedMessage = getFormattedMessage(
                    config.getMessages().getCleared(),
                    vars
            );

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (getPlayerChatNotification(player)) {
                    player.sendMessage(clearedMessage);
                }
            }
        }

        if (actionBarTaskId != 0) {
            Bukkit.getScheduler().cancelTask(actionBarTaskId);
            actionBarTaskId = 0;
        }

        // 清除所有玩家的 ActionBar
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendActionBar(player, "");
        }
    }

    /**
     * 设置玩家聊天栏通知状态
     */
    public void setPlayerChatNotification(Player player, boolean enabled) {
        playerChatNotifications.put(player, enabled);
    }

    /**
     * 获取玩家聊天栏通知状态
     */
    public boolean getPlayerChatNotification(Player player) {
        // 默认返回true（开启）
        return playerChatNotifications.getOrDefault(player, true);
    }

    /**
     * 设置玩家ActionBar通知状态
     */
    public void setPlayerActionBarNotification(Player player, boolean enabled) {
        playerActionBarNotifications.put(player, enabled);
        if (!enabled) {
            sendActionBar(player, "");
        }
    }

    /**
     * 获取玩家ActionBar通知状态
     */
    public boolean getPlayerActionBarNotification(Player player) {
        // 默认返回true（开启）
        return playerActionBarNotifications.getOrDefault(player, true);
    }

    // 立即清理命令
    public void clearNow() {
        if (broadcastMessages) {
            // 发送手动清理消息（只发送给开启了聊天栏提示的玩家）
            String manualClearMessage = getFormattedMessage(
                    config.getMessages().getManualClear()
            );

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (getPlayerChatNotification(player)) {
                    player.sendMessage(manualClearMessage);
                }
            }
        }
        clearItems();
    }

    public ClearLagConfig getConfig() {
        return config;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 发送测试 ActionBar 给玩家
     */
    public void sendTestActionBar(Player player) {
        if (!showActionBar) {
            player.sendMessage(getFormattedMessage(ChatColor.YELLOW + "ActionBar功能已停用"));
            return;
        }

        String testMessage = ChatColor.GREEN + "✓ " + ChatColor.YELLOW +
                "清理掉落物ActionBar测试";
        sendActionBar(player, testMessage);

        // 5秒后清除
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            sendActionBar(player, "");
        }, 100L); // 5秒 = 100 ticks
    }

    /**
     * 获取配置文件路径
     */
    public String getConfigPath() {
        return clearConfigFile.getAbsolutePath();
    }

    /**
     * 获取配置文件名
     */
    public String getConfigFileName() {
        return clearConfigFile.getName();
    }
}