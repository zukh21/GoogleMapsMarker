package com.example.gmapmarker.interfaces

import com.example.gmapmarker.dto.MarkerDataEntity

interface ItemListener {
    fun markerItemOnClick(markerDataEntity: MarkerDataEntity)
    fun markerUpdate(markerDataEntity: MarkerDataEntity, description: String)
}