package kuku.plugin.function.backups;

import kuku.plugin.function.FunctionPlugin;
import kuku.plugin.function.utils.LogUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.zip.*;

/**
 * 備份管理器
 * 負責處理伺服器備份相關功能
 */
public class BackupManager {

    private final FunctionPlugin plugin;
    private final Logger logger;
    private final BackupConfig config;

    private BukkitTask autoBackupTask;
    private boolean isBackingUp = false;

    public BackupManager(FunctionPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.config = new BackupConfig(plugin);
    }

    /**
     * 啟用備份模塊
     */
    public void enable() {
        logger.info("§a正在啟用備份模塊...");

        // 確保插件資料目錄存在
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // 確保備份目錄存在
        File backupDir = config.getBackupFolder();
        if (!backupDir.exists()) {
            backupDir.mkdirs();
            logger.info("§e創建備份目錄: " + backupDir.getAbsolutePath());
        }

        // 啟動自動備份任務
        if (config.isAutoBackupEnabled()) {
            startAutoBackup();
        }

        // 註冊命令
        registerCommands();

        logger.info("§a備份模塊已啟用");
        logger.info("§e自動備份間隔: " + config.getAutoBackupInterval() + " 分鐘");
        logger.info("§e備份保存路徑: " + backupDir.getAbsolutePath());
        logger.info("§e最大備份數量: " + config.getMaxBackupCount());
    }

    /**
     * 禁用備份模塊
     */
    public void disable() {
        logger.info("§c正在禁用備份模塊...");

        // 停止自動備份任務
        if (autoBackupTask != null) {
            autoBackupTask.cancel();
            autoBackupTask = null;
        }

        // 如果正在備份，等待完成
        if (isBackingUp) {
            logger.warning("§e備份正在進行中，等待完成...");
            // 這裡可以實現等待邏輯，但通常備份應該很快就會完成
        }

        logger.info("§c備份模塊已禁用");
    }

    /**
     * 重新加載配置
     */
    public void reload() {
        logger.info("§e重新加載備份配置...");

        // 停止舊的自動備份任務
        if (autoBackupTask != null) {
            autoBackupTask.cancel();
        }

        // 重新加載配置
        config.reload();

        // 啟動新的自動備份任務
        if (config.isAutoBackupEnabled()) {
            startAutoBackup();
        }

        logger.info("§a備份配置已重載");
    }

    /**
     * 註冊命令
     */
    private void registerCommands() {
        // 命令現在由BackupCommand類處理
        // 這裡只需要初始化命令執行器
        plugin.getCommand("kb").setExecutor(new BackupCommand(plugin, this));
        plugin.getCommand("kukubackup").setExecutor(new BackupCommand(plugin, this));
    }

