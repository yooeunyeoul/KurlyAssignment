package com.brady.kurly.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brady.kurly.data.local.entity.WishEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WishDao {

    @Query("SELECT productId FROM wishes")
    fun observeWishIds(): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM wishes WHERE productId = :productId)")
    suspend fun isWished(productId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wish: WishEntity)

    @Query("DELETE FROM wishes WHERE productId = :productId")
    suspend fun delete(productId: Long)
}
