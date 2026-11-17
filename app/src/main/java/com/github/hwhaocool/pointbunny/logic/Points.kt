package com.github.hwhaocool.pointbunny.logic

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 积分精度工具：最小单位是 0.1 分。
 *
 * 数据库和内存里统一用 Int 保存「0.1 分」的个数（例如 1 表示 0.1 分，15 表示 1.5 分），
 * 不直接使用 Double/Float，避免 0.1 + 0.2 = 0.30000000000000004 这类浮点误差
 * 导致余额比较（如 `balance >= 心愿目标`）出错。
 */
object Points {

    /** 1 分 = 10 个最小单位（0.1 分） */
    const val SCALE = 10

    /** 整数分 -> 内部单位：100 -> 1000（即 100.0 分） */
    fun ofWhole(points: Int): Int = points * SCALE

    /** 小数值（如 JSON 导入的 1.5）-> 内部单位，按 0.1 精度四舍五入 */
    fun fromDecimal(value: Double): Int = (value * SCALE).roundToInt()

    /** 内部单位 -> 小数值（仅用于导出 JSON） */
    fun toDecimal(tenths: Int): Double = tenths.toDouble() / SCALE

    /** 内部单位 -> 展示文本，整数省略小数位：125 -> "12.5"，120 -> "12" */
    fun format(tenths: Int): String {
        val sign = if (tenths < 0) "-" else ""
        val v = abs(tenths)
        val whole = v / SCALE
        val frac = v % SCALE
        return if (frac == 0) "$sign$whole" else "$sign$whole.$frac"
    }

    /**
     * 解析用户输入（最多一位小数），返回内部单位。
     * 允许 "12"、"12."、".5"、"0.5" 这类输入；空串或非法字符返回 null。
     */
    fun parse(text: String): Int? {
        val t = text.trim()
        if (t.isEmpty()) return null
        if (!Regex("^\\d{0,5}(\\.\\d?)?$").matches(t)) return null
        val dot = t.indexOf('.')
        val wholeText = (if (dot >= 0) t.substring(0, dot) else t).ifEmpty { "0" }
        val whole = wholeText.toIntOrNull() ?: return null
        val fracText = if (dot >= 0) t.substring(dot + 1) else ""
        val frac = fracText.firstOrNull()?.digitToInt() ?: 0
        return whole * SCALE + frac
    }

    /** 小键盘输入一位数字（整数最多 5 位、小数最多 1 位） */
    fun appendDigit(current: String, digit: Int): String {
        val dot = current.indexOf('.')
        if (dot >= 0) {
            // 小数位已经有数字了就不再追加
            if (current.length - dot - 1 >= 1) return current
            return current + digit
        }
        if (current.length >= 5) return current
        return if (current == "0") digit.toString() else current + digit
    }

    /** 小键盘输入小数点（只允许一个小数点） */
    fun appendDot(current: String): String = when {
        current.contains('.') -> current
        current.isEmpty() -> "0."
        else -> "$current."
    }

    /** 清理系统输入框里的文本：只保留数字和一个小数点，整数最多 5 位、小数最多 1 位 */
    fun sanitizeInput(text: String): String {
        val t = text.trim()
        if (t.isEmpty()) return ""
        val dot = t.indexOf('.')
        val whole = (if (dot >= 0) t.substring(0, dot) else t)
            .filter { it.isDigit() }
            .take(5)
        if (dot < 0) return whole
        val frac = t.substring(dot + 1)
            .filter { it.isDigit() }
            .take(1)
        return "$whole.$frac"
    }
}
