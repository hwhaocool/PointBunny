package com.github.hwhaocool.pointbunny.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.github.hwhaocool.pointbunny.data.ReasonEntity
import com.github.hwhaocool.pointbunny.ui.comp.BackHeader
import com.github.hwhaocool.pointbunny.ui.theme.CandyPink
import com.github.hwhaocool.pointbunny.ui.theme.CandyPinkPale
import com.github.hwhaocool.pointbunny.ui.theme.Cream
import com.github.hwhaocool.pointbunny.ui.theme.DangerRed
import com.github.hwhaocool.pointbunny.ui.theme.MintGreen
import com.github.hwhaocool.pointbunny.ui.theme.TextBrown
import com.github.hwhaocool.pointbunny.ui.theme.TextBrownLight
import com.github.hwhaocool.pointbunny.ui.theme.WarmOrange
import com.github.hwhaocool.pointbunny.ui.theme.White

/** 内置 emoji 图标选择（按 8 个一行排列） */
private val EMOJI_CHOICES = listOf(
    "🧹", "👕", "🍚", "🦷", "💃", "⭐", "🧸", "📺",
    "🍦", "🎡", "🍭", "📚", "🎨", "🎹", "⚽", "🏀",
    "🚲", "🐶", "🐱", "🐰", "🦕", "🌈", "🍎", "🥕",
    "🍰", "🎂", "🧁", "🎁", "🎈", "🪁", "🧩", "🎮",
    "🛁", "🧼", "😴", "👍", "❤️", "🌟", "✨", "🎵",
    "🏆", "🥇", "🍬", "🖍️", "✏️", "🏃", "🧗", "🎤"
)

