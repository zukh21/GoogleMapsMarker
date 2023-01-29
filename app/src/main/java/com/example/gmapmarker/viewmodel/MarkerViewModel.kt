package com.example.gmapmarker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gmapmarker.dto.MarkerDataEntity
import com.example.gmapmarker.repository.MarkerRepository
import com.google.android.gms.maps.model.Marker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarkerViewModel @Inject constructor(private val repository: MarkerRepository) : ViewModel() {
    private val _markers: Flow<List<MarkerDataEntity>> = repository.getAll()
    val markers: Flow<List<MarkerDataEntity>> = _markers
    fun insert(markerDataEntity: MarkerDataEntity) = viewModelScope.launch {
        try {
            repository.insert(markerDataEntity)
        } catch (e: Exception) {
            error(e)
        }
    }


    fun clearAll() = viewModelScope.launch {
        try {
            repository.clearAll()
        } catch (e: Exception) {
            error(e)
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.removeById(id)
        } catch (e: Exception) {
            error(e)
        }
    }
}