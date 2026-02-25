package kuku.plugin.function.scoreboard;

import java.util.HashMap;
import java.util.Map;

/**
 * 玩家计分板状态缓存，用于差分更新
 */
public class ScoreboardCache {
    private String lastTitle;
    private final Map<Integer, String> lastLines = new HashMap<>(); // 行索引 -> 行内容
    private final Map<Integer, String> lastEntries = new HashMap<>(); // 行索引 -> 唯一标识符
    private long lastUpdateTime;

    public String getLastTitle() { return lastTitle; }
    public void setLastTitle(String lastTitle) { this.lastTitle = lastTitle; }

    public String getLastLine(int index) { return lastLines.get(index); }
    public void setLastLine(int index, String line) { lastLines.put(index, line); }

    public String getLastEntry(int index) { return lastEntries.get(index); }
    public void setLastEntry(int index, String entry) { lastEntries.put(index, entry); }

    public long getLastUpdateTime() { return lastUpdateTime; }
    public void setLastUpdateTime(long lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }

    public void clear() {
        lastTitle = null;
        lastLines.clear();
        lastEntries.clear();
    }

    public Map<Integer, String> getLastEntries() {
        return new HashMap<>(lastEntries); // 返回副本避免外部修改
    }
}