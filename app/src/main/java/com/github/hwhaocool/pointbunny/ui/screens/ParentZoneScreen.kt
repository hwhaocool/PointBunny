package com.github.hwhaocool.pointbunny.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.hwhaocool.pointbunny.ui.theme.CandyPink
import com.github.hwhaocool.pointbunny.ui.theme.CandyPinkPale
import com.github.hwhaocool.pointbunny.ui.theme.DangerRed
import com.github.hwhaocool.pointbunny.ui.theme.MintGreen
import com.github.hwhaocool.pointbunny.ui.theme.TextBrown
import com.github.hwhaocool.pointbunny.ui.theme.TextBrownLight
import com.github.hwhaocool.pointbunny.ui.theme.White

/**
 * 家长专区（信息导航页）：
 *  - 数据概览（余额 / 记录数 / 心愿数）
 *  - 「数据分享」入口（JSON 备份已挪到 DataShareScreen）
 *  - 「关于」入口（应用信息 / 本机信息）
 *  - 「清空全部数据」保留在本页（双重确认）
 */
@Composable
fun ParentZoneScreen(
    balance: Int,
    recordCount: Int,
    wishCount: Int,
    onDataShare: () -> Unit,
    onAbout: () -> Unit,
    onClearAll: () -> Unit
) {
    var confirm1 by remember { mutableStateOf(false) }
    var confirm2 by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            "👨‍👩‍👧 家长专区",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextBrown,
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // ---- 数据概览 ----
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OverviewCell("当前余额", "$balance 分", CandyPink, Modifier.weight(1f))
                    OverviewCell("积分记录", "$recordCount 条", MintGreen, Modifier.weight(1f))
                    OverviewCell("心愿数量", "$wishCount 个", CandyPink, Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(14.dp))

            // ---- 导航入口 ----
            NavEntry("📤", "数据分享", "备份 / 复制 JSON 数据", onDataShare)
            Spacer(Modifier.height(10.dp))
            NavEntry("ℹ️", "关于", "应用信息 · 本机信息", onAbout)

            Spacer(Modifier.height(20.dp))

            // ---- 清空数据（危险区） ----
            Surface(
                onClick = { confirm1 = true },
                shape = RoundedCornerShape(50),
                color = DangerRed.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "🗑️ 清空全部数据",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DangerRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 15.dp)
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "清空后无法恢复，请先到「数据分享」复制好 JSON 备份！",
                fontSize = 11.sp,
                color = TextBrownLight,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    // ---- 第一次确认 ----
    if (confirm1) {
        AlertDialog(
            onDismissRequest = { confirm1 = false },
            containerColor = White,
            title = { Text("确定要清空数据吗？", fontWeight = FontWeight.Bold) },
            text = { Text("会删掉全部积分记录、心愿和勋章，而且不能恢复哦！") },
            confirmButton = {
                TextButton(onClick = {
                    confirm1 = false
                    confirm2 = true
                }) { Text("继续", color = DangerRed) }
            },
            dismissButton = {
                TextButton(onClick = { confirm1 = false }) { Text("取消") }
            }
        )
    }

    // ---- 第二次确认 ----
    if (confirm2) {
        AlertDialog(
            onDismissRequest = { confirm2 = false },
            containerColor = White,
            title = { Text("真的要全部删掉吗？😢", fontWeight = FontWeight.Bold) },
            text = { Text("这是最后一次确认，点下去所有数据就消失啦！") },
            confirmButton = {
                TextButton(onClick = {
                    confirm2 = false
                    onClearAll()
                }) { Text("确认清空", color = DangerRed) }
            },
            dismissButton = {
                TextButton(onClick = { confirm2 = false }) { Text("我再想想") }
            }
        )
    }
}

/** 导航入口卡片 */
@Composable
private fun NavEntry(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(CandyPinkPale, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextBrown)
                Text(subtitle, fontSize = 12.sp, color = TextBrownLight)
            }
            Text("›", fontSize = 24.sp, color = TextBrownLight)
        }
    }
}

@Composable
private fun OverviewCell(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = TextBrownLight)
        Spacer(Modifier.height(3.dp))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}
