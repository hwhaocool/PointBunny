package com.github.hwhaocool.pointbunny.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.hwhaocool.pointbunny.BuildConfig
import com.github.hwhaocool.pointbunny.ui.comp.BackHeader
import com.github.hwhaocool.pointbunny.ui.theme.TextBrown
import com.github.hwhaocool.pointbunny.ui.theme.TextBrownLight
import com.github.hwhaocool.pointbunny.ui.theme.White

/**
 * 关于页：应用信息 + 本机信息。
 * 参考 android-dianji-v3.1 的「应用信息」实现。
 */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        BackHeader("ℹ️ 关于", onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            SectionCard("应用信息") {
                InfoRow("应用名称", "积分小兔 PointBunny")
                InfoRow("版本号", "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                InfoRow("构建时间", BuildConfig.BUILD_TIME)
                InfoRow("构建类型", BuildConfig.BUILD_TYPE + if (BuildConfig.DEBUG) " · 调试" else "")
                InfoRow("包名", BuildConfig.APPLICATION_ID)
            }

            Spacer(Modifier.height(14.dp))

            SectionCard("本机信息") {
                InfoRow("手机型号", "${Build.MANUFACTURER} ${Build.MODEL}")
                InfoRow("系统版本", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            }
        }
    }
}

/** 分组卡片：标题 + 内容 */
@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextBrown)
        Spacer(Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                content()
            }
        }
    }
}

/** 键值信息行 */
@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, color = TextBrownLight)
        Spacer(Modifier.weight(1f))
        Text(value, fontSize = 13.sp, color = TextBrown, fontWeight = FontWeight.Medium)
    }
}
