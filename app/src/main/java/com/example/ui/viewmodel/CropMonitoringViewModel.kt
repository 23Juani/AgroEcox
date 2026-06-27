package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import com.example.data.repository.CropMonitoringRepository
import com.example.data.repository.PlantRepository
import com.example.model.AgronomoEntity
import com.example.model.CultivoEntity
import com.example.model.Plant
import com.example.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

class CropMonitoringViewModel(
    private val repository: CropMonitoringRepository,
    private val authRepository: AuthRepository,
    private val plantRepository: PlantRepository
) : ViewModel() {
    
    private val _crops = MutableStateFlow<List<CultivoEntity>>(emptyList())
    val crops = _crops.asStateFlow()

    val agronomos: StateFlow<List<AgronomoEntity>> = repository.getAllAgronomos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val plants: StateFlow<List<Plant>> = plantRepository.allPlants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser = authRepository.currentUser.value

    init {
        viewModelScope.launch {
            if (currentUser != null) {
                if (currentUser.role == UserRole.AGRICULTOR) {
                    repository.getMonitoredCropsForUser(currentUser.uid).collectLatest {
                        _crops.value = it
                    }
                } else {
                    repository.getMonitoredCropsForEspecialista(currentUser.uid).collectLatest {
                        _crops.value = it
                    }
                }
            }
        }
    }

    fun addCrop(plantId: String, especialistaId: String?) {
        viewModelScope.launch {
            if (currentUser != null && currentUser.role == UserRole.AGRICULTOR) {
                repository.insertCrop(currentUser.uid, especialistaId, plantId)
            }
        }
    }

    fun deleteCrop(crop: CultivoEntity) {
        viewModelScope.launch {
            repository.deleteCrop(crop)
        }
    }
}
