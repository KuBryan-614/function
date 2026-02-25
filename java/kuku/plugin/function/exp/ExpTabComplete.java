package kuku.plugin.function.exp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 經驗系統的 Tab 自動補全處理器
 */
public class ExpTabComplete implements TabCompleter {

    private final ExpManager expManager;

    public ExpTabComplete(ExpManager expManager) {
        this.expManager = expManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // 只有玩家可以使用這些指令
        if (!(sender instanceof Player)) {
            return completions;
        }

        String commandName = command.getName().toLowerCase();

        switch (commandName) {
            case "exp":
                handleExpTabComplete(args, completions);
                break;

            case "collect":
                handleCollectTabComplete(args, completions);
                break;

            case "input":
                handleInputTabComplete(args, completions);
                break;
        }

        // 過濾匹配的項目
        return filterCompletions(args, completions);
    }

    /**
     * 處理 /exp 指令的 Tab 補全
     */
    private void handleExpTabComplete(String[] args, List<String> completions) {
        if (args.length == 1) {
            completions.addAll(Arrays.asList("help", "balance", "bal", "convert", "info"));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("convert")) {
                completions.addAll(getLevelSuggestions());
            }
        }
    }

    /**
     * 處理 /collect 指令的 Tab 補全
     */
    private void handleCollectTabComplete(String[] args, List<String> completions) {
        if (args.length == 1) {
            completions.add("exp");
            completions.add("all");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("exp")) {
            completions.addAll(getLevelSuggestions());
            completions.add("all");
            completions.add("max");
        }
    }

    /**
     * 處理 /input 指令的 Tab 補全
     */
    private void handleInputTabComplete(String[] args, List<String> completions) {
        if (args.length == 1) {
            completions.add("exp");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("exp")) {
            completions.addAll(getLevelSuggestions());
            completions.add("all");
            completions.add("half");
            completions.add("quarter");
        }
    }

    /**
     * 獲取等級建議列表
     */
    private List<String> getLevelSuggestions() {
        List<String> levels = new ArrayList<>();

        // 常用等級建議
        int[] commonLevels = {1, 5, 10, 20, 30, 40, 50, 75, 100, 150, 200, 300, 500, 1000};
        for (int level : commonLevels) {
            levels.add(String.valueOf(level));
        }

        return levels;
    }

    /**
     * 根據當前輸入過濾補全列表
     */
    private List<String> filterCompletions(String[] args, List<String> completions) {
        if (args.length == 0 || completions.isEmpty()) {
            return completions;
        }

        String lastArg = args[args.length - 1].toLowerCase();
        List<String> filtered = new ArrayList<>();

        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(lastArg)) {
                filtered.add(completion);
            }
        }

        // 如果沒有匹配項，返回所有補全
        return filtered.isEmpty() ? completions : filtered;
    }

    /**
     * 根據玩家當前經驗提供智能建議
     */
    public List<String> getSmartLevelSuggestions(Player player, String action) {
        List<String> suggestions = new ArrayList<>();

        if (player == null) {
            return suggestions;
        }

        int playerLevel = player.getLevel();
        int playerTotalExp = expManager.getTotalExperience(player);

        // 添加常用選項
        suggestions.add("1");
        suggestions.add("5");
        suggestions.add("10");
        suggestions.add("20");

        // 根據玩家等級添加相關建議
        if (playerLevel >= 10) {
            suggestions.add("25");
        }
        if (playerLevel >= 30) {
            suggestions.add("50");
        }
        if (playerLevel >= 50) {
            suggestions.add("75");
        }
        if (playerLevel >= 100) {
            suggestions.add("100");
        }

        // 根據動作類型添加特殊建議
        if (action.equals("input")) {
            // 存入時的智能建議
            suggestions.add("half");     // 一半經驗
            suggestions.add("quarter");  // 四分之一經驗
            suggestions.add("all");      // 所有經驗

            // 計算玩家當前等級的一半
            int halfLevel = playerLevel / 2;
            if (halfLevel > 0) {
                suggestions.add(String.valueOf(halfLevel));
            }

            // 計算玩家當前等級的四分之一
            int quarterLevel = playerLevel / 4;
            if (quarterLevel > 0) {
                suggestions.add(String.valueOf(quarterLevel));
            }
        } else if (action.equals("collect")) {
            // 取出時的智能建議

            // 獲取玩家儲存的經驗
            double storedExp = expManager.getStoredExp(player.getUniqueId());

            // 計算儲存的經驗相當於多少等級
            int storedLevel = 0;
            double tempExp = storedExp;
            while (tempExp >= expManager.levelToExp(storedLevel + 1)) {
                storedLevel++;
                tempExp -= expManager.levelToExp(storedLevel);
            }

            // 添加基於儲存等級的建議
            if (storedLevel > 0) {
                int[] percentages = {25, 50, 75, 100};
                for (int percentage : percentages) {
                    int suggestedLevel = (storedLevel * percentage) / 100;
                    if (suggestedLevel > 0) {
                        suggestions.add(String.valueOf(suggestedLevel));
                    }
                }

                suggestions.add(String.valueOf(storedLevel));
            }

            suggestions.add("all");
            suggestions.add("max");
        }

        return suggestions;
    }

    /**
     * 為玩家提供個性化的 Tab 補全
     */
    public List<String> getPersonalizedTabComplete(Player player, String commandName, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (commandName.equals("collect") || commandName.equals("input")) {
                completions.add("exp");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("exp")) {
            completions.addAll(getSmartLevelSuggestions(player, commandName));
        }

        return filterCompletions(args, completions);
    }
}