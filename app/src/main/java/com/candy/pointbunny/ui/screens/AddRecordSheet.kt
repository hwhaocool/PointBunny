package com.candy.pointbunny.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.candy.pointbunny.data.RecordEntity
import com.candy.pointbunny.ui.comp.NumberKeyboard
import com.candy.pointbunny.ui.theme.CandyPink
import com.candy.pointbunny.ui.theme.CandyPinkPale
import com.candy.pointbunny.ui.theme.Cream
import com.candy.pointbunny.ui.theme.MintGreen
import com.candy.pointbunny.ui.theme.TextBrown
import com.candy.pointbunny.ui.theme.TextBrownLight
import com.candy.pointbunny.ui.theme.WarmOrange
import com.candy.pointbunny.ui.theme.White

/** 攒分原因（与原型一致，💃 跳舞为新增，✏️ 可自定义） */
val EARN_REASONS = listOf(
    "🧹" to "帮忙做家务",
    "👕" to "自己穿衣服",
    "🍚" to "好好吃饭",
    "🦷" to "按时刷牙",
    "💃" to "跳舞",
    "⭐" to "表现棒棒的",
    "✏️" to "其他好事"
)

/** 花分事项（与原型一致） */
val SPEND_REASONS = listOf(
    "🧸" to "买个玩具",
    "📺" to "看动画片",
    "🍦" to "吃冰淇淋",
    "🎡" to "去游乐场",
    "🍭" to "买零食",
    "✏️" to "其他事情"
)

private val QUICK_POINTS = listOf(1, 2, 5, 10, 20)

/**
 * 攒分 / 花分 底部弹窗：
 * 大数字键盘 + 快捷加分 + 原因选择 + 保存。
 * 花分超过余额时：抖动警告 + 提示，不关闭弹窗。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordSheet(
    type: String,                      // "earn" / "spend"
    balance: Int,
    onDismiss: () -> Unit,
    onSave: (points: Int, reason: String, emoji: String) -> Unit,
    onWarn: (String) -> Unit,          // 余额不足等警告回调（显示 toast）
    editTarget: RecordEntity? = null   // 非空 = 编辑模式
) {
    val isEarn = type == "earn"
    val reasons = if (isEarn) EARN_REASONS else SPEND_REASONS
    val themeColor = if (isEarn) MintGreen else WarmOrange
    val isEdit = editTarget != null
    // 编辑花分记录时，先还原本条已扣的积分，避免误判余额不足
    val spendLimit = if (!isEarn) balance + (editTarget?.points ?: 0) else balance

    var input by remember(editTarget) { mutableStateOf(editTarget?.points?.toString() ?: "") }
    var reasonIndex by remember(editTarget) {
        mutableIntStateOf(
            editTarget?.let { t ->
                val idx = reasons.indexOfFirst { it.second == t.reason }
                if (idx >= 0) idx else reasons.indexOfFirst { it.first == "✏️" }.coerceAtLeast(0)
            } ?: 0
        )
    }
    var customReason by remember(editTarget) {
        mutableStateOf(
            editTarget?.let { t ->
                if (reasons.any { it.second == t.reason }) "" else t.reason
            } ?: ""
        )
    }
    val shake = remember { Animatable(0f) }
    var shakeKey by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    // 当前选中的是否为「其他」项（✏️ 可编辑）
    val isOtherSelected = reasons[reasonIndex].first == "✏️"

    // 抖动动画：余额不足时触发
    LaunchedEffect(shakeKey) {
        if (shakeKey > 0) {
            listOf(14f, -12f, 9f, -7f, 5f, -3f, 0f).forEach { v ->
                shake.animateTo(v, tween(42))
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Cream,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // ---- 标题 ----
            Text(
                if (isEdit) "✏️ 修改记录" else if (isEarn) "🐰 攒积分啦！" else "🛍️ 要花积分吗？",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = TextBrown,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            if (!isEarn) {
                Text(
                    "现在有 $balance 分",
                    fontSize = 13.sp,
                    color = TextBrownLight,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(Modifier.height(12.dp))

            // ---- 数字显示区（超余额时抖动） ----
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = White,
                border = BorderStroke(2.dp, themeColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = shake.value.dp)
            ) {
                Text(
                    text = if (input.isEmpty()) "0" else input,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = themeColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // ---- 快捷积分 chips ----
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QUICK_POINTS.forEach { p ->
                    Surface(
                        onClick = {
                            input = p.toString()
                            reasonIndex = reasonIndex // 保持
                        },
                        shape = RoundedCornerShape(50),
                        color = CandyPinkPale,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "+$p",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CandyPink,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 9.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ---- 原因选择（2 列 3 行） ----
            reasons.chunked(2).forEach { rowPair ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowPair.forEach { (emoji, text) ->
                        val idx = reasons.indexOfFirst { it.second == text }
                        val selected = reasonIndex == idx
                        Surface(
                            onClick = { reasonIndex = idx },
                            shape = RoundedCornerShape(18.dp),
                            color = if (selected) themeColor.copy(alpha = 0.16f) else White,
                            border = if (selected)
                                BorderStroke(2.dp, themeColor)
                            else null,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 52.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                            ) {
                                Text(emoji, fontSize = 20.sp)
                                Spacer(Modifier.size(6.dp))
                                Text(
                                    text,
                                    fontSize = 14.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = TextBrown
                                )
                            }
                        }
                    }
                    if (rowPair.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(6.dp))

            // ---- 自定义「其他」原因（选中 ✏️ 后可编辑） ----
            if (isOtherSelected) {
                OutlinedTextField(
                    value = customReason,
                    onValueChange = { customReason = it.take(12) },
                    placeholder = {
                        Text(
                            if (isEarn) "写一写：做了什么好事～" else "写一写：想做什么事～",
                            fontSize = 14.sp
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
            }

            // ---- 大数字键盘 ----
            NumberKeyboard(
                onDigit = { d ->
                    if (input.length < 5) {
                        val s = input + d.toString()
                        // 去掉前导零
                        input = s.toIntOrNull()?.toString() ?: input
                    }
                },
                onClear = { input = "" },
                onBackspace = { input = if (input.isNotEmpty()) input.dropLast(1) else "" }
            )

            Spacer(Modifier.height(16.dp))

            // ---- 保存按钮 ----
            Surface(
                onClick = {
                    val points = input.toIntOrNull() ?: 0
                    if (points <= 0) {
                        onWarn("先选一个数字哦～")
                        return@Surface
                    }
                    if (!isEarn && points > spendLimit) {
                        // 余额不足：抖动 + 提示，不关闭
                        shakeKey++
                        onWarn("积分不够啦，先和爸爸妈妈一起攒一攒吧😢")
                        return@Surface
                    }
                    val (emoji, reason) = reasons[reasonIndex]
                    val finalReason = if (isOtherSelected) {
                        customReason.trim().ifEmpty { reason }
                    } else reason
                    onSave(points, finalReason, emoji)
                },
                shape = RoundedCornerShape(50),
                color = themeColor,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        if (isEdit) "保存修改 ✓" else "保存 ✓",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White
                    )
                }
            }
        }
    }
}
