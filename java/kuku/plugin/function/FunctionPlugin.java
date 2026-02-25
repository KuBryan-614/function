package kuku.plugin.function;

import kuku.plugin.function.backups.BackupManager;
import kuku.plugin.function.clearlag.ClearLagManager;
import kuku.plugin.function.easybox.EasyBoxManager;
import kuku.plugin.function.exp.ExpManager;
import kuku.plugin.function.helpGUI.HelpGUIManager;
import kuku.plugin.function.monitor.TPSChecker;
import kuku.plugin.function.nc.NcManager;
import kuku.plugin.function.rightclick.RightClickHarvestManager;
import kuku.plugin.function.seed.SeedManager;
import kuku.plugin.function.craftNewTable.SlimeCraftManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;

import kuku.plugin.function.join.JoinManager;
import kuku.plugin.function.scoreboard.ScoreboardManager;
import kuku.plugin.function.chest.ChestManager;
import kuku.plugin.function.utils.LogUtils;
import kuku.plugin.function.totel.TotelManager;
import kuku.plugin.function.discord.DiscordManager;
import kuku.plugin.function.elevator.ElevatorManager;
import kuku.plugin.function.half.HalfSlabManager;

/**
 * Kuku Function Plugin
 * 版本: 1.21.10
 * 模塊化功能插件
 */
public class FunctionPlugin extends JavaPlugin {

    private static FunctionPlugin instance;
    private Logger logger;
    private PluginDescriptionFile description;
    private FileConfiguration config;

    // 模塊管理器
    private Map<String, Object> modules;
    private JoinManager joinManager;
    private ScoreboardManager scoreboardManager;
    private ChestManager chestManager;
    private NcManager ncManager;
    private TotelManager totelManager;
    private DiscordManager discordManager;
    private ElevatorManager elevatorManager;
    private HalfSlabManager halfSlabManager;
    private ClearLagManager clearLagManager;
    private SlimeCraftManager slimeCraftManager;
    private RightClickHarvestManager rightClickHarvestManager;
    private SeedManager seedManager;
    private EasyBoxManager easyBoxManager;
    private HelpGUIManager helpGUIManager;
    private BackupManager backupManager;
    private ExpManager expManager;
    private TPSChecker tpsChecker;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        description = getDescription();

        // 加載配置
        loadConfiguration();

        modules = new HashMap<>();

        // 初始化所有模塊
        initializeModules();

        // 註冊指令
        registerCommands();

