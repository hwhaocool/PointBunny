package com.github.hwhaocool.pointbunny.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.hwhaocool.pointbunny.logic.BadgeDefs
import com.github.hwhaocool.pointbunny.logic.StatsLogic
import com.github.hwhaocool.pointbunny.ui.theme.CandyPinkPale
import com.github.hwhaocool.pointbunny.ui.theme.MintGreen
import com.github.hwhaocool.pointbunny.ui.theme.SunnyYellow
import com.github.hwhaocool.pointbunny.ui.theme.TextBrown
import com.github.hwhaocool.pointbunny.ui.theme.TextBrownLight
import com.github.hwhaocool.pointbunny.ui.theme.White
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 我的勋章：6 枚成就勋章，2 列网格展示
 * 已解锁显示彩色 emoji + 解锁日期；未解锁半透明 + 🔒
 */
@Composable
fun BadgeScreen(
    unlocked: Set<String>,
    unlockTimes: Map<String, Long>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            "🏅 我的勋章",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextBrown,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
        )
        Text(
            "已收集 ${unlocked.size} / ${BadgeDefs.ALL.size} 枚，继续加油！",
            fontSize = 13.sp,
            color = TextBrownLight
        )

        Spacer(Modifier.height(14.dp))

        BadgeDefs.ALL.chunked(2).forEach { rowPair ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowPair.forEach { def ->
                    val got = def.key in unlocked
                    val time = unlockTimes[def.key]
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = if (got) White else CandyPinkPale.copy(alpha = 0.5f),
                        shadowElevation = if (got) 2.dp else 0.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(vertical = 20.dp, horizontal = 10.dp)
                                .alpha(if (got) 1f else 0.55f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(74.dp)
                                    .background(
                                        when {
                                            got && def.key in listOf("b1", "b5") -> SunnyYellow
                                            got && def.key in listOf("b2", "b6") -> CandyPinkPale
                                            else -> MintGreen.copy(alpha = 0.2f)
                                        },
                                        androidx.compose.foundation.shape.CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(def.emoji, fontSize = 34.sp)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    def.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextBrown
                                )
                                if (!got) {
                                    Spacer(Modifier.size(4.dp))
                                    Text("🔒", fontSize = 12.sp)
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(def.desc, fontSize = 12.sp, color = TextBrownLight)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (got && time != null)
                                    StatsLogic.toLocalDate(time)
                                        .format(DateTimeFormatter.ofPattern("M月d日解锁"))
                                else "还未解锁",
                                fontSize = 11.sp,
                                color = if (got) MintGreen else TextBrownLight.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                if (rowPair.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
