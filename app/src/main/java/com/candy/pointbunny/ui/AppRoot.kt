package com.candy.pointbunny.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.candy.pointbunny.ui.screens.AddRecordSheet
import com.candy.pointbunny.ui.screens.BadgeScreen
import com.candy.pointbunny.ui.screens.HomeScreen
import com.candy.pointbunny.ui.screens.HistoryScreen
import com.candy.pointbunny.ui.screens.ParentZoneScreen
import com.candy.pointbunny.ui.screens.StatsScreen
import com.candy.pointbunny.ui.screens.WishScreen
import com.candy.pointbunny.ui.theme.CandyPink
import com.candy.pointbunny.ui.theme.CandyPinkLight
import com.candy.pointbunny.ui.theme.Cream
import com.candy.pointbunny.ui.theme.TextBrown
import com.candy.pointbunny.ui.theme.TextBrownLight
import com.candy.pointbunny.ui.theme.White
import com.candy.pointbunny.vm.BunnyViewModel

/**
 * 应用骨架：
 *  - 4 个主 Tab：首页 / 历史 / 统计 / 心愿
 *  - 二级页：我的勋章、家长专区（从首页进入，底部导航始终可用）
 *  - 攒分 / 花分底部弹窗挂在最顶层
 *  - 全局 Toast 事件统一走 ViewModel 的 toast 通道
 */
@Composable
fun AppRoot(vm: BunnyViewModel = viewModel()) {
    val records by vm.records.collectAsState()
    val wishes by vm.wishes.collectAsState()
    val balance by vm.balance.collectAsState()
    val unlocked by vm.unlockedBadges.collectAsState()
    val unlockTimes by vm.badgeUnlockTimes.collectAsState()
    val toast by vm.toast.collectAsState()
    val context = LocalContext.current

    var current by rememberSaveable { mutableStateOf("home") }
    var showAddSheet by remember { mutableStateOf<String?>(null) }

    // 导出 JSON 只在数据变化时重建
    val exportJson = remember(records, wishes, balance) { vm.buildExportJson() }

    // ---- 全局 Toast ----
    LaunchedEffect(toast) {
        toast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.consumeToast()
        }
    }

    Scaffold(
        containerColor = Cream,
        bottomBar = {
            NavigationBar(containerColor = White) {
                val tabs = listOf(
                    Triple("home", "🏠", "首页"),
                    Triple("history", "📜", "历史"),
                    Triple("stats", "📊", "统计"),
                    Triple("wish", "🎁", "心愿")
                )
                tabs.forEach { (route, emoji, label) ->
                    NavigationBarItem(
                        selected = current == route,
                        onClick = { current = route },
                        icon = { Text(emoji, fontSize = 22.sp) },
                        label = { Text(label, fontSize = 11.sp, color = TextBrown) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CandyPinkLight
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = current,
                transitionSpec = {
                    fadeIn(tween(160)) togetherWith fadeOut(tween(160))
                },
                label = "screen"
            ) { route ->
                when (route) {
                    "history" -> HistoryScreen(
                        records = records,
                        onDelete = vm::deleteRecord
                    )
                    "stats" -> StatsScreen(records = records)
                    "wish" -> WishScreen(
                        wishes = wishes,
                        balance = balance,
                        onAdd = vm::addWish,
                        onRedeem = vm::redeemWish,
                        onDelete = vm::deleteWish
                    )
                    "badge" -> BadgeScreen(unlocked, unlockTimes)
                    "parent" -> ParentZoneScreen(
                        balance = balance,
                        recordCount = records.size,
                        wishCount = wishes.size,
                        json = exportJson,
                        onClearAll = vm::clearAllData
                    )
                    else -> HomeScreen(
                        balance = balance,
                        isEmpty = records.isEmpty(),
                        onEarn = { showAddSheet = "earn" },
                        onSpend = { showAddSheet = "spend" },
                        onGoHistory = { current = "history" },
                        onGoStats = { current = "stats" },
                        onGoWish = { current = "wish" },
                        onGoBadge = { current = "badge" },
                        onGoParent = { current = "parent" }
                    )
                }
            }

            // ---- 攒分 / 花分底部弹窗（顶层） ----
            showAddSheet?.let { type ->
                AddRecordSheet(
                    type = type,
                    balance = balance,
                    onDismiss = { showAddSheet = null },
                    onSave = { points, reason, emoji ->
                        vm.addRecord(type, points, reason, emoji)
                        showAddSheet = null
                    },
                    onWarn = vm::showToast
                )
            }
        }
    }
}