/**
 * 事项管理：自由新增 / 修改 / 删除攒分、花分事项（名称 + emoji）。
 * 保存后立即在「攒积分」「花积分」页面生效。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReasonManageScreen(
    earnReasons: List<ReasonEntity>,
    spendReasons: List<ReasonEntity>,
    onBack: () -> Unit,
    onAdd: (type: String, emoji: String, name: String, freeText: Boolean) -> Unit,
    onUpdate: (reason: ReasonEntity, emoji: String, name: String, freeText: Boolean) -> Unit,
    onDelete: (reason: ReasonEntity) -> Unit,
    onWarn: (String) -> Unit
) {
    var showForm by remember { mutableStateOf(false) }
    var editingReason by remember { mutableStateOf<ReasonEntity?>(null) }
    var formType by remember { mutableStateOf("earn") }
    var formEmoji by remember { mutableStateOf(EMOJI_CHOICES.first()) }
    var formName by remember { mutableStateOf("") }
    var formFreeText by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<ReasonEntity?>(null) }

    fun openAdd(type: String) {
        editingReason = null
        formType = type
        formEmoji = EMOJI_CHOICES.first()
        formName = ""
        formFreeText = false
        showForm = true
    }

    fun openEdit(r: ReasonEntity) {
        editingReason = r
        formType = r.type
        formEmoji = r.emoji
        formName = r.name
        formFreeText = r.freeText
        showForm = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        BackHeader("🏷️ 事项管理", onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // ---- 攒积分事项 ----
            SectionTitle("攒积分事项", MintGreen)
            Spacer(Modifier.height(8.dp))
            earnReasons.forEach { r ->
                ReasonRow(r, onEdit = { openEdit(r) }, onDelete = { pendingDelete = r })
                Spacer(Modifier.height(8.dp))
            }
            AddButton("＋ 新增攒积分事项", MintGreen) { openAdd("earn") }

            Spacer(Modifier.height(22.dp))

            // ---- 花积分事项 ----
            SectionTitle("花积分事项", WarmOrange)
            Spacer(Modifier.height(8.dp))
            spendReasons.forEach { r ->
                ReasonRow(r, onEdit = { openEdit(r) }, onDelete = { pendingDelete = r })
                Spacer(Modifier.height(8.dp))
            }
            AddButton("＋ 新增花积分事项", WarmOrange) { openAdd("spend") }
        }
    }

    // ---- 删除确认 ----
    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = White,
            title = { Text("删除事项？", fontWeight = FontWeight.Bold) },
            text = { Text("「${target.emoji} ${target.name}」删除后，攒/花积分页就不会再显示它了。") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(target)
                    pendingDelete = null
                }) { Text("删除", color = DangerRed) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("取消") }
            }
        )
    }

    // ---- 新增 / 编辑表单 ----
    if (showForm) {
        ModalBottomSheet(
            onDismissRequest = { showForm = false },
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
                Text(
                    if (editingReason == null) "新增事项" else "编辑事项",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBrown,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(14.dp))

                // ---- 类型（仅新增时可选择） ----
                if (editingReason == null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TypeChip("攒积分", "earn", formType == "earn", MintGreen, { formType = "earn" }, Modifier.weight(1f))
                        TypeChip("花积分", "spend", formType == "spend", WarmOrange, { formType = "spend" }, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                } else {
                    Text(
                        "类型：${if (editingReason?.type == "earn") "攒积分" else "花积分"}",
                        fontSize = 13.sp,
                        color = TextBrownLight
                    )
                    Spacer(Modifier.height(10.dp))
                }

                // ---- 名称 ----
                OutlinedTextField(
                    value = formName,
                    onValueChange = { formName = it.take(10) },
                    label = { Text("事项名称", fontSize = 13.sp) },
                    placeholder = { Text("例如：帮忙浇花", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))

                // ---- 自由输入开关 ----
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("选中后可自由输入文字", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextBrown)
                        Text("像「其他好事」一样，记账时再写具体内容", fontSize = 11.sp, color = TextBrownLight)
                    }
                    Switch(checked = formFreeText, onCheckedChange = { formFreeText = it })
                }
                Spacer(Modifier.height(14.dp))

                // ---- emoji 选择 ----
                Text("选择图标", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextBrown)
                Spacer(Modifier.height(8.dp))
                EmojiGrid(selected = formEmoji, onSelect = { formEmoji = it })

                Spacer(Modifier.height(16.dp))

                // ---- 保存 ----
                Surface(
                    onClick = {
                        val name = formName.trim()
                        if (name.isEmpty()) {
                            onWarn("先写一个名称哦～")
                            return@Surface
                        }
                        if (editingReason == null) {
                            onAdd(formType, formEmoji, name, formFreeText)
                        } else {
                            onUpdate(editingReason!!, formEmoji, name, formFreeText)
                        }
                        showForm = false
                    },
                    shape = RoundedCornerShape(50),
                    color = CandyPink,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "保存 ✓",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, color: androidx.compose.ui.graphics.Color) {
    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
}

@Composable
private fun ReasonRow(
    reason: ReasonEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = White,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(reason.emoji, fontSize = 24.sp)
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(reason.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextBrown)
                if (reason.freeText) {
                    Text("自由输入", fontSize = 11.sp, color = TextBrownLight)
                }
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                Text("✏️", fontSize = 16.sp)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                Text("🗑️", fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun AddButton(text: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 13.dp)
        )
    }
}

@Composable
private fun TypeChip(
    label: String,
    type: String,
    selected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) color.copy(alpha = 0.18f) else White,
        border = if (selected) BorderStroke(2.dp, color) else null,
        modifier = modifier
    ) {
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) color else TextBrown,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 11.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmojiGrid(selected: String, onSelect: (String) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        EMOJI_CHOICES.forEach { e ->
            Surface(
                onClick = { onSelect(e) },
                shape = RoundedCornerShape(12.dp),
                color = if (e == selected) CandyPink.copy(alpha = 0.25f) else White,
                border = if (e == selected) BorderStroke(2.dp, CandyPink) else null,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(e, fontSize = 20.sp)
                }
            }
        }
    }
}
