package kuku.plugin.function.discord;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Discord 配置管理器 (無權限檢查版本)
 */
public class DiscordConfig {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public DiscordConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        reloadConfig();
    }

    /**
     * 重載配置
     */
    public void reloadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    /**
     * 檢查配置是否有效
     */
    public boolean isValid() {
        return isEnabled() &&
                !getBotToken().isEmpty() &&
                !getBotToken().equals("YOUR_BOT_TOKEN_HERE") &&
                !getChannelId().isEmpty() &&
                !getChannelId().equals("YOUR_CHANNEL_ID_HERE");
    }

    // === 基本設置 ===

    public boolean isEnabled() {
        return config.getBoolean("enabled", true);
    }

    public String getBotToken() {
        return config.getString("bot_token", "");
    }

    public String getGuildId() {
        return config.getString("guild_id", "");
    }

    public String getChannelId() {
        return config.getString("channel_id", "");
    }

    public String getConsoleChannelId() {
        return config.getString("console_channel_id", "");
    }

    public String getCommandPrefix() {
        return config.getString("prefix", "!");
    }

    public String getEmbedColor() {
        return config.getString("embed_color", "#3498db");
    }

    public int getMessageMaxLength() {
        return config.getInt("message_max_length", 1500);
    }

    // === 聊天格式 ===

    public String getChatFormatMinecraft() {
        return config.getString("chat_format.minecraft", "&9[Discord] &b%user%&f: %message%");
    }

    public String getChatFormatDiscord() {
        return config.getString("chat_format.discord", "`[%world%]` **%player%**: %message%");
    }

    // === 事件開關 ===

    public boolean isEventPlayerJoinEnabled() {
        return config.getBoolean("events.player_join", true);
    }

    public boolean isEventPlayerQuitEnabled() {
        return config.getBoolean("events.player_quit", true);
    }

    public boolean isEventPlayerChatEnabled() {
        return config.getBoolean("events.player_chat", true);
    }

    public boolean isEventPlayerDeathEnabled() {
        return config.getBoolean("events.player_death", true);
    }

    public boolean isEventServerStartEnabled() {
        return config.getBoolean("events.server_start", true);
    }

    public boolean isEventServerStopEnabled() {
        return config.getBoolean("events.server_stop", true);
    }

    /**
     * 記錄配置狀態
     */
    public void logConfigStatus() {
        plugin.getLogger().info("=== Discord 配置狀態 ===");
        plugin.getLogger().info("啟用狀態: " + isEnabled());
        plugin.getLogger().info("頻道ID: " + getChannelId());
        plugin.getLogger().info("指令前綴: " + getCommandPrefix());
        plugin.getLogger().info("=======================");
    }
}