    /**
     * 啟動自動備份任務
     */
    private void startAutoBackup() {
        int interval = config.getAutoBackupInterval() * 60 * 20; // 轉換為ticks

        autoBackupTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (!isBackingUp) {
                performBackup(null, true);
            }
        }, interval, interval);

        logger.info("§a已啟動自動備份任務，間隔: " + config.getAutoBackupInterval() + " 分鐘");
    }

    /**
     * 執行備份操作
     * @param sender 命令發送者（可為null）
     * @param isAuto 是否為自動備份
     * @return 備份是否成功開始
     */
    public boolean performBackup(CommandSender sender, boolean isAuto) {
        if (isBackingUp) {
            if (sender != null) {
                sender.sendMessage(ChatColor.RED + "備份正在進行中，請稍候...");
            }
            return false;
        }

        isBackingUp = true;

        // 在異步線程中執行備份
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String backupName = createBackupName(isAuto);

                // 發送開始通知
                String startMessage = ChatColor.GOLD + "[" + ChatColor.YELLOW + "備份" + ChatColor.GOLD + "] " +
                        ChatColor.GREEN + "開始創建備份: " + ChatColor.WHITE + backupName;

                if (config.shouldNotify() && !isAuto) {
                    Bukkit.broadcastMessage(startMessage);
                } else if (sender != null) {
                    sender.sendMessage(startMessage);
                }

                logger.info("§a開始創建備份: " + backupName);

                // 保存世界（確保所有區塊都寫入磁盤）
                if (!isAuto) {
                    saveWorlds();
                }

                // 創建備份文件
                File backupFile = createBackup(backupName);

                if (backupFile != null && backupFile.exists()) {
                    // 計算文件大小
                    String size = formatFileSize(backupFile.length());

                    // 清理舊備份
                    cleanupOldBackups();

                    // 發送完成通知
                    String successMessage = ChatColor.GOLD + "[" + ChatColor.YELLOW + "備份" + ChatColor.GOLD + "] " +
                            ChatColor.GREEN + "備份完成! " +
                            ChatColor.WHITE + "文件: " + backupFile.getName() +
                            ChatColor.GRAY + " (" + size + ")";

                    if (config.shouldNotify() && !isAuto) {
                        Bukkit.broadcastMessage(successMessage);
                    } else if (sender != null) {
                        sender.sendMessage(successMessage);
                    }

                    logger.info("§a備份創建成功: " + backupFile.getName() + " (" + size + ")");

                    // 記錄到控制台
                    LogUtils.logSuccess(logger, "備份",
                            "創建備份完成: " + backupFile.getName() + " (" + size + ")");
                } else {
                    throw new IOException("備份文件創建失敗");
                }

            } catch (Exception e) {
                String errorMessage = ChatColor.GOLD + "[" + ChatColor.YELLOW + "備份" + ChatColor.GOLD + "] " +
                        ChatColor.RED + "備份失敗: " + e.getMessage();

                if (sender != null) {
                    sender.sendMessage(errorMessage);
                }

                logger.severe("§c備份失敗: " + e.getMessage());
                e.printStackTrace();
            } finally {
                isBackingUp = false;
            }
        });

        return true;
    }

    /**
     * 保存所有世界
     */
    private void saveWorlds() {
        try {
            // 保存所有世界
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getWorlds().forEach(world -> {
                    world.save();
                    logger.info("§e已保存世界: " + world.getName());
                });

                // 保存玩家數據
                Bukkit.savePlayers();
                logger.info("§e已保存玩家數據");
            });

            // 等待一小段時間確保數據寫入
            Thread.sleep(1000);

        } catch (Exception e) {
            logger.warning("§c保存世界時出錯: " + e.getMessage());
        }
    }

    /**
     * 創建備份名稱
     * @param isAuto 是否為自動備份
     * @return 備份名稱
     */
    private String createBackupName(boolean isAuto) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
        String timestamp = sdf.format(new Date());

        if (isAuto) {
            return config.getBackupPrefix() + "_auto_" + timestamp;
        } else {
            return config.getBackupPrefix() + "_manual_" + timestamp;
        }
    }

    /**
     * 創建備份檔案
     * @param backupName 備份名稱
     * @return 備份檔案
     */
    private File createBackup(String backupName) throws IOException {
        String backupFileName = backupName + "." + config.getCompressionType();
        File backupFile = new File(config.getBackupFolder(), backupFileName);

        // 獲取伺服器根目錄
        File serverRoot = getServerRoot();

        if (serverRoot == null) {
            throw new IOException("無法獲取伺服器根目錄，請檢查插件安裝路徑");
        }

        if (config.isDebug()) {
            logger.info("§e[除錯] 伺服器根目錄: " + serverRoot.getAbsolutePath());
            logger.info("§e[除錯] 備份檔案將保存到: " + backupFile.getAbsolutePath());
        }

        // 根據壓縮類型選擇創建方法
        if (config.getCompressionType().equalsIgnoreCase("zip")) {
            createZipBackup(serverRoot, backupFile);
        } else if (config.getCompressionType().equalsIgnoreCase("tar.gz")) {
            createTarGzBackup(serverRoot, backupFile);
        } else {
            createZipBackup(serverRoot, backupFile); // 默認使用zip
        }

        return backupFile;
    }

    /**
     * 獲取伺服器根目錄
     */
    private File getServerRoot() {
        // 方法1: 使用世界容器（通常指向伺服器根目錄）
        try {
            File worldContainer = Bukkit.getWorldContainer();
            if (worldContainer != null && worldContainer.exists()) {
                if (config.isDebug()) {
                    logger.info("§e[除錯] 使用世界容器作為根目錄: " + worldContainer.getAbsolutePath());
                }
                return worldContainer;
            }
        } catch (Exception e) {
            if (config.isDebug()) {
                logger.warning("§c[除錯] 無法獲取世界容器: " + e.getMessage());
            }
        }

        // 方法2: 使用當前工作目錄
        try {
            File currentDir = new File(".");
            if (currentDir.exists()) {
                if (config.isDebug()) {
                    logger.info("§e[除錯] 使用當前工作目錄作為根目錄: " + currentDir.getAbsolutePath());
                }
                return currentDir.getAbsoluteFile();
            }
        } catch (Exception e) {
            if (config.isDebug()) {
                logger.warning("§c[除錯] 無法使用當前工作目錄: " + e.getMessage());
            }
        }

        logger.severe("§c無法確定伺服器根目錄！");
        return null;
    }

    /**
     * 創建ZIP備份
     */
    private void createZipBackup(File serverRoot, File backupFile) throws IOException {
        List<String> backupItems = config.getBackupFolders();
        List<String> excludePatterns = config.getExcludePatterns();

        if (serverRoot == null || !serverRoot.exists()) {
            throw new IOException("伺服器根目錄不存在或不可訪問: " +
                    (serverRoot == null ? "null" : serverRoot.getAbsolutePath()));
        }

        logger.info("§a開始備份，伺服器根目錄: " + serverRoot.getAbsolutePath());

        if (config.isDebug()) {
            logger.info("§e[除錯] 備份項目: " + backupItems);
            logger.info("§e[除錯] 排除模式: " + excludePatterns);
        }

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(backupFile))) {
            // 設置壓縮級別
            zos.setLevel(Deflater.BEST_COMPRESSION);

            // 記錄總檔案數和大小
            AtomicLong totalFiles = new AtomicLong(0);
            AtomicLong totalSize = new AtomicLong(0);
            AtomicLong skippedFiles = new AtomicLong(0);

            for (String item : backupItems) {
                File file = new File(serverRoot, item);

                if (!file.exists()) {
                    if (config.isDebug()) {
                        logger.warning("§e[除錯] 備份項目不存在，跳過: " + item);
                    }
                    continue;
                }

                if (file.isDirectory()) {
                    // 備份目錄
                    Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {
                            // 檢查是否在排除列表中
                            if (shouldExclude(filePath, excludePatterns)) {
                                return FileVisitResult.CONTINUE;
                            }

                            try {
                                // 創建ZIP條目
                                String entryName = serverRoot.toPath().relativize(filePath).toString();
                                ZipEntry zipEntry = new ZipEntry(entryName);
                                zipEntry.setTime(Files.getLastModifiedTime(filePath).toMillis());

                                // 添加條目到ZIP
                                zos.putNextEntry(zipEntry);

                                // 使用緩衝流讀取檔案，避免鎖定問題
                                try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(filePath))) {
                                    byte[] buffer = new byte[8192];
                                    int bytesRead;
                                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                                        zos.write(buffer, 0, bytesRead);
                                    }
                                }

                                zos.closeEntry();

                                // 更新統計
                                totalFiles.incrementAndGet();
                                totalSize.addAndGet(Files.size(filePath));

                                // 每1000個檔案輸出一次進度
                                if (totalFiles.get() % 1000 == 0) {
                                    logger.info("§e已處理 " + totalFiles.get() + " 個檔案...");
                                }
                            } catch (IOException e) {
                                // 處理檔案鎖定錯誤
                                String errorMsg = e.getMessage();
                                if (errorMsg.contains("鎖定") || errorMsg.contains("locked") ||
                                        errorMsg.contains("無法存取") || errorMsg.contains("access")) {

                                    logger.warning("§c檔案被鎖定，跳過: " + filePath + " - " + e.getMessage());
                                    skippedFiles.incrementAndGet();

                                    // 嘗試關閉ZIP條目（如果已打開）
                                    try {
                                        zos.closeEntry();
                                    } catch (IOException ignored) {}
                                } else {
                                    // 其他錯誤重新拋出
                                    logger.warning("§c備份檔案時發生錯誤: " + filePath + " - " + e.getMessage());
                                }
                            }

                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            // 檢查是否在排除列表中
                            if (shouldExclude(dir, excludePatterns)) {
                                return FileVisitResult.SKIP_SUBTREE;
                            }

                            // 創建目錄條目
                            if (!dir.equals(file.toPath())) {
                                String entryName = serverRoot.toPath().relativize(dir).toString() + "/";
                                ZipEntry zipEntry = new ZipEntry(entryName);
                                zipEntry.setTime(Files.getLastModifiedTime(dir).toMillis());
                                try {
                                    zos.putNextEntry(zipEntry);
                                    zos.closeEntry();
                                } catch (IOException e) {
                                    // 忽略目錄創建錯誤
                                }
                            }

                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                            // 處理無法訪問的檔案
                            if (exc.getMessage().contains("鎖定") || exc.getMessage().contains("locked") ||
                                    exc.getMessage().contains("無法存取") || exc.getMessage().contains("access")) {

                                logger.warning("§c無法訪問檔案（可能被鎖定）: " + file + " - " + exc.getMessage());
                                skippedFiles.incrementAndGet();
                                return FileVisitResult.CONTINUE;
                            }
                            return super.visitFileFailed(file, exc);
                        }
                    });
                } else {
                    // 備份單個檔案
                    if (shouldExclude(file.toPath(), excludePatterns)) {
                        continue;
                    }

                    try {
                        String entryName = serverRoot.toPath().relativize(file.toPath()).toString();
                        ZipEntry zipEntry = new ZipEntry(entryName);
                        zipEntry.setTime(file.lastModified());

                        zos.putNextEntry(zipEntry);

                        // 使用緩衝流讀取檔案
                        try (FileInputStream fis = new FileInputStream(file);
                             BufferedInputStream bis = new BufferedInputStream(fis)) {

                            byte[] buffer = new byte[8192];
                            int len;
                            while ((len = bis.read(buffer)) > 0) {
                                zos.write(buffer, 0, len);
                            }
                        }

                        zos.closeEntry();

                        totalFiles.incrementAndGet();
                        totalSize.addAndGet(file.length());

                        if (config.isDebug()) {
                            logger.info("§e[除錯] 備份檔案: " + entryName);
                        }
                    } catch (IOException e) {
                        // 處理檔案鎖定錯誤
                        String errorMsg = e.getMessage();
                        if (errorMsg.contains("鎖定") || errorMsg.contains("locked") ||
                                errorMsg.contains("無法存取") || errorMsg.contains("access")) {

                            logger.warning("§c檔案被鎖定，跳過: " + file.getAbsolutePath() + " - " + e.getMessage());
                            skippedFiles.incrementAndGet();

                            // 嘗試關閉ZIP條目
                            try {
                                zos.closeEntry();
                            } catch (IOException ignored) {}
                        } else {
                            throw e;
                        }
                    }
                }
            }

            logger.info("§a備份完成！");
            logger.info("§e總共處理 " + totalFiles.get() + " 個檔案");
            logger.info("§e跳過 " + skippedFiles.get() + " 個被鎖定的檔案");
            logger.info("§e總大小: " + formatFileSize(totalSize.get()));

            if (skippedFiles.get() > 0) {
                logger.warning("§c注意：跳過了 " + skippedFiles.get() + " 個被鎖定的檔案，備份可能不完整");
                logger.warning("§c建議在伺服器閒置時或使用 /save-all 命令後再進行備份");
            }
        }
    }

    public boolean performSafeBackup(CommandSender sender, boolean isAuto) {
        if (isBackingUp) {
            if (sender != null) {
                sender.sendMessage(ChatColor.RED + "備份正在進行中，請稍候...");
            }
            return false;
        }

        isBackingUp = true;

        // 在備份前執行 save-all 命令（在主線程）
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-all");
            if (sender != null) {
                sender.sendMessage(ChatColor.YELLOW + "已執行 save-all 命令，正在保存世界...");
            }
            logger.info("§e執行 save-all 命令保存世界...");

            // 延遲2秒確保世界保存完成
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // 在異步線程中執行備份
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        String backupName = createBackupName(isAuto);

                        // 發送開始通知
                        String startMessage = ChatColor.GOLD + "[" + ChatColor.YELLOW + "備份" + ChatColor.GOLD + "] " +
                                ChatColor.GREEN + "開始創建備份: " + ChatColor.WHITE + backupName;

                        if (config.shouldNotify() && !isAuto) {
                            Bukkit.broadcastMessage(startMessage);
                        } else if (sender != null) {
                            sender.sendMessage(startMessage);
                        }

                        logger.info("§a開始創建備份: " + backupName);

                        // 創建備份檔案
                        File backupFile = createBackup(backupName);

                        if (backupFile != null && backupFile.exists()) {
                            // 計算檔案大小
                            String size = formatFileSize(backupFile.length());

                            // 清理舊備份
                            cleanupOldBackups();

                            // 發送完成通知
                            String successMessage = ChatColor.GOLD + "[" + ChatColor.YELLOW + "備份" + ChatColor.GOLD + "] " +
                                    ChatColor.GREEN + "備份完成! " +
                                    ChatColor.WHITE + "檔案: " + backupFile.getName() +
                                    ChatColor.GRAY + " (" + size + ")";

                            if (config.shouldNotify() && !isAuto) {
                                Bukkit.broadcastMessage(successMessage);
                            } else if (sender != null) {
                                sender.sendMessage(successMessage);
                            }

                            logger.info("§a備份創建成功: " + backupFile.getName() + " (" + size + ")");

                            // 記錄到控制台
                            LogUtils.logSuccess(logger, "備份",
                                    "創建備份完成: " + backupFile.getName() + " (" + size + ")");
                        } else {
                            throw new IOException("備份檔案創建失敗");
                        }

                    } catch (Exception e) {
                        String errorMessage = ChatColor.GOLD + "[" + ChatColor.YELLOW + "備份" + ChatColor.GOLD + "] " +
                                ChatColor.RED + "備份失敗: " + e.getMessage();

                        if (sender != null) {
                            sender.sendMessage(errorMessage);
                        }

                        logger.severe("§c備份失敗: " + e.getMessage());
                        e.printStackTrace();
                    } finally {
                        isBackingUp = false;
                    }
                });
            }, 40L); // 2秒後執行 (20 ticks = 1秒)
        });

        return true;
    }

    /**
     * 創建TAR.GZ備份（簡單實現）
     */
    private void createTarGzBackup(File serverRoot, File backupFile) throws IOException {
        // 這裡簡化實現，實際生產環境可能需要使用Apache Commons Compress等庫
        // 我們暫時使用ZIP作為替代
        createZipBackup(serverRoot, backupFile);
    }

    /**
     * 檢查文件是否應該排除
     */
    private boolean shouldExclude(Path filePath, List<String> excludePatterns) {
        String pathStr = filePath.toString().replace(File.separator, "/");

        for (String pattern : excludePatterns) {
            pattern = pattern.replace(File.separator, "/");

            // 簡單的通配符匹配
            if (pattern.contains("*")) {
                String regex = pattern.replace(".", "\\.").replace("*", ".*");
                if (pathStr.matches(regex)) {
                    return true;
                }
            } else if (pathStr.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 清理舊備份
     */
    private void cleanupOldBackups() {
        File backupDir = config.getBackupFolder();
        File[] backupFiles = backupDir.listFiles((dir, name) ->
                name.startsWith(config.getBackupPrefix()) &&
                        (name.endsWith(".zip") || name.endsWith(".tar.gz"))
        );

        if (backupFiles == null || backupFiles.length <= config.getMaxBackupCount()) {
            return;
        }

        // 按修改時間排序（舊的在前）
        Arrays.sort(backupFiles, Comparator.comparingLong(File::lastModified));

        // 刪除超出數量的舊備份
        int filesToDelete = backupFiles.length - config.getMaxBackupCount();
        for (int i = 0; i < filesToDelete; i++) {
            File oldBackup = backupFiles[i];
            if (oldBackup.delete()) {
                logger.info("§e刪除舊備份: " + oldBackup.getName());
            } else {
                logger.warning("§c無法刪除舊備份: " + oldBackup.getName());
            }
        }
    }

    /**
     * 格式化文件大小
     */
    public String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 獲取備份列表
     */
    public List<String> getBackupList() {
        List<String> backups = new ArrayList<>();
        File backupDir = config.getBackupFolder();

        if (backupDir.exists() && backupDir.isDirectory()) {
            File[] backupFiles = backupDir.listFiles((dir, name) ->
                    name.startsWith(config.getBackupPrefix()) &&
                            (name.endsWith(".zip") || name.endsWith(".tar.gz"))
            );

            if (backupFiles != null) {
                // 按修改時間排序（新的在前）
                Arrays.sort(backupFiles, Comparator.comparingLong(File::lastModified).reversed());

                for (File backup : backupFiles) {
                    String size = formatFileSize(backup.length());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String time = sdf.format(new Date(backup.lastModified()));

                    backups.add(ChatColor.YELLOW + backup.getName() +
                            ChatColor.GRAY + " (" + size + ", " + time + ")");
                }
            }
        }

        return backups;
    }

    /**
     * 在伺服器關閉時執行備份
     */
    public void backupOnStop() {
        if (config.isBackupOnStop() && !isBackingUp) {
            logger.info("§a伺服器關閉，開始執行關機備份...");
            performBackup(null, true);

            // 等待備份完成（最大等待30秒）
            int waitCount = 0;
            while (isBackingUp && waitCount < 30) {
                try {
                    Thread.sleep(1000);
                    waitCount++;
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    // ================ Getter 方法 ================

    public boolean isBackingUp() {
        return isBackingUp;
    }

    public BackupConfig getConfig() {
        return config;
    }
}