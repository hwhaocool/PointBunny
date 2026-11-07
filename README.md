# 🐰 积分小兔 PointBunny（Android 版）

面向 3-6 岁小朋友的积分积累和消耗系统。糖果粉配色 + 小兔子吉祥物，全部数据保存在手机本地。

技术栈：**Kotlin + Jetpack Compose (Material 3) + Room**  
当前版本：**v1.1（versionCode 2）**

## ✨ 功能清单

| 功能 | 说明 |
|------|------|
| 🏠 首页 | 当前剩余积分大数字＋「攒积分／花积分」两大按钮＋积分历史／统计／心愿／勋章四个快捷入口；右上角 ⚙️ 进入家长专区 |
| ➕ 攒积分 | 底部弹窗：积分显示区＋快捷 **+1/+2/+5/+10/+20**＋自研超大数字键盘（**默认收起，可展开**，不调系统输入法）＋两列事项选择；支持「自由输入」事项 |
| ➖ 花积分 | 同一弹窗的花分模式，显示当前余额；余额不足时**抖动警告**＋提示「积分不够啦，先和爸爸妈妈一起攒一攒吧😢」，不会保存 |
| ✏️ 修改记录 | 历史页点 ✏️ 用同一弹窗编辑积分／事项／图标，保存后余额自动重算（花分编辑时会先还原本条已扣积分，避免误判余额不足） |
| 📜 积分历史 | 顶部本月攒／花汇总；按「今天／昨天／x月x日 周x」分组，每条含 **yyyy年M月d日 HH:mm:ss** 完整时间与 ±分数；支持编辑、删除（删除带确认，余额同步回退） |
| 📊 积分统计 | 本周（周一~周日，按天 7 桶）／本月（按周 4~5 桶）／本年（按月 12 桶）／**选日期**（≤14 天按天，>14 天按月）；攒／花／结余三卡 + 绿橙对比柱状图 + 最爱攒分原因 Top3；自定义区间带开始/结束日期选择器 |
| 🎁 心愿清单 | 心愿卡（emoji＋名称＋进度条＋还差 x 分／可以兑换啦）；攒够可一键「兑换」（自动生成一笔花分记录并标记达成）；支持添加、删除心愿 |
| 🏅 我的勋章 | 6 枚成就：攒分新手／小富翁（当前积分满 100）／第一笔消费／坚持一周（连续 7 天攒分）／攒分大师（累计攒满 2000）／心愿达成；解锁时 Toast 恭喜，并显示解锁日期 |
| 👨‍👩‍👧 家长专区 | 首页右上角 ⚙️ 进入；数据概览（余额／记录数／心愿数）＋导航入口：数据分享、事项管理、关于；底部「清空全部数据」（双重确认，清空记录／心愿／勋章，**自定义事项保留**） |
| 📤 数据分享 | 导出 JSON 预览＋**一键复制到剪贴板**；支持从剪贴板粘贴／手动输入 JSON 并**增量导入**（追加合并，不覆盖已有数据；导入后按全部数据补发未解锁勋章） |
| 🏷️ 事项管理 | 攒分／花分事项改为 **Room 数据库存储**，可自由新增、修改、删除；可设置 emoji、名称和「选中后可自由输入文字」开关，保存后立即在攒分／花分弹窗生效 |
| ℹ️ 关于 | 应用名称、版本号、构建时间、构建类型、包名，以及手机型号／Android 版本等本机信息 |

> 底部导航固定 4 个主 Tab：首页／历史／统计／心愿；勋章、家长专区及其子页作为二级页面进入，底部导航始终可用。

## 📁 目录结构

```
PointBunny/
├── build.gradle.kts / settings.gradle.kts / gradle.properties
├── build.sh                              # 命令行一键构建脚本
├── gradle/wrapper/gradle-wrapper.properties
└── app/
    ├── build.gradle.kts                  # minSdk 26 / targetSdk 34 / version 1.1
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/                          # 糖果粉主题 + 小兔子自适应图标
        └── java/com/github/hwhaocool/pointbunny/
            ├── MainActivity.kt
            ├── data/                     # Room：记录 / 心愿 / 勋章 / 事项 四张表（DB v2）
            ├── logic/                    # Badges.kt 勋章解锁 + StatsBuckets.kt 统计分桶
            ├── vm/BunnyViewModel.kt      # 全局状态、CRUD、JSON 导入导出
            └── ui/
                ├── AppRoot.kt            # 4 Tab 导航 + 二级页路由 + 弹窗
                ├── theme/Theme.kt        # 糖果粉配色（固定浅色）
                ├── comp/                 # 数字键盘 / Canvas 柱状图 / 二级页返回顶栏
                └── screens/              # 9 个界面 + AddRecordSheet（新增/编辑两用）
```

## 🚀 如何运行

