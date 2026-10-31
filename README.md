# 🐰 积分小兔 PointBunny（Android 版）

面向 3-6 岁小朋友的积分积累和消耗系统。糖果粉配色 + 小兔子吉祥物，全部数据保存在手机本地。

技术栈：**Kotlin + Jetpack Compose (Material 3) + Room**

## ✨ 功能清单

| 功能 | 说明 |
|------|------|
| 🏠 首页 | 展示当前剩余积分（大数字）＋「攒积分／花积分」两大按钮＋快捷入口 |
| ➕ 攒积分 | 底部弹窗：**自研超大数字键盘**（不调系统输入法）＋快捷 +1/+2/+5/+10/+20 ＋ 原因选择（帮忙做家务/自己穿衣服/好好吃饭/按时刷牙/表现棒棒的/其他好事） |
| ➖ 花积分 | 同上；余额不足时**抖动警告**＋提示「积分不够啦，先和爸爸妈妈一起攒一攒吧😢」，不会保存 |
| 📜 积分历史 | 按「今天／昨天／x月x日 周x」分组，每条含时间与 ±分数；点击可删除（带确认，删除后余额同步回退） |
| 📊 积分统计 | 本周（按天7桶）／本月（按周5桶）／本年（按月12桶）／**指定日期区间**（≤14天按天，>14天按月）；攒／花／结余三卡 + 绿橙对比柱状图 + 最爱攒分原因 Top3 |
| 🎁 心愿清单 | 心愿卡（emoji+名称+进度条+还差x分）；攒够可一键「兑换」（自动生成一笔花分记录） |
| 🏅 我的勋章 | 6 枚成就：攒分新手／小富翁(满100)／第一笔消费／坚持一周(连续7天攒分)／攒分大师(累计2000)／心愿达成；解锁时 Toast 恭喜 |
| 👨‍👩‍👧 家长专区 | 首页右上角 ⚙️ 进入；数据概览 + JSON 全量预览 + **一键复制到剪贴板** + 清空全部数据（双重确认） |

## 📁 目录结构

```
PointBunny/
├── build.gradle.kts / settings.gradle.kts / gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
└── app/
    ├── build.gradle.kts                 # minSdk 26 / targetSdk 34
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/                         # 糖果粉主题 + 小兔子自适应图标
        └── java/com/candy/pointbunny/
            ├── MainActivity.kt
            ├── data/                    # Room：记录 / 心愿 / 勋章 三张表
            ├── logic/                   # 勋章解锁 + 统计分桶算法
            ├── vm/BunnyViewModel.kt     # 全局状态 + JSON 导出
            └── ui/
                ├── AppRoot.kt           # 底部Tab导航 + Toast事件流
                ├── theme/Theme.kt       # 糖果粉配色
                ├── comp/                # 大数字键盘 / Canvas柱状图
                └── screens/             # 8 个界面
```

## 🚀 如何运行

**方式一（推荐）：Android Studio**
1. 安装 Android Studio Ladybug (2024.1) 或更新版本（自带 JDK 17）
2. `File → Open` 选择本 `PointBunny` 文件夹
3. 首次打开会自动下载 Gradle 8.9 与依赖（需联网，耐心等待）
4. 连接手机（开启 USB 调试）或启动模拟器，点击 ▶️ Run

**方式二：命令行**
```bash
cd PointBunny
gradle wrapper          # 首次需要本机有 Gradle，或跳过此步直接用 IDE
./gradlew assembleDebug # APK 输出在 app/build/outputs/apk/debug/
```

> 首次构建需要下载依赖，请确保网络可访问 google() / mavenCentral() 仓库。

## 🗄️ 数据说明

- 所有数据保存在 Room 数据库（`point_bunny.db`），**不上传任何服务器**
- 余额 = 所有攒分记录之和 − 所有花分记录之和（删除记录后自动重算，无需手动维护）
- 导出 JSON 与原型格式完全一致：

```json
{
  "app": "积分小兔",
  "version": 1,
  "exportedAt": "2026-10-29T15:00:00+08:00",
  "balance": 130,
  "records": [
    { "type": "earn", "points": 10, "reason": "表现棒棒的", "emoji": "⭐", "time": "..." }
  ],
  "wishes": [ { "emoji": "🦕", "name": "恐龙绘本", "target": 60, "redeemed": false } ]
}
```

## 🔧 常用自定义

| 想改什么 | 改哪里 |
|----------|--------|
| 攒分/花分的原因选项 | `ui/screens/AddRecordSheet.kt` 顶部 `EARN_REASONS` / `SPEND_REASONS` |
| 配色 | `ui/theme/Theme.kt` |
| 心愿可选 emoji | `ui/screens/WishScreen.kt` 顶部 `WISH_EMOJIS` |
| 勋章规则 | `logic/Badges.kt` |
| 最低支持的安卓版本 | `app/build.gradle.kts` 中 `minSdk`（当前 26 = Android 8.0） |
