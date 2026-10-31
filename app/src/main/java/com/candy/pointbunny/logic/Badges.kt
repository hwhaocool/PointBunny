package com.candy.pointbunny.logic

import com.candy.pointbunny.data.RecordEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** 勋章定义（与原型 HTML 中 6 个勋章一致） */
data class BadgeDef(
    val key: String,
    val emoji: String,
    val title: String,
    val desc: String
)

object BadgeDefs {
    val ALL = listOf(
        BadgeDef("b1", "🌟", "攒分新手", "第一次攒分"),
        BadgeDef("b2", "💰", "小富翁", "积分满 100 分"),
        BadgeDef("b3", "🛍️", "第一笔消费", "第一次花积分"),
        BadgeDef("b4", "🔥", "坚持一周", "连续 7 天攒分"),
        BadgeDef("b5", "👑", "攒分大师", "累计攒满 2000 分"),
        BadgeDef("b6", "🎁", "心愿达成", "兑换一个心愿"),
    )

    private val map = ALL.associateBy { it.key }
    fun byKey(key: String): BadgeDef = map.getValue(key)
}

/** 勋章解锁判定逻辑 */
object BadgeLogic {

    private val zone: ZoneId = ZoneId.systemDefault()

    fun toLocalDate(timeMs: Long): LocalDate =
        Instant.ofEpochMilli(timeMs).atZone(zone).toLocalDate()

    /** 计算当前余额 */
    fun balanceOf(records: List<RecordEntity>): Int =
        records.sumOf { if (it.isEarn) it.points else -it.points }

    /** 累计攒分总数 */
    fun totalEarn(records: List<RecordEntity>): Int =
        records.filter { it.isEarn }.sumOf { it.points }

    /**
     * 连续攒分天数：从今天（或昨天）往前数，每天至少有一笔攒分记录。
     * 今天还没攒时从昨天起算，避免"今天刚打开 app"把连续天数清零。
     */
    fun earnStreak(records: List<RecordEntity>): Int {
        val days = records.filter { it.isEarn }
            .map { toLocalDate(it.time) }
            .toSet()
        var d = LocalDate.now()
        if (!days.contains(d)) d = d.minusDays(1)
        var streak = 0
        while (days.contains(d)) {
            streak++
            d = d.minusDays(1)
        }
        return streak
    }

    /**
     * 保存一笔新记录后检查勋章解锁。
     * @return 本次新解锁的勋章 key 列表（顺序即解锁顺序）
     */
    fun checkAfterSave(
        type: String,
        records: List<RecordEntity>,
        unlocked: Set<String>
    ): List<String> {
        val newOnes = mutableListOf<String>()
        val balance = balanceOf(records)
        val totalEarn = totalEarn(records)

        if (type == "earn" && "b1" !in unlocked) newOnes += "b1"
        if (balance >= 100 && "b2" !in unlocked) newOnes += "b2"
        if (type == "spend" && "b3" !in unlocked) newOnes += "b3"
        if (earnStreak(records) >= 7 && "b4" !in unlocked) newOnes += "b4"
        if (totalEarn >= 2000 && "b5" !in unlocked) newOnes += "b5"
        return newOnes
    }
}
