package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.PlantRepository
import com.example.model.Plant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.ui.viewmodel.ChatMessage
import com.example.data.repository.GeminiRepository

class HomeViewModel(
    private val repository: PlantRepository,
    private val geminiRepository: GeminiRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory = _chatHistory.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading = _isChatLoading.asStateFlow()

    // Assuming user region is known, let's mock it for demo.
    private val userRegion = "Templado"

    val plants: StateFlow<List<Plant>> = combine(
        repository.allPlants,
        _searchQuery
    ) { all, query ->
        if (query.isBlank()) all else all.filter { 
            it.commercialName.contains(query, ignoreCase = true) || 
            it.scientificName.contains(query, ignoreCase = true) 
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recommendedPlants: StateFlow<List<Plant>> = repository.getPlantsByRegion(userRegion)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearGlobalChat() {
        _chatHistory.value = emptyList()
        geminiRepository.clearHistory()
    }

    fun sendGlobalMessage(msg: String) {
        if (msg.isBlank()) return
        
        _chatHistory.value = _chatHistory.value + ChatMessage(msg, true)
        _isChatLoading.value = true

        viewModelScope.launch {
            val currentPlants = plants.value
            val response = geminiRepository.sendGlobalMessage(msg, currentPlants)
            _chatHistory.value = _chatHistory.value + ChatMessage(response, false)
            _isChatLoading.value = false
        }
    }
}
