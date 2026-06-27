package com.example.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.MainActivity
import com.example.model.CropType
import com.example.model.PaymentMethod
import com.example.ui.viewmodel.ProfileViewModel
import com.example.ui.viewmodel.ViewModelFactory

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onProfileUpdated: (Boolean) -> Unit
) {
    val context = LocalContext.current as MainActivity
    val viewModel: ProfileViewModel = viewModel(factory = ViewModelFactory(authRepository = context.authRepository))
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.logout(onNavigateToLogin) }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = uiState.fullName,
                    onValueChange = viewModel::onFullNameChange,
                    label = { Text("Nombre Completo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = {},
                    label = { Text("Correo Electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (uiState.role == com.example.model.UserRole.AGRICULTOR) {
                    OutlinedTextField(
                        value = uiState.location,
                        onValueChange = viewModel::onLocationChange,
                        label = { Text("Ubicación (Coordenadas o Dirección)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Tipo de Cultivo", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = uiState.cropType == CropType.MICROCULTIVO,
                                onClick = { viewModel.onCropTypeChange(CropType.MICROCULTIVO) }
                            )
                            Text("Microcultivos")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = uiState.cropType == CropType.MACROCULTIVO,
                                onClick = { viewModel.onCropTypeChange(CropType.MACROCULTIVO) }
                            )
                            Text("Macrocultivos")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Método de Pago", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = uiState.paymentMethod == PaymentMethod.CARD,
                                onClick = { viewModel.onPaymentMethodChange(PaymentMethod.CARD) }
                            )
                            Text("Tarjeta")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = uiState.paymentMethod == PaymentMethod.QR,
                                onClick = { viewModel.onPaymentMethodChange(PaymentMethod.QR) }
                            )
                            Text("Código QR")
                        }
                    }
                }

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
                if (uiState.successMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(uiState.successMessage!!, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.updateProfile(onProfileUpdated) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Actualizar Perfil", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
