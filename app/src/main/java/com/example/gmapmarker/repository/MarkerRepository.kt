package com.example.gmapmarker.repository

import com.example.gmapmarker.dto.MarkerDataEntity
import com.google.android.gms.maps.model.Marker
import kotlinx.coroutines.flow.Flow

interface MarkerRepository {
    fun getAll(): Flow<List<MarkerDataEntity>>
    suspend fun insert(markerDataEntity: MarkerDataEntity)
    suspend fun clearAll()
    suspend fun removeById(id: Long)
}