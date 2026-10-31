#!/usr/bin/env bash
# 构建 PointBunny (com.candy.pointbunny) 的 APK
# 用法: ./build.sh [debug|release]   默认 debug
set -euo pipefail

# ---------- 配置 ----------
GRADLE_HOME="${GRADLE_HOME:-/opt/sdk/gradle/gradle-8.14}"
ANDROID_HOME="${ANDROID_HOME:-/opt/sdk/android/android-sdk}"
BUILD_TYPE="${1:-debug}"        # debug 或 release
TASK="assemble${BUILD_TYPE^}"   # assembleDebug / assembleRelease

cd "$(dirname "$0")"

# ---------- JDK (AGP 8.x 需要 Java 17，忽略环境里的 JDK 8) ----------
if [[ -z "${JAVA_HOME_OVERRIDE:-}" ]]; then
    for d in /opt/sdk/java/zulu-17 /usr/lib/jvm/java-17-openjdk-amd64; do
        if [[ -x "$d/bin/java" ]]; then JAVA_HOME_OVERRIDE="$d"; break; fi
    done
fi
export JAVA_HOME="$JAVA_HOME_OVERRIDE"
if ! "$JAVA_HOME/bin/java" -version 2>&1 | grep -q 'version "17'; then
    echo "错误: 需要 JDK 17，当前 JAVA_HOME=$JAVA_HOME" >&2
    exit 1
fi

# ---------- 环境检查 ----------
if [[ ! -x "$GRADLE_HOME/bin/gradle" ]]; then
    echo "错误: 找不到 gradle 可执行文件: $GRADLE_HOME/bin/gradle" >&2
    exit 1
fi
if [[ ! -d "$ANDROID_HOME/platforms/android-34" ]]; then
    echo "错误: 缺少 Android SDK platform android-34 (ANDROID_HOME=$ANDROID_HOME)" >&2
    exit 1
fi

export ANDROID_HOME ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$GRADLE_HOME/bin:$PATH"

# ---------- 构建 ----------
echo "==> JDK: $JAVA_HOME"
echo "==> Gradle: $($GRADLE_HOME/bin/gradle --version | awk '/^Gradle/{print $2}')"
echo "==> Android SDK: $ANDROID_HOME"
echo "==> Task: $TASK"
echo

gradle "$TASK" \
    --no-daemon \
    --console=plain \
    -Dorg.gradle.jvmargs="-Xmx3g -Dfile.encoding=UTF-8"

# ---------- 输出 ----------
APK_DIR="app/build/outputs/apk/$BUILD_TYPE"
echo
echo "==> 产物:"
find app/build/outputs/apk -name "*.apk" -printf "  %p  (%s bytes)\n"
