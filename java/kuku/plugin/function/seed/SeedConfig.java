package kuku.plugin.function.seed;

import kuku.plugin.function.FunctionPlugin;

/**
 * 种子查看配置 - 简化版
 */
public class SeedConfig {

    public SeedConfig(FunctionPlugin plugin) {
        // 简化版本，不需要复杂配置
    }

    /**
     * 加载配置
     */
    public void load() {
        // 简化版本，不需要加载配置
    }

    /**
     * 重新加载
     */
    public void reload() {
        // 简化版本
    }

    /**
     * 检查是否启用（简化版始终启用）
     */
    public boolean isEnabled() {
        return true;
    }
}