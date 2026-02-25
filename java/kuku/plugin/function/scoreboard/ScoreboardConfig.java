package kuku.plugin.function.scoreboard;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scoreboard.DisplaySlot;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

/**
 * 計分板配置管理器
 * 使用獨立的 scoreboard.yml 配置文件
 */
public class ScoreboardConfig {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    // 默認配置
    private static final String DEFAULT_TITLE = "&6&l伺服器資訊";
    private static final int DEFAULT_UPDATE_INTERVAL = 20; // 20 ticks = 1秒

    public ScoreboardConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "scoreboard.yml");
        loadConfig();
    }

    /**
     * 加載配置文件
     */
    public void loadConfig() {
        if (!configFile.exists()) {
            plugin.saveResource("scoreboard.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        setupDefaults();
        saveConfig();
    }

    /**
     * 重載配置文件
     */
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * 保存配置文件
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("無法保存 scoreboard.yml: " + e.getMessage());
        }
    }

    /**
     * 設置默認配置
     */
    private void setupDefaults() {
        // 基本設置
        config.addDefault("enabled", true);
        config.addDefault("update-interval", DEFAULT_UPDATE_INTERVAL);
        config.addDefault("worlds.all-worlds", true);

        // 世界白名單
        List<String> defaultWorlds = new ArrayList<>();
        defaultWorlds.add("world");
        defaultWorlds.add("world_nether");
        defaultWorlds.add("world_the_end");
        config.addDefault("worlds.whitelist", defaultWorlds);

        // 計分板標題
        config.addDefault("title", DEFAULT_TITLE);
        config.addDefault("title-update-interval", 100); // 5秒

        // 計分板內容（更新：刪除金錢和伺服器IP，添加玩家延遲）
        List<String> defaultLines = new ArrayList<>();
        defaultLines.add("&7玩家: &f{player}");
        defaultLines.add("&7生態域: &f{biome}");
        defaultLines.add("&a══════════");
        defaultLines.add("&7世界: &f{world}");
        defaultLines.add("&7座標: &f{x} &8| &f{y} &8| &f{z}");
        defaultLines.add("&b══════════");
        defaultLines.add("&7在線: &f{online}/{max}");
        defaultLines.add("&7延遲: &f{ping}ms");
        defaultLines.add("&7時間: &f{time}");
        defaultLines.add("&c══════════");
        defaultLines.add("&7血量: &f{health} ❤");
        defaultLines.add("&7飽食: &f{food} 🍖");
        defaultLines.add("&7TPS: &f{tps}");
        config.addDefault("lines", defaultLines);

        // 佔位符說明（更新：刪除金錢和伺服器IP）
        List<String> placeholderHelp = new ArrayList<>();
        placeholderHelp.add("可用的佔位符:");
        placeholderHelp.add("  {player} - 玩家名稱");
        placeholderHelp.add("  {displayname} - 生態域名稱");
        placeholderHelp.add("  {biome} - 玩家等級");
        placeholderHelp.add("  {health} - 玩家血量");
        placeholderHelp.add("  {food} - 玩家飽食度");
        placeholderHelp.add("  {world} - 世界名稱");
        placeholderHelp.add("  {x} - X座標");
        placeholderHelp.add("  {y} - Y座標");
        placeholderHelp.add("  {z} - Z座標");
        placeholderHelp.add("  {online} - 在線玩家數");
        placeholderHelp.add("  {max} - 最大玩家數");
        placeholderHelp.add("  {time} - 當前時間");
        placeholderHelp.add("  {date} - 當前日期");
        placeholderHelp.add("  {ping} - 玩家延遲(ms)");
        placeholderHelp.add("  {tps} - 伺服器TPS");
        config.addDefault("placeholders", placeholderHelp);

        // 動畫設置
        config.addDefault("animations.enabled", false);
        config.addDefault("animations.title.frames", new ArrayList<String>());
        config.addDefault("animations.title.interval", 10);

        config.options().copyDefaults(true);
    }

    // =============== 獲取配置值的方法 ===============

    public boolean isEnabled() {
        return config.getBoolean("enabled", true);
    }

    public int getUpdateInterval() {
        return config.getInt("update-interval", DEFAULT_UPDATE_INTERVAL);
    }

    public boolean isAllWorlds() {
        return config.getBoolean("worlds.all-worlds", true);
    }

    public List<String> getWorldWhitelist() {
        return config.getStringList("worlds.whitelist");
    }

    public String getTitle() {
        return formatMessage(config.getString("title", DEFAULT_TITLE));
    }

    public int getTitleUpdateInterval() {
        return config.getInt("title-update-interval", 100);
    }

    public List<String> getLines() {
        List<String> lines = config.getStringList("lines");
        List<String> formattedLines = new ArrayList<>();

        for (String line : lines) {
            formattedLines.add(formatMessage(line));
        }

        return formattedLines;
    }

    // 動畫相關
    public boolean isAnimationsEnabled() {
        return config.getBoolean("animations.enabled", false);
    }

    public List<String> getTitleAnimationFrames() {
        List<String> frames = config.getStringList("animations.title.frames");
        List<String> formattedFrames = new ArrayList<>();

        for (String frame : frames) {
            formattedFrames.add(formatMessage(frame));
        }

        return formattedFrames;
    }

    public int getTitleAnimationInterval() {
        return config.getInt("animations.title.interval", 10);
    }

    // 輔助方法
    private String formatMessage(String message) {
        if (message == null) return "";
        return message.replace('&', '§');
    }

    /**
     * 檢查世界是否在白名單中
     */
    public boolean isWorldAllowed(String worldName) {
        if (isAllWorlds()) {
            return true;
        }
        return getWorldWhitelist().contains(worldName);
    }

    /**
     * 獲取原始配置對象
     */
    public FileConfiguration getRawConfig() {
        return config;
    }

    /**
     * 獲取配置文件
     */
    public File getConfigFile() {
        return configFile;
    }
}