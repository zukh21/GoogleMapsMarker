package com.example.gmapmarker.dao

import androidx.room.*
import com.example.gmapmarker.dto.MarkerDataEntity
import com.google.android.gms.maps.model.Marker
import kotlinx.coroutines.flow.Flow

@Dao
interface MarkerDataDao {
    @Query("SELECT * FROM MarkerDataEntity ORDER BY id DESC")
    fun getAll(): Flow<List<MarkerDataEntity>>

    @Insert
    suspend fun insert(markerDataEntity: MarkerDataEntity)

    @Query("DELETE FROM MarkerDataEntity")
    suspend fun clearAll()

    @Query("DELETE FROM MarkerDataEntity WHERE id = :id")
    suspend fun removeById(id: Long)
}