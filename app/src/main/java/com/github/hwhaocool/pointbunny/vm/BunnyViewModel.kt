package com.github.hwhaocool.pointbunny.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.hwhaocool.pointbunny.data.AppDatabase
import com.github.hwhaocool.pointbunny.data.BadgeEntity
import com.github.hwhaocool.pointbunny.data.RecordEntity
import com.github.hwhaocool.pointbunny.data.WishEntity
import com.github.hwhaocool.pointbunny.logic.BadgeDefs
import com.github.hwhaocool.pointbunny.logic.BadgeLogic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 全局唯一 ViewModel：持有记录 / 心愿 / 勋章 / 余额，
 * 并提供保存、删除、兑换、导出 JSON、清空等操作。
 */
class BunnyViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.get(app)
    private val recordDao = db.recordDao()
    private val wishDao = db.wishDao()
    private val badgeDao = db.badgeDao()

    private val isoFmt: DateTimeFormatter =
        DateTimeFormatter.ISO_OFFSET_DATE_TIME

    // ---------- 数据流 ----------

    val records: StateFlow<List<RecordEntity>> = recordDao.flowAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val wishes: StateFlow<List<WishEntity>> = wishDao.flowAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val balance: StateFlow<Int> = recordDao.flowBalance()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val unlockedBadges: StateFlow<Set<String>> = badgeDao.flowAll()
        .map { list -> list.map { it.key }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val badgeUnlockTimes: StateFlow<Map<String, Long>> = badgeDao.flowAll()
        .map { list -> list.associate { it.key to it.unlockedAt } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    // ---------- 全局 Toast 通道 ----------

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast
    fun showToast(msg: String) { _toast.value = msg }
    fun consumeToast() { _toast.value = null }

    // ---------- 记录操作 ----------

    /** 保存一笔攒分/花分记录，同时检查勋章解锁 */
    fun addRecord(type: String, points: Int, reason: String, emoji: String) {
        viewModelScope.launch {
            recordDao.insert(
                RecordEntity(
                    time = System.currentTimeMillis(),
                    points = points,
                    reason = reason,
                    emoji = emoji,
                    type = type
                )
            )
            val all = recordDao.allOnce()
            val unlocked = badgeDao.allKeys().toSet()
            val newOnes = BadgeLogic.checkAfterSave(type, all, unlocked)
            if (newOnes.isNotEmpty()) {
                val now = System.currentTimeMillis()
                newOnes.forEach { key -> badgeDao.insert(BadgeEntity(key, now)) }
                val names = newOnes.joinToString("、") { BadgeDefs.byKey(it).title }
                _toast.value = "🎉 获得新勋章：$names"
            } else {
                _toast.value =
                    if (type == "earn") "✅ 攒到 $points 分，你真棒！"
                    else "花掉了 $points 分，继续加油～"
            }
        }
    }

    /** 删除一条历史记录（余额随记录自动重算） */
    fun deleteRecord(r: RecordEntity) {
        viewModelScope.launch {
            recordDao.delete(r.id)
            _toast.value = "已删除这条记录"
        }
    }

    /** 编辑一条历史记录（积分/原因/emoji 可改，余额自动重算） */
    fun updateRecord(record: RecordEntity, points: Int, reason: String, emoji: String) {
        viewModelScope.launch {
            recordDao.update(
                record.copy(points = points, reason = reason, emoji = emoji)
            )
            _toast.value = "✅ 已更新这条记录"
        }
    }

    // ---------- 心愿操作 ----------

    fun addWish(emoji: String, name: String, target: Int) {
        viewModelScope.launch {
            wishDao.insert(WishEntity(emoji = emoji, name = name, target = target))
            _toast.value = "🌟 新心愿「$name」已添加！"
        }
    }

    /** 尝试兑换心愿：生成一笔花分记录 + 标记心愿完成 + 解锁 b6 */
    fun redeemWish(w: WishEntity) {
        viewModelScope.launch {
            if (balance.value < w.target) {
                _toast.value = "积分不够啦，先和爸爸妈妈一起攒一攒吧😢"
                return@launch
            }
            val now = System.currentTimeMillis()
            recordDao.insert(
                RecordEntity(
                    time = now,
                    points = w.target,
                    reason = "兑换心愿·${w.name}",
                    emoji = w.emoji,
                    type = "spend"
                )
            )
            wishDao.update(w.copy(done = true, doneAt = now))
            if ("b6" !in badgeDao.allKeys().toSet()) {
                badgeDao.insert(BadgeEntity("b6", now))
                _toast.value = "🎉 心愿兑换成功！获得新勋章：心愿达成"
            } else {
                _toast.value = "🎁 心愿「${w.name}」兑换成功！"
            }
        }
    }

    fun deleteWish(w: WishEntity) {
        viewModelScope.launch {
            // 通过先删后插实现（Room 无直接按主键删除的 Update 实体接口时，用全量重建最简单）
            val rest = wishes.value.filter { it.id != w.id }
            wishDao.deleteAll()
            rest.forEach { wishDao.insert(it) }
            _toast.value = "心愿「${w.name}」已删除"
        }
    }

    // ---------- 家长专区 ----------

    /** 生成与原型 HTML 一致格式的 JSON */
    fun buildExportJson(): String {
        val o = JSONObject()
        o.put("app", "积分小兔")
        o.put("version", 1)
        o.put("exportedAt", Instant.now().atZone(ZoneId.systemDefault()).format(isoFmt))
        o.put("balance", balance.value)

        val arr = JSONArray()
        records.value.sortedByDescending { it.time }.forEach { r ->
            arr.put(
                JSONObject()
                    .put("type", r.type)
                    .put("points", r.points)
                    .put("reason", r.reason)
                    .put("emoji", r.emoji)
                    .put("time", Instant.ofEpochMilli(r.time).atZone(ZoneId.systemDefault()).format(isoFmt))
            )
        }
        o.put("records", arr)

        val warr = JSONArray()
        wishes.value.forEach { w ->
            warr.put(
                JSONObject()
                    .put("emoji", w.emoji)
                    .put("name", w.name)
                    .put("target", w.target)
                    .put("redeemed", w.done)
            )
        }
        o.put("wishes", warr)
        return o.toString(2)
    }

    /** 清空全部数据（调用前 UI 已做双重确认） */
    fun clearAllData() {
        viewModelScope.launch {
            recordDao.deleteAll()
            wishDao.deleteAll()
            badgeDao.deleteAll()
            _toast.value = "数据已全部清空"
        }
    }

    /**
     * 导入 JSON（增量合并）：解析记录 + 心愿，追加到现有数据，不覆盖已有内容；
     * 导入后根据全部数据重算并补发未解锁的勋章。与 buildExportJson 的格式一一对应。
     */
    fun importData(jsonText: String) {
        viewModelScope.launch {
            try {
                val obj = JSONObject(jsonText)
                val recordsArr = obj.optJSONArray("records") ?: JSONArray()
                val wishesArr = obj.optJSONArray("wishes") ?: JSONArray()

                val importedRecords = mutableListOf<RecordEntity>()
                for (i in 0 until recordsArr.length()) {
                    val r = recordsArr.getJSONObject(i)
                    val type = r.optString("type", "earn")
                        .let { if (it == "spend") "spend" else "earn" }
                    val time = runCatching {
                        OffsetDateTime.parse(r.optString("time", ""))
                            .toInstant().toEpochMilli()
                    }.getOrDefault(System.currentTimeMillis())
                    importedRecords += RecordEntity(
                        time = time,
                        points = r.optInt("points", 0).coerceAtLeast(0),
                        reason = r.optString("reason", ""),
                        emoji = r.optString("emoji", "⭐"),
                        type = type
                    )
                }

                val importedWishes = mutableListOf<WishEntity>()
                for (i in 0 until wishesArr.length()) {
                    val w = wishesArr.getJSONObject(i)
                    val done = w.optBoolean("redeemed", false)
                    importedWishes += WishEntity(
                        emoji = w.optString("emoji", "🎁"),
                        name = w.optString("name", ""),
                        target = w.optInt("target", 0).coerceAtLeast(0),
                        done = done,
                        doneAt = if (done) System.currentTimeMillis() else null
                    )
                }

                // 增量合并：追加导入，不清空已有数据
                recordDao.insertAll(importedRecords)
                wishDao.insertAll(importedWishes)

                // 根据全部数据重算勋章，只补发未解锁的
                val allRecords = recordDao.allOnce()
                val allWishes = wishDao.allOnce()
                val existingKeys = badgeDao.allKeys().toSet()
                val now = System.currentTimeMillis()
                val toUnlock = mutableListOf<BadgeEntity>()
                fun unlock(key: String) {
                    if (key !in existingKeys && toUnlock.none { it.key == key }) {
                        toUnlock += BadgeEntity(key, now)
                    }
                }
                if (allRecords.any { it.isEarn }) unlock("b1")
                if (BadgeLogic.balanceOf(allRecords) >= 100) unlock("b2")
                if (allRecords.any { !it.isEarn }) unlock("b3")
                if (BadgeLogic.earnStreak(allRecords) >= 7) unlock("b4")
                if (BadgeLogic.totalEarn(allRecords) >= 2000) unlock("b5")
                if (allWishes.any { it.done }) unlock("b6")
                if (toUnlock.isNotEmpty()) badgeDao.insertAll(toUnlock)

                _toast.value = "✅ 导入成功：新增 ${importedRecords.size} 条记录、${importedWishes.size} 个心愿"
            } catch (e: Exception) {
                _toast.value = "导入失败：JSON 格式不对哦"
            }
        }
    }
}
