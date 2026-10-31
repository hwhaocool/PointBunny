package com.candy.pointbunny.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.candy.pointbunny.ui.theme.CandyPink
import com.candy.pointbunny.ui.theme.CandyPinkPale
import com.candy.pointbunny.ui.theme.DangerRed
import com.candy.pointbunny.ui.theme.MintGreen
import com.candy.pointbunny.ui.theme.TextBrown
import com.candy.pointbunny.ui.theme.TextBrownLight
import com.candy.pointbunny.ui.theme.White

/**
 * 家长专区：
 *  - 数据概览（余额 / 记录数 / 心愿数）
 *  - JSON 全量预览 + 一键复制到剪贴板（换手机迁移）
 *  - 清空全部数据（双重确认）
 */
@Composable
fun ParentZoneScreen(
    balance: Int,
    recordCount: Int,
    wishCount: Int,
    json: String,
    onClearAll: () -> Unit
) {
    val context = LocalContext.current
    var confirm1 by remember { mutableStateOf(false) }
    var confirm2 by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
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

            // ---- 导出说明 ----
            Text(
                "📤 数据备份（JSON）",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextBrown
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "点「复制到剪贴板」后，把内容粘贴到微信/备忘录保存。换手机时把这份 JSON 交给新手机的家长专区即可恢复。",
                fontSize = 12.sp,
                color = TextBrownLight
            )
            Spacer(Modifier.height(8.dp))

            // ---- JSON 预览 ----
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CandyPinkPale.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    Modifier
                        .padding(12.dp)
                        .heightIn(max = 280.dp)
                ) {
                    Text(
                        json,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        color = TextBrown,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---- 复制按钮 ----
            Surface(
                onClick = {
                    val clipboard = context
                        .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(
                        ClipData.newPlainText("积分小兔数据备份", json)
                    )
                },
                shape = RoundedCornerShape(50),
                color = CandyPink,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "📋 一键复制到剪贴板",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(vertical = 15.dp)
                )
            }

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
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(vertical = 15.dp)
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "清空后无法恢复，请先复制好 JSON 备份！",
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
