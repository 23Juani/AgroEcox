package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.GeminiRepository
import com.example.data.repository.PlantRepository
import com.example.model.Plant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class PlantDetailViewModel(
    private val plantRepository: PlantRepository,
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private val _plant = MutableStateFlow<Plant?>(null)
    val plant = _plant.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory = _chatHistory.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading = _isChatLoading.asStateFlow()

    fun loadPlant(plantId: String) {
        viewModelScope.launch {
            plantRepository.getPlantByIdFlow(plantId).collect { p ->
                if (_plant.value == null) {
                    geminiRepository.clearHistory()
                }
                _plant.value = p
            }
        }
    }

    fun sendMessage(msg: String) {
        if (msg.isBlank()) return
        val currentPlant = _plant.value ?: return

        _chatHistory.value = _chatHistory.value + ChatMessage(msg, true)
        _isChatLoading.value = true

        viewModelScope.launch {
            val response = geminiRepository.sendMessage(
                userMessage = msg,
                plant = currentPlant
            )
            _chatHistory.value = _chatHistory.value + ChatMessage(response, false)
            _isChatLoading.value = false
        }
    }
}

data class ChatMessage(val text: String, val isUser: Boolean)
