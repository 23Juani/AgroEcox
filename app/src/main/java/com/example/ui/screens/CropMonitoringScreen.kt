package com.example.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.MainActivity
import com.example.model.CultivoEntity
import com.example.model.Plant
import com.example.model.AgronomoEntity
import com.example.model.UserRole
import com.example.ui.viewmodel.CropMonitoringViewModel
import com.example.ui.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropMonitoringScreen(
    onBack: () -> Unit,
    onNavigateToCalendar: (String) -> Unit,
    onNavigateToVisualCalendar: (String) -> Unit
) {
    val context = LocalContext.current as MainActivity
    val currentUserId = context.authRepository.currentUser.value?.uid ?: "mock-uid"
    
    val viewModel: CropMonitoringViewModel = viewModel(
        factory = ViewModelFactory(
            cropMonitoringRepository = context.cropMonitoringRepository,
            authRepository = context.authRepository,
            plantRepository = context.plantRepository,
            uid = currentUserId
        )
    )

    val crops by viewModel.crops.collectAsState()
    val plants by viewModel.plants.collectAsState()
    val agronomos by viewModel.agronomos.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val isAgricultor = viewModel.currentUser?.role == UserRole.AGRICULTOR

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monitoreo de Cultivos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (isAgricultor) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Cultivo", tint = MaterialTheme.colorScheme.onSecondary)
                }
            }
        }
    ) { padding ->
        if (crops.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay cultivos en monitoreo aún.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(crops) { crop ->
                    val plant = plants.find { it.id == crop.id_planta }
                    val agronomo = agronomos.find { it.id_especialista == crop.id_especialista }
                    MonitoringCard(
                        crop = crop,
                        plantName = plant?.commercialName ?: "Desconocida",
                        agronomoName = agronomo?.nombre ?: "Sin asignar",
                        onDelete = { if(isAgricultor) viewModel.deleteCrop(crop) },
                        onCalendar = { onNavigateToCalendar(crop.id_cultivo) },
                        onVisualCalendar = { onNavigateToVisualCalendar(crop.id_cultivo) },
                        isAgricultor = isAgricultor
                    )
                }
            }
        }

        if (showAddDialog) {
            AddCropDialog(
                plants = plants,
                agronomos = agronomos,
                onDismiss = { showAddDialog = false },
                onAdd = { plantId, especialistaId ->
                    viewModel.addCrop(plantId, especialistaId)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun MonitoringCard(
    crop: CultivoEntity,
    plantName: String,
    agronomoName: String,
    onDelete: () -> Unit,
    onCalendar: () -> Unit,
    onVisualCalendar: () -> Unit,
    isAgricultor: Boolean
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(plantName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (isAgricultor) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Agrónomo: $agronomoName", style = MaterialTheme.typography.bodyMedium)
            
            val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(crop.fecha_inicio))
            Text("Iniciado: $dateStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Estado: ${crop.estado}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onCalendar, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.List, contentDescription = "Lista")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver Calendario de Control")
                }
                IconButton(
                    onClick = onVisualCalendar,
                    modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Calendario Visual", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCropDialog(
    plants: List<Plant>,
    agronomos: List<AgronomoEntity>,
    onDismiss: () -> Unit,
    onAdd: (String, String?) -> Unit
) {
    var selectedPlantId by remember { mutableStateOf(plants.firstOrNull()?.id ?: "") }
    var selectedAgronomoId by remember { mutableStateOf<String?>(null) }
    
    var plantExpanded by remember { mutableStateOf(false) }
    var agronomoExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Cultivo") },
        text = {
            Column {
                Text("Selecciona una Planta:")
                ExposedDropdownMenuBox(
                    expanded = plantExpanded,
                    onExpandedChange = { plantExpanded = !plantExpanded }
                ) {
                    OutlinedTextField(
                        value = plants.find { it.id == selectedPlantId }?.commercialName ?: "Seleccione planta",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = plantExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = plantExpanded,
                        onDismissRequest = { plantExpanded = false }
                    ) {
                        plants.forEach { plant ->
                            DropdownMenuItem(
                                text = { Text(plant.commercialName) },
                                onClick = {
                                    selectedPlantId = plant.id
                                    plantExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Selecciona un Especialista (Opcional):")
                ExposedDropdownMenuBox(
                    expanded = agronomoExpanded,
                    onExpandedChange = { agronomoExpanded = !agronomoExpanded }
                ) {
                    OutlinedTextField(
                        value = agronomos.find { it.id_especialista == selectedAgronomoId }?.nombre ?: "Sin asignar",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = agronomoExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = agronomoExpanded,
                        onDismissRequest = { agronomoExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ninguno") },
                            onClick = {
                                selectedAgronomoId = null
                                agronomoExpanded = false
                            }
                        )
                        agronomos.forEach { agronomo ->
                            DropdownMenuItem(
                                text = { Text(agronomo.nombre) },
                                onClick = {
                                    selectedAgronomoId = agronomo.id_especialista
                                    agronomoExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(selectedPlantId, selectedAgronomoId) },
                enabled = selectedPlantId.isNotBlank()
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

