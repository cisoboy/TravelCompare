package com.example.data

import com.example.data.db.FavoriteRouteDao
import com.example.data.db.FavoriteRouteEntity
import kotlinx.coroutines.flow.Flow

class RouteRepository(private val dao: FavoriteRouteDao) {
    val allSavedRoutes: Flow<List<FavoriteRouteEntity>> = dao.getAllRoutes()

    suspend fun saveRoute(route: FavoriteRouteEntity): Long {
        return dao.insertRoute(route)
    }

    suspend fun deleteRoute(id: Long) {
        dao.deleteRouteById(id)
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }
}
