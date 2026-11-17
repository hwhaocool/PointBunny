package com.github.hwhaocool.pointbunny.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {

    @Query("SELECT * FROM records ORDER BY time DESC, id DESC")
    fun flowAll(): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records ORDER BY time DESC, id DESC")
    suspend fun allOnce(): List<RecordEntity>

    @Insert
    suspend fun insert(r: RecordEntity): Long

    @Insert
    suspend fun insertAll(records: List<RecordEntity>)

    @Update
    suspend fun update(r: RecordEntity)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM records")
    suspend fun deleteAll()

    /** 余额 = 所有攒分之和 − 所有花分之和（单位 0.1 分） */
    @Query(
        "SELECT COALESCE(SUM(CASE WHEN type='earn' THEN points ELSE -points END), 0) FROM records"
    )
    fun flowBalance(): Flow<Int>
}

@Dao
interface WishDao {

    @Query("SELECT * FROM wishes ORDER BY done ASC, id DESC")
    fun flowAll(): Flow<List<WishEntity>>

    @Query("SELECT * FROM wishes")
    suspend fun allOnce(): List<WishEntity>

    @Insert
    suspend fun insert(w: WishEntity): Long

    @Insert
    suspend fun insertAll(wishes: List<WishEntity>)

    @Update
    suspend fun update(w: WishEntity)

    @Query("DELETE FROM wishes")
    suspend fun deleteAll()
}

@Dao
interface BadgeDao {

    @Query("SELECT * FROM badges ORDER BY unlockedAt ASC")
    fun flowAll(): Flow<List<BadgeEntity>>

    @Query("SELECT `key` FROM badges")
    suspend fun allKeys(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(b: BadgeEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(badges: List<BadgeEntity>)

    @Query("DELETE FROM badges")
    suspend fun deleteAll()
}

@Dao
interface ReasonDao {

    @Query("SELECT * FROM reasons WHERE type = :type ORDER BY sortOrder ASC, id ASC")
    fun flowByType(type: String): Flow<List<ReasonEntity>>

    @Query("SELECT * FROM reasons WHERE type = :type ORDER BY sortOrder ASC, id ASC")
    suspend fun allByType(type: String): List<ReasonEntity>

    @Query("SELECT COUNT(*) FROM reasons")
    suspend fun count(): Int

    @Insert
    suspend fun insert(r: ReasonEntity): Long

    @Insert
    suspend fun insertAll(rs: List<ReasonEntity>)

    @Update
    suspend fun update(r: ReasonEntity)

    @Query("DELETE FROM reasons WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM reasons")
    suspend fun deleteAll()
}

@Database(
    entities = [RecordEntity::class, WishEntity::class, BadgeEntity::class, ReasonEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDao
    abstract fun wishDao(): WishDao
    abstract fun badgeDao(): BadgeDao
    abstract fun reasonDao(): ReasonDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** v1 -> v2：新增事项表 reasons */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `reasons` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`type` TEXT NOT NULL, " +
                        "`emoji` TEXT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`freeText` INTEGER NOT NULL, " +
                        "`sortOrder` INTEGER NOT NULL)"
                )
            }
        }

        /**
         * v2 -> v3：积分精度由整数分改为 0.1 分。
         * 表结构不变（points / target 仍是 INTEGER），只把历史数据统一 ×10。
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE `records` SET `points` = `points` * 10")
                db.execSQL("UPDATE `wishes` SET `target` = `target` * 10")
            }
        }

        /** v3 -> v4：事项表新增「默认积分」列（单位 0.1 分，0 表示不预设） */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `reasons` ADD COLUMN `defaultPoints` INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "point_bunny.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
