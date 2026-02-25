package kuku.plugin.function.backups;

import org.bukkit.configuration.file.FileConfiguration;
import kuku.plugin.function.FunctionPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * 備份模塊配置
 */
public class BackupConfig {

    private final FunctionPlugin plugin;
    private FileConfiguration config;

    // 配置路徑常量
    private static final String MODULE_ENABLED = "modules.backups";
    private static final String BACKUP_ENABLED = "backups.enabled";
    private static final String AUTO_BACKUP_ENABLED = "backups.auto.enabled";
    private static final String AUTO_BACKUP_INTERVAL = "backups.auto.interval";
    private static final String BACKUP_FOLDERS = "backups.folders";
    private static final String BACKUP_EXCLUDES = "backups.excludes";
    private static final String BACKUP_COMPRESSION = "backups.compression";
    private static final String BACKUP_MAX_COUNT = "backups.max-count";
    private static final String BACKUP_KEEP_DAYS = "backups.keep-days";
    private static final String BACKUP_SAVE_PATH = "backups.save-path";
    private static final String BACKUP_PREFIX = "backups.prefix";
    private static final String BACKUP_PERMISSION = "backups.permission";
    private static final String BACKUP_ON_STOP = "backups.on-stop";
    private static final String BACKUP_NOTIFY = "backups.notify";
    private static final String BACKUP_DEBUG = "backups.debug";
    private static final String SKIP_LOCKED_FILES = "backups.skip-locked-files";
    private static final String RETRY_LOCKED_FILES = "backups.retry-locked-files";
    private static final String RETRY_DELAY = "backups.retry-delay";

    public BackupConfig(FunctionPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * 重新加載配置
     */
    public void reload() {
        this.config = plugin.getPluginConfig();

        // 設置默認值
        setDefaults();
    }

    /**
     * 設置默認配置值
     */
    private void setDefaults() {
        // 模塊總開關
        config.addDefault(MODULE_ENABLED, true);

        // 備份基本設置
        config.addDefault(BACKUP_ENABLED, true);
        config.addDefault(BACKUP_PERMISSION, "kuku.backups.use");

        // 自動備份設置
        config.addDefault(AUTO_BACKUP_ENABLED, true);
        config.addDefault(AUTO_BACKUP_INTERVAL, 30); // 分鐘

        // 備份目錄設置
        config.addDefault(BACKUP_FOLDERS, Arrays.asList(
                "world",
                "world_nether",
                "world_the_end",
                "plugins",
                "server.properties",
                "bukkit.yml",
                "spigot.yml",
                "paper.yml",
                "ops.json",
                "whitelist.json",
                "banned-ips.json",
                "banned-players.json"
        ));

        config.addDefault(BACKUP_EXCLUDES, Arrays.asList(
                "world/playerdata/*.dat_old",
                "world/stats/*.json_old",
                "world/*.tmp",
                "plugins/**/*.jar",
                "logs"
        ));

        // 備份保存設置 - 改為插件資料目錄下的 backups 資料夾
        config.addDefault(BACKUP_SAVE_PATH, "backups");
        config.addDefault(BACKUP_PREFIX, "backup");
        config.addDefault(BACKUP_COMPRESSION, "zip"); // zip 或 tar.gz
        config.addDefault(BACKUP_MAX_COUNT, 50);
        config.addDefault(BACKUP_KEEP_DAYS, 30);

        // 其他設置
        config.addDefault(BACKUP_ON_STOP, true);
        config.addDefault(BACKUP_NOTIFY, true);
        config.addDefault(BACKUP_DEBUG, false);

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    // ================ Getter 方法 ================

    public boolean isModuleEnabled() {
        return config.getBoolean(MODULE_ENABLED, true);
    }

    public boolean isEnabled() {
        return config.getBoolean(BACKUP_ENABLED, true);
    }

    public boolean isAutoBackupEnabled() {
        return config.getBoolean(AUTO_BACKUP_ENABLED, true);
    }

    public int getAutoBackupInterval() {
        return config.getInt(AUTO_BACKUP_INTERVAL, 30);
    }

    public List<String> getBackupFolders() {
        return config.getStringList(BACKUP_FOLDERS);
    }

    public List<String> getExcludePatterns() {
        return config.getStringList(BACKUP_EXCLUDES);
    }

    public String getCompressionType() {
        return config.getString(BACKUP_COMPRESSION, "zip");
    }

    public int getMaxBackupCount() {
        return config.getInt(BACKUP_MAX_COUNT, 50);
    }

    public int getKeepDays() {
        return config.getInt(BACKUP_KEEP_DAYS, 30);
    }

    /**
     * 獲取備份保存路徑（相對於插件資料目錄）
     */
    public String getSavePath() {
        return config.getString(BACKUP_SAVE_PATH, "backups");
    }

    /**
     * 獲取備份檔案的完整路徑
     */
    public File getBackupFolder() {
        // 創建插件資料目錄下的 backups 資料夾
        File backupDir = new File(plugin.getDataFolder(), getSavePath());
        return backupDir;
    }

    public String getBackupPrefix() {
        return config.getString(BACKUP_PREFIX, "backup");
    }

    public String getPermission() {
        return config.getString(BACKUP_PERMISSION, "kuku.backups.use");
    }

    public boolean isBackupOnStop() {
        return config.getBoolean(BACKUP_ON_STOP, true);
    }

    public boolean shouldNotify() {
        return config.getBoolean(BACKUP_NOTIFY, true);
    }

    public boolean isDebug() {
        return config.getBoolean(BACKUP_DEBUG, false);
    }
}