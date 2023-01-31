package com.example.gmapmarker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gmapmarker.dao.MarkerDataDao
import com.example.gmapmarker.dto.MarkerDataEntity
import javax.inject.Singleton

@Singleton
@Database(entities = [MarkerDataEntity::class], version = 1)
abstract class AppDb : RoomDatabase() {
    abstract fun markerDataDao(): MarkerDataDao
}