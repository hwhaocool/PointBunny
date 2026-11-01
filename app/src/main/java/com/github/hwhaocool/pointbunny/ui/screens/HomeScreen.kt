package com.github.hwhaocool.pointbunny.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import com.github.hwhaocool.pointbunny.ui.theme.CandyPink
import com.github.hwhaocool.pointbunny.ui.theme.CandyPinkLight
import com.github.hwhaocool.pointbunny.ui.theme.MintGreen
import com.github.hwhaocool.pointbunny.ui.theme.TextBrown
import com.github.hwhaocool.pointbunny.ui.theme.TextBrownLight
import com.github.hwhaocool.pointbunny.ui.theme.WarmOrange
import com.github.hwhaocool.pointbunny.ui.theme.White

/**
 * 首页：小兔子 + 积分宝藏 + 两大按钮 + 四个快捷入口
 */
@Composable
fun HomeScreen(
    balance: Int,
    isEmpty: Boolean,
    onEarn: () -> Unit,
    onSpend: () -> Unit,
    onGoHistory: () -> Unit,
    onGoStats: () -> Unit,
    onGoWish: () -> Unit,
    onGoBadge: () -> Unit,
    onGoParent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        // ---- 顶栏 ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "🐰 积分小兔",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextBrown,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onGoParent) {
                Icon(
                    Icons.Rounded.Settings,
                    contentDescription = "家长专区",
                    tint = TextBrownLight
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // ---- 积分宝藏卡 ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(CandyPink, CandyPinkLight)
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(vertical = 30.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CuteBunny()
                Spacer(Modifier.height(6.dp))
                Text("我的积分宝藏", fontSize = 15.sp, color = White)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        balance.toString(),
                        fontSize = 62.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White
                    )
                    Text(
                        " 分",
                        fontSize = 18.sp,
                        color = White,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                }
                Text(
                    if (isEmpty) "快攒下第一颗积分吧！" else "继续加油哦，小兔子看好你～",
                    fontSize = 13.sp,
                    color = White.copy(alpha = 0.9f)
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // ---- 攒分 / 花分 两大按钮 ----
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            BigActionButton(
                text = "攒积分",
                symbol = "＋",
                color = MintGreen,
                onClick = onEarn,
                modifier = Modifier.weight(1f)
            )
            BigActionButton(
                text = "花积分",
                symbol = "－",
                color = WarmOrange,
                onClick = onSpend,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(18.dp))

        // ---- 快捷入口 ----
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickEntry("📜", "积分历史", onGoHistory, Modifier.weight(1f))
            QuickEntry("📊", "积分统计", onGoStats, Modifier.weight(1f))
            QuickEntry("🎁", "心愿清单", onGoWish, Modifier.weight(1f))
            QuickEntry("🏅", "我的勋章", onGoBadge, Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))
    }
}

/** 更可爱的手绘小兔子：长耳朵 + 腮红 + 高光眼睛 + ω 小嘴 */
@Composable
private fun CuteBunny(modifier: Modifier = Modifier) {
    val earPink = CandyPinkLight
    val outline = TextBrown.copy(alpha = 0.20f)
    val eye = TextBrown
    val nose = CandyPink
    val blush = CandyPink.copy(alpha = 0.45f)

    Canvas(modifier = modifier.size(112.dp)) {
        val w = size.width
        val h = size.height

        val earW = w * 0.20f
        val earH = h * 0.44f
        val earCorner = w * 0.11f
        val innerW = w * 0.105f
        val innerH = h * 0.26f
        val innerCorner = innerW / 2f

        // 左耳（略向左倾）
        rotate(-12f, pivot = Offset(w * 0.30f, h * 0.26f)) {
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.30f - earW / 2f, h * 0.03f),
                size = Size(earW, earH),
                cornerRadius = CornerRadius(earCorner, earCorner)
            )
            drawRoundRect(
                color = earPink,
                topLeft = Offset(w * 0.30f - innerW / 2f, h * 0.09f),
                size = Size(innerW, innerH),
                cornerRadius = CornerRadius(innerCorner, innerCorner)
            )
        }
        // 右耳（略向右倾）
        rotate(12f, pivot = Offset(w * 0.70f, h * 0.26f)) {
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.70f - earW / 2f, h * 0.03f),
                size = Size(earW, earH),
                cornerRadius = CornerRadius(earCorner, earCorner)
            )
            drawRoundRect(
                color = earPink,
                topLeft = Offset(w * 0.70f - innerW / 2f, h * 0.09f),
                size = Size(innerW, innerH),
                cornerRadius = CornerRadius(innerCorner, innerCorner)
            )
        }

        // 脸
        val faceR = w * 0.34f
        val faceCenter = Offset(w * 0.5f, h * 0.66f)
        drawCircle(Color.White, faceR, faceCenter)
        drawCircle(outline, faceR, faceCenter, style = Stroke(width = w * 0.012f))

        // 眼睛 + 高光
        val eyeY = faceCenter.y - faceR * 0.16f
        val eyeR = faceR * 0.085f
        drawCircle(eye, eyeR, Offset(faceCenter.x - faceR * 0.34f, eyeY))
        drawCircle(eye, eyeR, Offset(faceCenter.x + faceR * 0.34f, eyeY))
        val glintR = eyeR * 0.38f
        drawCircle(Color.White, glintR, Offset(faceCenter.x - faceR * 0.34f - eyeR * 0.28f, eyeY - eyeR * 0.32f))
        drawCircle(Color.White, glintR, Offset(faceCenter.x + faceR * 0.34f - eyeR * 0.28f, eyeY - eyeR * 0.32f))

        // 腮红
        val blushR = faceR * 0.15f
        drawCircle(blush, blushR, Offset(faceCenter.x - faceR * 0.52f, faceCenter.y + faceR * 0.10f))
        drawCircle(blush, blushR, Offset(faceCenter.x + faceR * 0.52f, faceCenter.y + faceR * 0.10f))

        // 鼻子
        val noseCenter = Offset(faceCenter.x, faceCenter.y + faceR * 0.02f)
        drawCircle(nose, faceR * 0.06f, noseCenter)

        // 嘴巴（ω 形，两个小弧）
        val mouthY = noseCenter.y + faceR * 0.10f
        val mouthR = faceR * 0.10f
        val strokeW = w * 0.014f
        drawArc(
            color = eye,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(noseCenter.x - mouthR, mouthY),
            size = Size(mouthR, mouthR),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )
        drawArc(
            color = eye,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(noseCenter.x, mouthY),
            size = Size(mouthR, mouthR),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )
    }
}

/** 攒分 / 花分超大按钮 */
@Composable
private fun BigActionButton(
    text: String,
    symbol: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = color,
        shadowElevation = 4.dp,
        modifier = modifier.height(92.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(symbol, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = White)
            Spacer(Modifier.size(6.dp))
            Text(text, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = White)
        }
    }
}

/** 快捷入口圆钮 */
@Composable
private fun QuickEntry(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = White,
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 26.sp)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, color = TextBrown)
        }
    }
}
