package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import com.example.model.CropType
import com.example.model.PaymentMethod
import com.example.model.User
import com.example.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: AuthRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val currentUser = repository.currentUser.value
        if (currentUser != null) {
            _uiState.value = _uiState.value.copy(
                fullName = currentUser.fullName,
                email = currentUser.email, // normally read-only in edit
                role = currentUser.role,
                location = currentUser.location,
                cropType = currentUser.cropType,
                paymentMethod = currentUser.paymentMethod
            )
        }
    }

    fun onFullNameChange(name: String) { _uiState.value = _uiState.value.copy(fullName = name) }
    fun onLocationChange(loc: String) { _uiState.value = _uiState.value.copy(location = loc) }
    fun onCropTypeChange(type: CropType) { _uiState.value = _uiState.value.copy(cropType = type) }
    fun onPaymentMethodChange(method: PaymentMethod) { _uiState.value = _uiState.value.copy(paymentMethod = method) }

    fun updateProfile(onSuccess: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            val currentUser = repository.currentUser.value
            if (currentUser != null) {
                val updatedUser = currentUser.copy(
                    fullName = _uiState.value.fullName,
                    location = _uiState.value.location,
                    cropType = _uiState.value.cropType,
                    paymentMethod = _uiState.value.paymentMethod
                )
                val result = repository.updateProfile(updatedUser)
                if (result.isSuccess) {
                    val isMacro = updatedUser.cropType == CropType.MACROCULTIVO
                    _uiState.value = _uiState.value.copy(successMessage = "Perfil actualizado")
                    onSuccess(isMacro)
                } else {
                    _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.message ?: "Error al actualizar perfil")
                }
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun logout(onLogout: () -> Unit) {
        repository.logout()
        onLogout()
    }
}

data class ProfileUiState(
    val fullName: String = "",
    val email: String = "",
    val role: UserRole = UserRole.AGRICULTOR,
    val location: String = "",
    val cropType: CropType = CropType.MICROCULTIVO,
    val paymentMethod: PaymentMethod = PaymentMethod.CARD,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
