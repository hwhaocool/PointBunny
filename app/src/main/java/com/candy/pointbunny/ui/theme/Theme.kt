package com.candy.pointbunny.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ---------- 糖果粉配色（与原型 HTML 一致） ----------
val CandyPink = Color(0xFFFF8FAB)       // 主粉
val CandyPinkLight = Color(0xFFFFD3DE)  // 浅粉
val CandyPinkPale = Color(0xFFFFE9EF)   // 更浅的粉（按钮底）
val Cream = Color(0xFFFFF6F0)           // 奶油底色
val SunnyYellow = Color(0xFFFFE7A3)     // 奶黄
val MintGreen = Color(0xFF7ED9A4)       // 薄荷绿（攒分）
val MintGreenLight = Color(0xFFDFF6E9)
val WarmOrange = Color(0xFFFFA66B)      // 暖橙（花分）
val WarmOrangeLight = Color(0xFFFFE9DB)
val TextBrown = Color(0xFF6B4F5B)       // 深可可文字
val TextBrownLight = Color(0xFF9C8893)
val White = Color(0xFFFFFFFF)
val DangerRed = Color(0xFFFF6B6B)

private val LightColors = lightColorScheme(
    primary = CandyPink,
    onPrimary = White,
    primaryContainer = CandyPinkLight,
    onPrimaryContainer = TextBrown,
    secondary = SunnyYellow,
    onSecondary = TextBrown,
    secondaryContainer = SunnyYellow,
    onSecondaryContainer = TextBrown,
    tertiary = MintGreen,
    onTertiary = White,
    background = Cream,
    onBackground = TextBrown,
    surface = White,
    onSurface = TextBrown,
    surfaceVariant = CandyPinkPale,
    onSurfaceVariant = TextBrownLight,
    error = DangerRed,
    onError = White,
    outline = CandyPinkLight
)

@Composable
fun PointBunnyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // 儿童应用固定浅色更友好，这里保留入口
    content: @Composable () -> Unit
) {
    // 糖果风固定使用浅色主题，避免夜间模式破坏配色
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
