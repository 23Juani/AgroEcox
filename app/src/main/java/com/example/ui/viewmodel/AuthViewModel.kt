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

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(email: String) { _uiState.value = _uiState.value.copy(email = email) }
    fun onPasswordChange(pass: String) { _uiState.value = _uiState.value.copy(password = pass) }
    fun onFullNameChange(name: String) { _uiState.value = _uiState.value.copy(fullName = name) }
    fun onRoleChange(role: UserRole) { _uiState.value = _uiState.value.copy(role = role) }
    fun onLocationChange(loc: String) { _uiState.value = _uiState.value.copy(location = loc) }
    fun onCropTypeChange(type: CropType) { _uiState.value = _uiState.value.copy(cropType = type) }
    fun onPaymentMethodChange(method: PaymentMethod) { _uiState.value = _uiState.value.copy(paymentMethod = method) }

    fun login(onSuccess: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.login(_uiState.value.email, _uiState.value.password)
            if (result.isSuccess) {
                val isMacro = result.getOrNull()?.cropType == CropType.MACROCULTIVO
                onSuccess(isMacro)
            } else {
                _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.message ?: "Error al iniciar sesión")
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun register(onSuccess: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val user = User(
                fullName = _uiState.value.fullName,
                email = _uiState.value.email,
                role = _uiState.value.role,
                location = _uiState.value.location,
                cropType = _uiState.value.cropType,
                paymentMethod = _uiState.value.paymentMethod
            )
            val result = repository.register(user, _uiState.value.password)
            if (result.isSuccess) {
                val isMacro = user.cropType == CropType.MACROCULTIVO
                onSuccess(isMacro)
            } else {
                _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.message ?: "Error al registrarse")
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val role: UserRole = UserRole.AGRICULTOR,
    val location: String = "",
    val cropType: CropType = CropType.MICROCULTIVO,
    val paymentMethod: PaymentMethod = PaymentMethod.CARD,
    val isLoading: Boolean = false,
    val error: String? = null
)
