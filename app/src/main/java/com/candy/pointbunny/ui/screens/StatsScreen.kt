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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.candy.pointbunny.data.RecordEntity
import com.candy.pointbunny.logic.StatsLogic
import com.candy.pointbunny.logic.StatsMode
import com.candy.pointbunny.ui.comp.BunnyBarChart
import com.candy.pointbunny.ui.comp.ChartLegend
import com.candy.pointbunny.ui.theme.CandyPink
import com.candy.pointbunny.ui.theme.CandyPinkPale
import com.candy.pointbunny.ui.theme.MintGreen
import com.candy.pointbunny.ui.theme.TextBrown
import com.candy.pointbunny.ui.theme.TextBrownLight
import com.candy.pointbunny.ui.theme.WarmOrange
import com.candy.pointbunny.ui.theme.White
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 统计页：本周 / 本月 / 本年 / 选日期
 *  攒 / 花 / 结余 三卡 + 对比柱状图 + 最爱攒分原因 Top3
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(records: List<RecordEntity>) {
    var mode by remember { mutableStateOf(StatsMode.WEEK) }
    var customStart by remember { mutableStateOf<LocalDate?>(null) }
    var customEnd by remember { mutableStateOf<LocalDate?>(null) }
    var picking by remember { mutableIntStateOf(0) } // 1=选开始 2=选结束 0=不弹

    val today = LocalDate.now()
    val (rangeStart, rangeEnd) = StatsLogic.rangeOf(mode, today, customStart, customEnd)
    val buckets = StatsLogic.buckets(mode, records, today, customStart, customEnd)
    val (totalEarn, totalSpend) = StatsLogic.totalsIn(rangeStart, rangeEnd, records)
    val top3 = StatsLogic.topEarnReasons(rangeStart, rangeEnd, records)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            "📊 积分统计",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextBrown,
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
        )

        // ---- 模式切换 ----
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatsMode.entries.forEach { m ->
                val selected = mode == m
                Surface(
                    onClick = {
                        mode = m
                        if (m == StatsMode.CUSTOM && customStart == null) {
                            customStart = today.minusDays(6)
                            customEnd = today
                        }
                    },
                    shape = RoundedCornerShape(50),
                    color = if (selected) CandyPink else White,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        m.label,
                        fontSize = 14.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) White else TextBrownLight,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }

        // ---- 自定义日期选择 ----
        if (mode == StatsMode.CUSTOM) {
            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DatePickButton("开始", customStart, Modifier.weight(1f)) { picking = 1 }
                Text("至", fontSize = 14.sp, color = TextBrownLight)
                DatePickButton("结束", customEnd, Modifier.weight(1f)) { picking = 2 }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ---- 攒 / 花 / 结余 三卡 ----
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("攒分", "+$totalEarn", MintGreen, Modifier.weight(1f))
            StatCard("花分", "-$totalSpend", WarmOrange, Modifier.weight(1f))
            StatCard("结余", "${totalEarn - totalSpend}", CandyPink, Modifier.weight(1f))
        }

        Spacer(Modifier.height(14.dp))

        // ---- 柱状图卡片 ----
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("攒分 vs 花分", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextBrown)
                    ChartLegend()
                }
                Spacer(Modifier.height(8.dp))
                BunnyBarChart(
                    buckets = buckets,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ---- 最爱攒分原因 Top3 ----
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("🏆 最爱攒分原因", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextBrown)
                Spacer(Modifier.height(10.dp))
                if (top3.isEmpty()) {
                    Text(
                        "这个时间段还没有攒分记录～",
                        fontSize = 13.sp,
                        color = TextBrownLight
                    )
                } else {
                    val medals = listOf("🥇", "🥈", "🥉")
                    top3.forEachIndexed { i, r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Text(medals[i], fontSize = 20.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(r.emoji, fontSize = 18.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                r.reason,
                                fontSize = 14.sp,
                                color = TextBrown,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${r.total} 分",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CandyPink
                            )
                        }
                    }
                }
            }
        }
    }

    // ---- 日期选择弹窗 ----
    if (picking != 0) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { picking = 0 },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { ms ->
                        val d = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()
                        if (picking == 1) {
                            customStart = d
                            // 保证开始不晚于结束
                            customEnd?.let { e -> if (e.isBefore(d)) customEnd = d }
                        } else {
                            customEnd = d
                            customStart?.let { s -> if (d.isBefore(s)) customStart = d }
                        }
                    }
                    picking = 0
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { picking = 0 }) { Text("取消") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun DatePickButton(
    prefix: String,
    date: LocalDate?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = White,
        border = androidx.compose.foundation.BorderStroke(1.dp, CandyPinkPale),
        modifier = modifier
    ) {
        Text(
            text = prefix + "：" + (date?.let { "${it.monthValue}/${it.dayOfMonth}" } ?: "未选"),
            fontSize = 13.sp,
            color = TextBrown,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.14f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 12.sp, color = TextBrownLight)
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                maxLines = 1
            )
        }
    }
}