        // 使用 LogUtils 記錄插件啟用
        LogUtils.logPluginEnable(logger, description.getName(), description.getVersion(), modules.size());
    }

    /**
     * 註冊所有指令
     */
    private void registerCommands() {
        // 註冊 Discord 命令
        getCommand("discord").setExecutor(this);
        getCommand("testelevator").setExecutor(this);

        // 註冊幫助GUI命令（在HelpGUIManager中設置執行器）
        Command helpguiCommand = getCommand("helpgui");
        if (helpguiCommand != null) {
            // 命令执行器将在 HelpGUIManager 中设置
        } else {
            logger.warning("§e帮助GUI命令未在 plugin.yml 中找到，请检查配置！");
        }

        // 備份命令現在由BackupManager中的BackupCommand處理

        logger.info("§a指令系統初始化完成");
    }

    /**
     * 處理指令執行
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 只處理 discord 命令，其他命令由各自的模塊處理
        if (command.getName().equalsIgnoreCase("discord")) {
            handleDiscordCommand(sender, args);
            return true;
        }

        // 其他命令已由各自的CommandExecutor處理
        return false;
    }

    @Override
    public void onDisable() {
        // 禁用所有模塊
        disableAllModules();

        // 使用 LogUtils 記錄插件禁用
        LogUtils.logPluginDisable(logger, description.getName(), modules.size());
    }

    /**
     * 加載配置文件
     */
    private void loadConfiguration() {
        // 保存默認配置
        saveDefaultConfig();

        // 重載配置
        reloadConfig();

        // 獲取配置實例
        config = getConfig();

        // 設置默認值
        setupDefaults();

        // 保存配置（確保新添加的默認值被保存）
        saveConfig();

        logger.info("§a配置文件已加載完成");
    }

    /**
     * 設置配置默認值
     */
    private void setupDefaults() {
        // 插件設置
        config.addDefault("settings.debug", false);
        config.addDefault("settings.language", "zh_TW");
        config.addDefault("settings.auto-update", false);

        // 模块总开关 - 使用英文键
        config.addDefault("modules.join", true);
        config.addDefault("modules.scoreboard", true);
        config.addDefault("modules.chest", true);
        config.addDefault("modules.totel", true);
        config.addDefault("modules.discord", true);

        // 指令權限
        config.addDefault("commands.nc.enabled", true);
        config.addDefault("commands.nc.permission", "kuku.command.nc");
        config.addDefault("commands.discord.permission", "kuku.command.discord");

        // 日誌設置
        config.addDefault("logging.level", "INFO");
        config.addDefault("logging.file", true);
        config.addDefault("logging.console", true);

        // 儲物箱模塊設置
        config.addDefault("chest.enabled", true);
        config.addDefault("chest.allowOpenWithBlockAbove", true);
        config.addDefault("chest.handleDoubleChests", true);
        config.addDefault("chest.showDebugMessages", false);
        config.addDefault("chest.customChestTypes", new String[]{});

        config.addDefault("elevator.enabled", true);
        config.addDefault("elevator.range", 10);
        config.addDefault("elevator.cooldown", 500);
        config.addDefault("elevator.sound-enabled", true);
        config.addDefault("elevator.jump-up", true);
        config.addDefault("elevator.sneak-down", true);
        config.addDefault("elevator.sneak-click-info", true);
        config.addDefault("elevator.show-message", true);
        config.addDefault("elevator.safety-check", true);
        config.addDefault("elevator.sign-identifier", "[電梯]");
        config.addDefault("elevator.support-platforms", true);
        config.addDefault("elevator.platform-min-size", 1);
        config.addDefault("elevator.platform-max-size", 3);

        // 模塊總開關
        config.addDefault("modules.halfslab", true);
        // 半磚模塊詳細設置（主配置中的預設值）
        config.addDefault("half.enabled", true);
        config.addDefault("half.show-message", true);
        config.addDefault("half.play-effects", true);
        config.addDefault("half.drop-items", true);
        config.addDefault("half.raytrace-distance", 6.0);
        config.addDefault("half.particle-count", 20);
        config.addDefault("half.cooldown-ticks", 1);
        config.addDefault("half.excluded-materials", new String[]{});
        config.addDefault("half.permissions.enabled", true);
        config.addDefault("half.permissions.required", "kuku.halfslab.use");

        // 在 setupDefaults 方法中添加默认配置
        config.addDefault("modules.slimecraft", true);
        config.addDefault("slimecraft.enabled", true);
        config.addDefault("slimecraft.debug", false);
        config.addDefault("slimecraft.recipes.snowball-to-compressed.enabled", true);
        config.addDefault("slimecraft.recipes.snowball-to-compressed.output-amount", 1);
        config.addDefault("slimecraft.recipes.compressed-to-core.enabled", true);
        config.addDefault("slimecraft.recipes.compressed-to-core.output-amount", 1);
        config.addDefault("slimecraft.recipes.slimeball.enabled", true);
        config.addDefault("slimecraft.recipes.slimeball.output-amount", 1);
        config.addDefault("slimecraft.recipes.decompose.enabled", true);
        config.addDefault("slimecraft.recipes.decompose.output-amount", 9);

        // 右键采收模块
        config.addDefault("enabled", true);

        // 清理掉落物模块开关
        config.addDefault("modules.clearlag", true); // 在模块总开关部分添加

        // 种子查看模块
        config.addDefault("modules.seed", true);
        config.addDefault("seed.enabled", true);
        config.addDefault("seed.message-prefix", "&6&l[種子] &f");

        // EasyBox 默認配置
        config.addDefault("modules.easybox", true);
        config.addDefault("easybox.enabled", true);
        config.addDefault("easybox.lore-enabled", true);
        config.addDefault("easybox.lore-title", "&7[ &e內容 &7] &8(%total% 個物品)");
        config.addDefault("easybox.lore-format", " &7• %item% &8x%amount%");
        config.addDefault("easybox.max-lore-lines", 6);
        config.addDefault("easybox.empty-message", " &7• 空的");
        config.addDefault("easybox.inventory-title", "&8[&6界伏盒&8]");

        // 自動破壞開關
        config.addDefault("modules.autodestroy", true);
        config.addDefault("autodestroy.enabled", true);
        config.addDefault("autodestroy.max-per-player", 10);
        config.addDefault("autodestroy.cooldown-ticks", 2);
        config.addDefault("autodestroy.enable-silk-touch", true);
        config.addDefault("autodestroy.enable-fortune", true);
        config.addDefault("autodestroy.require-online", true);

        //music開關
        config.addDefault("modules.music", true);

        //help GUI
        config.addDefault("modules.helpgui", true);
        config.addDefault("helpgui.enabled", true);

        //backups
        config.addDefault("modules.backups", true);
        config.addDefault("backups.enabled", true);
        config.addDefault("backups.auto.enabled", true);
        config.addDefault("backups.auto.interval", 30);
        config.addDefault("backups.folders", Arrays.asList(
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
        config.addDefault("backups.excludes", Arrays.asList(
                "world/playerdata/*.dat_old",
                "world/stats/*.json_old",
                "world/*.tmp",
                "plugins/**/*.jar",
                "logs"
        ));
        config.addDefault("backups.compression", "zip");
        config.addDefault("backups.max-count", 50);
        config.addDefault("backups.keep-days", 30);
        config.addDefault("backups.save-path", "backups");
        config.addDefault("backups.prefix", "backup");
        config.addDefault("backups.permission", "kuku.backups.use");
        config.addDefault("backups.on-stop", true);
        config.addDefault("backups.notify", true);
        config.addDefault("backups.debug", false);

        // 經驗存取模塊配置
        config.addDefault("modules.exp", true);
        config.addDefault("exp.enabled", true);
        config.addDefault("exp.max-level-per-action", 1000);
        config.addDefault("exp.show-particles", true);
        config.addDefault("exp.allow-special-commands", true); // 允許 all/half/quarter/max
        config.addDefault("exp.play-sounds", true);
        config.addDefault("exp.message-prefix", "§6§l[經驗系統] §f");

        // TPS监控模块
        config.addDefault("modules.tps", true); // 模块总开关
        config.addDefault("tps.enabled", true);
        config.addDefault("tps.detailed-info", true);
        config.addDefault("tps.show-warning", true);
        config.addDefault("tps.low-tps-threshold", 15.0);
        config.addDefault("tps.high-tps-threshold", 18.0);
        config.addDefault("tps.command-permission", "kuku.command.tps");
        config.addDefault("tps.auto-broadcast", false);
        config.addDefault("tps.broadcast-interval", 300);
        config.addDefault("tps.broadcast-tps-threshold", 10.0);






        config.options().copyDefaults(true);       // 播放音效

    }

    /**
     * 檢查模塊是否啟用
     */
    private boolean isModuleEnabled(String moduleName) {
        // 模块名已使用英文
        return config.getBoolean("modules." + moduleName, true);
    }

    /**
     * 初始化所有模塊
     */
    private void initializeModules() {
        LogUtils.startModuleStatus(logger);
        logger.info("§e正在初始化模塊...");

        // 確保數據文件夾存在
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // 初始化加入模塊
        try {
            joinManager = new JoinManager(this);

            // 先檢查模塊總開關，再檢查模塊自身配置
            if (isModuleEnabled("join") && joinManager.getConfig().isEnabled()) {
                joinManager.enable();
                modules.put("join", joinManager);
                LogUtils.logModuleLoad(logger, "加入", true, "使用獨立配置");
            } else {
                LogUtils.logModuleLoad(logger, "加入", false, "模塊禁用");
            }
        } catch (Exception e) {
            LogUtils.logError(logger, "加入模塊", e.getMessage());
            e.printStackTrace();
        }

        // 初始化計分板模塊
        try {
            scoreboardManager = new ScoreboardManager(this);

            if (isModuleEnabled("scoreboard") && scoreboardManager.getConfig().isEnabled()) {
                scoreboardManager.enable();
                modules.put("scoreboard", scoreboardManager);
                LogUtils.logModuleLoad(logger, "計分板", true, "使用獨立配置");
            } else {
                LogUtils.logModuleLoad(logger, "計分板", false, "模塊禁用");
            }
        } catch (Exception e) {
            LogUtils.logError(logger, "計分板模塊", e.getMessage());
            e.printStackTrace();
        }

        // 初始化儲物箱模塊
        try {
            chestManager = new ChestManager(this);

            if (isModuleEnabled("chest") && chestManager.getConfig().isEnabled()) {
                chestManager.enable();
                modules.put("chest", chestManager);
                LogUtils.logModuleLoad(logger, "儲物箱", true, "允許上方有方塊開啟");
            } else {
                LogUtils.logModuleLoad(logger, "儲物箱", false, "模塊禁用");
            }
        } catch (Exception e) {
            LogUtils.logError(logger, "儲物箱模塊", e.getMessage());
            e.printStackTrace();
        }

        // 初始化座標計算模塊
        try {
            ncManager = new NcManager(this);

            if (isModuleEnabled("nc") && ncManager.getConfig().isEnabled()) {
                ncManager.enable();
                modules.put("nc", ncManager);
                LogUtils.logModuleLoad(logger, "座標計算", true, "使用獨立配置");
            } else {
                LogUtils.logModuleLoad(logger, "座標計算", false, "模塊禁用");
            }
        } catch (Exception e) {
            LogUtils.logError(logger, "座標計算模塊", e.getMessage());
            e.printStackTrace();
        }

        // 初始化不死圖騰模塊
        try {
            totelManager = new TotelManager(this);

            if (isModuleEnabled("totel")) {
                totelManager.enable();
                modules.put("totel", totelManager);
                LogUtils.logModuleLoad(logger, "不死圖騰", true, "簡化版 - 全背包觸發");
            } else {
                LogUtils.logModuleLoad(logger, "不死圖騰", false, "模塊禁用");
            }
        } catch (Exception e) {
            LogUtils.logError(logger, "不死圖騰模塊", e.getMessage());
            e.printStackTrace();
        }

        // ========== 新增：初始化 Discord 模塊 ==========
        try {
            discordManager = new DiscordManager(this);

            if (isModuleEnabled("discord") && discordManager.getConfig().isValid()) {
                discordManager.enable();
                modules.put("discord", discordManager);
                LogUtils.logModuleLoad(logger, "Discord", true, "雙向互通功能");
            } else {
                LogUtils.logModuleLoad(logger, "Discord", false, "配置無效或模塊禁用");
            }
        } catch (Exception e) {
            LogUtils.logError(logger, "Discord模塊", e.getMessage());
            e.printStackTrace();
        }

        try {
            elevatorManager = new ElevatorManager(this);

            if (isModuleEnabled("elevator") && config.getBoolean("elevator.enabled", true)) {
                elevatorManager.enable();
                modules.put("elevator", elevatorManager);
                LogUtils.logModuleLoad(logger, "電梯", true, "羊毛+告示牌電梯系統");

                // 輸出調試信息
                logger.info("§a[電梯] 電梯模塊已成功加載");
            } else {
                LogUtils.logModuleLoad(logger, "電梯", false, "模塊禁用");
            }
        } catch (Exception e) {
            LogUtils.logError(logger, "電梯模塊", e.getMessage());
            e.printStackTrace();
        }

        try {
            halfSlabManager = new HalfSlabManager(this);

            if (isModuleEnabled("halfslab") && halfSlabManager.getConfig().isEnabled()) {
                halfSlabManager.enable();
                modules.put("halfslab", halfSlabManager);
                LogUtils.logModuleLoad(logger, "半磚單格破壞", true, "潛行時單格破壞");
            } else {
                LogUtils.logModuleLoad(logger, "半磚單格破壞", false, "模塊禁用");
            }
        } catch (Exception e) {
            LogUtils.logError(logger, "半磚單格破壞模塊", e.getMessage());
            e.printStackTrace();
        }

        try {
            clearLagManager = new ClearLagManager(this);

            if (isModuleEnabled("clearlag")) {
                clearLagManager.enable();
                modules.put("clearlag", clearLagManager);
                LogUtils.logModuleLoad(logger, "清理掉落物", true,
                        "定时清理掉落物，显示倒计时");
            } else {
                LogUtils.logModuleLoad(logger, "清理掉落物", false, "模块禁用");
            }
        } catch (Exception e) {
            LogUtils.logError(logger, "清理掉落物模块", e.getMessage());
            e.printStackTrace();
        }

        try {
            slimeCraftManager = new SlimeCraftManager(this);

            if (isModuleEnabled("slimecraft") && slimeCraftManager.getConfig().isEnabled()) {
                slimeCraftManager.enable();
                modules.put("slimecraft", slimeCraftManager);
                LogUtils.logModuleLoad(logger, "黏液球合成", true, "多层合成系统");
            } else {
                LogUtils.logModuleLoad(logger, "黏液球合成", false, "模块禁用");
            }
        } catch (Exception e) {
            LogUtils.logError(logger, "黏液球合成模块", e.getMessage());
            e.printStackTrace();
        }

        // 初始化右键采收模块
        try {
            rightClickHarvestManager = new RightClickHarvestManager(this);

            if (isModuleEnabled("rightclick") && rightClickHarvestManager.isEnabled()) {
                rightClickHarvestManager.enable();
                modules.put("rightclick", rightClickHarvestManager);
                // 日志已经在 enable 方法中记录
            } else {
                LogUtils.logModuleLoad(logger, "右键采收", false, "模块禁用");
            }
        } catch (Exception e) {
            LogUtils.logError(logger, "右键采收模块", e.getMessage());
            e.printStackTrace();
        }

        // 初始化种子查看模块
        try {
            seedManager = new SeedManager(this);

            if (isModuleEnabled("seed")) {
                seedManager.enable();
                modules.put("seed", seedManager);
                LogUtils.logModuleLoad(logger, "种子查看", true, "所有玩家可用 /seed 指令");
            } else {
                LogUtils.logModuleLoad(logger, "种子查看", false, "模块禁用");
            }
        } catch (Exception e) {
            LogUtils.logError(logger, "种子查看模块", e.getMessage());
            e.printStackTrace();
        }

        // 初始化界伏盒模塊
        try {
            easyBoxManager = new EasyBoxManager(this);

            if (isModuleEnabled("easybox") && easyBoxManager.getConfig().isEnabled()) {
                easyBoxManager.enable();
                modules.put("easybox", easyBoxManager);
                LogUtils.logModuleLoad(logger, "界伏盒", true, "右鍵空氣開啟 + Lore顯示");
            } else {
                LogUtils.logModuleLoad(logger, "界伏盒", false, "模塊禁用");
            }
        } catch (Exception e) {
            LogUtils.logError(logger, "界伏盒模塊", e.getMessage());
            e.printStackTrace();
        }

        //helpGUI初始化
        try {
            if (isModuleEnabled("helpgui") && config.getBoolean("helpgui.enabled", true)) {
                helpGUIManager = new HelpGUIManager(this);
                helpGUIManager.enable();
                modules.put("helpgui", helpGUIManager);
                LogUtils.logModuleLoad(logger, "帮助GUI", true, "蹲下+F打开 (54格)");
            } else {
                LogUtils.logModuleLoad(logger, "帮助GUI", false, "模块禁用");
            }
        } catch (Exception e) {
            LogUtils.logError(logger, "帮助GUI模块", e.getMessage());
            e.printStackTrace();
        }

        // 初始化備份模塊
        try {
            backupManager = new BackupManager(this);

            if (isModuleEnabled("backups")) {
                backupManager.enable();
                modules.put("backups", backupManager);
                LogUtils.logModuleLoad(logger, "備份系統", true,
                        "自動備份間隔: " + backupManager.getConfig().getAutoBackupInterval() + "分鐘");
            } else {
                LogUtils.logModuleLoad(logger, "備份系統", false, "模塊禁用");
            }
        } catch (Exception e) {
            LogUtils.logError(logger, "備份系統模塊", e.getMessage());
            e.printStackTrace();
        }

        // 初始化經驗存取模塊
        try {
            expManager = new ExpManager(this);

            if (isModuleEnabled("exp") && expManager.isEnabled()) {
                expManager.enable();
                modules.put("exp", expManager);
                LogUtils.logModuleLoad(logger, "經驗存取", true, "等級經驗雙向轉換");
            } else {
                LogUtils.logModuleLoad(logger, "經驗存取", false, "模塊禁用");
            }
        } catch (Exception e) {
            LogUtils.logError(logger, "經驗存取模塊", e.getMessage());
            e.printStackTrace();
        }

        // 初始化TPS监控模块
        try {
            tpsChecker = new TPSChecker(this);

            if (isModuleEnabled("tps")) {
                tpsChecker.enable();
                modules.put("tps", tpsChecker);
                LogUtils.logModuleLoad(logger, "TPS监控", true, "实时监控服务器性能");
            } else {
                LogUtils.logModuleLoad(logger, "TPS监控", false, "模块禁用");
            }
        } catch (Exception e) {
            LogUtils.logError(logger, "TPS监控模块", e.getMessage());
            e.printStackTrace();
        }

        // ============================================

        // 記錄模塊狀態總結
        LogUtils.logSeparator(logger);
        LogUtils.logValueStatus(logger, "已加載模塊數量", String.valueOf(modules.size()));
        logger.info("§e模塊初始化完成!");
    }

    /**
     * 禁用所有模塊
     */
    private void disableAllModules() {
        for (Object module : modules.values()) {
            if (module instanceof JoinManager) {
                ((JoinManager) module).disable();
            } else if (module instanceof ScoreboardManager) {
                ((ScoreboardManager) module).disable();
            } else if (module instanceof ChestManager) {
                ((ChestManager) module).disable();
            } else if (module instanceof NcManager) {
                ((NcManager) module).disable();
            } else if (module instanceof TotelManager) {
                ((TotelManager) module).disable();
            } else if (module instanceof DiscordManager) {
                ((DiscordManager) module).disable();
            } else if (module instanceof ElevatorManager) {
                ((ElevatorManager) module).disable();
            } else if (module instanceof HalfSlabManager) {
                ((HalfSlabManager) module).disable();
            } else if (module instanceof ClearLagManager) {
                ((ClearLagManager) module).disable();
            } else if (module instanceof SlimeCraftManager) {
                ((SlimeCraftManager) module).disable();
            } else if (module instanceof RightClickHarvestManager) {
                ((RightClickHarvestManager) module).disable();
            } else if (module instanceof SeedManager) {
                ((SeedManager) module).disable();
            } else if (module instanceof EasyBoxManager) {
                ((EasyBoxManager) module).disable();
            } else if (module instanceof HelpGUIManager) {
                ((HelpGUIManager) module).disable();
            } else if (module instanceof BackupManager) {
                ((BackupManager) module).disable();
            } else if (module instanceof ExpManager) {
                ((ExpManager) module).disable();
            } else if (module instanceof TPSChecker) {
                ((TPSChecker) module).disable();
            }
        }
        modules.clear();
    }

    /**
     * 重載插件配置
     */
    public void reloadPlugin() {
        // 重載主配置
        reloadConfig();
        config = getConfig();

        // 記錄重載開始
        logger.info("§a開始重載插件配置...");
        LogUtils.logSeparator(logger);

        // 重載所有已加載模塊
        int reloadedCount = 0;
        for (Object module : modules.values()) {
            try {
                if (module instanceof JoinManager) {
                    ((JoinManager) module).reload();
                    reloadedCount++;
                } else if (module instanceof ScoreboardManager) {
                    ((ScoreboardManager) module).reload();
                    reloadedCount++;
                } else if (module instanceof ChestManager) {
                    ((ChestManager) module).reload();
                    reloadedCount++;
                } else if (module instanceof NcManager) {
                    ((NcManager) module).reload();
                    reloadedCount++;
                } else if (module instanceof TotelManager) {
                    ((TotelManager) module).reload();
                    reloadedCount++;
                } else if (module instanceof DiscordManager) { // 新增 Discord 模塊重載
                    ((DiscordManager) module).reload();
                    reloadedCount++;
                } else if (module instanceof HalfSlabManager) {
                    ((HalfSlabManager) module).reload();
                    reloadedCount++;
                } else if (module instanceof ClearLagManager) {
                    ((ClearLagManager) module).reload();
                    reloadedCount++;
                } else if (module instanceof SlimeCraftManager) {
                    ((SlimeCraftManager) module).reload();
                    reloadedCount++;
                } else if (module instanceof RightClickHarvestManager) {
                    ((RightClickHarvestManager) module).reload();
                    reloadedCount++;
                } else if (module instanceof SeedManager) {
                    ((SeedManager) module).reload();
                    reloadedCount++;
                } else if (module instanceof EasyBoxManager) {
                    ((EasyBoxManager) module).reload();
                    reloadedCount++;
                } else if (module instanceof BackupManager) {
                    ((BackupManager) module).reload();
                    reloadedCount++;
                } else if (module instanceof ExpManager) {
                    ((ExpManager) module).reload();
                    reloadedCount++;
                } else if (module instanceof TPSChecker) {
                    ((TPSChecker) module).reload();
                    reloadedCount++;
                }
            } catch (Exception e) {
                LogUtils.logError(logger, "模塊重載", "重載模塊時發生錯誤: " + e.getMessage());
            }
        }

        LogUtils.logSeparator(logger);
        LogUtils.logSuccess(logger, "插件",
                "配置重載完成! 已重載 " + reloadedCount + " 個模塊");
    }

    /**
     * 處理 Discord 命令
     */
    private void handleDiscordCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kuku.command.discord")) {
            sender.sendMessage(ChatColor.RED + "你沒有權限使用此指令");
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(getDiscordStatus());
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!discordManager.isEnabled()) {
                    sender.sendMessage(ChatColor.RED + "Discord 模塊未啟用");
                    return;
                }
                discordManager.reload();
                sender.sendMessage(ChatColor.GREEN + "Discord 配置已重載");
                break;

            case "status":
                sender.sendMessage(getDiscordStatus());
                break;

            case "send":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "用法: /discord send <訊息>");
                    return;
                }

                if (!discordManager.isEnabled()) {
                    sender.sendMessage(ChatColor.RED + "Discord 模塊未啟用");
                    return;
                }

                StringBuilder message = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    message.append(args[i]).append(" ");
                }

                discordManager.sendMessage("**[遊戲內]** " + sender.getName() + ": " + message.toString());
                sender.sendMessage(ChatColor.GREEN + "訊息已發送到 Discord");
                break;

            case "broadcast":
                if (!sender.hasPermission("kuku.discord.admin")) {
                    sender.sendMessage(ChatColor.RED + "你沒有權限執行此指令");
                    return;
                }

                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "用法: /discord broadcast <訊息>");
                    return;
                }

                StringBuilder broadcastMsg = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    broadcastMsg.append(args[i]).append(" ");
                }

                String formatted = ChatColor.translateAlternateColorCodes('&',
                        "&6[Discord廣播] &f" + broadcastMsg.toString());

                Bukkit.broadcastMessage(formatted);
                discordManager.sendMessage("📢 **伺服器廣播**: " + broadcastMsg.toString());
                sender.sendMessage(ChatColor.GREEN + "廣播訊息已發送到 Discord 和遊戲內");
                break;

            default:
                sender.sendMessage(ChatColor.GOLD + "=== Discord 指令幫助 ===");
                sender.sendMessage(ChatColor.YELLOW + "/discord status - 查看 Discord 狀態");
                sender.sendMessage(ChatColor.YELLOW + "/discord send <訊息> - 發送訊息到 Discord");
                if (sender.hasPermission("kuku.discord.admin")) {
                    sender.sendMessage(ChatColor.YELLOW + "/discord reload - 重載 Discord 配置");
                    sender.sendMessage(ChatColor.YELLOW + "/discord broadcast <訊息> - 廣播訊息到 Discord 和遊戲");
                }
        }
    }

    /**
     * 獲取 Discord 狀態訊息
     */
    private String getDiscordStatus() {
        if (discordManager == null) {
            return "§cDiscord 模塊未初始化";
        }

        if (!discordManager.isEnabled()) {
            return "§cDiscord 模塊未啟用";
        }

        return discordManager.getStatus();
    }

    /**
     * 管理模塊狀態（根據配置啟用/禁用模塊）
     */
    private void manageModuleStates() {
        // 這裡可以根據需要實現動態啟用/禁用模塊
        // 目前設計是重載時不改變模塊啟用狀態，需要重啟插件才能變更
        logger.info("§e注意：模塊啟用狀態變更需要重啟插件生效");
    }

    // =============== 公共方法 ===============

    public static FunctionPlugin getInstance() {
        return instance;
    }

    public SeedManager getSeedManager() {
        return seedManager;
    }

    public FileConfiguration getPluginConfig() {
        return config;
    }

    public JoinManager getJoinManager() {
        return joinManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public ChestManager getChestManager() {
        return chestManager;
    }

    public Object getModule(String name) {
        return modules.get(name);
    }

    public int getModuleCount() {
        return modules.size();
    }

    public EasyBoxManager getEasyBoxManager() {
        return easyBoxManager;
    }

    public TotelManager getTotelManager() {
        return totelManager;
    }

    public DiscordManager getDiscordManager() {
        return discordManager;
    }

    public ElevatorManager getElevatorManager() {
        return elevatorManager;
    }

    public HalfSlabManager getHalfSlabManager() {
        return halfSlabManager;
    }

    public ClearLagManager getClearLagManager() {
        return clearLagManager;
    }

    public SlimeCraftManager getSlimeCraftManager() {
        return slimeCraftManager;
    }

    public RightClickHarvestManager getRightClickHarvestManager() {
        return rightClickHarvestManager;
    }

    public boolean isAllowEmptyHand() {
        return config.getBoolean("allow-empty-hand", true);
    }

    public boolean isModuleLoaded(String name) {
        return modules.containsKey(name);
    }

    public HelpGUIManager getHelpGUIManager() {return helpGUIManager;}

    public BackupManager getBackupManager() {return backupManager;}

    public ExpManager getExpManager() {return expManager;}

    public TPSChecker getTPSChecker() {return tpsChecker;}


}
