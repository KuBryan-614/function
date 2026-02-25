package kuku.plugin.function.discord;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Discord 事件監聽器 (簡化版本)
 */
public class DiscordListener extends ListenerAdapter {

    private final DiscordManager manager;

    public DiscordListener(DiscordManager manager) {
        this.manager = manager;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // 忽略機器人自己的訊息
        if (event.getAuthor().isBot()) {
            return;
        }

        String message = event.getMessage().getContentRaw();
        String userName = event.getAuthor().getName();
        String channelId = event.getChannel().getId();

        // 檢查是否在主聊天頻道
        if (manager.getTextChannel() != null &&
                channelId.equals(manager.getTextChannel().getId())) {
            handleChatChannelMessage(message, userName);
        }

        // 檢查是否在控制台頻道
        else if (manager.getConsoleChannel() != null &&
                channelId.equals(manager.getConsoleChannel().getId())) {
            handleConsoleChannelMessage(message, userName);
        }
    }

    /**
     * 處理主聊天頻道訊息
     */
    private void handleChatChannelMessage(String message, String userName) {
        // 檢查是否為指令
        String prefix = manager.getConfig().getCommandPrefix();
        if (message.startsWith(prefix)) {
            handleCommand(message.substring(prefix.length()), userName);
            return;
        }

        // 普通聊天訊息
        manager.sendToMinecraft(userName, message);
    }

    /**
     * 處理控制台頻道訊息
     */
    private void handleConsoleChannelMessage(String message, String userName) {
        // 在控制台頻道中，所有訊息都視為指令
        manager.executeMinecraftCommand(message, userName);
    }

    /**
     * 處理指令
     */
    private void handleCommand(String command, String userName) {
        String[] args = command.split(" ");
        String cmd = args[0].toLowerCase();

        switch (cmd) {
            case "help":
                sendHelp(userName);
                break;
            case "status":
                sendStatus();
                break;
            case "players":
                sendOnlinePlayers();
                break;
            case "ip":
            case "address":
                sendServerAddress();
                break;
            default:
                // 如果不是內建指令，嘗試執行 Minecraft 指令
                manager.executeMinecraftCommand(command, userName);
        }
    }

    private void sendHelp(String userName) {
        String prefix = manager.getConfig().getCommandPrefix();
        String help = "📖 **可用指令**\n" +
                "`" + prefix + "help` - 顯示此幫助\n" +
                "`" + prefix + "status` - 顯示伺服器狀態\n" +
                "`" + prefix + "players` - 顯示在線玩家\n" +
                "`" + prefix + "ip` - 顯示伺服器地址\n" +
                "\n**執行指令**:\n" +
                "`" + prefix + "<mc指令>` - 執行 Minecraft 指令\n" +
                "例如: `" + prefix + "list` 或 `" + prefix + "time set day`";

        manager.sendMessage(help);
    }

    private void sendStatus() {
        int online = org.bukkit.Bukkit.getOnlinePlayers().size();
        int max = org.bukkit.Bukkit.getMaxPlayers();
        String tps = String.format("%.2f", org.bukkit.Bukkit.getServer().getTPS()[0]);

        String status = "🟢 **伺服器狀態**\n" +
                "👥 在線玩家: " + online + "/" + max + "\n" +
                "⚡ TPS: " + tps + "\n" +
                "⏰ 時間: " + java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "\n" +
                "📅 日期: " + java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        manager.sendMessage(status);
    }

    private void sendOnlinePlayers() {
        java.util.List<org.bukkit.entity.Player> players =
                new java.util.ArrayList<>(org.bukkit.Bukkit.getOnlinePlayers());

        if (players.isEmpty()) {
            manager.sendMessage("👻 目前沒有玩家在線");
            return;
        }

        StringBuilder playerList = new StringBuilder("👥 **在線玩家 (" + players.size() + ")**\n");
        for (int i = 0; i < players.size(); i++) {
            playerList.append(i + 1).append(". ").append(players.get(i).getName());
            if (i < players.size() - 1) {
                playerList.append("\n");
            }
        }

        manager.sendMessage(playerList.toString());
    }

    private void sendServerAddress() {
        String ip = org.bukkit.Bukkit.getServer().getIp();
        int port = org.bukkit.Bukkit.getServer().getPort();

        if (ip == null || ip.isEmpty() || ip.equals("0.0.0.0")) {
            ip = "伺服器IP";
        }

        String address = "🌐 **伺服器地址**\n" +
                "IP: `" + ip + ":" + port + "`\n" +
                "版本: " + org.bukkit.Bukkit.getVersion() + "\n" +
                "類型: " + (org.bukkit.Bukkit.getServer().getOnlineMode() ? "正版" : "離線");

        manager.sendMessage(address);
    }
}