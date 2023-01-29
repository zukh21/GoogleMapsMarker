package com.example.gmapmarker.repository

import com.example.gmapmarker.dao.MarkerDataDao
import com.example.gmapmarker.dto.MarkerDataEntity
import com.google.android.gms.maps.model.Marker
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MarkerRepositoryImpl @Inject constructor(
    private val markerDataDao: MarkerDataDao,
) : MarkerRepository {
    override fun getAll(): Flow<List<MarkerDataEntity>> = markerDataDao.getAll()

    override suspend fun insert(markerDataEntity: MarkerDataEntity) {
        markerDataDao.insert(markerDataEntity)
    }

    override suspend fun clearAll() {
        markerDataDao.clearAll()
    }

    override suspend fun removeById(id: Long) {
        markerDataDao.removeById(id)
    }

}