package kuku.plugin.function.backups;

import kuku.plugin.function.FunctionPlugin;
import kuku.plugin.function.utils.LogUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * 備份命令處理器
 * 處理 /kb 和 /kukubackup 命令
 */
public class BackupCommand implements CommandExecutor, TabCompleter {

    private final FunctionPlugin plugin;
    private final Logger logger;
    private final BackupManager backupManager;

    public BackupCommand(FunctionPlugin plugin, BackupManager backupManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.backupManager = backupManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 檢查是否是備份命令
        if (command.getName().equalsIgnoreCase("kb") || command.getName().equalsIgnoreCase("kukubackup")) {
            return handleKbCommand(sender, args);
        }

        return false;
    }

    /**
     * 處理 kb 命令
     */
    private boolean handleKbCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            showKbHelp(sender);
            return true;
        }

        // 檢查備份命令
        if (args[0].equalsIgnoreCase("backups") || args[0].equalsIgnoreCase("backup")) {
            if (backupManager != null && backupManager.getConfig().isEnabled()) {
                // 移除第一個參數（backups），將剩餘參數傳遞給備份管理器
                String[] backupArgs = Arrays.copyOfRange(args, 1, args.length);
                return handleBackupCommand(sender, backupArgs);
            } else {
                sender.sendMessage(ChatColor.RED + "備份模塊未啟用！");
                return true;
            }
        }

        // 其他 kb 子命令...
        showKbHelp(sender);
        return true;
    }

    /**
     * 處理備份子命令
     */
    private boolean handleBackupCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(backupManager.getConfig().getPermission())) {
            sender.sendMessage(ChatColor.RED + "你沒有權限使用此命令！");
            return true;
        }

        if (args.length == 0) {
            showBackupHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
            case "backup":
                // 檢查是否正在備份
                if (backupManager.isBackingUp()) {
                    sender.sendMessage(ChatColor.RED + "備份正在進行中，請稍候...");
                    return true;
                }

                // 執行備份
                boolean started = backupManager.performBackup(sender, false);
                if (started) {
                    sender.sendMessage(ChatColor.GREEN + "已開始創建備份...");
                }
                return true;

            case "list":
                showBackupList(sender);
                return true;

            case "info":
            case "status":
                showBackupInfo(sender);
                return true;

            case "safebackup":
            case "safe":
                // 檢查是否正在備份
                if (backupManager.isBackingUp()) {
                    sender.sendMessage(ChatColor.RED + "備份正在進行中，請稍候...");
                    return true;
                }

                // 執行安全備份
                boolean safeStarted = backupManager.performSafeBackup(sender, false);
                if (safeStarted) {
                    sender.sendMessage(ChatColor.GREEN + "已開始創建安全備份（執行 save-all 後備份）...");
                }
                return true;

            case "reload":
                // 檢查權限
                if (!sender.hasPermission("kuku.backups.admin")) {
                    sender.sendMessage(ChatColor.RED + "你沒有權限執行此命令！");
                    return true;
                }

                backupManager.reload();
                sender.sendMessage(ChatColor.GREEN + "備份配置已重載");
                return true;

            case "stop":
                // 檢查權限
                if (!sender.hasPermission("kuku.backups.admin")) {
                    sender.sendMessage(ChatColor.RED + "你沒有權限執行此命令！");
                    return true;
                }

                if (backupManager.isBackingUp()) {
                    // 注意：由於備份是異步的，停止可能會導致文件損壞
                    sender.sendMessage(ChatColor.YELLOW + "備份正在進行中，停止可能會導致文件損壞。");
                    sender.sendMessage(ChatColor.YELLOW + "建議等待備份完成。");
                    // 這裡可以實現停止邏輯，但為了安全暫時不實現
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "目前沒有正在進行的備份");
                }
                return true;

            case "now":
                // 立即執行備份（簡化命令）
                if (backupManager.isBackingUp()) {
                    sender.sendMessage(ChatColor.RED + "備份正在進行中，請稍候...");
                    return true;
                }

                backupManager.performBackup(sender, false);
                return true;

            case "help":
            default:
                showBackupHelp(sender);
                return true;
        }
    }

    /**
     * 顯示備份列表
     */
    private void showBackupList(CommandSender sender) {
        List<String> backups = backupManager.getBackupList();

        sender.sendMessage(ChatColor.GOLD + "=== 備份列表 ===");

        if (backups.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "沒有找到備份文件");
        } else {
            for (int i = 0; i < Math.min(backups.size(), 10); i++) {
                // 修正字符串連接問題
                sender.sendMessage(ChatColor.GREEN + Integer.toString(i + 1) + ". " + backups.get(i));
            }

            if (backups.size() > 10) {
                sender.sendMessage(ChatColor.GRAY + "... 還有 " + Integer.toString(backups.size() - 10) + " 個備份未顯示");
            }

            sender.sendMessage(ChatColor.GRAY + "共 " + Integer.toString(backups.size()) + " 個備份文件");
            sender.sendMessage(ChatColor.GRAY + "使用 /kb backups info 查看詳細信息");
        }
    }

    /**
     * 顯示備份信息
     */
    private void showBackupInfo(CommandSender sender) {
        BackupConfig config = backupManager.getConfig();

        sender.sendMessage(ChatColor.GOLD + "=== 備份系統信息 ===");

        // 狀態信息
        sender.sendMessage(ChatColor.YELLOW + "狀態: " +
                (backupManager.isBackingUp() ? ChatColor.RED + "備份中" : ChatColor.GREEN + "空閒"));

        // 配置信息
        sender.sendMessage(ChatColor.YELLOW + "自動備份: " +
                (config.isAutoBackupEnabled() ? ChatColor.GREEN + "啟用" : ChatColor.RED + "禁用"));

        if (config.isAutoBackupEnabled()) {
            sender.sendMessage(ChatColor.YELLOW + "備份間隔: " + ChatColor.WHITE +
                    config.getAutoBackupInterval() + " 分鐘");

            // 計算下次備份時間
            int intervalTicks = config.getAutoBackupInterval() * 60 * 20;
            long nextBackupTime = System.currentTimeMillis() + (intervalTicks * 50); // 估算

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
            String nextTime = sdf.format(new java.util.Date(nextBackupTime));
            sender.sendMessage(ChatColor.YELLOW + "下次備份: " + ChatColor.WHITE + "約 " + nextTime);
        }

        // 文件信息
        java.io.File backupDir = new java.io.File(config.getSavePath());
        if (backupDir.exists()) {
            java.io.File[] backupFiles = backupDir.listFiles((dir, name) ->
                    name.startsWith(config.getBackupPrefix()) &&
                            (name.endsWith(".zip") || name.endsWith(".tar.gz"))
            );

            int backupCount = backupFiles != null ? backupFiles.length : 0;
            sender.sendMessage(ChatColor.YELLOW + "備份數量: " + ChatColor.WHITE + backupCount +
                    ChatColor.GRAY + " / " + config.getMaxBackupCount());

            // 計算總大小
            long totalSize = 0;
            if (backupFiles != null) {
                for (java.io.File file : backupFiles) {
                    totalSize += file.length();
                }
            }
            sender.sendMessage(ChatColor.YELLOW + "總大小: " + ChatColor.WHITE +
                    backupManager.formatFileSize(totalSize));
        }

        // 其他設置
        sender.sendMessage(ChatColor.YELLOW + "關機備份: " +
                (config.isBackupOnStop() ? ChatColor.GREEN + "啟用" : ChatColor.RED + "禁用"));
        sender.sendMessage(ChatColor.YELLOW + "壓縮格式: " + ChatColor.WHITE + config.getCompressionType());
        sender.sendMessage(ChatColor.YELLOW + "保存路徑: " + ChatColor.WHITE + config.getSavePath());

        // 權限信息
        if (sender instanceof Player) {
            Player player = (Player) sender;
            sender.sendMessage(ChatColor.YELLOW + "你的權限: " +
                    (player.hasPermission(config.getPermission()) ?
                            ChatColor.GREEN + "有" : ChatColor.RED + "無"));
        }
    }

    /**
     * 顯示 kb 命令幫助
     */
    private void showKbHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Kuku 插件命令 ===");
        sender.sendMessage(ChatColor.YELLOW + "/kb backups" + ChatColor.GRAY + " - 備份系統命令");
        sender.sendMessage(ChatColor.YELLOW + "/discord" + ChatColor.GRAY + " - Discord相關命令");
        sender.sendMessage(ChatColor.YELLOW + "/helpgui" + ChatColor.GRAY + " - 打開幫助GUI");

        if (sender.hasPermission("kuku.backups.admin")) {
            sender.sendMessage(ChatColor.GOLD + "=== 管理員命令 ===");
            sender.sendMessage(ChatColor.YELLOW + "/kb backups reload" + ChatColor.GRAY + " - 重載備份配置");
        }
    }

    /**
     * 顯示備份命令幫助
     */
    private void showBackupHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== 備份系統命令 ===");
        sender.sendMessage(ChatColor.YELLOW + "/kb backups create" + ChatColor.GRAY + " - 創建一個新的備份");
        sender.sendMessage(ChatColor.YELLOW + "/kb backups now" + ChatColor.GRAY + " - 立即創建備份（簡化命令）");
        sender.sendMessage(ChatColor.YELLOW + "/kb backups list" + ChatColor.GRAY + " - 顯示備份列表（最多10個）");
        sender.sendMessage(ChatColor.YELLOW + "/kb backups info" + ChatColor.GRAY + " - 顯示備份系統信息");

        if (sender.hasPermission("kuku.backups.admin")) {
            sender.sendMessage(ChatColor.GOLD + "=== 管理員命令 ===");
            sender.sendMessage(ChatColor.YELLOW + "/kb backups reload" + ChatColor.GRAY + " - 重載備份配置");
            sender.sendMessage(ChatColor.YELLOW + "/kb backups stop" + ChatColor.GRAY + " - 停止當前備份（危險）");
        }

        sender.sendMessage(ChatColor.GRAY + "需要權限: " + backupManager.getConfig().getPermission());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // 只處理 kb 或 kukubackup 命令
        if (!command.getName().equalsIgnoreCase("kb") && !command.getName().equalsIgnoreCase("kukubackup")) {
            return completions;
        }

        // 第一級補全
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> suggestions = Arrays.asList("backups", "backup");

            for (String suggestion : suggestions) {
                if (suggestion.toLowerCase().startsWith(partial)) {
                    completions.add(suggestion);
                }
            }
        }

        // 第二級補全（backups 子命令）
        else if (args.length == 2 &&
                (args[0].equalsIgnoreCase("backups") || args[0].equalsIgnoreCase("backup"))) {

            String partial = args[1].toLowerCase();
            List<String> suggestions = Arrays.asList(
                    "create", "now", "list", "info", "status", "help"
            );

            // 添加管理員命令
            if (sender.hasPermission("kuku.backups.admin")) {
                suggestions = Arrays.asList(
                        "create", "now", "list", "info", "status", "reload", "stop", "help"
                );
            }

            for (String suggestion : suggestions) {
                if (suggestion.toLowerCase().startsWith(partial)) {
                    completions.add(suggestion);
                }
            }
        }

        return completions;
    }
}