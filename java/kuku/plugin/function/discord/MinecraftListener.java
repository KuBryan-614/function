package kuku.plugin.function.discord;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.BroadcastMessageEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Minecraft 事件監聽器 (增強版)
 */
public class MinecraftListener implements Listener {

    private final DiscordManager manager;

    // 指令白名單（只記錄這些指令）
    private final Set<String> commandWhitelist = new HashSet<>();

    // 敏感指令黑名單（不記錄這些指令）
    private final Set<String> commandBlacklist = new HashSet<>();

    public MinecraftListener(DiscordManager manager) {
        this.manager = manager;
        initializeCommandLists();
    }

    private void initializeCommandLists() {
        // 可以記錄的指令
        commandWhitelist.add("say");
        commandWhitelist.add("broadcast");
        commandWhitelist.add("announce");

        // 敏感指令，不記錄
        commandBlacklist.add("op");
        commandBlacklist.add("deop");
        commandBlacklist.add("give");
        commandBlacklist.add("gamemode");
        commandBlacklist.add("ban");
        commandBlacklist.add("pardon");
        commandBlacklist.add("whitelist");
        commandBlacklist.add("stop");
        commandBlacklist.add("restart");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!manager.isEnabled() || !manager.isConnected()) {
            return;
        }

        Player player = event.getPlayer();
        String playerName = player.getName();

        // 檢查是否為新玩家
        boolean isFirstJoin = !player.hasPlayedBefore();

        if (isFirstJoin) {
            manager.sendEmbedMessage(
                    "新玩家加入",
                    "🎉 **" + playerName + "** 第一次加入伺服器！\n" +
                            "👋 歡迎新玩家！"
            );
        } else {
            manager.sendEmbedMessage(
                    "玩家加入",
                    "✅ **" + playerName + "** 加入了遊戲\n" +
                            "📍 上次登入: " + formatLastPlayed(player.getLastPlayed()) + "\n" +
                            "👥 在線玩家: " + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers()
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!manager.isEnabled() || !manager.isConnected()) {
            return;
        }

        Player player = event.getPlayer();
        String playerName = player.getName();

        manager.sendEmbedMessage(
                "玩家離開",
                "🚪 **" + playerName + "** 離開了遊戲\n" +
                        "⏱️ 遊戲時間: " + formatPlayTime(player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE)) + "\n" +
                        "👥 剩餘玩家: " + (Bukkit.getOnlinePlayers().size() - 1) + "/" + Bukkit.getMaxPlayers()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!manager.isEnabled() || !manager.isConnected() || event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        String playerName = player.getName();
        String message = event.getMessage();

        // 檢查是否為管理員消息
        String prefix = player.isOp() || player.hasPermission("discord.admin") ? "👑 " : "";

        // 發送聊天消息
        manager.sendMessage(prefix + "**" + playerName + "**: " + ChatColor.stripColor(message));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!manager.isEnabled() || !manager.isConnected()) {
            return;
        }

        String deathMessage = event.getDeathMessage();
        if (deathMessage != null && !deathMessage.isEmpty()) {
            // 美化死亡消息
            String formattedDeath = ChatColor.stripColor(deathMessage);
            manager.sendMessage("💀 " + formattedDeath);

            // 如果有玩家死亡，記錄詳細信息
            if (event.getEntity() != null) {
                Player player = event.getEntity();
                manager.sendEmbedMessage(
                        "玩家死亡",
                        "💀 **" + player.getName() + "** 死亡了\n" +
                                "📝 原因: " + formattedDeath + "\n" +
                                "📍 位置: " + formatLocation(player.getLocation())
                );
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerCommand(ServerCommandEvent event) {
        if (!manager.isEnabled() || !manager.isConnected()) {
            return;
        }

        String fullCommand = event.getCommand().trim();

        // 提取指令名稱
        String[] parts = fullCommand.split(" ");
        if (parts.length == 0) return;

        String commandName = parts[0].toLowerCase();

        // 檢查是否為say指令
        if (commandName.equals("say") && parts.length > 1) {
            StringBuilder message = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                message.append(parts[i]).append(" ");
            }

            manager.sendEmbedMessage(
                    "伺服器公告",
                    "📢 **伺服器公告**\n" +
                            message.toString().trim() + "\n\n" +
                            "👤 發佈者: 控制台"
            );
        }

        // 記錄重要指令（排除敏感指令）
        else if (shouldLogCommand(commandName, fullCommand)) {
            manager.sendEmbedMessage(
                    "指令執行",
                    "⚡ **控制台指令**\n" +
                            "`" + fullCommand + "`\n\n" +
                            "👤 執行者: 控制台"
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBroadcastMessage(BroadcastMessageEvent event) {
        if (!manager.isEnabled() || !manager.isConnected()) {
            return;
        }

        String message = event.getMessage();
        if (message != null && !message.isEmpty()) {
            manager.sendEmbedMessage(
                    "伺服器廣播",
                    "📣 **伺服器廣播**\n" +
                            ChatColor.stripColor(message)
            );
        }
    }

    // === 輔助方法 ===

    private boolean shouldLogCommand(String commandName, String fullCommand) {
        // 檢查黑名單
        for (String blacklisted : commandBlacklist) {
            if (commandName.startsWith(blacklisted)) {
                return false;
            }
        }

        // 檢查白名單
        for (String whitelisted : commandWhitelist) {
            if (commandName.startsWith(whitelisted)) {
                return true;
            }
        }

        // 默認不記錄其他指令
        return false;
    }

    private String formatLastPlayed(long lastPlayed) {
        if (lastPlayed == 0) return "從未";

        long diff = System.currentTimeMillis() - lastPlayed;
        long days = diff / (1000 * 60 * 60 * 24);

        if (days == 0) {
            long hours = diff / (1000 * 60 * 60);
            if (hours == 0) {
                long minutes = diff / (1000 * 60);
                return minutes + " 分鐘前";
            }
            return hours + " 小時前";
        }

        return days + " 天前";
    }

    private String formatPlayTime(int ticks) {
        int minutes = ticks / (20 * 60);
        if (minutes < 60) {
            return minutes + " 分鐘";
        }

        int hours = minutes / 60;
        if (hours < 24) {
            return hours + " 小時";
        }

        int days = hours / 24;
        return days + " 天";
    }

    private String formatLocation(org.bukkit.Location location) {
        if (location == null) return "未知";

        return String.format(
                "世界: %s, X: %d, Y: %d, Z: %d",
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }
}