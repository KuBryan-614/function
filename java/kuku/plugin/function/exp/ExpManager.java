package kuku.plugin.function.exp;

import kuku.plugin.function.FunctionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 經驗存取管理器
 * 功能：讓玩家可以存取經驗值到個人帳戶
 */
public class ExpManager implements CommandExecutor{

    private final FunctionPlugin plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;
    private ExpTabComplete tabCompleter;

    private Map<UUID, Double> storedExp; // 儲存玩家的經驗值（以經驗值為單位）

    public ExpManager(FunctionPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "exp_storage.yml");
        this.storedExp = new HashMap<>();
        this.tabCompleter = new ExpTabComplete(this); // 創建 TabCompleter 實例

        loadData();
    }

    /**
     * 啟用模塊
     */
    public void enable() {
        // 註冊指令執行器
        plugin.getCommand("exp").setExecutor(this);
        plugin.getCommand("collect").setExecutor(this);
        plugin.getCommand("input").setExecutor(this);

        // 註冊 TabCompleter
        plugin.getCommand("exp").setTabCompleter(tabCompleter);
        plugin.getCommand("collect").setTabCompleter(tabCompleter);
        plugin.getCommand("input").setTabCompleter(tabCompleter);

        plugin.getLogger().info("§a經驗存取模塊已啟用");
    }

    /**
     * 禁用模塊
     */
    public void disable() {
        saveData();
        plugin.getLogger().info("§c經驗存取模塊已禁用");
    }

    /**
     * 重載模塊
     */
    public void reload() {
        loadData();
        plugin.getLogger().info("§a經驗存取模塊已重載");
    }

    /**
     * 加載數據
     */
    private void loadData() {
        if (!dataFile.exists()) {
            plugin.saveResource("exp_storage.yml", false);
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        storedExp.clear();

        if (dataConfig.contains("stored_exp")) {
            ConfigurationSection section = dataConfig.getConfigurationSection("stored_exp");
            if (section != null) {
                for (String uuidStr : section.getKeys(false)) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        double exp = section.getDouble(uuidStr, 0);
                        storedExp.put(uuid, exp);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("無效的UUID格式: " + uuidStr);
                    }
                }
            }
        }

        plugin.getLogger().info("§a已加載 " + storedExp.size() + " 位玩家的經驗儲存數據");
    }

    /**
     * 保存數據
     */
    private void saveData() {
        dataConfig.set("stored_exp", null); // 清空舊數據

        for (Map.Entry<UUID, Double> entry : storedExp.entrySet()) {
            if (entry.getValue() > 0) {
                dataConfig.set("stored_exp." + entry.getKey().toString(), entry.getValue());
            }
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("§c保存經驗儲存數據時出錯: " + e.getMessage());
        }
    }

    /**
     * 獲取玩家儲存的經驗值
     */
    public double getStoredExp(UUID uuid) {
        return storedExp.getOrDefault(uuid, 0.0);
    }

    /**
     * 設置玩家儲存的經驗值
     */
    public void setStoredExp(UUID uuid, double exp) {
        storedExp.put(uuid, Math.max(0, exp));
    }

    /**
     * 增加玩家儲存的經驗值
     */
    public void addStoredExp(UUID uuid, double exp) {
        double current = getStoredExp(uuid);
        setStoredExp(uuid, current + Math.max(0, exp));
    }

    /**
     * 減少玩家儲存的經驗值
     */
    public boolean removeStoredExp(UUID uuid, double exp) {
        double current = getStoredExp(uuid);
        if (current >= exp) {
            setStoredExp(uuid, current - exp);
            return true;
        }
        return false;
    }

    /**
     * 將等級轉換為經驗值
     * 使用Minecraft的經驗計算公式
     */
    public int levelToExp(int level) {
        if (level <= 0) return 0;

        if (level <= 15) {
            return level * level + 6 * level;
        } else if (level <= 30) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }

    /**
     * 從等級和經驗進度計算總經驗值
     */
    public int calculateTotalExp(int level, float expProgress) {
        int total = 0;

        // 累加前面等級所需的經驗
        for (int i = 0; i < level; i++) {
            total += levelToExp(i);
        }

        // 加上當前等級的經驗進度
        int nextLevelExp = levelToExp(level);
        total += (int) (expProgress * nextLevelExp);

        return total;
    }

    /**
     * 從總經驗值計算等級和經驗進度
     */
    private int[] calculateLevelAndProgress(int totalExp) {
        int level = 0;
        int exp = totalExp;

        // 計算等級
        while (true) {
            int expForNextLevel = levelToExp(level);
            if (exp < expForNextLevel) {
                break;
            }
            exp -= expForNextLevel;
            level++;
        }

        return new int[]{level, exp};
    }

    /**
     * 從玩家身上扣除經驗
     */
    public boolean deductExp(Player player, int levels) {
        int totalExp = getTotalExperience(player);
        int expNeeded = levelToExp(levels);

        if (totalExp < expNeeded) {
            return false;
        }

        int newTotalExp = totalExp - expNeeded;
        setTotalExperience(player, newTotalExp);
        return true;
    }

    /**
     * 給予玩家經驗
     */
    private void giveExpToPlayer(Player player, int levels) {
        double expToGive = levelToExp(levels);
        player.giveExp((int) expToGive);
    }

    /**
     * 給予玩家指定等級的經驗
     */
    public void giveExp(Player player, int levels) {
        int currentTotalExp = getTotalExperience(player);
        int expToGive = levelToExp(levels);
        int newTotalExp = currentTotalExp + expToGive;

        setTotalExperience(player, newTotalExp);
    }

    /**
     * 將經驗值轉換為等級字符串表示
     */
    public String expToLevelString(double exp) {
        int expInt = (int) exp;
        int level = 0;

        while (expInt >= levelToExp(level)) {
            expInt -= levelToExp(level);
            level++;
        }

        if (level == 0) {
            return "0 等";
        }

        float progress = 0.0f;
        int nextLevelExp = levelToExp(level);
        if (nextLevelExp > 0) {
            progress = (float) expInt / nextLevelExp * 100;
        }

        return String.format("%d 等 (%.1f%%)", level, progress);
    }

    /**
     * 獲取玩家總經驗值
     */
    public int getTotalExperience(Player player) {
        return calculateTotalExp(player.getLevel(), player.getExp());
    }

    /**
     * 設置玩家總經驗值
     */
    private void setTotalExperience(Player player, int totalExp) {
        int[] levelAndExp = calculateLevelAndProgress(totalExp);
        int level = levelAndExp[0];
        int remainingExp = levelAndExp[1];
        int expForNextLevel = levelToExp(level);

        float expProgress = 0.0f;
        if (expForNextLevel > 0) {
            expProgress = (float) remainingExp / expForNextLevel;
        }

        player.setLevel(level);
        player.setExp(expProgress);
    }

    /**
     * 檢查玩家是否有足夠的經驗
     */
    public boolean hasEnoughExp(Player player, int levels) {
        int totalExp = getTotalExperience(player);
        int expNeeded = levelToExp(levels);
        return totalExp >= expNeeded;
    }

    /**
     * 處理指令
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用此指令!");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("exp")) {
            return handleExpCommand(player, args);
        } else if (command.getName().equalsIgnoreCase("collect")) {
            return handleCollectCommand(player, args);
        } else if (command.getName().equalsIgnoreCase("input")) {
            return handleInputCommand(player, args);
        }

        return false;
    }

    /**
     * 處理 /exp 指令
     */
    private boolean handleExpCommand(Player player, String[] args) {
        if (args.length == 0) {
            showExpInfo(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("help")) {
            showHelp(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("balance") || args[0].equalsIgnoreCase("bal")) {
            showBalance(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("convert")) {
            if (args.length < 2) {
                player.sendMessage("§c用法: /exp convert <等級>");
                return true;
            }

            try {
                int level = Integer.parseInt(args[1]);
                if (level <= 0 || level > 1000) {
                    player.sendMessage("§c等級必須在 1-1000 之間");
                    return true;
                }

                double exp = levelToExp(level);
                player.sendMessage("§6§l[經驗系統] §e" + level + " 等需要 §6" + (int)exp + " §e點經驗值");
            } catch (NumberFormatException e) {
                player.sendMessage("§c請輸入有效的數字!");
            }
            return true;
        }

        player.sendMessage("§c未知指令! 使用 /exp help 查看幫助");
        return true;
    }

    /**
     * 處理 /collect 指令
     */
    private boolean handleCollectCommand(Player player, String[] args) {
        if (args.length < 2 || !args[0].equalsIgnoreCase("exp")) {
            player.sendMessage("§c用法: /collect exp <等級|all|max>");
            player.sendMessage("§e範例: /collect exp 10 - 取出10等的經驗");
            player.sendMessage("§e範例: /collect exp all - 取出所有儲存經驗");
            player.sendMessage("§e範例: /collect exp max - 取出最大可取的經驗");
            return true;
        }

        String levelArg = args[1].toLowerCase();
        int level;
        double storedExp = getStoredExp(player.getUniqueId());

        // 處理特殊關鍵字
        if (levelArg.equals("all")) {
            // 計算所有儲存經驗相當於多少等級
            level = 0;
            double tempExp = storedExp;
            while (tempExp >= levelToExp(level + 1)) {
                level++;
                tempExp -= levelToExp(level);
            }

            if (level <= 0) {
                player.sendMessage("§c你的儲存中沒有經驗!");
                return true;
            }
        } else if (levelArg.equals("max")) {
            // 取出最大可取的經驗（不超過玩家等級上限）
            int maxSafeLevel = Math.min(calculateMaxCollectableLevel(player), 1000);
            level = maxSafeLevel;
        } else {
            // 嘗試解析數字
            try {
                level = Integer.parseInt(levelArg);
            } catch (NumberFormatException e) {
                player.sendMessage("§c請輸入有效的數字或 all/max!");
                return true;
            }
        }

        if (level <= 0) {
            player.sendMessage("§c等級必須大於0!");
            return true;
        }

        if (level > 1000) {
            player.sendMessage("§c一次最多只能取出1000等的經驗!");
            return true;
        }

        // 檢查儲存中是否有足夠經驗
        int expNeeded = levelToExp(level);
        if (storedExp < expNeeded) {
            player.sendMessage("§c儲存經驗不足! 你只有 " + formatExp(storedExp) + " 經驗");
            player.sendMessage("§e需要 " + expNeeded + " 經驗 (" + level + "等)");
            return true;
        }

        // 執行取出
        if (performCollect(player, level)) {
            player.sendMessage("§a§l✓ §a成功取出 " + level + " 等經驗!");
        }

        return true;
    }

    /**
     * 計算最大可取的等級
     */
    private int calculateMaxCollectableLevel(Player player) {
        double storedExp = getStoredExp(player.getUniqueId());
        int maxLevel = 0;
        double tempExp = storedExp;

        // 找到最大可取得的等級
        while (tempExp >= levelToExp(maxLevel + 1)) {
            maxLevel++;
            tempExp -= levelToExp(maxLevel);
        }

        return maxLevel;
    }

    /**
     * 執行存入操作
     */
    private boolean performInput(Player player, int level) {
        int expNeeded = levelToExp(level);

        if (!hasEnoughExp(player, level)) {
            return false;
        }

        // 扣除玩家經驗
        deductExp(player, level);

        // 存入到儲存系統
        addStoredExp(player.getUniqueId(), expNeeded);

        // 保存數據
        saveData();

        // 顯示結果
        double newStoredExp = getStoredExp(player.getUniqueId());
        player.sendMessage("§e目前儲存經驗: " + formatExp(newStoredExp));
        player.sendMessage("§e相當於: " + expToLevelString(newStoredExp));

        // 播放音效
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);

        return true;
    }

    /**
     * 執行取出操作
     */
    private boolean performCollect(Player player, int level) {
        int expNeeded = levelToExp(level);
        double storedExp = getStoredExp(player.getUniqueId());

        if (storedExp < expNeeded) {
            return false;
        }

        // 從儲存中扣除經驗
        removeStoredExp(player.getUniqueId(), expNeeded);

        // 給予玩家經驗
        giveExp(player, level);

        // 保存數據
        saveData();

        // 顯示結果
        double newStoredExp = getStoredExp(player.getUniqueId());
        player.sendMessage("§e剩餘儲存經驗: " + formatExp(newStoredExp));
        player.sendMessage("§e相當於: " + expToLevelString(newStoredExp));

        // 播放音效
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        return true;
    }

    /**
     * 處理 /input 指令
     */
    private boolean handleInputCommand(Player player, String[] args) {
        if (args.length < 2 || !args[0].equalsIgnoreCase("exp")) {
            player.sendMessage("§c用法: /input exp <等級|all|half|quarter>");
            player.sendMessage("§e範例: /input exp 10 - 存入10等的經驗");
            player.sendMessage("§e範例: /input exp all - 存入所有經驗");
            player.sendMessage("§e範例: /input exp half - 存入一半經驗");
            return true;
        }

        String levelArg = args[1].toLowerCase();
        int level;

        // 處理特殊關鍵字
        if (levelArg.equals("all")) {
            // 存入所有經驗
            level = player.getLevel();
            if (level <= 0) {
                player.sendMessage("§c你沒有經驗可以存入!");
                return true;
            }
        } else if (levelArg.equals("half")) {
            // 存入一半經驗
            level = player.getLevel() / 2;
            if (level <= 0) {
                player.sendMessage("§c你的等級太低，無法存入一半經驗!");
                return true;
            }
        } else if (levelArg.equals("quarter")) {
            // 存入四分之一經驗
            level = player.getLevel() / 4;
            if (level <= 0) {
                player.sendMessage("§c你的等級太低，無法存入四分之一經驗!");
                return true;
            }
        } else {
            // 嘗試解析數字
            try {
                level = Integer.parseInt(levelArg);
            } catch (NumberFormatException e) {
                player.sendMessage("§c請輸入有效的數字或 all/half/quarter!");
                return true;
            }
        }

        if (level <= 0) {
            player.sendMessage("§c等級必須大於0!");
            return true;
        }

        if (level > 1000) {
            player.sendMessage("§c一次最多只能存取1000等的經驗!");
            return true;
        }

        // 檢查玩家是否有足夠經驗
        if (!hasEnoughExp(player, level)) {
            int playerTotalExp = getTotalExperience(player);
            int expNeeded = levelToExp(level);
            player.sendMessage("§c經驗不足! 你只有 " + playerTotalExp + " 經驗");
            player.sendMessage("§e需要 " + expNeeded + " 經驗 (" + level + "等)");
            return true;
        }

        // 執行存入
        if (performInput(player, level)) {
            player.sendMessage("§a§l✓ §a成功存入 " + level + " 等經驗!");
        }

        return true;
    }

    /**
     * 顯示經驗信息
     */
    private void showExpInfo(Player player) {
        double stored = getStoredExp(player.getUniqueId());
        int playerLevel = player.getLevel();
        int playerExp = getTotalExperience(player);

        player.sendMessage("§6§l╔═══════════[ 經驗系統 ]═══════════╗");
        player.sendMessage("§e目前等級: §6" + playerLevel + " 等");
        player.sendMessage("§e總經驗值: §6" + playerExp + " 經驗");
        player.sendMessage("§e儲存經驗: §6" + formatExp(stored));
        player.sendMessage("§6§l╚═══════════════════════════════════╝");
        player.sendMessage("§e使用 §6/exp help §e查看指令幫助");
    }

    /**
     * 顯示幫助信息
     */
    private void showHelp(Player player) {
        player.sendMessage("§6§l╔═══════════[ 經驗系統幫助 ]═══════════╗");
        player.sendMessage("§e/exp §7- 查看你的經驗信息");
        player.sendMessage("§e/exp balance §7- 查看儲存的經驗");
        player.sendMessage("§e/exp convert <等級> §7- 查看等級對應的經驗值");
        player.sendMessage("§e/input exp <等級> §7- 存入指定等級的經驗");
        player.sendMessage("§e/collect exp <等級> §7- 取出指定等級的經驗");
        player.sendMessage("§6§l╚═══════════════════════════════════════╝");
    }

    /**
     * 顯示餘額
     */
    private void showBalance(Player player) {
        double stored = getStoredExp(player.getUniqueId());

        // 計算相當於多少等級
        int equivalentLevel = 0;
        double tempExp = stored;
        while (tempExp >= levelToExp(equivalentLevel + 1)) {
            equivalentLevel++;
            tempExp -= levelToExp(equivalentLevel);
        }

        player.sendMessage("§6§l[經驗系統] §e你的經驗儲存餘額:");
        player.sendMessage("§e總經驗值: §6" + formatExp(stored));
        player.sendMessage("§e相當於等級: §6約 " + equivalentLevel + " 等");
        if (equivalentLevel > 0) {
            player.sendMessage("§e(還多 " + String.format("%.1f", tempExp) + " 經驗)");
        }
    }

    /**
     * 格式化經驗值顯示
     */
    private String formatExp(double exp) {
        if (exp >= 1000000) {
            return String.format("%.2fM", exp / 1000000);
        } else if (exp >= 1000) {
            return String.format("%.1fk", exp / 1000);
        } else {
            return String.format("%.0f", exp);
        }
    }


    /**
     * 獲取模塊名稱
     */
    public String getName() {
        return "exp";
    }

    /**
     * 檢查模塊是否啟用
     */
    public boolean isEnabled() {
        return plugin.getPluginConfig().getBoolean("modules.exp", true);
    }
}