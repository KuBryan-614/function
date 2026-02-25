# 🎮 Kuku Function Plugin

![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.10-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)
![Downloads](https://img.shields.io/badge/Downloads-%2B-yellow.svg)

> 🌟 高度模組化的多功能 Minecraft 服務器插件，集成超過 15 個實用功能，提升服務器管理效率與玩家遊戲體驗！

## 📦 快速開始

### 環境要求
- Minecraft 伺服器：1.21.10 或更高
- Java：JDK 17+
- 推薦伺服器核心：Bukkit / Spigot / Paper

### 安裝步驟
1. 從 Releases 下載最新版本 `KukuFunction.jar`
2. 將 JAR 放入伺服器 `plugins/` 目錄
3. 啟動或重啟伺服器
4. 編輯 `plugins/KukuFunction/config.yml`
5. 使用 `/kuku reload` 或重啟伺服器以套用設定

## 🚀 核心功能一覽

### 服務器管理模組
- TPS 監控：`/ktps` — 實時監控伺服器性能（權限 `kuku.command.tps`）
- 自動清理：`/clearlag` — 定時清理掉落物（權限 `kuku.clearlag.use`）
- 備份系統：`/backup` — 自動化備份管理（權限 `kuku.backups.use`）
- Discord 整合：`/discord` — 遊戲 ↔ Discord 通訊（權限 `kuku.command.discord`）

### 玩家體驗模組
- 智能計分板：動態顯示玩家資訊（多行配置）
- 經驗存取：`/exp` — 等級與經驗瓶轉換
- 半磚破壞：潛行右鍵單格破壞半磚
- 右鍵採收：右鍵收穫成熟作物
- 界伏盒（Chest）快速管理：右鍵空氣時開啟

### 建築工具模組
- 電梯系統：羊毛 + 告示牌電梯，多層支援
- 座標計算：`/nc` — 快速建築座標計算
- 儲物箱優化：允許在上方有方塊時開啟（雙箱支援）
- 黏液球合成：自定義合成、壓縮雪球合成

### 實用工具模組
- 不死圖騰：低血量自動觸發（簡化版）
- 查看種子：`/seed`
- 幫助 GUI：`/helpgui`（圖形化）
- 加入／離開訊息：可自定義並支援變數

## 📋 完整指令列表（精簡）

```bash
# 插件管理
/kuku reload         # 重載插件配置
/kuku status         # 查看插件狀態

# Discord 管理
/discord reload
/discord broadcast <訊息>
/discord send <訊息>

# 備份管理
/backup create [名稱]
/backup list
/backup restore <名稱>
/backup delete <名稱>

# 測試
/testelevator

# 玩家常用
/ktps   # 別名: /tps, /lag
/seed
/nc <計算式>
/exp <數量|all|half>
/helpgui
/discord
```

## ⚙️ 範例 config.yml（重點節選）

```yaml
settings:
  debug: false
  language: "zh_TW"
  auto-update: false

modules:
  join: true
  scoreboard: true
  chest: true
  tps: true
  discord: true
  elevator: true
  halfslab: true
  exp: true
  backups: true
  helpgui: true
  rightclick: true
  seed: true
  slimecraft: true
  totel: true
  easybox: true
  clearlag: true
  nc: true

tps:
  enabled: true
  detailed-info: true
  show-warning: true
  low-tps-threshold: 15.0
  high-tps-threshold: 18.0
  auto-broadcast: false
  broadcast-interval: 300
  broadcast-tps-threshold: 10.0

elevator:
  enabled: true
  range: 10
  cooldown: 500
  sound-enabled: true
  jump-up: true
  sneak-down: true
  sign-identifier: "[電梯]"

backups:
  enabled: true
  auto:
    enabled: true
    interval: 30
  folders:
    - "world"
    - "world_nether"
    - "world_the_end"
    - "plugins"
    - "server.properties"
  excludes:
    - "world/playerdata/*.dat_old"
    - "logs"
  compression: "zip"
  max-count: 50
  keep-days: 30
  save-path: "backups"
  on-stop: true
  notify: true
```

每個模組也有獨立設定檔，放在 `plugins/KukuFunction/modules/`。

```
KukuFunction/
├── config.yml
├── modules/
│   ├── discord.yml
│   ├── scoreboard.yml
│   ├── join.yml
│   ├── elevator.yml
│   └── backups.yml
└── data/
```

## 🔐 權限（重點）
- 全域：`kuku.*`
- 玩家：`kuku.player.*`
- 管理員：`kuku.admin.*`

模組與指令權限舉例：
```text
kuku.command.tps
kuku.command.discord
kuku.discord.send
kuku.backups.use
kuku.backups.admin
kuku.command.exp
kuku.halfslab.use
kuku.elevator.use
```

## 🎯 使用範例

範例：設置電梯
1. 放置羊毛作為平台
2. 在羊毛上方放告示牌，第一行寫 `[電梯]`
3. 玩家站在平台上：跳躍向上、潛行向下移動到最近的電梯

範例：經驗系統
```bash
/exp store 10      # 存 10 級為經驗瓶
/exp store all     # 存全部
/exp store half    # 存一半
/exp take 5        # 取出 5 個經驗瓶
```

範例：備份管理
```bash
/backup create 建築備份-2024
/backup list
/backup restore 建築備份-2024
```

## 🔧 故障排除

常見問題：
- 插件無法啟用？
  - 確認 Java 17+
  - 確認伺服器核心為 Bukkit/Spigot/Paper
  - 檢查插件衝突

- TPS 顯示不準？
  - TPS 計算基於伺服器 tick，建議使用 `/timings` 分析

- 電梯不工作？
  - 確認 `sign-identifier` 設定與使用羊毛
  - 確認玩家權限 `kuku.elevator.use`
  - 檢查 elevator.range 設定

- 備份過大？
  - 啟用壓縮（zip）
  - 減少備份資料夾
  - 調整保留策略

## 📝 日誌
- `plugins/KukuFunction/logs/latest.log`
- `plugins/KukuFunction/logs/error.log`
- `plugins/KukuFunction/logs/debug.log`（需啟用 debug）

## 📚 API（供開發者）
範例：取得 Plugin 實例
```java
FunctionPlugin plugin = FunctionPlugin.getInstance();
```

使用模組 API：
```java
TPSChecker tpsChecker = plugin.getTPSChecker();
double currentTPS = tpsChecker.getCurrentTPS();

ExpManager expManager = plugin.getExpManager();
DiscordManager discordManager = plugin.getDiscordManager();
```

事件監聽範例：
```java
@EventHandler
public void onPlayerJoin(PlayerJoinEvent event) {
    if (plugin.isModuleLoaded("scoreboard")) {
        // 更新計分板
    }
}
```

## 📈 性能與優化建議
伺服器設定（精簡）：
- spigot.yml / paper.yml 調整 mob/activation 範圍與 chunk unload 設定
- 關閉不必要模組以節省資源
- 減少備份頻率、使用緩存（如模組支援）

## 🤝 貢獻指南
歡迎 PR 與 Issue：
1. Fork
2. git checkout -b feature/your-feature
3. 提交並推送，建立 Pull Request

開發環境：
```bash
git clone https://github.com/yourusername/KukuFunction.git
# 建議使用 IntelliJ IDEA，設定 Java 17，加入 Spigot API 依賴
```

## 📄 許可證
本專案使用 MIT License，詳見 LICENSE 檔案。

## 🌟 支援與回饋
- 文件：GitHub Wiki
- 討論：Discord 頻道
- 問題回報：GitHub Issues
- 功能建議：Feature Requests

感謝使用 Kuku Function Plugin！如果你喜歡本插件，請給一個 ⭐，並在上線前務必備份伺服器資料與充分測試。