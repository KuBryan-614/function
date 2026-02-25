package kuku.plugin.function.clearlag;

public class ClearLagConfig {
    private boolean enabled;
    private long cleanInterval;
    private long countdownTime;
    private boolean showActionBar;
    private boolean broadcastMessages;

    // 物品保护设置
    private boolean protectNamedItems;
    private boolean protectEnchantedItems;
    private int minItemsToClear;
    private int maxClearPerCycle;

    // ActionBar设置
    private boolean actionBarEnabled;
    private String actionBarPosition;
    private boolean actionBarAnimation;
    private boolean actionBarProgressBar;
    private int actionBarProgressBarLength;
    private String actionBarProgressBarFilled;
    private String actionBarProgressBarEmpty;
    private String actionBarProgressBarHighColor;
    private String actionBarProgressBarMediumColor;
    private String actionBarProgressBarLowColor;
    private String actionBarFormatCountdown;
    private String actionBarFormatTesting;

    // 性能优化设置
    private int performanceCheckInterval;
    private boolean asyncOperations;
    private boolean chunkLoadOptimization;
    private int entityScanLimit;
    private boolean batchProcessing;
    private int batchSize;

    // 日志设置
    private String loggingLevel;
    private boolean loggingFile;
    private boolean loggingConsole;
    private String loggingFormat;
    private boolean loggingCommands;
    private boolean loggingClears;
    private boolean loggingErrors;
    private boolean loggingWarnings;

    // 提示消息设置
    private boolean hintsEnabled;
    private boolean hintsFirstJoin;
    private int hintsInterval;

    // 消息设置
    private ClearLagMessages messages;

    public ClearLagConfig() {
        this.messages = new ClearLagMessages();
    }

    // Getters and Setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public long getCleanInterval() { return cleanInterval; }
    public void setCleanInterval(long cleanInterval) { this.cleanInterval = cleanInterval; }

    public long getCountdownTime() { return countdownTime; }
    public void setCountdownTime(long countdownTime) { this.countdownTime = countdownTime; }

    public boolean isShowActionBar() { return showActionBar; }
    public void setShowActionBar(boolean showActionBar) { this.showActionBar = showActionBar; }

    public boolean isBroadcastMessages() { return broadcastMessages; }
    public void setBroadcastMessages(boolean broadcastMessages) { this.broadcastMessages = broadcastMessages; }

    public boolean isProtectNamedItems() { return protectNamedItems; }
    public void setProtectNamedItems(boolean protectNamedItems) { this.protectNamedItems = protectNamedItems; }

    public boolean isProtectEnchantedItems() { return protectEnchantedItems; }
    public void setProtectEnchantedItems(boolean protectEnchantedItems) { this.protectEnchantedItems = protectEnchantedItems; }

    public int getMinItemsToClear() { return minItemsToClear; }
    public void setMinItemsToClear(int minItemsToClear) { this.minItemsToClear = minItemsToClear; }

    public int getMaxClearPerCycle() { return maxClearPerCycle; }
    public void setMaxClearPerCycle(int maxClearPerCycle) { this.maxClearPerCycle = maxClearPerCycle; }

    public boolean isActionBarEnabled() { return actionBarEnabled; }
    public void setActionBarEnabled(boolean actionBarEnabled) { this.actionBarEnabled = actionBarEnabled; }

    public String getActionBarPosition() { return actionBarPosition; }
    public void setActionBarPosition(String actionBarPosition) { this.actionBarPosition = actionBarPosition; }

    public boolean isActionBarAnimation() { return actionBarAnimation; }
    public void setActionBarAnimation(boolean actionBarAnimation) { this.actionBarAnimation = actionBarAnimation; }

    public boolean isActionBarProgressBar() { return actionBarProgressBar; }
    public void setActionBarProgressBar(boolean actionBarProgressBar) { this.actionBarProgressBar = actionBarProgressBar; }

    public int getActionBarProgressBarLength() { return actionBarProgressBarLength; }
    public void setActionBarProgressBarLength(int actionBarProgressBarLength) { this.actionBarProgressBarLength = actionBarProgressBarLength; }

    public String getActionBarProgressBarFilled() { return actionBarProgressBarFilled; }
    public void setActionBarProgressBarFilled(String actionBarProgressBarFilled) { this.actionBarProgressBarFilled = actionBarProgressBarFilled; }

