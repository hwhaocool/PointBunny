package com.candy.pointbunny.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 一条积分记录（攒分 earn / 花分 spend）
 * 与原型 HTML 中的 record 数据结构保持一致
 */
@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val time: Long,        // 时间戳（毫秒）
    val points: Int,       // 积分数量（永远为正数，方向由 type 决定）
    val reason: String,    // 原因 / 事项文字
    val emoji: String,     // 对应的 emoji 图标
    val type: String       // "earn" = 攒分, "spend" = 花分
) {
    val isEarn: Boolean get() = type == "earn"
}

/**
 * 一个心愿（例如：恐龙绘本 60 分）
 */
@Entity(tableName = "wishes")
data class WishEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val emoji: String,
    val name: String,
    val target: Int,            // 目标积分
    val done: Boolean = false,  // 是否已兑换
    val doneAt: Long? = null    // 兑换时间
)

/**
 * 已解锁的成就勋章（key 对应 BadgeDefs 中的 b1~b6）
 */
@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey val key: String,
    val unlockedAt: Long
)
