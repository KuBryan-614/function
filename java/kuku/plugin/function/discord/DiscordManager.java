package kuku.plugin.function.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Discord 模組管理器 (無權限檢查版本)
 */
public class DiscordManager {

    private final JavaPlugin plugin;
    private final DiscordConfig config;

    private JDA jda;
    private TextChannel textChannel;
    private TextChannel consoleChannel;
    private boolean enabled = false;
    private boolean connected = false;

    public DiscordManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = new DiscordConfig(plugin);
    }

    /**
     * 啟用 Discord 模組
     */
    public void enable() {
        if (enabled) {
            return;
        }

        // 檢查配置
        if (!config.isEnabled()) {
            plugin.getLogger().info("Discord 模組在配置中被禁用");
            return;
        }

        if (!config.isValid()) {
            plugin.getLogger().warning("Discord 配置無效，請檢查 token 和頻道 ID");
            plugin.getLogger().warning("bot_token 和 channel_id 不能是預設值");
            return;
        }

        enabled = true;

        // 啟動 Discord 機器人
        startBot();

        // 註冊事件監聽器
        Bukkit.getPluginManager().registerEvents(new MinecraftListener(this), plugin);

        plugin.getLogger().info("Discord 模組已啟用");
        config.logConfigStatus();
    }

    /**
     * 禁用 Discord 模組
     */
    public void disable() {
        if (!enabled) {
            return;
        }

        enabled = false;

        // 發送伺服器關閉通知
        if (config.isEventServerStopEnabled() && connected) {
            sendShutdownMessage();
        }

        // 關閉 Discord 連接
        shutdownBot();

        plugin.getLogger().info("Discord 模組已禁用");
    }

    /**
     * 重載配置
     */
    public void reload() {
        config.reloadConfig();

        if (enabled) {
            // 檢查是否需要重新連接
            if (!config.isEnabled()) {
                plugin.getLogger().info("配置已變更，Discord 模組被禁用");
                disable();
                return;
            }

            // 重新連接 Discord
            shutdownBot();
            startBot();

            plugin.getLogger().info("Discord 配置已重載");
        }
    }

    /**
     * 啟動 Discord 機器人
     */
    private void startBot() {
        if (!config.isValid()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                plugin.getLogger().info("正在啟動 Discord 機器人...");

                JDABuilder builder = JDABuilder.createDefault(config.getBotToken())
                        .enableIntents(
                                GatewayIntent.GUILD_MEMBERS,
                                GatewayIntent.GUILD_MESSAGES,
                                GatewayIntent.MESSAGE_CONTENT
                        )
                        .setActivity(Activity.playing("在 Minecraft 伺服器遊玩"))
                        .addEventListeners(new DiscordListener(this));

                this.jda = builder.build();
                this.jda.awaitReady();

                // 獲取主聊天頻道
                String channelId = config.getChannelId();
                if (!channelId.isEmpty()) {
                    this.textChannel = jda.getTextChannelById(channelId);
                    if (textChannel == null) {
                        plugin.getLogger().warning("找不到指定的 Discord 頻道: " + channelId);
                    } else {
                        plugin.getLogger().info("已連接至 Discord 頻道: " + textChannel.getName());
                    }
                }

                // 獲取控制台頻道（可選）
                String consoleChannelId = config.getConsoleChannelId();
                if (!consoleChannelId.isEmpty()) {
                    this.consoleChannel = jda.getTextChannelById(consoleChannelId);
                    if (consoleChannel != null) {
                        plugin.getLogger().info("已連接至控制台頻道: " + consoleChannel.getName());
                    }
                }

                this.connected = true;
                plugin.getLogger().info("Discord 機器人啟動成功！");

                // 發送伺服器啟動通知
                if (config.isEventServerStartEnabled()) {
                    sendStartupMessage();
                }

            } catch (InterruptedException e) {
                plugin.getLogger().severe("Discord 啟動被中斷");
                Thread.currentThread().interrupt();
                this.connected = false;
            } catch (IllegalArgumentException e) {
                // 處理無效的 token 或其他參數錯誤
                if (e.getMessage() != null && e.getMessage().toLowerCase().contains("token")) {
                    plugin.getLogger().severe("Discord 登入失敗，請檢查 Token: " + e.getMessage());
                } else {
                    plugin.getLogger().severe("Discord 參數錯誤: " + e.getMessage());
                }
                this.connected = false;
            } catch (Exception e) {
                // 處理其他所有異常
                plugin.getLogger().severe("Discord 啟動失敗: " + e.getMessage());

                // 如果異常信息包含 token 相關的關鍵字
                String errorMsg = e.getMessage();
                if (errorMsg != null &&
                        (errorMsg.toLowerCase().contains("token") ||
                                errorMsg.toLowerCase().contains("login"))) {
                    plugin.getLogger().severe("請檢查 Discord Bot Token 是否正確");
                }

                e.printStackTrace();
                this.connected = false;
            }
        });
    }

    /**
     * 關閉 Discord 機器人
     */
    private void shutdownBot() {
        if (jda != null) {
            try {
                jda.shutdown();
                if (!jda.awaitShutdown(5, TimeUnit.SECONDS)) {
                    jda.shutdownNow();
                }

                plugin.getLogger().info("Discord 機器人已關閉");
            } catch (InterruptedException e) {
                plugin.getLogger().warning("關閉 Discord 機器人時被中斷");
                Thread.currentThread().interrupt();
                jda.shutdownNow();
            } catch (Exception e) {
                plugin.getLogger().warning("關閉 Discord 機器人時出錯: " + e.getMessage());
            } finally {
                jda = null;
                textChannel = null;
                consoleChannel = null;
            }
        }
        connected = false;
    }

    /**
     * 發送啟動通知
     */
    private void sendStartupMessage() {
        if (!connected || textChannel == null) {
            return;
        }

        String message = "🟢 **伺服器已啟動**\n" +
                "📌 版本: " + Bukkit.getVersion() + "\n" +
                "👥 最大玩家: " + Bukkit.getMaxPlayers() + "\n" +
                "⏰ 啟動時間: " + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        sendEmbedMessage("伺服器啟動", message);
    }

    /**
     * 發送關閉通知
     */
    private void sendShutdownMessage() {
        if (!connected || textChannel == null) {
            return;
        }

        int onlineCount = Bukkit.getOnlinePlayers().size();
        String message = "🔴 **伺服器已關閉**\n" +
                "👥 關閉時在線: " + onlineCount + " 位玩家\n" +
                "⏰ 關閉時間: " + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        sendEmbedMessage("伺服器關閉", message);
    }

    // === 公開方法 ===

    /**
     * 發送玩家加入訊息
     */
    public void sendPlayerJoin(String playerName) {
        if (!shouldSendEvent(config.isEventPlayerJoinEnabled())) {
            return;
        }

        String message = String.format("✅ **%s** 加入了伺服器", playerName);
        sendMessage(message);
    }

    /**
     * 發送玩家離開訊息
     */
    public void sendPlayerQuit(String playerName) {
        if (!shouldSendEvent(config.isEventPlayerQuitEnabled())) {
            return;
        }

        String message = String.format("🚪 **%s** 離開了伺服器", playerName);
        sendMessage(message);
    }

    /**
     * 發送玩家聊天訊息
     */
    public void sendPlayerChat(String playerName, String message, String worldName) {
        if (!shouldSendEvent(config.isEventPlayerChatEnabled())) {
            return;
        }

        // 過濾顏色代碼
        String cleanMessage = ChatColor.stripColor(message);

        // 格式化訊息
        String format = config.getChatFormatDiscord()
                .replace("%player%", playerName)
                .replace("%message%", cleanMessage)
                .replace("%world%", worldName);

        sendMessage(format);
    }

    /**
     * 發送玩家死亡訊息
     */
    public void sendPlayerDeath(String deathMessage) {
        if (!shouldSendEvent(config.isEventPlayerDeathEnabled())) {
            return;
        }

        String message = "💀 " + ChatColor.stripColor(deathMessage);
        sendMessage(message);
    }

    /**
     * 發送一般訊息到 Discord
     */
    public void sendMessage(String message) {
        if (!connected || textChannel == null) {
            return;
        }

        // 限制訊息長度
        if (message.length() > config.getMessageMaxLength()) {
            message = message.substring(0, config.getMessageMaxLength() - 3) + "...";
        }

        final String finalMessage = message;
        textChannel.sendMessage(finalMessage).queue(
                success -> {},
                error -> plugin.getLogger().warning("發送訊息至 Discord 失敗: " + error.getMessage())
        );
    }

    /**
     * 發送嵌入式訊息到 Discord
     */
    public void sendEmbedMessage(String title, String description) {
        if (!connected || textChannel == null) {
            return;
        }

        net.dv8tion.jda.api.EmbedBuilder embed = new net.dv8tion.jda.api.EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(java.awt.Color.decode(config.getEmbedColor()))
                .setFooter("來自 Minecraft 伺服器", null)
                .setTimestamp(java.time.Instant.now());

        textChannel.sendMessageEmbeds(embed.build()).queue();
    }

    /**
     * 發送控制台訊息到 Discord
     */
    public void sendConsoleMessage(String message) {
        if (!connected || consoleChannel == null) {
            return;
        }

        // 限制訊息長度
        if (message.length() > 1000) {
            message = message.substring(0, 1000) + "...";
        }

        consoleChannel.sendMessage("```\n" + message + "\n```").queue();
    }

    /**
     * 從 Discord 發送訊息到 Minecraft
     */
    public void sendToMinecraft(String discordUser, String message) {
        if (!enabled) {
            return;
        }

        // 格式化訊息
        String format = config.getChatFormatMinecraft()
                .replace("%user%", discordUser)
                .replace("%message%", message);

        String coloredMessage = ChatColor.translateAlternateColorCodes('&', format);

        // 在主線程廣播訊息
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.broadcastMessage(coloredMessage);
        });

        plugin.getLogger().info("[Discord -> MC] " + discordUser + ": " + message);
    }

    /**
     * 執行 Minecraft 指令（從 Discord）
     */
    public void executeMinecraftCommand(String command, String executor) {
        if (!enabled) {
            return;
        }

        // 執行指令（無權限檢查）
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

                String result = success ? "✅ 指令執行成功" : "❌ 指令執行失敗";
                plugin.getLogger().info("[Discord指令] " + executor + " 執行了: " + command + " (" + (success ? "成功" : "失敗") + ")");

                // 發送回饋到 Discord
                sendMessage(result + ": `" + command + "`");
            } catch (Exception e) {
                plugin.getLogger().warning("執行指令時出錯: " + e.getMessage());
                sendMessage("❌ 指令執行時出錯: `" + command + "`");
            }
        });
    }

    /**
     * 檢查是否應該發送事件
     */
    private boolean shouldSendEvent(boolean eventEnabled) {
        return enabled && connected && textChannel != null && eventEnabled;
    }

    // === Getter 方法 ===

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isConnected() {
        return connected;
    }

    public DiscordConfig getConfig() {
        return config;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public TextChannel getTextChannel() {
        return textChannel;
    }

    public TextChannel getConsoleChannel() {
        return consoleChannel;
    }

    /**
     * 獲取狀態訊息
     */
    public String getStatus() {
        if (!enabled) {
            return "§cDiscord 模組未啟用";
        }

        if (!connected) {
            return "§eDiscord 模組已啟用，但未連接";
        }

        String channelName = textChannel != null ? textChannel.getName() : "未知";
        String consoleName = consoleChannel != null ? consoleChannel.getName() : "未設置";

        return String.format(
                "§aDiscord 模組已啟用並連接\n" +
                        "§7主頻道: §f%s\n" +
                        "§7控制台頻道: §f%s\n" +
                        "§7指令前綴: §f%s",
                channelName, consoleName, config.getCommandPrefix()
        );
    }
}