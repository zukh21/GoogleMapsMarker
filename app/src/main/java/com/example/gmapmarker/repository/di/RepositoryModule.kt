package com.example.gmapmarker.repository.di

import com.example.gmapmarker.repository.MarkerRepository
import com.example.gmapmarker.repository.MarkerRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {
    @Binds
    @Singleton
    fun provideMarkerRepositoryImpl(impl: MarkerRepositoryImpl): MarkerRepository
}