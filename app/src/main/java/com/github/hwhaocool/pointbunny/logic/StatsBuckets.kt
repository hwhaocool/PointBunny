package com.github.hwhaocool.pointbunny.logic

import com.github.hwhaocool.pointbunny.data.RecordEntity
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

/** 统计模式（与原型一致：本周 / 本月 / 本年 / 选日期） */
enum class StatsMode(val label: String) {
    WEEK("本周"), MONTH("本月"), YEAR("本年"), CUSTOM("选日期")
}

/** 柱状图的一个分桶：标签 + 该区间内攒分/花分总和 */
data class StatBucket(val label: String, val earn: Int, val spend: Int)

/** 最爱攒分原因条目（Top 榜） */
data class ReasonStat(val emoji: String, val reason: String, val total: Int)

/** 统计分桶逻辑 */
object StatsLogic {

    private val zone: ZoneId = ZoneId.systemDefault()

    fun toLocalDate(timeMs: Long): LocalDate =
        Instant.ofEpochMilli(timeMs).atZone(zone).toLocalDate()

    private val weekNames = arrayOf("一", "二", "三", "四", "五", "六", "日")

    /** 当前模式对应的数据起止日期（含首尾） */
    fun rangeOf(
        mode: StatsMode,
        today: LocalDate = LocalDate.now(),
        customStart: LocalDate? = null,
        customEnd: LocalDate? = null
    ): Pair<LocalDate, LocalDate> = when (mode) {
        StatsMode.WEEK -> {
            val mon = today.with(DayOfWeek.MONDAY)
            mon to mon.plusDays(6)
        }
        StatsMode.MONTH -> today.withDayOfMonth(1) to today.withDayOfMonth(today.lengthOfMonth())
        StatsMode.YEAR -> today.withDayOfYear(1) to today.withDayOfYear(today.lengthOfYear())
        StatsMode.CUSTOM -> (customStart ?: today.minusDays(6)) to (customEnd ?: today)
    }

    /**
     * 按模式把时间范围切成若干桶：
     *  - 本周：按天，周一 ~ 周日，7 桶
     *  - 本月：按周，第 1~5 周（1-7 / 8-14 / 15-21 / 22-28 / 29-月末）
     *  - 本年：按月，1~12 桶
     *  - 自定义：≤14 天按天；超过 14 天按月
     */
    fun buckets(
        mode: StatsMode,
        records: List<RecordEntity>,
        today: LocalDate = LocalDate.now(),
        customStart: LocalDate? = null,
        customEnd: LocalDate? = null
    ): List<StatBucket> {
        data class Acc(val label: String, val start: LocalDate, val end: LocalDate) {
            var earn = 0
            var spend = 0
        }

        val accs: List<Acc> = when (mode) {
            StatsMode.WEEK -> {
                val mon = today.with(DayOfWeek.MONDAY)
                (0..6).map { i ->
                    val d = mon.plusDays(i.toLong())
                    Acc("周${weekNames[i]}", d, d)
                }
            }
            StatsMode.MONTH -> {
                val len = today.lengthOfMonth()
                val n = ceil(len / 7.0).toInt()
                (0 until n).map { i ->
                    val s = today.withDayOfMonth(i * 7 + 1)
                    val e = minOf(s.plusDays(6), today.withDayOfMonth(len))
                    Acc("第${i + 1}周", s, e)
                }
            }
            StatsMode.YEAR -> {
                val jan1 = today.withDayOfYear(1)
                (0..11).map { i ->
                    val first = jan1.plusMonths(i.toLong())
                    Acc("${first.monthValue}月",
                        first.withDayOfMonth(1),
                        first.withDayOfMonth(first.lengthOfMonth()))
                }
            }
            StatsMode.CUSTOM -> {
                val s = customStart ?: today.minusDays(6)
                val e = customEnd ?: today
                val realStart = if (s.isAfter(e)) e else s
                val realEnd = if (s.isAfter(e)) s else e
                val days = ChronoUnit.DAYS.between(realStart, realEnd) + 1
                if (days <= 14) {
                    (0 until days.toInt()).map { i ->
                        val d = realStart.plusDays(i.toLong())
                        Acc("${d.monthValue}/${d.dayOfMonth}", d, d)
                    }
                } else {
                    val list = mutableListOf<Acc>()
                    var cur = realStart.withDayOfMonth(1)
                    while (!cur.isAfter(realEnd)) {
                        val mStart = maxOf(cur, realStart)
                        val mEnd = minOf(cur.withDayOfMonth(cur.lengthOfMonth()), realEnd)
                        list += Acc("${cur.monthValue}月", mStart, mEnd)
                        cur = cur.plusMonths(1)
                    }
                    list
                }
            }
        }

        // 把记录归入桶（单次遍历，桶数量少，直接双循环即可）
        records.forEach { r ->
            val d = toLocalDate(r.time)
            accs.forEach { a ->
                if (!d.isBefore(a.start) && !d.isAfter(a.end)) {
                    if (r.isEarn) a.earn += r.points else a.spend += r.points
                }
            }
        }
        return accs.map { StatBucket(it.label, it.earn, it.spend) }
    }

    /** 区间内攒分 / 花分总和 */
    fun totalsIn(
        start: LocalDate,
        end: LocalDate,
        records: List<RecordEntity>
    ): Pair<Int, Int> {
        var earn = 0
        var spend = 0
        records.forEach { r ->
            val d = toLocalDate(r.time)
            if (!d.isBefore(start) && !d.isAfter(end)) {
                if (r.isEarn) earn += r.points else spend += r.points
            }
        }
        return earn to spend
    }

    /** 区间内最爱攒分原因 Top N（按积分总和排序） */
    fun topEarnReasons(
        start: LocalDate,
        end: LocalDate,
        records: List<RecordEntity>,
        top: Int = 3
    ): List<ReasonStat> =
        records.filter { it.isEarn }
            .map { it to toLocalDate(it.time) }
            .filter { (_, d) -> !d.isBefore(start) && !d.isAfter(end) }
            .groupBy { it.first.reason }
            .map { (_, list) ->
                ReasonStat(
                    emoji = list.first().first.emoji,
                    reason = list.first().first.reason,
                    total = list.sumOf { it.first.points }
                )
            }
            .sortedByDescending { it.total }
            .take(top)
}
