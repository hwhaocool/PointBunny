package com.candy.pointbunny.ui.comp

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.candy.pointbunny.logic.StatBucket
import com.candy.pointbunny.ui.theme.MintGreen
import com.candy.pointbunny.ui.theme.TextBrown
import com.candy.pointbunny.ui.theme.TextBrownLight
import com.candy.pointbunny.ui.theme.WarmOrange

/**
 * 攒分 vs 花分 对比柱状图（Canvas 自绘，与原型一致）：
 *  - 每个分桶两根柱：绿色 = 攒，橙色 = 花
 *  - 柱顶显示数值，底部显示标签
 */
@Composable
fun BunnyBarChart(
    buckets: List<StatBucket>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val valueStyle = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = TextBrown
    )
    val labelStyle = TextStyle(
        fontSize = 11.sp,
        color = TextBrownLight
    )
    val earnColor = MintGreen
    val spendColor = WarmOrange
    val gridColor = TextBrownLight.copy(alpha = 0.18f)
    val maxV = remember(buckets) {
        maxOf(buckets.maxOfOrNull { maxOf(it.earn, it.spend) } ?: 1, 1)
    }

    Canvas(modifier = modifier) {
        if (buckets.isEmpty()) return@Canvas
        val w = size.width
        val h = size.height
        val topPad = 34f        // 柱顶数值空间
        val bottomPad = 46f     // 底部标签空间
        val chartH = h - topPad - bottomPad
        val groupW = w / buckets.size
        val barW = groupW * 0.24f
        val gap = barW * 0.35f
        val corner = CornerRadius(barW / 2.4f)

        // 基线
        drawLine(
            color = gridColor,
            start = Offset(0f, topPad + chartH),
            end = Offset(w, topPad + chartH),
            strokeWidth = 2f
        )

        buckets.forEachIndexed { i, b ->
            val cx = groupW * i + groupW / 2f

            // 攒分柱
            if (b.earn > 0) {
                val bh = chartH * b.earn / maxV
                drawRoundRect(
                    color = earnColor,
                    topLeft = Offset(cx - gap - barW, topPad + chartH - bh),
                    size = Size(barW, bh),
                    cornerRadius = corner
                )
                val tm = textMeasurer.measure(b.earn.toString(), valueStyle)
                drawText(
                    tm,
                    topLeft = Offset(
                        cx - gap - barW / 2f - tm.size.width / 2f,
                        topPad + chartH - bh - tm.size.height - 4f
                    )
                )
            }

            // 花分柱
            if (b.spend > 0) {
                val bh = chartH * b.spend / maxV
                drawRoundRect(
                    color = spendColor,
                    topLeft = Offset(cx + gap, topPad + chartH - bh),
                    size = Size(barW, bh),
                    cornerRadius = corner
                )
                val tm = textMeasurer.measure(b.spend.toString(), valueStyle)
                drawText(
                    tm,
                    topLeft = Offset(
                        cx + gap + barW / 2f - tm.size.width / 2f,
                        topPad + chartH - bh - tm.size.height - 4f
                    )
                )
            }

            // 底部标签
            val lm = textMeasurer.measure(b.label, labelStyle)
            drawText(
                lm,
                topLeft = Offset(
                    cx - lm.size.width / 2f,
                    h - bottomPad + 12f
                )
            )
        }
    }
}

/** 图例小圆点 + 文字（攒 / 花） */
@Composable
fun ChartLegend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(MintGreen to "攒分", WarmOrange to "花分").forEach { (color, label) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(Modifier.width(5.dp))
                Text(label, fontSize = 12.sp, color = TextBrown)
            }
        }
    }
}
