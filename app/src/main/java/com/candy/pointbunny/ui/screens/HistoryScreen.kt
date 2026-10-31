package com.candy.pointbunny.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.candy.pointbunny.data.RecordEntity
import com.candy.pointbunny.logic.StatsLogic
import com.candy.pointbunny.ui.theme.CandyPinkPale
import com.candy.pointbunny.ui.theme.DangerRed
import com.candy.pointbunny.ui.theme.MintGreen
import com.candy.pointbunny.ui.theme.TextBrown
import com.candy.pointbunny.ui.theme.TextBrownLight
import com.candy.pointbunny.ui.theme.WarmOrange
import com.candy.pointbunny.ui.theme.White
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** 日期分组标题：今天 / 昨天 / M月d日 周x */
private fun dayLabel(d: LocalDate, today: LocalDate): String {
    return when (d) {
        today -> "今天"
        today.minusDays(1) -> "昨天"
        else -> {
            val week = arrayOf("日", "一", "二", "三", "四", "五", "六")[d.dayOfWeek.value % 7]
            "${d.monthValue}月${d.dayOfMonth}日 周$week"
        }
    }
}

/**
 * 积分历史页：顶部本月攒/花汇总 + 按日期分组记录 + 删除（带确认）
 */
@Composable
fun HistoryScreen(
    records: List<RecordEntity>,
    onDelete: (RecordEntity) -> Unit
) {
    var pendingDelete by remember { mutableStateOf<RecordEntity?>(null) }
    val today = LocalDate.now()

    // 本月攒/花汇总
    val monthStart = today.withDayOfMonth(1)
    val (mEarn, mSpend) = StatsLogic.totalsIn(monthStart, today, records)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            "📜 积分历史",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextBrown,
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
        )

        // ---- 本月汇总卡 ----
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryCell("本月攒下", "+$mEarn", MintGreen, Modifier.weight(1f))
                Box(
                    Modifier
                        .size(1.dp, 34.dp)
                        .background(TextBrownLight.copy(alpha = 0.25f))
                )
                SummaryCell("本月花掉", "-$mSpend", WarmOrange, Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(14.dp))

        // ---- 记录分组列表 ----
        if (records.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                Text("还没有记录哦，快去攒第一分吧！🐰", fontSize = 15.sp, color = TextBrownLight)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                var lastLabel: String? = null
                records.forEach { r ->
                    val d = StatsLogic.toLocalDate(r.time)
                    val label = dayLabel(d, today)
                    if (label != lastLabel) {
                        Text(
                            label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextBrownLight,
                            modifier = Modifier.padding(top = 10.dp, bottom = 8.dp, start = 4.dp)
                        )
                        lastLabel = label
                    }
                    RecordRow(r, onClick = { pendingDelete = r })
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    // ---- 删除确认 ----
    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = White,
            title = { Text("删掉这条记录吗？", fontWeight = FontWeight.Bold) },
            text = {
                Text("「${target.emoji} ${target.reason} ${if (target.isEarn) "+" else "-"}${target.points}分」删掉后积分也会跟着变回去哦～")
            },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(target)
                    pendingDelete = null
                }) { Text("删掉它", color = DangerRed) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("先留着") }
            }
        )
    }
}

@Composable
private fun SummaryCell(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = TextBrownLight)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@Composable
private fun RecordRow(r: RecordEntity, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = White,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // emoji 圆底
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (r.isEarn) MintGreen.copy(alpha = 0.18f) else WarmOrange.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(r.emoji, fontSize = 20.sp)
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    r.reason,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBrown
                )
                Text(
                    r.time.let {
                        java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("HH:mm"))
                    },
                    fontSize = 12.sp,
                    color = TextBrownLight
                )
            }
            Text(
                if (r.isEarn) "+${r.points} 分" else "-${r.points} 分",
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (r.isEarn) MintGreen else WarmOrange
            )
            Spacer(Modifier.size(10.dp))
            Text("🗑️", fontSize = 16.sp)
        }
    }
}
