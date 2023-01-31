package com.example.gmapmarker.repository

import com.example.gmapmarker.dao.MarkerDataDao
import com.example.gmapmarker.dto.MarkerDataEntity
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

    override suspend fun updateById(id: Long, title: String, lat: Double, lng: Double, description: String) {
        markerDataDao.updateById(id, title, lat, lng, description)
    }

}