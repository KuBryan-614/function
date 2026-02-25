package kuku.plugin.function.scoreboard;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;

/**
 * 計分板事件監聽器（簡化版）
 */
public class ScoreboardListener implements Listener {

    private final ScoreboardManager scoreboardManager;

    public ScoreboardListener(ScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 檢查是否啟用計分板
        if (!scoreboardManager.isEnabled()) return;

        // 檢查世界是否允許
        if (!scoreboardManager.getConfig().isWorldAllowed(event.getPlayer().getWorld().getName())) {
            return;
        }

        // 立即創建計分板
        scoreboardManager.createScoreboard(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        scoreboardManager.removeScoreboard(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        if (!scoreboardManager.isEnabled()) return;

        // 檢查新世界是否允許
        if (scoreboardManager.getConfig().isWorldAllowed(event.getPlayer().getWorld().getName())) {
            // 在新世界創建計分板
            scoreboardManager.createScoreboard(event.getPlayer());
        } else {
            // 移除計分板
            scoreboardManager.removeScoreboard(event.getPlayer());
        }
    }
}