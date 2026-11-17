package com.github.hwhaocool.pointbunny.ui.comp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.hwhaocool.pointbunny.ui.theme.SunnyYellow
import com.github.hwhaocool.pointbunny.ui.theme.TextBrown

/**
 * 给 3-6 岁小朋友用的超大数字键盘（支持 0.1 分精度）：
 *   1 2 3 C
 *   4 5 6 ⌫
 *   7 8 9 .
 *     0（整行加宽）
 * 不调起系统输入法，按键高度 58dp 方便小手指点击。
 */
@Composable
fun NumberKeyboard(
    onDigit: (Int) -> Unit,
    onDot: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf("1", "2", "3", "C"),
        listOf("4", "5", "6", "⌫"),
        listOf("7", "8", "9", ".")
    )
    val keyHeight = 58.dp
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { key ->
                    NumberKey(
                        key = key,
                        modifier = Modifier
                            .weight(1f)
                            .height(keyHeight),
                        onClick = {
                            when (key) {
                                "C" -> onClear()
                                "⌫" -> onBackspace()
                                "." -> onDot()
                                else -> onDigit(key.toInt())
                            }
                        }
                    )
                }
            }
        }
        // 0 单独占一整行，小手指更好点
        NumberKey(
            key = "0",
            modifier = Modifier
                .fillMaxWidth()
                .height(keyHeight),
            onClick = { onDigit(0) }
        )
    }
}

@Composable
private fun NumberKey(
    key: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val isAction = key == "C" || key == "⌫"
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (isAction) SunnyYellow else Color.White,
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = key,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextBrown
            )
        }
    }
}
