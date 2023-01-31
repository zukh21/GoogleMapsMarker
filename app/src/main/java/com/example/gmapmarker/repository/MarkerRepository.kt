package com.example.gmapmarker.repository

import com.example.gmapmarker.dto.MarkerDataEntity
import kotlinx.coroutines.flow.Flow

interface MarkerRepository {
    fun getAll(): Flow<List<MarkerDataEntity>>
    suspend fun insert(markerDataEntity: MarkerDataEntity)
    suspend fun clearAll()
    suspend fun removeById(id: Long)
    suspend fun updateById(id: Long, title: String, lat: Double, lng: Double, description: String)
}