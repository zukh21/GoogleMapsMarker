package com.example.gmapmarker.dao

import androidx.room.*
import com.example.gmapmarker.dto.MarkerDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarkerDataDao {
    @Query("SELECT * FROM MarkerDataEntity ORDER BY id DESC")
    fun getAll(): Flow<List<MarkerDataEntity>>

    @Insert
    suspend fun insert(markerDataEntity: MarkerDataEntity)

    @Query("DELETE FROM MarkerDataEntity")
    suspend fun clearAll()

    @Query("UPDATE MarkerDataEntity SET title = :title, lat = :lat, lng = :lng, description = :description WHERE id = :id")
    suspend fun updateById(id: Long, title: String, lat: Double, lng: Double, description: String)

    @Query("DELETE FROM MarkerDataEntity WHERE id = :id")
    suspend fun removeById(id: Long)
}