**方式一（推荐）：Android Studio**
1. 安装 Android Studio Ladybug (2024.1) 或更新版本（自带 JDK 17）
2. `File → Open` 选择本 `PointBunny` 文件夹
3. 首次打开会按 `gradle/wrapper/gradle-wrapper.properties` 下载 Gradle 8.9 与依赖（需联网，耐心等待）
4. 连接手机（开启 USB 调试）或启动模拟器，点击 ▶️ Run

**方式二：命令行**

仓库自带构建脚本，默认使用 `/opt/sdk` 下的 JDK 17、Gradle 和 Android SDK：

```bash
cd PointBunny
./build.sh          # 等价于 ./build.sh debug
./build.sh release  # 构建 release 包
```

可通过环境变量覆盖路径：`JAVA_HOME_OVERRIDE`、`GRADLE_HOME`、`ANDROID_HOME`。要求：

- JDK 17
- Gradle 8.x（脚本默认 `/opt/sdk/gradle/gradle-8.14`）
- Android SDK Platform 34

也可以直接使用本机 Gradle：

```bash
gradle assembleDebug
```

APK 输出在 `app/build/outputs/apk/debug/`，产物命名为 `PointBunny-<版本号>-<构建类型>.apk`（当前为 `PointBunny-1.1-debug.apk`）。

> 首次构建需要下载依赖，请确保网络可访问 google() / mavenCentral()（项目已配置阿里云镜像优先）。

### 打 Tag 自动构建 APK（GitHub Actions）

推送形如 `v1.0.0` 的 tag 会自动触发 `.github/workflows/build-apk-on-tag.yml`：

```bash
git tag v1.0.0
git push origin v1.0.0
```

- tag 去掉前缀 `v` 即为 `versionName`（`v1.0.0` → `1.0.0`）
- `versionCode` 自动计算为 `major * 10000 + minor * 100 + patch`（`1.0.0` → `10000`）
- 构建 debug APK（debug 签名，可直接安装），上传为 Actions artifact，并附加到同名 GitHub Release
- 产物文件名为 `PointBunny-<versionName>.apk`，例如 `PointBunny-1.0.0.apk`

> 未配置正式签名，因此自动发布的是 debug 签名 APK；如需上架应用商店，请自行配置 release 签名。

## 🗄️ 数据说明

- 所有数据保存在 Room 数据库（`point_bunny.db`），**不上传任何服务器**
- 共 4 张表：积分记录 `records`、心愿 `wishes`、勋章 `badges`、事项 `reasons`
- 数据库版本为 2，从 v1 升级时会自动新增 `reasons` 表
- 余额 = 所有攒分记录之和 − 所有花分记录之和（删除／修改记录后自动重算，无需手动维护）
- 导出 JSON 包含**积分记录和心愿**；事项和勋章不直接导出，导入后勋章会根据全部数据自动重新判定补发：

```json
{
  "app": "积分小兔",
  "version": 1,
  "exportedAt": "2026-10-29T15:00:00+08:00",
  "balance": 130,
  "records": [
    { "type": "earn", "points": 10, "reason": "表现棒棒的", "emoji": "⭐", "time": "2026-10-29T15:00:00+08:00" }
  ],
  "wishes": [ { "emoji": "🦕", "name": "恐龙绘本", "target": 60, "redeemed": false } ]
}
```

- 导入为**增量合并**：记录和心愿直接追加到现有数据，不会覆盖；重复导入会产生重复记录
- 导入时 `time` 解析失败会回退为当前时间，`points` / `target` 为负数时按 0 处理

## 🔧 常用自定义

| 想改什么 | 改哪里 |
|----------|--------|
| 首次安装的默认攒分／花分事项 | `vm/BunnyViewModel.kt` 顶部 `DEFAULT_EARN_REASONS` / `DEFAULT_SPEND_REASONS`（仅在事项表为空时写入；日常增删改请在 App 内「家长专区 → 事项管理」操作） |
| 事项可选 emoji | `ui/screens/ReasonManageScreen.kt` 顶部 `EMOJI_CHOICES` |
| 配色 | `ui/theme/Theme.kt`（固定浅色主题） |
| 心愿可选 emoji | `ui/screens/WishScreen.kt` 顶部 `WISH_EMOJIS` |
| 勋章规则 | `logic/Badges.kt` |
| 统计分桶／自定义区间规则 | `logic/StatsBuckets.kt` |
| 版本号／构建时间 | `app/build.gradle.kts` 中 `versionCode`、`versionName`、`BUILD_TIME`（可用 `-PbuildTime=...` 覆盖） |
| APK 产物命名 | `app/build.gradle.kts` 中 `outputFileName` |
| 最低支持的安卓版本 | `app/build.gradle.kts` 中 `minSdk`（当前 26 = Android 8.0） |
