package kuku.plugin.function.clearlag;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClearLagTabCompleter implements TabCompleter {

    // 主命令补全列表
    private static final List<String> MAIN_COMMANDS = Arrays.asList(
            "help", "chat", "actionbar", "status", "now", "reload", "setinterval", "setcountdown", "test", "config"
    );

    // 管理员命令列表
    private static final List<String> ADMIN_COMMANDS = Arrays.asList(
            "now", "status", "reload", "setinterval", "setcountdown", "test", "config"
    );

    // 玩家命令列表
    private static final List<String> PLAYER_COMMANDS = Arrays.asList(
            "help", "chat", "actionbar", "status"
    );

    // 设置项补全
    private static final List<String> SETTINGS = Arrays.asList(
            "interval", "countdown", "actionbar", "broadcast",
            "protect-named", "protect-enchanted", "min-items", "max-clear"
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // 如果没有参数，返回所有可用的命令
        if (args.length == 1) {
            List<String> availableCommands = getAvailableCommands(sender);

            for (String cmd : availableCommands) {
                if (cmd.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(cmd);
                }
            }

            // 如果没有匹配的，返回所有可用命令
            if (completions.isEmpty() && args[0].isEmpty()) {
                return availableCommands;
            }

            return completions.stream().sorted().collect(Collectors.toList());
        }

        // 第二级参数补全
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "setinterval":
                case "setcountdown":
                    // 提供一些常用的时间值
                    completions.addAll(Arrays.asList("60", "120", "300", "600", "1200"));
                    break;

                case "reload":
                    // 提供配置项选择
                    completions.addAll(Arrays.asList("config", "all"));
                    break;

                case "test":
                    // 测试选项
                    completions.addAll(Arrays.asList("countdown", "actionbar", "clear"));
                    break;

                case "config":
                    // 配置选项
                    completions.addAll(Arrays.asList("path"));
                    break;
            }

            // 过滤以当前输入开头的项
            if (!args[1].isEmpty()) {
                completions = completions.stream()
                        .filter(c -> c.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }

            return completions;
        }

        // 超过2个参数，返回空列表
        return new ArrayList<>();
    }

    /**
     * 获取发送者可用的命令列表
     */
    private List<String> getAvailableCommands(CommandSender sender) {
        List<String> commands = new ArrayList<>();

        if (sender instanceof Player) {
            Player player = (Player) sender;

            // 玩家可用的基础命令
            commands.addAll(PLAYER_COMMANDS);

            // 管理员命令检查
            if (player.hasPermission("kuku.clearlag.admin")) {
                commands.addAll(ADMIN_COMMANDS);

                // 去重
                commands = commands.stream().distinct().collect(Collectors.toList());
            }
        } else {
            // 控制台拥有所有命令权限
            commands.addAll(MAIN_COMMANDS);
        }

        return commands;
    }

    /**
     * 智能命令建议（根据权限和上下文）
     */
    public List<String> getSmartSuggestions(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return getAvailableCommands(sender);
        }

        String currentArg = args[args.length - 1].toLowerCase();
        List<String> suggestions = new ArrayList<>();

        // 根据不同的命令提供不同的建议
        if (args.length == 1) {
            // 第一个参数 - 命令名
            suggestions = getAvailableCommands(sender).stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(currentArg))
                    .collect(Collectors.toList());

            // 如果当前输入为空或只有一个字符，提供最常用的命令
            if (currentArg.isEmpty() || currentArg.length() <= 1) {
                List<String> commonCommands = new ArrayList<>();

                // 所有玩家都有的常用命令
                commonCommands.add("help");

                if (sender instanceof Player) {
                    commonCommands.add("chat");
                    commonCommands.add("actionbar");
                    commonCommands.add("status");
                }

                if (sender.hasPermission("kuku.clearlag.admin")) {
                    commonCommands.add("now");
                    commonCommands.add("reload");
                }

                // 只保留匹配的
                suggestions = commonCommands.stream()
                        .filter(cmd -> cmd.toLowerCase().startsWith(currentArg))
                        .collect(Collectors.toList());
            }
        }

        return suggestions;
    }

    /**
     * 获取命令描述（用于帮助信息）
     */
    public static String getCommandDescription(String command) {
        switch (command.toLowerCase()) {
            case "help":
                return "顯示此幫助訊息";
            case "chat":
                return "開關聊天欄清理提示";
            case "actionbar":
                return "開關物品欄上方清理提示";
            case "status":
                return "查看你的清理設置狀態";
            case "now":
                return "立即清理掉落物";
            case "reload":
                return "重載清理模組配置";
            case "setinterval":
                return "設定清理間隔時間";
            case "setcountdown":
                return "設定清理倒數時間";
            case "test":
                return "測試清理模組功能";
            case "config":
                return "查看配置檔案信息";
            default:
                return "未知命令";
        }
    }
}