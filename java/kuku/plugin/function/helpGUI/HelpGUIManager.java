package kuku.plugin.function.helpGUI;

import kuku.plugin.function.FunctionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;

public class HelpGUIManager implements Listener {

    private final FunctionPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private boolean enabled = false;

    public HelpGUIManager(FunctionPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        if (enabled) return;

        try {
            // 注册事件监听器
            Bukkit.getPluginManager().registerEvents(this, plugin);
            enabled = true;

            plugin.getLogger().info("§a帮助GUI模块已启用 - 蹲下+F打开");
        } catch (Exception e) {
            plugin.getLogger().severe("§c注册帮助GUI事件监听器时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void disable() {
        if (!enabled) return;

        enabled = false;
        cooldowns.clear();
        plugin.getLogger().info("§c帮助GUI模块已禁用");
    }

    @EventHandler
    public void onPlayerSwapHands(PlayerSwapHandItemsEvent event) {
        if (!enabled) return;

        Player player = event.getPlayer();

        // 必须蹲下
        if (!player.isSneaking()) return;

        // 简单冷却
        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (cooldowns.containsKey(playerId)) {
            long last = cooldowns.get(playerId);
            if (now - last < 500) return;
        }

        // 阻止F键默认行为
        event.setCancelled(true);

        // 打开GUI
        openHelpGUI(player);
        cooldowns.put(playerId, now);
    }

    private void openHelpGUI(Player player) {
        try {
            // 创建54格（6行）大箱子GUI
            Inventory gui = Bukkit.createInventory(null, 54, "§6§l伺服器指令幫助");

            // 填充边框
            fillBorder(gui);

            // 添加分类标题
            addCategoryTitles(gui);

            // 添加指令项目
            addCommandItems(gui);

            // 添加使用说明
            addUsageInfo(gui, player);

            // 打开GUI
            player.openInventory(gui);
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);

        } catch (Exception e) {
            player.sendMessage("§c打開幫助選單時出錯，請通知管理員");
            plugin.getLogger().warning("打開幫助GUI時出錯: " + e.getMessage());
        }
    }

    /**
     * 填充边框（54格）
     */
    private void fillBorder(Inventory gui) {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7");
            border.setItemMeta(meta);
        }

        // 填充上下边框（第一行和最后一行）
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, border); // 第一行（slot 0-8）
            gui.setItem(i + 45, border); // 最后一行（slot 45-53）
        }

        // 填充左右边框（第一列和最后一列）
        for (int i = 0; i < 6; i++) {
            gui.setItem(i * 9, border); // 第一列（slot 0, 9, 18, 27, 36, 45）
            gui.setItem(i * 9 + 8, border); // 最后一列（slot 8, 17, 26, 35, 44, 53）
        }
    }

    /**
     * 添加分类标题
     */
    private void addCategoryTitles(Inventory gui) {
        // 家指令分类标题
        gui.setItem(10, createTitleItem(Material.BOOKSHELF, "§6§lhome類系統"));

        // 传送指令分类标题
        gui.setItem(19, createTitleItem(Material.ENDER_EYE, "§5§l傳送類系統"));

        gui.setItem(28, createTitleItem(Material.CHEST_MINECART, "§5§l其他系統"));
    }

    /**
     * 创建标题物品
     */
    private ItemStack createTitleItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList("§7點擊右方圖標就可複製"));
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * 添加指令项目
     */
    private void addCommandItems(Inventory gui) {
        // ========== 家指令系统 ==========
        gui.setItem(12, createCommandItem(
                Material.WHITE_BED,
                "§a§l回家",
                "/home",
                "§7回到你的默認家",
                "§e立即傳送回默認家"
        ));

        gui.setItem(13, createCommandItem(
                Material.BLUE_BED,
                "§c§l設置家",
                "/sethome <家名稱>",
                "§7設置一個新家",
                "§e示例: /sethome home (這是默認值可以修改)"
        ));

        gui.setItem(14, createCommandItem(
                Material.RED_CARPET,
                "§c§l刪除家",
                "/delhome <家名稱>",
                "§7刪除指定的家",
                "§e示例: /delhome home (這是默認值可以修改)"
        ));

        gui.setItem(15, createCommandItem(
                Material.PAPER,
                "§f§l家列表",
                "/homes",
                "§7查看你設置的所有家",
                "§e顯示所有家名稱和位置"
        ));

        // ========== 传送系统 ==========
        gui.setItem(21, createCommandItem(
                Material.ENDER_PEARL,
                "§d§l傳送請求",
                "/tpa <玩家>",
                "§7請求傳送到指定玩家",
                "§e示例: /tpa Steve"
        ));

        gui.setItem(22, createCommandItem(
                Material.LIME_DYE,
                "§a§l接受傳送",
                "/tpaccept",
                "§7接受玩家的傳送請求",
                "§e同意對方傳送過來"
        ));

        gui.setItem(23, createCommandItem(
                Material.RED_DYE,
                "§c§l拒絕傳送",
                "/tpadeny",
                "§7拒絕玩家的傳送請求",
                "§e不同意對方傳送"
        ));


        gui.setItem(24, createCommandItem(
                Material.COMPASS,
                "§e§l返回死亡點",
                "/back",
                "§7返回上次死亡的地點",
                "§e有冷卻時間限制"
        ));

        // ========== 其他常用指令 ==========
        gui.setItem(30, createCommandItem(
                Material.WHITE_WOOL,
                "§e§l電梯使用方法,支持1x1,2x2,3x3",
                "無",
                "§7在羊毛上放置告示牌",
                "§e[電梯]",
                "§e樓層(可自行輸入)",
                "§e這個樓層的介紹"
        ));

        gui.setItem(31, createCommandItem(
                Material.WHEAT,
                "§e§l使用空手右鍵收穫",
                "無",
                "§7在作物成熟後使用空手右鍵來採收他"
        ));

        gui.setItem(32, createCommandItem(
                Material.WHEAT_SEEDS,
                "§e§l種子碼",
                "/seed",
                "§7可用來查看地形"
        ));

        gui.setItem(33, createCommandItem(
                Material.NETHERRACK,
                "§e§l計算座標",
                "/nc",
                "§7計算主世界跟地域的對應座標"
        ));

        gui.setItem(34, createCommandItem(
                Material.CHERRY_SLAB,
                "§e§l挖掘半磚",
                "無",
                "§7蹲下並根據你的視線來挖上半磚還是下半磚"
        ));

        gui.setItem(38, createCommandItem(
                Material.CHEST,
                "§e§l箱子優化",
                "無",
                "§7可以在箱子上放完整並正常打開了d(`･∀･)b"
        ));

        gui.setItem(39, createCommandItem(
                Material.EXPERIENCE_BOTTLE,
                "§e§l使用/collect ext <等級> 來取得你的經驗",
                "/collect exp <等級>",
                "§7<等級要輸入正整數喔>"
        ));

        gui.setItem(40, createCommandItem(
                Material.EXPERIENCE_BOTTLE,
                "§e§l使用/input ext <等級> 來存儲你的經驗",
                "/input exp <等級>",
                "§7<等級要輸入正整數喔>"
        ));

        gui.setItem(41, createCommandItem(
                Material.REDSTONE,
                "§e§l使用/ktps來查看伺服器的TPS",
                "/ktps"
        ));
    }

    /**
     * 创建指令物品
     */
    private ItemStack createCommandItem(Material material, String name, String command, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);

            // 构建描述
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(line);
            }
            loreList.add("");
            loreList.add("§6指令: §f" + command);
            loreList.add("§a點擊複製到聊天欄");

            meta.setLore(loreList);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 添加使用说明
     */
    private void addUsageInfo(Inventory gui, Player player) {
        // 使用说明
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta meta = info.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§e§l使用說明");
            meta.setLore(Arrays.asList(
                    "§7玩家: §a" + player.getName(),
                    "",
                    "§e使用方法:",
                    "§7• 蹲下 + 按F鍵打開此選單",
                    "§7• 點擊圖標複製對應指令",
                    "§7• 指令會出現在聊天欄",
                    "§7• 按T打開聊天，↑鍵獲取指令",
                    "",
                    "§6重要提示:",
                    "§7<> 表示必須填寫的參數",
                    "§7示例: /sethome 主城",
                    "§7示例: /tpa Steve"
            ));
            info.setItemMeta(meta);
        }

        gui.setItem(49, info);

        // 关闭按钮
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§c§l關閉選單");
            closeMeta.setLore(Arrays.asList("§7點擊關閉幫助選單"));
            closeButton.setItemMeta(closeMeta);
        }
        gui.setItem(53, closeButton);

        // 添加空位填充
        ItemStack empty = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta emptyMeta = empty.getItemMeta();
        if (emptyMeta != null) {
            emptyMeta.setDisplayName("§7");
            empty.setItemMeta(emptyMeta);
        }

        // 第5行的边框位置
        gui.setItem(45, empty);
        gui.setItem(46, empty);
        gui.setItem(47, empty);
        gui.setItem(48, empty);
        gui.setItem(50, empty);
        gui.setItem(51, empty);
        gui.setItem(52, empty);
    }

    /**
     * 处理库存点击事件 - 修正版
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6§l伺服器指令幫助")) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        // 检查是否是关闭按钮
        if (clickedItem.getType() == Material.BARRIER) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1, 1);
            return;
        }

        // 检查是否是标题物品或信息物品（不处理）
        Material type = clickedItem.getType();
        if (type == Material.PAPER ||
                type == Material.BOOKSHELF ||
                type == Material.ENDER_EYE ||
                type == Material.CHEST ||
                type == Material.GRAY_STAINED_GLASS_PANE) {
            // 检查是否是使用说明纸
            if (type == Material.PAPER && clickedItem.hasItemMeta()) {
                ItemMeta meta = clickedItem.getItemMeta();
                if (meta.hasDisplayName() && meta.getDisplayName().contains("使用說明")) {
                    return;
                }
            }
            return;
        }

        // 获取物品的Lore来提取指令
        if (!clickedItem.hasItemMeta()) return;

        ItemMeta meta = clickedItem.getItemMeta();
        if (!meta.hasLore()) {
            player.sendMessage("§c此物品沒有指令信息");
            return;
        }

        // 从Lore中查找指令
        String command = extractCommandFromLore(meta.getLore());

        if (command != null && !command.isEmpty()) {
            // 复制指令到聊天栏
            copyCommandToChat(player, command);

            // 播放点击音效
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);

            // 重要：关闭GUI（根据你的要求）
            player.closeInventory();

        } else {
            // 如果没有找到指令，发送调试信息
            player.sendMessage("§c無法從此物品提取指令，請通知管理員");
            plugin.getLogger().warning("无法从物品提取指令 - 物品: " + type + ", Lore: " + meta.getLore());
        }
    }

    /**
     * 从Lore中提取指令 - 改进版
     */
    private String extractCommandFromLore(List<String> lore) {
        if (lore == null || lore.isEmpty()) return null;

        for (String line : lore) {
            // 移除颜色代码后查找
            String cleanLine = ChatColor.stripColor(line);
            if (cleanLine.startsWith("指令: ")) {
                // 返回指令部分
                return cleanLine.substring(4).trim();
            }

            // 或者查找带有颜色代码的
            if (line.startsWith("§6指令: §f")) {
                return line.substring(8); // 移除"§6指令: §f"
            }
        }
        return null;
    }

    /**
     * 复制指令到玩家聊天栏 - 修正版
     */
    private void copyCommandToChat(Player player, String command) {
        try {
            // 确保命令不以空格开头
            command = command.trim();

            // 方法1: 使用Spigot的TextComponent（推荐）
            sendClickableCommand(player, command);

        } catch (Exception e) {
            // 方法2: 如果不支持spigot组件，使用普通方法
            player.sendMessage("§a指令已複製: §e" + command);
            player.sendMessage("§7請按 §eT §7然後按 §e↑ §7鍵獲取指令");

            // 记录到玩家最近的命令历史
            try {
                player.setMetadata("last_copied_command",
                        new org.bukkit.metadata.FixedMetadataValue(plugin, command));
            } catch (Exception ex) {
                // 忽略元数据设置错误
            }
        }
    }

    /**
     * 发送可点击复制的命令消息 - 修正版
     */
    private void sendClickableCommand(Player player, String command) {
        try {
            // 创建可点击的文本组件
            net.md_5.bungee.api.chat.TextComponent message = new net.md_5.bungee.api.chat.TextComponent(
                    "§a點擊這裡複製指令: §e" + command);

            // 设置点击事件为建议命令（在聊天框中显示）
            message.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                    net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND, command));

            // 设置悬停提示
            message.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(
                    net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                    new net.md_5.bungee.api.chat.ComponentBuilder("點擊將指令放入聊天框\n然後按Enter發送").color(net.md_5.bungee.api.ChatColor.GREEN).create()));

            // 发送消息
            player.spigot().sendMessage(message);

            // 额外提示
            player.sendMessage("§7提示: 按 §eT §7打開聊天，然後按 §e↑ §7鍵獲取指令");

        } catch (NoClassDefFoundError | Exception e) {
            // 如果不支持spigot API，使用替代方法
            player.sendMessage("§a指令已複製: §e" + command);
            player.sendMessage("§7請按 §eT §7然後按 §e↑ §7鍵獲取指令");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 重新加载配置（如果需要）
     */
    public void reload() {
        // 这里可以添加重新加载配置的代码
        plugin.getLogger().info("§a幫助GUI配置已重新加載");
    }
}