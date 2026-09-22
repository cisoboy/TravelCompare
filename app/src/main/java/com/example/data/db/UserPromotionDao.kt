package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPromotionDao {

    @Query("SELECT * FROM user_promotions")
    fun getAllPromotions(): Flow<List<UserPromotionEntity>>

    @Query("SELECT * FROM user_promotions WHERE provider = :provider")
    fun getPromotionsByProvider(provider: String): Flow<List<UserPromotionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(promotion: UserPromotionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(promotions: List<UserPromotionEntity>)

    @Update
    suspend fun update(promotion: UserPromotionEntity)

    @Query("UPDATE user_promotions SET isActive = :isActive WHERE id = :id")
    suspend fun setPromotionActive(id: String, isActive: Boolean)

    @Delete
    suspend fun delete(promotion: UserPromotionEntity)

    @Query("DELETE FROM user_promotions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM user_promotions WHERE provider = :provider")
    suspend fun deleteByProvider(provider: String)
}
