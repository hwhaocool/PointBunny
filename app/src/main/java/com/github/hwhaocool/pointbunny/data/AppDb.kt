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

    /** 余额 = 所有攒分之和 − 所有花分之和 */
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

@Database(
    entities = [RecordEntity::class, WishEntity::class, BadgeEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDao
    abstract fun wishDao(): WishDao
    abstract fun badgeDao(): BadgeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "point_bunny.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
