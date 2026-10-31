package com.github.hwhaocool.pointbunny.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.hwhaocool.pointbunny.ui.comp.BackHeader
import com.github.hwhaocool.pointbunny.ui.theme.CandyPink
import com.github.hwhaocool.pointbunny.ui.theme.CandyPinkPale
import com.github.hwhaocool.pointbunny.ui.theme.TextBrown
import com.github.hwhaocool.pointbunny.ui.theme.TextBrownLight
import com.github.hwhaocool.pointbunny.ui.theme.White

/**
 * 数据分享：JSON 全量备份 + 一键复制到剪贴板（换手机迁移用）。
 * 从家长专区入口进入。
 */
@Composable
fun DataShareScreen(json: String, onBack: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        BackHeader("📤 数据分享", onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            Text(
                "点「复制到剪贴板」后，把内容粘贴到微信/备忘录保存。换手机时把这份 JSON 交给新手机的家长专区即可恢复。",
                fontSize = 12.sp,
                color = TextBrownLight
            )
            Spacer(Modifier.height(10.dp))

            // ---- JSON 预览 ----
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CandyPinkPale.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    Modifier
                        .padding(12.dp)
                        .heightIn(max = 400.dp)
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
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 15.dp)
                )
            }
        }
    }
}
