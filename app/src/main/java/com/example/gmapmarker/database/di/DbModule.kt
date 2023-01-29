package com.example.gmapmarker.database.di

import android.content.Context
import androidx.room.Room
import com.example.gmapmarker.dao.MarkerDataDao
import com.example.gmapmarker.database.AppDb
import com.example.gmapmarker.objects.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DbModule {
    @Singleton
    @Provides
    fun provideDb(@ApplicationContext context: Context): AppDb =
        Room.databaseBuilder(context, AppDb::class.java, Constants.DATABASE_NAME).build()

    @Provides
    fun provideMarkerDataDao(appDb: AppDb): MarkerDataDao = appDb.markerDataDao()

}