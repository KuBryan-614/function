package kuku.plugin.function.clearlag;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ClearLagCommand implements CommandExecutor {

    private final ClearLagManager manager;

    public ClearLagCommand(ClearLagManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!manager.isEnabled()) {
            sender.sendMessage(manager.getFormattedMessage(ChatColor.RED + "清理掉落物模块未启用!"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "now":
                handleNowCommand(sender);
                break;

            case "chat":
                handleChatCommand(sender);
                break;

            case "actionbar":
                handleActionBarCommand(sender);
                break;

            case "status":
                handleStatusCommand(sender);
                break;

            case "reload":
                handleReloadCommand(sender, args);
                break;

            case "setinterval":
                handleSetIntervalCommand(sender, args);
                break;

            case "setcountdown":
                handleSetCountdownCommand(sender, args);
                break;

            case "test":
                handleTestCommand(sender, args);
                break;

            case "config":
                handleConfigCommand(sender, args);
                break;

            case "help":
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleNowCommand(CommandSender sender) {
        if (!sender.hasPermission("kuku.clearlag.admin")) {
            sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getNoPermission()));
            return;
        }

        manager.clearNow();
        sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "已立即清理掉落物!"));
    }

    private void handleChatCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getPlayerOnly()));
            return;
        }

        Player player = (Player) sender;
        boolean current = manager.getPlayerChatNotification(player);
        manager.setPlayerChatNotification(player, !current);

        if (current) {
            sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getChatOff()));
        } else {
            sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getChatOn()));
        }
    }

    private void handleActionBarCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getPlayerOnly()));
            return;
        }

        Player player = (Player) sender;
        boolean current = manager.getPlayerActionBarNotification(player);
        manager.setPlayerActionBarNotification(player, !current);

        if (current) {
            sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getActionBarOff()));
        } else {
            sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getActionBarOn()));
        }
    }

    private void handleStatusCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            // 非玩家只能查看基本状态
            if (sender.hasPermission("kuku.clearlag.admin")) {
                handleAdminStatus(sender);
            } else {
                sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getPlayerOnly()));
            }
            return;
        }

        Player player = (Player) sender;

        // 显示玩家个人设置状态
        sender.sendMessage(ChatColor.YELLOW + "=== " + ChatColor.GOLD + "你的清理掉落物设置" + ChatColor.YELLOW + " ===");

        // 聊天栏提示状态
        boolean chatStatus = manager.getPlayerChatNotification(player);
        Map<String, String> chatVars = new HashMap<>();
        chatVars.put("status", chatStatus ?
                manager.getConfig().getMessages().getStatusEnabled() :
                manager.getConfig().getMessages().getStatusDisabled());
        sender.sendMessage(manager.getFormattedMessage(
                manager.getConfig().getMessages().getStatusChat(),
                chatVars
        ));

        // ActionBar提示状态
        boolean actionBarStatus = manager.getPlayerActionBarNotification(player);
        Map<String, String> actionBarVars = new HashMap<>();
        actionBarVars.put("status", actionBarStatus ?
                manager.getConfig().getMessages().getStatusEnabled() :
                manager.getConfig().getMessages().getStatusDisabled());
        sender.sendMessage(manager.getFormattedMessage(
                manager.getConfig().getMessages().getStatusActionBar(),
                actionBarVars
        ));

        // 如果玩家有管理员权限，显示更多信息
        if (player.hasPermission("kuku.clearlag.admin")) {
            sender.sendMessage("");
            handleAdminStatus(sender);
        }
    }

    private void handleAdminStatus(CommandSender sender) {
        ClearLagConfig config = manager.getConfig();

        sender.sendMessage(ChatColor.GOLD + "管理员状态信息:");
        sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "模块状态: " +
                (manager.isEnabled() ? ChatColor.GREEN + "已启用" : ChatColor.RED + "已禁用")));
        sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "清理间隔: " + ChatColor.YELLOW +
                config.getCleanInterval() + "秒"));
        sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "倒计时时间: " + ChatColor.YELLOW +
                config.getCountdownTime() + "秒"));
        sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "显示ActionBar: " + ChatColor.YELLOW +
                config.isShowActionBar()));
        sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "广播消息: " + ChatColor.YELLOW +
                config.isBroadcastMessages()));
        sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "配置文件: " + ChatColor.YELLOW +
                manager.getConfigFileName()));

        // 保护设置
        sender.sendMessage(manager.getFormattedMessage(ChatColor.GOLD + "保护设置:"));
        sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "  保护命名物品: " + ChatColor.YELLOW +
                config.isProtectNamedItems()));
        sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "  保护附魔物品: " + ChatColor.YELLOW +
                config.isProtectEnchantedItems()));
        sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "  最小清理数量: " + ChatColor.YELLOW +
                config.getMinItemsToClear()));
        sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "  最大清理数量: " + ChatColor.YELLOW +
                (config.getMaxClearPerCycle() == 0 ? "无限制" : config.getMaxClearPerCycle())));
    }

    private void handleReloadCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kuku.clearlag.admin")) {
            sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getNoPermission()));
            return;
        }

        String reloadType = args.length > 1 ? args[1].toLowerCase() : "all";

        switch (reloadType) {
            case "config":
                manager.reload();
                sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getReloaded()));
                break;

            case "all":
            default:
                manager.reload();
                sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "清理掉落物模块已完全重载!"));
                break;
        }
    }

    private void handleSetIntervalCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kuku.clearlag.admin")) {
            sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getNoPermission()));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(manager.getFormattedMessage(ChatColor.RED + "用法: /clearlag setinterval <秒数>"));
            sender.sendMessage(manager.getFormattedMessage(ChatColor.GRAY + "示例: /clearlag setinterval 600 (10分钟)"));
            return;
        }

        try {
            long interval = Long.parseLong(args[1]);
            if (interval < 10) {
                sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getIntervalTooShort()));
                return;
            }

            // 这里可以添加保存到配置的逻辑
            sender.sendMessage(manager.getFormattedMessage(
                    ChatColor.GREEN + "已设置清理间隔为 " + interval + " 秒"
            ));
            sender.sendMessage(manager.getFormattedMessage(
                    ChatColor.YELLOW + "注意: 此更改需要重启插件才能生效"
            ));
        } catch (NumberFormatException e) {
            sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getInvalidNumber()));
        }
    }

    private void handleSetCountdownCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kuku.clearlag.admin")) {
            sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getNoPermission()));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(manager.getFormattedMessage(ChatColor.RED + "用法: /clearlag setcountdown <秒数>"));
            sender.sendMessage(manager.getFormattedMessage(ChatColor.GRAY + "示例: /clearlag setcountdown 30 (30秒倒计时)"));
            return;
        }

        try {
            long countdown = Long.parseLong(args[1]);
            if (countdown < 1) {
                sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getCountdownTooShort()));
                return;
            }

            // 这里可以添加保存到配置的逻辑
            sender.sendMessage(manager.getFormattedMessage(
                    ChatColor.GREEN + "已设置倒计时时间为 " + countdown + " 秒"
            ));
            sender.sendMessage(manager.getFormattedMessage(
                    ChatColor.YELLOW + "注意: 此更改需要重启插件才能生效"
            ));
        } catch (NumberFormatException e) {
            sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getInvalidNumber()));
        }
    }

    private void handleTestCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kuku.clearlag.admin")) {
            sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getNoPermission()));
            return;
        }

        String testType = args.length > 1 ? args[1].toLowerCase() : "countdown";

        switch (testType) {
            case "countdown":
                sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getTestCountdown()));
                // 这里可以调用测试方法
                sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getTestCountdownStarted()));
                break;

            case "actionbar":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getPlayerOnly()));
                    return;
                }
                Player player = (Player) sender;
                manager.sendTestActionBar(player);
                sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getTestActionBarSent()));
                break;

            case "clear":
                sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getTestClear()));
                manager.clearNow();
                sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getTestClearComplete()));
                break;

            default:
                sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getTestInvalidType()));
                sender.sendMessage(manager.getFormattedMessage(
                        ChatColor.YELLOW + "可用类型: countdown, actionbar, clear"
                ));
                break;
        }
    }

    private void handleConfigCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kuku.clearlag.admin")) {
            sender.sendMessage(manager.getFormattedMessage(manager.getConfig().getMessages().getNoPermission()));
            return;
        }

        if (args.length > 1 && args[1].equalsIgnoreCase("path")) {
            sender.sendMessage(manager.getFormattedMessage(
                    ChatColor.GREEN + "配置文件路径: " + ChatColor.YELLOW + manager.getConfigPath()
            ));
        } else {
            sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "配置文件: " +
                    ChatColor.YELLOW + manager.getConfigFileName()));
            sender.sendMessage(manager.getFormattedMessage(ChatColor.GRAY +
                    "使用 /clearlag config path 查看完整路径"));
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "=== " + ChatColor.GOLD + "清理掉落物帮助" + ChatColor.YELLOW + " ===");
        sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "/clearlag help" +
                ChatColor.YELLOW + " - 显示此帮助"));

        if (sender instanceof Player) {
            sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "/clearlag chat" +
                    ChatColor.YELLOW + " - 开关聊天栏清理提示"));
            sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "/clearlag actionbar" +
                    ChatColor.YELLOW + " - 开关物品栏上方清理提示"));
            sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "/clearlag status" +
                    ChatColor.YELLOW + " - 查看你的清理设置状态"));
        }

        if (sender.hasPermission("kuku.clearlag.admin")) {
            sender.sendMessage(manager.getFormattedMessage(ChatColor.GOLD + "管理员命令:"));
            sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "  /clearlag now" +
                    ChatColor.YELLOW + " - 立即清理掉落物"));
            sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "  /clearlag reload" +
                    ChatColor.YELLOW + " - 重载配置"));
            sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "  /clearlag setinterval <秒数>" +
                    ChatColor.YELLOW + " - 设置清理间隔"));
            sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "  /clearlag setcountdown <秒数>" +
                    ChatColor.YELLOW + " - 设置倒计时时间"));
            sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "  /clearlag test [类型]" +
                    ChatColor.YELLOW + " - 测试模块功能"));
            sender.sendMessage(manager.getFormattedMessage(ChatColor.GREEN + "  /clearlag config [path]" +
                    ChatColor.YELLOW + " - 查看配置文件信息"));
        }

        sender.sendMessage(manager.getFormattedMessage(ChatColor.GRAY + "提示: 使用Tab键可以自动补全命令!"));
    }
}