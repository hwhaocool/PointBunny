package com.github.hwhaocool.pointbunny.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.hwhaocool.pointbunny.ui.comp.BackHeader
import com.github.hwhaocool.pointbunny.ui.theme.CandyPink
import com.github.hwhaocool.pointbunny.ui.theme.CandyPinkPale
import com.github.hwhaocool.pointbunny.ui.theme.DangerRed
import com.github.hwhaocool.pointbunny.ui.theme.MintGreen
import com.github.hwhaocool.pointbunny.ui.theme.TextBrown
import com.github.hwhaocool.pointbunny.ui.theme.TextBrownLight
import com.github.hwhaocool.pointbunny.ui.theme.White

/**
 * 数据分享：JSON 全量备份（复制导出） + 数据导入（粘贴恢复）。
 * 从家长专区入口进入，实现两台手机之间的数据共享。
 */
@Composable
fun DataShareScreen(
    json: String,
    onBack: () -> Unit,
    onImport: (String) -> Unit,
    onWarn: (String) -> Unit
) {
    val context = LocalContext.current
    var importText by remember { mutableStateOf("") }
    var showImportConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        BackHeader("📤 数据分享", onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // ---- 数据导出 ----
            Text(
                "📤 数据导出",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextBrown
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "点「复制到剪贴板」后，把内容粘贴到微信/备忘录发给另一台手机。",
                fontSize = 12.sp,
                color = TextBrownLight
            )
            Spacer(Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CandyPinkPale.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    Modifier
                        .padding(12.dp)
                        .heightIn(max = 260.dp)
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

            Spacer(Modifier.height(10.dp))

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
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 15.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ---- 数据导入 ----
            Text(
                "📥 数据导入",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextBrown
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "把另一台手机导出的 JSON 粘贴到下面，点「导入」即可合并进当前数据。",
                fontSize = 12.sp,
                color = TextBrownLight
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = importText,
                onValueChange = { importText = it },
                placeholder = { Text("粘贴 JSON 数据到这里", fontSize = 13.sp) },
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
            )

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    onClick = {
                        val clipboard = context
                            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val text = clipboard.primaryClip
                            ?.getItemAt(0)
                            ?.coerceToText(context)
                            ?.toString()
                            ?: ""
                        if (text.isBlank()) {
                            onWarn("剪贴板里还没有内容哦")
                        } else {
                            importText = text
                        }
                    },
                    shape = RoundedCornerShape(50),
                    color = CandyPinkPale,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "📥 从剪贴板粘贴",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CandyPink,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 14.dp)
                    )
                }

                Surface(
                    onClick = {
                        if (importText.isBlank()) {
                            onWarn("先粘贴 JSON 数据哦～")
                        } else {
                            showImportConfirm = true
                        }
                    },
                    shape = RoundedCornerShape(50),
                    color = MintGreen,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "导入",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 14.dp)
                    )
                }
            }
        }
    }

    // ---- 导入确认 ----
    if (showImportConfirm) {
        AlertDialog(
            onDismissRequest = { showImportConfirm = false },
            containerColor = White,
            title = { Text("导入数据吗？", fontWeight = FontWeight.Bold) },
            text = { Text("导入会把数据增量合并进当前记录，不会覆盖已有数据。重复导入会产生重复记录哦～") },
            confirmButton = {
                TextButton(onClick = {
                    showImportConfirm = false
                    onImport(importText)
                }) { Text("确认导入", color = DangerRed) }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirm = false }) { Text("取消") }
            }
        )
    }
}
