package kuku.plugin.function.join;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Sound;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

/**
 * 加入功能配置管理器
 * 使用獨立的 join.yml 配置文件
 */
public class JoinConfig {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    // 默認配置
    private static final String DEFAULT_WELCOME = "&a[+] &f{player} 加入了遊戲";
    private static final String DEFAULT_QUIT = "&c[-] &f{player} 離開了遊戲";

    public JoinConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "join.yml");
        loadConfig();
    }

    /**
     * 加載配置文件
     */
    public void loadConfig() {
        if (!configFile.exists()) {
            plugin.saveResource("join.yml", false);
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
            plugin.getLogger().severe("無法保存 join.yml: " + e.getMessage());
        }
    }

    /**
     * 設置默認配置
     */
    private void setupDefaults() {
        // 功能開關
        config.addDefault("enabled", true);

        // 功能模塊開關
        config.addDefault("modules.welcome-message.enabled", true);
        config.addDefault("modules.quit-message.enabled", true);
        config.addDefault("modules.personal-welcome.enabled", true);
        config.addDefault("modules.first-join.enabled", true);
        config.addDefault("modules.sound-effects.enabled", true);

        // 消息配置
        config.addDefault("messages.join", DEFAULT_WELCOME);
        config.addDefault("messages.quit", DEFAULT_QUIT);

        // 個人歡迎消息（多行）
        List<String> defaultPersonalWelcome = new ArrayList<>();
        defaultPersonalWelcome.add("&6&l歡迎來到伺服器!");
        defaultPersonalWelcome.add("&7當前在線: &f{online}&7/&f{max} 玩家");
        defaultPersonalWelcome.add("&7使用 &f/help &7查看幫助");
        config.addDefault("messages.personal-welcome", defaultPersonalWelcome);

        // 首次加入消息
        List<String> defaultFirstJoin = new ArrayList<>();
        defaultFirstJoin.add("&a✦ &f檢測到你是第一次加入本伺服器!");
        defaultFirstJoin.add("&7請查看新手引導了解伺服器規則");
        config.addDefault("messages.first-join", defaultFirstJoin);

        // 延遲配置（單位：tick，20tick=1秒）
        config.addDefault("delays.personal-welcome", 20);
        config.addDefault("delays.first-join", 40);

        // 音效配置
        config.addDefault("sounds.first-join.enabled", true);
        config.addDefault("sounds.first-join.type", "ENTITY_PLAYER_LEVELUP");
        config.addDefault("sounds.first-join.volume", 1.0);
        config.addDefault("sounds.first-join.pitch", 1.0);

        config.addDefault("sounds.welcome.enabled", false);
        config.addDefault("sounds.welcome.type", "ENTITY_EXPERIENCE_ORB_PICKUP");
        config.addDefault("sounds.welcome.volume", 0.5);
        config.addDefault("sounds.welcome.pitch", 1.2);

        // 佔位符說明
        List<String> placeholderHelp = new ArrayList<>();
        placeholderHelp.add("可用的佔位符:");
        placeholderHelp.add("  {player} - 玩家名稱");
        placeholderHelp.add("  {online} - 當前在線玩家數");
        placeholderHelp.add("  {max} - 伺服器最大玩家數");
        placeholderHelp.add("  {world} - 世界名稱");
        config.addDefault("placeholders", placeholderHelp);

        config.options().copyDefaults(true);
    }

    // =============== 獲取配置值的方法 ===============

    public boolean isEnabled() {
        return config.getBoolean("enabled", true);
    }

    // 模塊啟用狀態檢查
    public boolean isWelcomeMessageEnabled() {
        return config.getBoolean("modules.welcome-message.enabled", true);
    }

    public boolean isQuitMessageEnabled() {
        return config.getBoolean("modules.quit-message.enabled", true);
    }

    public boolean isPersonalWelcomeEnabled() {
        return config.getBoolean("modules.personal-welcome.enabled", true);
    }

    public boolean isFirstJoinEnabled() {
        return config.getBoolean("modules.first-join.enabled", true);
    }

    public boolean isSoundEffectsEnabled() {
        return config.getBoolean("modules.sound-effects.enabled", true);
    }

    // 消息獲取方法
    public String getJoinMessage() {
        return formatMessage(config.getString("messages.join", DEFAULT_WELCOME));
    }

    public String getQuitMessage() {
        return formatMessage(config.getString("messages.quit", DEFAULT_QUIT));
    }

    public List<String> getPersonalWelcomeMessages() {
        return formatMessages(config.getStringList("messages.personal-welcome"));
    }

    public List<String> getFirstJoinMessages() {
        return formatMessages(config.getStringList("messages.first-join"));
    }

    // 延遲獲取
    public int getPersonalWelcomeDelay() {
        return config.getInt("delays.personal-welcome", 20);
    }

    public int getFirstJoinDelay() {
        return config.getInt("delays.first-join", 40);
    }

    // 音效配置獲取
    public boolean isFirstJoinSoundEnabled() {
        return config.getBoolean("sounds.first-join.enabled", true) && isSoundEffectsEnabled();
    }

    public Sound getFirstJoinSound() {
        return getSound("sounds.first-join.type", "ENTITY_PLAYER_LEVELUP");
    }

    public float getFirstJoinVolume() {
        return (float) config.getDouble("sounds.first-join.volume", 1.0);
    }

    public float getFirstJoinPitch() {
        return (float) config.getDouble("sounds.first-join.pitch", 1.0);
    }

    public boolean isWelcomeSoundEnabled() {
        return config.getBoolean("sounds.welcome.enabled", false) && isSoundEffectsEnabled();
    }

    public Sound getWelcomeSound() {
        return getSound("sounds.welcome.type", "ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    public float getWelcomeVolume() {
        return (float) config.getDouble("sounds.welcome.volume", 0.5);
    }

    public float getWelcomePitch() {
        return (float) config.getDouble("sounds.welcome.pitch", 1.2);
    }

    // 輔助方法
    private String formatMessage(String message) {
        if (message == null) return "";
        return message.replace('&', '§');
    }

    private List<String> formatMessages(List<String> messages) {
        List<String> formatted = new ArrayList<>();
        for (String message : messages) {
            formatted.add(formatMessage(message));
        }
        return formatted;
    }

    private Sound getSound(String path, String defaultValue) {
        String soundName = config.getString(path, defaultValue);
        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("未知的音效類型: " + soundName + "，使用默認值: " + defaultValue);
            try {
                return Sound.valueOf(defaultValue.toUpperCase());
            } catch (IllegalArgumentException e2) {
                return Sound.ENTITY_PLAYER_LEVELUP;
            }
        }
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