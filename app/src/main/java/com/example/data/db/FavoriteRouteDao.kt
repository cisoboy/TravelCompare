package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteRouteDao {
    @Query("SELECT * FROM favorite_routes ORDER BY isFavorite DESC, timestamp DESC")
    fun getAllRoutes(): Flow<List<FavoriteRouteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: FavoriteRouteEntity): Long

    @Query("DELETE FROM favorite_routes WHERE id = :id")
    suspend fun deleteRouteById(id: Long)

    @Query("DELETE FROM favorite_routes WHERE isFavorite = 0")
    suspend fun clearHistory()
}
