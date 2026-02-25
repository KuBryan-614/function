package kuku.plugin.function.scoreboard;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BiomeHelper {
    private static final Map<Biome, String> BIOME_NAMES = new HashMap<>();
    private static final Map<Biome, ChatColor> BIOME_COLORS = new HashMap<>();
    private static final Map<Biome, String> BIOME_CATEGORIES = new HashMap<>();
    private static final Map<Biome, String> BIOME_SHORT_NAMES = new HashMap<>();

    static {
        initializeBiomeNames();
        initializeBiomeColors();
        initializeBiomeCategories();
        initializeShortNames();
    }

    private static void initializeBiomeNames() {
        // 平原
        BIOME_NAMES.put(Biome.PLAINS, "🌾 平原");
        BIOME_NAMES.put(Biome.SUNFLOWER_PLAINS, "🌻 向日葵平原");
        BIOME_NAMES.put(Biome.SNOWY_PLAINS, "❄️ 雪原");
        BIOME_NAMES.put(Biome.MEADOW, "🌸 草甸");

        // 森林
        BIOME_NAMES.put(Biome.FOREST, "🌳 森林");
        BIOME_NAMES.put(Biome.FLOWER_FOREST, "💐 繁花森林");
        BIOME_NAMES.put(Biome.BIRCH_FOREST, "🌲 桦木森林");
        BIOME_NAMES.put(Biome.DARK_FOREST, "🌑 黑森林");
        BIOME_NAMES.put(Biome.OLD_GROWTH_BIRCH_FOREST, "🌳 原始桦木森林");
        BIOME_NAMES.put(Biome.CHERRY_GROVE, "🌸 樱花林");

        // 针叶林
        BIOME_NAMES.put(Biome.TAIGA, "🎄 针叶林");
        BIOME_NAMES.put(Biome.SNOWY_TAIGA, "❄️ 雪地针叶林");
        BIOME_NAMES.put(Biome.OLD_GROWTH_PINE_TAIGA, "🌲 原始松木针叶林");
        BIOME_NAMES.put(Biome.OLD_GROWTH_SPRUCE_TAIGA, "🌲 原始云杉针叶林");

        // 丛林与热带
        BIOME_NAMES.put(Biome.JUNGLE, "🌴 丛林");
        BIOME_NAMES.put(Biome.SPARSE_JUNGLE, "🌴 稀疏丛林");
        BIOME_NAMES.put(Biome.BAMBOO_JUNGLE, "🎋 竹林");
        BIOME_NAMES.put(Biome.SAVANNA, "🌵 热带草原");
        BIOME_NAMES.put(Biome.SAVANNA_PLATEAU, "🏞️ 热带高原");
        BIOME_NAMES.put(Biome.WINDSWEPT_SAVANNA, "💨 风袭热带草原");

        // 干旱地带
        BIOME_NAMES.put(Biome.DESERT, "🏜️ 沙漠");
        BIOME_NAMES.put(Biome.BADLANDS, "🟫 恶地");
        BIOME_NAMES.put(Biome.ERODED_BADLANDS, "⛰️ 侵蚀恶地");
        BIOME_NAMES.put(Biome.WOODED_BADLANDS, "🌵 疏林恶地");

        // 山地
        BIOME_NAMES.put(Biome.WINDSWEPT_HILLS, "💨 风袭丘陵");
        BIOME_NAMES.put(Biome.WINDSWEPT_GRAVELLY_HILLS, "💨 风袭砾石丘陵");
        BIOME_NAMES.put(Biome.WINDSWEPT_FOREST, "💨 风袭森林");
        BIOME_NAMES.put(Biome.STONY_PEAKS, "🗻 石峰");
        BIOME_NAMES.put(Biome.JAGGED_PEAKS, "⛰️ 锯齿山峰");
        BIOME_NAMES.put(Biome.FROZEN_PEAKS, "❄️ 冰封山峰");

        // 水域
        BIOME_NAMES.put(Biome.OCEAN, "🌊 海洋");
        BIOME_NAMES.put(Biome.DEEP_OCEAN, "🌊 深海");
        BIOME_NAMES.put(Biome.WARM_OCEAN, "🌊 暖水海洋");
        BIOME_NAMES.put(Biome.LUKEWARM_OCEAN, "🌊 温水海洋");
        BIOME_NAMES.put(Biome.COLD_OCEAN, "🌊 冷水海洋");
        BIOME_NAMES.put(Biome.FROZEN_OCEAN, "🧊 冻洋");
        BIOME_NAMES.put(Biome.RIVER, "〰️ 河流");
        BIOME_NAMES.put(Biome.FROZEN_RIVER, "🧊 冻河");
        BIOME_NAMES.put(Biome.BEACH, "🏖️ 沙滩");
        BIOME_NAMES.put(Biome.SNOWY_BEACH, "❄️ 雪地沙滩");
        BIOME_NAMES.put(Biome.STONY_SHORE, "🪨 石岸");

        // 雪地
        BIOME_NAMES.put(Biome.ICE_SPIKES, "🧊 冰刺之地");
        BIOME_NAMES.put(Biome.SNOWY_SLOPES, "⛷️ 雪坡");
        BIOME_NAMES.put(Biome.GROVE, "🌲 雪林");

        // 沼泽与蘑菇岛
        BIOME_NAMES.put(Biome.MUSHROOM_FIELDS, "🍄 蘑菇岛");
        BIOME_NAMES.put(Biome.SWAMP, "🐸 沼泽");
        BIOME_NAMES.put(Biome.MANGROVE_SWAMP, "🌴 红树林沼泽");

        // 下界
        BIOME_NAMES.put(Biome.NETHER_WASTES, "🔥 下界荒地");
        BIOME_NAMES.put(Biome.SOUL_SAND_VALLEY, "💀 灵魂砂谷");
        BIOME_NAMES.put(Biome.CRIMSON_FOREST, "🌺 绯红森林");
        BIOME_NAMES.put(Biome.WARPED_FOREST, "🌀 扭曲森林");
        BIOME_NAMES.put(Biome.BASALT_DELTAS, "🌋 玄武岩三角洲");

        // 末地
        BIOME_NAMES.put(Biome.THE_END, "🌌 末地");
        BIOME_NAMES.put(Biome.END_HIGHLANDS, "🌌 末地高地");
        BIOME_NAMES.put(Biome.END_MIDLANDS, "🌌 末地中部");
        BIOME_NAMES.put(Biome.SMALL_END_ISLANDS, "🌌 小型末地岛屿");
        BIOME_NAMES.put(Biome.END_BARRENS, "🌌 末地荒地");

        // 洞穴
        BIOME_NAMES.put(Biome.DRIPSTONE_CAVES, "🪨 钟乳石洞穴");
        BIOME_NAMES.put(Biome.LUSH_CAVES, "🌿 繁茂洞穴");
        BIOME_NAMES.put(Biome.DEEP_DARK, "🌑 深暗之域");

        // 1.21 新增生态域 - 使用兼容性写法
        NamespacedKey trialChambersKey = NamespacedKey.minecraft("trial_chambers");
        Biome trialChambersBiome = Registry.BIOME.get(trialChambersKey);
        if (trialChambersBiome != null) {
            BIOME_NAMES.put(trialChambersBiome, "⚔️ 试炼密室");
        }

        // 处理未定义的生态域
        Registry.BIOME.forEach(entry -> {
            Biome biome = entry;
            if (!BIOME_NAMES.containsKey(biome)) {
                BIOME_NAMES.put(biome, "❓ " + formatBiomeName(biome));
            }
        });
    }

    private static void initializeBiomeColors() {
        // 绿色系
        BIOME_COLORS.put(Biome.PLAINS, ChatColor.GREEN);
        BIOME_COLORS.put(Biome.FOREST, ChatColor.DARK_GREEN);
        BIOME_COLORS.put(Biome.BIRCH_FOREST, ChatColor.GREEN);
        BIOME_COLORS.put(Biome.FLOWER_FOREST, ChatColor.LIGHT_PURPLE);
        BIOME_COLORS.put(Biome.DARK_FOREST, ChatColor.DARK_PURPLE);
        BIOME_COLORS.put(Biome.JUNGLE, ChatColor.DARK_GREEN);
        BIOME_COLORS.put(Biome.BAMBOO_JUNGLE, ChatColor.DARK_GREEN);
        BIOME_COLORS.put(Biome.CHERRY_GROVE, ChatColor.LIGHT_PURPLE);
        BIOME_COLORS.put(Biome.MEADOW, ChatColor.GREEN);
        BIOME_COLORS.put(Biome.SNOWY_PLAINS, ChatColor.WHITE);

        // 黄色系
        BIOME_COLORS.put(Biome.DESERT, ChatColor.YELLOW);
        BIOME_COLORS.put(Biome.SAVANNA, ChatColor.GOLD);
        BIOME_COLORS.put(Biome.BADLANDS, ChatColor.GOLD);
        BIOME_COLORS.put(Biome.SAVANNA_PLATEAU, ChatColor.GOLD);

        // 蓝色系
        BIOME_COLORS.put(Biome.OCEAN, ChatColor.BLUE);
        BIOME_COLORS.put(Biome.DEEP_OCEAN, ChatColor.DARK_BLUE);
        BIOME_COLORS.put(Biome.RIVER, ChatColor.AQUA);
        BIOME_COLORS.put(Biome.WARM_OCEAN, ChatColor.AQUA);
        BIOME_COLORS.put(Biome.LUKEWARM_OCEAN, ChatColor.AQUA);
        BIOME_COLORS.put(Biome.COLD_OCEAN, ChatColor.BLUE);
        BIOME_COLORS.put(Biome.FROZEN_OCEAN, ChatColor.AQUA);

        // 白色系
        BIOME_COLORS.put(Biome.ICE_SPIKES, ChatColor.AQUA);
        BIOME_COLORS.put(Biome.SNOWY_TAIGA, ChatColor.WHITE);
        BIOME_COLORS.put(Biome.JAGGED_PEAKS, ChatColor.WHITE);
        BIOME_COLORS.put(Biome.FROZEN_PEAKS, ChatColor.AQUA);
        BIOME_COLORS.put(Biome.SNOWY_BEACH, ChatColor.WHITE);
        BIOME_COLORS.put(Biome.SNOWY_SLOPES, ChatColor.WHITE);
        BIOME_COLORS.put(Biome.FROZEN_RIVER, ChatColor.AQUA);

        // 红色系
        BIOME_COLORS.put(Biome.NETHER_WASTES, ChatColor.RED);
        BIOME_COLORS.put(Biome.CRIMSON_FOREST, ChatColor.DARK_RED);
        BIOME_COLORS.put(Biome.SOUL_SAND_VALLEY, ChatColor.DARK_GRAY);
        BIOME_COLORS.put(Biome.BASALT_DELTAS, ChatColor.DARK_GRAY);

        // 紫色系
        BIOME_COLORS.put(Biome.THE_END, ChatColor.DARK_PURPLE);
        BIOME_COLORS.put(Biome.WARPED_FOREST, ChatColor.DARK_PURPLE);
        BIOME_COLORS.put(Biome.DEEP_DARK, ChatColor.DARK_PURPLE);
        BIOME_COLORS.put(Biome.MUSHROOM_FIELDS, ChatColor.LIGHT_PURPLE);

        // 灰色/棕色系
        BIOME_COLORS.put(Biome.WINDSWEPT_HILLS, ChatColor.GRAY);
        BIOME_COLORS.put(Biome.WINDSWEPT_GRAVELLY_HILLS, ChatColor.GRAY);
        BIOME_COLORS.put(Biome.WINDSWEPT_FOREST, ChatColor.GRAY);
        BIOME_COLORS.put(Biome.STONY_PEAKS, ChatColor.GRAY);
        BIOME_COLORS.put(Biome.BADLANDS, ChatColor.GOLD);
        BIOME_COLORS.put(Biome.ERODED_BADLANDS, ChatColor.GOLD);
        BIOME_COLORS.put(Biome.WOODED_BADLANDS, ChatColor.GOLD);
        BIOME_COLORS.put(Biome.DRIPSTONE_CAVES, ChatColor.GRAY);

        // 沼泽
        BIOME_COLORS.put(Biome.SWAMP, ChatColor.DARK_GREEN);
        BIOME_COLORS.put(Biome.MANGROVE_SWAMP, ChatColor.DARK_GREEN);
        BIOME_COLORS.put(Biome.LUSH_CAVES, ChatColor.DARK_GREEN);

        // 默认颜色
        ChatColor defaultColor = ChatColor.GRAY;
        Registry.BIOME.forEach(entry -> {
            Biome biome = entry;
            BIOME_COLORS.putIfAbsent(biome, defaultColor);
        });

        // 为试炼密室单独设置颜色（如果存在）
        NamespacedKey trialChambersKey = NamespacedKey.minecraft("trial_chambers");
        Biome trialChambersBiome = Registry.BIOME.get(trialChambersKey);
        if (trialChambersBiome != null) {
            BIOME_COLORS.put(trialChambersBiome, ChatColor.GOLD);
        }
    }

    private static void initializeBiomeCategories() {
        Registry.BIOME.forEach(entry -> {
            Biome biome = entry;
            BIOME_CATEGORIES.put(biome, determineBiomeCategory(biome));
        });
    }

    private static void initializeShortNames() {
        // 基础缩写
        BIOME_SHORT_NAMES.put(Biome.PLAINS, "平原");
        BIOME_SHORT_NAMES.put(Biome.SNOWY_PLAINS, "雪原");
        BIOME_SHORT_NAMES.put(Biome.FOREST, "森林");
        BIOME_SHORT_NAMES.put(Biome.DESERT, "沙漠");
        BIOME_SHORT_NAMES.put(Biome.JUNGLE, "丛林");
        BIOME_SHORT_NAMES.put(Biome.TAIGA, "针叶林");
        BIOME_SHORT_NAMES.put(Biome.OCEAN, "海洋");
        BIOME_SHORT_NAMES.put(Biome.RIVER, "河流");
        BIOME_SHORT_NAMES.put(Biome.SWAMP, "沼泽");
        BIOME_SHORT_NAMES.put(Biome.NETHER_WASTES, "下界");
        BIOME_SHORT_NAMES.put(Biome.THE_END, "末地");
        BIOME_SHORT_NAMES.put(Biome.WINDSWEPT_HILLS, "山地");
        BIOME_SHORT_NAMES.put(Biome.MUSHROOM_FIELDS, "蘑菇岛");
        BIOME_SHORT_NAMES.put(Biome.BADLANDS, "恶地");
        BIOME_SHORT_NAMES.put(Biome.SAVANNA, "草原");
        BIOME_SHORT_NAMES.put(Biome.BAMBOO_JUNGLE, "竹林");
        BIOME_SHORT_NAMES.put(Biome.DEEP_OCEAN, "深海");
        BIOME_SHORT_NAMES.put(Biome.FLOWER_FOREST, "繁花林");
        BIOME_SHORT_NAMES.put(Biome.ICE_SPIKES, "冰刺");
        BIOME_SHORT_NAMES.put(Biome.DRIPSTONE_CAVES, "钟乳洞");
        BIOME_SHORT_NAMES.put(Biome.LUSH_CAVES, "繁茂洞");
        BIOME_SHORT_NAMES.put(Biome.DEEP_DARK, "深暗域");
        BIOME_SHORT_NAMES.put(Biome.MANGROVE_SWAMP, "红树林");
        BIOME_SHORT_NAMES.put(Biome.CHERRY_GROVE, "樱花林");
        BIOME_SHORT_NAMES.put(Biome.JAGGED_PEAKS, "锯齿峰");
        BIOME_SHORT_NAMES.put(Biome.FROZEN_PEAKS, "冰峰");
        BIOME_SHORT_NAMES.put(Biome.STONY_PEAKS, "石峰");

        // 为未定义缩写的生态域自动生成
        Registry.BIOME.forEach(entry -> {
            Biome biome = entry;
            if (BIOME_SHORT_NAMES.containsKey(biome)) return;

            String displayName = BIOME_NAMES.getOrDefault(biome, "");
            String cleanName = displayName.replaceAll("[^\\u4e00-\\u9fa5]", "");

            if (!cleanName.isEmpty()) {
                BIOME_SHORT_NAMES.put(biome, cleanName.length() > 3 ?
                        cleanName.substring(0, 3) : cleanName);
            } else {
                NamespacedKey key = Registry.BIOME.getKey(biome);
                String fallback = (key != null) ? key.getKey().toUpperCase() : "???";
                BIOME_SHORT_NAMES.put(biome, fallback.length() > 4 ?
                        fallback.substring(0, 4) : fallback);
            }
        });

        // 试炼密室缩写（如果存在）
        NamespacedKey trialChambersKey = NamespacedKey.minecraft("trial_chambers");
        Biome trialChambersBiome = Registry.BIOME.get(trialChambersKey);
        if (trialChambersBiome != null) {
            BIOME_SHORT_NAMES.put(trialChambersBiome, "试炼室");
        }
    }

    // ========== 公共方法 ==========

    public static String getBiomeDisplayName(Biome biome) {
        if (biome == null) return "❓ 未知";
        return BIOME_NAMES.getOrDefault(biome, "❓ " + formatBiomeName(biome));
    }

    public static ChatColor getBiomeColor(Biome biome) {
        if (biome == null) return ChatColor.GRAY;
        return BIOME_COLORS.getOrDefault(biome, ChatColor.GRAY);
    }

    public static String getColoredBiomeName(Biome biome) {
        return getBiomeColor(biome) + getBiomeDisplayName(biome);
    }

    public static String getShortBiomeName(Biome biome) {
        if (biome == null) return "未知";
        return BIOME_SHORT_NAMES.getOrDefault(biome,
                getBiomeDisplayName(biome).replaceAll("[^\\u4e00-\\u9fa5]", ""));
    }

    public static String getColoredShortBiomeName(Biome biome) {
        if (biome == null) return ChatColor.GRAY + "未知";
        return getBiomeColor(biome) + getShortBiomeName(biome);
    }

    public static String getBiomeCategory(Biome biome) {
        if (biome == null) return "未知";
        return BIOME_CATEGORIES.getOrDefault(biome, "普通");
    }

    public static boolean isBiomeInCategory(Biome biome, String category) {
        if (biome == null || category == null) return false;
        return getBiomeCategory(biome).equals(category);
    }

    public static String getPlayerBiomeName(Player player) {
        if (player == null || !player.isOnline()) return ChatColor.GRAY + "❓ 未知";
        Location loc = player.getLocation();
        Biome biome = loc.getWorld().getBiome(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        return getColoredBiomeName(biome);
    }

    public static Biome getPlayerBiome(Player player) {
        if (player == null || !player.isOnline()) return Biome.PLAINS;
        Location loc = player.getLocation();
        return loc.getWorld().getBiome(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static String getPlayerShortBiomeName(Player player) {
        Biome biome = getPlayerBiome(player);
        return getColoredShortBiomeName(biome);
    }

    // ========== 私有辅助方法 ==========

    private static String formatBiomeName(Biome biome) {
        NamespacedKey key = Registry.BIOME.getKey(biome);
        if (key == null) return "Unknown";
        String keyStr = key.getKey();
        String[] words = keyStr.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) result.append(" ");
            result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1));
        }
        return result.toString();
    }

    private static String determineBiomeCategory(Biome biome) {
        NamespacedKey key = Registry.BIOME.getKey(biome);
        if (key == null) return "未知";
        String keyStr = key.getKey().toLowerCase();

        if (keyStr.contains("plain") || keyStr.contains("meadow")) return "平原";
        else if (keyStr.contains("forest") || keyStr.contains("grove") ||
                keyStr.contains("birch") || keyStr.contains("cherry")) return "森林";
        else if (keyStr.contains("jungle") || keyStr.contains("bamboo")) return "森林";
        else if (keyStr.contains("mountain") || keyStr.contains("hill") ||
                keyStr.contains("peak") || keyStr.contains("slope") ||
                keyStr.contains("windswept")) return "山地";
        else if (keyStr.contains("ocean") || keyStr.contains("river") ||
                keyStr.contains("beach") || keyStr.contains("shore")) return "水域";
        else if (keyStr.contains("snow") || keyStr.contains("ice") ||
                keyStr.contains("frozen")) return "雪地";
        else if (keyStr.contains("desert") || keyStr.contains("savanna") ||
                keyStr.contains("badland")) return "干旱";
        else if (keyStr.contains("swamp") || keyStr.contains("mangrove")) return "沼泽";
        else if (keyStr.contains("mushroom")) return "蘑菇岛";
        else if (keyStr.contains("nether") || keyStr.contains("crimson") ||
                keyStr.contains("warped") || keyStr.contains("soul") ||
                keyStr.contains("basalt")) return "下界";
        else if (keyStr.contains("end")) return "末地";
        else if (keyStr.contains("cave") || keyStr.contains("dark") ||
                keyStr.contains("dripstone") || keyStr.contains("lush")) return "洞穴";
        else if (keyStr.contains("trial")) return "特殊";
        else if (keyStr.contains("taiga")) return "森林";
        else return "普通";
    }

    public static String getBiomeStatistics() {
        int total = 0;
        for (var ignored : Registry.BIOME) total++;
        int translated = BIOME_NAMES.size();
        int withEmoji = 0;
        for (String name : BIOME_NAMES.values()) {
            if (name.matches(".*[\\p{So}\\p{Cn}].*")) withEmoji++;
        }
        return String.format("📊 生态域统计: 总数 %d | 已翻译 %d | 含 Emoji %d | 覆盖率 %.1f%%",
                total, translated, withEmoji, (translated * 100.0 / total));
    }
}