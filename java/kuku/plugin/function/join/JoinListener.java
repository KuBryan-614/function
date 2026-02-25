package kuku.plugin.function.join;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;
import org.bukkit.Sound;

/**
 * 加入功能事件監聽器
 * 處理玩家加入和退出事件
 */
public class JoinListener implements Listener {

    private final JoinManager joinManager;
    private final JoinConfig config;

    // 系統前綴
    private static final String SYSTEM_PREFIX = "§8|§6系統§8| §f";

    public JoinListener(JoinManager joinManager) {
        this.joinManager = joinManager;
        this.config = joinManager.getConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!joinManager.isEnabled()) return;

        Player player = event.getPlayer();

        // 公共加入消息
        if (config.isWelcomeMessageEnabled()) {
            String message = replacePlaceholders(config.getJoinMessage(), player);
            event.setJoinMessage(message);
        } else {
            event.setJoinMessage(null); // 禁用默認消息
        }

        // 個人歡迎消息（延遲發送）
        if (config.isPersonalWelcomeEnabled()) {
            joinManager.getPlugin().getServer().getScheduler().runTaskLater(joinManager.getPlugin(), () -> {
                if (player.isOnline()) {
                    for (String line : config.getPersonalWelcomeMessages()) {
                        // 加上系統前綴
                        player.sendMessage(SYSTEM_PREFIX + replacePlaceholders(line, player));
                    }

                    // 歡迎音效
                    if (config.isWelcomeSoundEnabled()) {
                        playSound(player,
                                config.getWelcomeSound(),
                                config.getWelcomeVolume(),
                                config.getWelcomePitch()
                        );
                    }
                }
            }, config.getPersonalWelcomeDelay());
        }

        // 首次加入處理
        if (config.isFirstJoinEnabled() && !player.hasPlayedBefore()) {
            handleFirstJoin(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!joinManager.isEnabled() || !config.isQuitMessageEnabled()) {
            event.setQuitMessage(null); // 禁用默認消息
            return;
        }

        Player player = event.getPlayer();
        String message = replacePlaceholders(config.getQuitMessage(), player);
        event.setQuitMessage(message);
    }

    /**
     * 處理首次加入
     */
    private void handleFirstJoin(Player player) {
        joinManager.getPlugin().getServer().getScheduler().runTaskLater(joinManager.getPlugin(), () -> {
            if (player.isOnline()) {
                // 發送首次加入消息（加上系統前綴）
                for (String line : config.getFirstJoinMessages()) {
                    player.sendMessage(SYSTEM_PREFIX + replacePlaceholders(line, player));
                }

                // 播放首次加入音效
                if (config.isFirstJoinSoundEnabled()) {
                    playSound(player,
                            config.getFirstJoinSound(),
                            config.getFirstJoinVolume(),
                            config.getFirstJoinPitch()
                    );
                }
            }
        }, config.getFirstJoinDelay());
    }

    /**
     * 替換佔位符
     */
    private String replacePlaceholders(String text, Player player) {
        if (text == null || text.isEmpty()) return text;

        String result = text
                .replace("{player}", player.getName())
                .replace("{online}", String.valueOf(joinManager.getPlugin().getServer().getOnlinePlayers().size()))
                .replace("{max}", String.valueOf(joinManager.getPlugin().getServer().getMaxPlayers()))
                .replace("{world}", player.getWorld().getName());

        return result;
    }

    /**
     * 播放音效
     */
    private void playSound(Player player, Sound sound, float volume, float pitch) {
        try {
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (Exception e) {
            joinManager.getPlugin().getLogger().warning("無法播放音效: " + e.getMessage());
        }
    }
}