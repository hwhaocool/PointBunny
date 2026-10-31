package com.github.hwhaocool.pointbunny.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.hwhaocool.pointbunny.data.WishEntity
import com.github.hwhaocool.pointbunny.ui.theme.CandyPink
import com.github.hwhaocool.pointbunny.ui.theme.CandyPinkPale
import com.github.hwhaocool.pointbunny.ui.theme.MintGreen
import com.github.hwhaocool.pointbunny.ui.theme.TextBrown
import com.github.hwhaocool.pointbunny.ui.theme.TextBrownLight
import com.github.hwhaocool.pointbunny.ui.theme.White
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** 可选心愿 emoji（与原型一致的 8 个） */
private val WISH_EMOJIS = listOf("🦕", "🎁", "🍦", "🧸", "🎡", "🚲", "📚", "🎨")

/**
 * 心愿清单：进度条 + 还差x分 / 可以兑换啦 + 兑换 + 添加心愿
 */
@Composable
fun WishScreen(
    wishes: List<WishEntity>,
    balance: Int,
    onAdd: (emoji: String, name: String, target: Int) -> Unit,
    onRedeem: (WishEntity) -> Unit,
    onDelete: (WishEntity) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    var pendingRedeem by remember { mutableStateOf<WishEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "🎁 心愿清单",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextBrown,
                modifier = Modifier.weight(1f)
            )
            Text(
                "现在有 $balance 分",
                fontSize = 13.sp,
                color = TextBrownLight
            )
        }

        if (wishes.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(top = 50.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🌈", fontSize = 44.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("还没有心愿，许一个吧！", fontSize = 15.sp, color = TextBrownLight)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp)
            ) {
                wishes.forEach { w ->
                    WishCard(
                        wish = w,
                        balance = balance,
                        onRedeemClick = { pendingRedeem = w },
                        onDeleteClick = { onDelete(w) }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }

    // ---- 添加心愿悬浮按钮（右下角大按钮） ----
    Box(Modifier.fillMaxSize()) {
        Surface(
            onClick = { showAdd = true },
            shape = RoundedCornerShape(50),
            color = CandyPink,
            shadowElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 96.dp)
        ) {
            Text(
                "＋ 添加心愿",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp)
            )
        }
    }

    // ---- 兑换确认 ----
    pendingRedeem?.let { w ->
        AlertDialog(
            onDismissRequest = { pendingRedeem = null },
            containerColor = White,
            title = { Text("兑换心愿？", fontWeight = FontWeight.Bold) },
            text = { Text("要用 ${w.target} 分兑换「${w.emoji} ${w.name}」吗？兑换后积分会扣掉哦～") },
            confirmButton = {
                TextButton(onClick = {
                    onRedeem(w)
                    pendingRedeem = null
                }) { Text("兑换！", color = CandyPink) }
            },
            dismissButton = {
                TextButton(onClick = { pendingRedeem = null }) { Text("再攒攒") }
            }
        )
    }

    // ---- 添加心愿弹窗 ----
    if (showAdd) {
        AddWishDialog(
            onDismiss = { showAdd = false },
            onConfirm = { emoji, name, target ->
                onAdd(emoji, name, target)
                showAdd = false
            }
        )
    }
}

@Composable
private fun WishCard(
    wish: WishEntity,
    balance: Int,
    onRedeemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val progress = if (wish.target <= 0) 1f
    else (balance.toFloat() / wish.target).coerceIn(0f, 1f)
    val animProgress by animateFloatAsState(progress, tween(600), label = "wishProgress")
    val canRedeem = balance >= wish.target && !wish.done

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(wish.emoji, fontSize = 34.sp)
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        wish.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBrown
                    )
                    Text(
                        text = if (wish.done) "已达成 ✅"
                        else if (canRedeem) "可以兑换啦🎉"
                        else "还差 ${wish.target - balance} 分",
                        fontSize = 13.sp,
                        color = when {
                            wish.done -> MintGreen
                            canRedeem -> CandyPink
                            else -> TextBrownLight
                        }
                    )
                }
                Text(
                    "🗑️", fontSize = 15.sp,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable(onClick = onDeleteClick)
                )
            }

            Spacer(Modifier.height(10.dp))

            // 进度条
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .background(CandyPinkPale, RoundedCornerShape(50))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(animProgress)
                        .height(14.dp)
                        .background(
                            if (wish.done) MintGreen else CandyPink,
                            RoundedCornerShape(50)
                        )
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "${balance.coerceAtMost(wish.target)} / ${wish.target} 分",
                fontSize = 11.sp,
                color = TextBrownLight
            )

            if (canRedeem) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    onClick = onRedeemClick,
                    shape = RoundedCornerShape(50),
                    color = MintGreen,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "兑换 🎉",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddWishDialog(
    onDismiss: () -> Unit,
    onConfirm: (emoji: String, name: String, target: Int) -> Unit
) {
    var emoji by remember { mutableStateOf(WISH_EMOJIS.first()) }
    var name by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        title = { Text("＋ 新心愿", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("选一个图案：", fontSize = 13.sp, color = TextBrownLight)
                Spacer(Modifier.height(6.dp))
                WISH_EMOJIS.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { e ->
                            val selected = emoji == e
                            Surface(
                                onClick = { emoji = e },
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) CandyPink.copy(alpha = 0.15f) else CandyPinkPale,
                                border = if (selected)
                                    androidx.compose.foundation.BorderStroke(2.dp, CandyPink) else null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    e, fontSize = 22.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 12) name = it },
                    label = { Text("心愿名称（请爸爸妈妈帮忙输入）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { v ->
                        targetText = v.filter { c -> c.isDigit() }.take(5)
                    },
                    label = { Text("需要多少分") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val target = targetText.toIntOrNull() ?: 0
                if (name.isNotBlank() && target > 0) {
                    onConfirm(emoji, name.trim(), target)
                }
            }) { Text("保存", color = CandyPink) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
