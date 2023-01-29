package com.example.gmapmarker.dto

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity
data class MarkerDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val title: String? = null,
    val lat: Double,
    val lng: Double,
    val description: String? = null,

    )