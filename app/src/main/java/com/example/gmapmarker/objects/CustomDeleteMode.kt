package com.example.gmapmarker.objects

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomDeleteMode @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {
    private val prefs = context.getSharedPreferences("delete_mode", Context.MODE_PRIVATE)
    private val deleteModeKey = "delete_mode_key"
    private val _deleteModeStateFlow = MutableStateFlow<Boolean?>(false)
    val deleteModeStateFlow: StateFlow<Boolean?> = _deleteModeStateFlow.asStateFlow()

    init {
        with(prefs.edit()) {
            putBoolean(deleteModeKey, false)
            apply()
        }
        val deleteMode = prefs.getBoolean(deleteModeKey, false)
        _deleteModeStateFlow.value = deleteMode
    }

    @Synchronized
    fun setDeleteMode(value: Boolean) {
        _deleteModeStateFlow.value = value
        with(prefs.edit()) {
            putBoolean(deleteModeKey, value)
            apply()
        }
    }
}