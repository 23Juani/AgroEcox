package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.repository.AuthRepository
import com.example.data.repository.CropMonitoringRepository
import com.example.data.repository.GeminiRepository
import com.example.data.repository.PlantRepository

class ViewModelFactory(
    private val authRepository: AuthRepository? = null,
    private val plantRepository: PlantRepository? = null,
    private val geminiRepository: GeminiRepository? = null,
    private val cropMonitoringRepository: CropMonitoringRepository? = null,
    private val uid: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository!!) as T
        }
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(authRepository!!) as T
        }
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(plantRepository!!, geminiRepository!!) as T
        }
        if (modelClass.isAssignableFrom(PlantDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlantDetailViewModel(plantRepository!!, geminiRepository!!) as T
        }
        if (modelClass.isAssignableFrom(CropMonitoringViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CropMonitoringViewModel(cropMonitoringRepository!!, authRepository!!, plantRepository!!) as T
        }
        if (modelClass.isAssignableFrom(CropCalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CropCalendarViewModel(
                (authRepository as AuthRepository).javaClass.getDeclaredField("dao").apply { isAccessible = true }.get(authRepository) as com.example.data.local.AgrinexusDao,
                authRepository, 
                uid ?: ""
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