    public String getActionBarProgressBarEmpty() { return actionBarProgressBarEmpty; }
    public void setActionBarProgressBarEmpty(String actionBarProgressBarEmpty) { this.actionBarProgressBarEmpty = actionBarProgressBarEmpty; }

    public String getActionBarProgressBarHighColor() { return actionBarProgressBarHighColor; }
    public void setActionBarProgressBarHighColor(String actionBarProgressBarHighColor) { this.actionBarProgressBarHighColor = actionBarProgressBarHighColor; }

    public String getActionBarProgressBarMediumColor() { return actionBarProgressBarMediumColor; }
    public void setActionBarProgressBarMediumColor(String actionBarProgressBarMediumColor) { this.actionBarProgressBarMediumColor = actionBarProgressBarMediumColor; }

    public String getActionBarProgressBarLowColor() { return actionBarProgressBarLowColor; }
    public void setActionBarProgressBarLowColor(String actionBarProgressBarLowColor) { this.actionBarProgressBarLowColor = actionBarProgressBarLowColor; }

    public String getActionBarFormatCountdown() { return actionBarFormatCountdown; }
    public void setActionBarFormatCountdown(String actionBarFormatCountdown) { this.actionBarFormatCountdown = actionBarFormatCountdown; }

    public String getActionBarFormatTesting() { return actionBarFormatTesting; }
    public void setActionBarFormatTesting(String actionBarFormatTesting) { this.actionBarFormatTesting = actionBarFormatTesting; }

    public int getPerformanceCheckInterval() { return performanceCheckInterval; }
    public void setPerformanceCheckInterval(int performanceCheckInterval) { this.performanceCheckInterval = performanceCheckInterval; }

    public boolean isAsyncOperations() { return asyncOperations; }
    public void setAsyncOperations(boolean asyncOperations) { this.asyncOperations = asyncOperations; }

    public boolean isChunkLoadOptimization() { return chunkLoadOptimization; }
    public void setChunkLoadOptimization(boolean chunkLoadOptimization) { this.chunkLoadOptimization = chunkLoadOptimization; }

    public int getEntityScanLimit() { return entityScanLimit; }
    public void setEntityScanLimit(int entityScanLimit) { this.entityScanLimit = entityScanLimit; }

    public boolean isBatchProcessing() { return batchProcessing; }
    public void setBatchProcessing(boolean batchProcessing) { this.batchProcessing = batchProcessing; }

    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }

    public String getLoggingLevel() { return loggingLevel; }
    public void setLoggingLevel(String loggingLevel) { this.loggingLevel = loggingLevel; }

    public boolean isLoggingFile() { return loggingFile; }
    public void setLoggingFile(boolean loggingFile) { this.loggingFile = loggingFile; }

    public boolean isLoggingConsole() { return loggingConsole; }
    public void setLoggingConsole(boolean loggingConsole) { this.loggingConsole = loggingConsole; }

    public String getLoggingFormat() { return loggingFormat; }
    public void setLoggingFormat(String loggingFormat) { this.loggingFormat = loggingFormat; }

    public boolean isLoggingCommands() { return loggingCommands; }
    public void setLoggingCommands(boolean loggingCommands) { this.loggingCommands = loggingCommands; }

    public boolean isLoggingClears() { return loggingClears; }
    public void setLoggingClears(boolean loggingClears) { this.loggingClears = loggingClears; }

    public boolean isLoggingErrors() { return loggingErrors; }
    public void setLoggingErrors(boolean loggingErrors) { this.loggingErrors = loggingErrors; }

    public boolean isLoggingWarnings() { return loggingWarnings; }
    public void setLoggingWarnings(boolean loggingWarnings) { this.loggingWarnings = loggingWarnings; }

    public boolean isHintsEnabled() { return hintsEnabled; }
    public void setHintsEnabled(boolean hintsEnabled) { this.hintsEnabled = hintsEnabled; }

    public boolean isHintsFirstJoin() { return hintsFirstJoin; }
    public void setHintsFirstJoin(boolean hintsFirstJoin) { this.hintsFirstJoin = hintsFirstJoin; }

    public int getHintsInterval() { return hintsInterval; }
    public void setHintsInterval(int hintsInterval) { this.hintsInterval = hintsInterval; }

    public ClearLagMessages getMessages() { return messages; }
    public void setMessages(ClearLagMessages messages) { this.messages = messages; }
}