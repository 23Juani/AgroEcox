package com.example.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.MainActivity
import com.example.model.ActividadEntity
import com.example.model.UserRole
import com.example.ui.viewmodel.CropCalendarViewModel
import com.example.ui.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropCalendarScreen(
    cropId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current as MainActivity
    
    val viewModel: CropCalendarViewModel = viewModel(
        factory = ViewModelFactory(
            authRepository = context.authRepository,
            uid = cropId
        )
    )

    val actividades by viewModel.actividades.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val currentUserRole = viewModel.currentUser?.role ?: UserRole.AGRICULTOR

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario de Control") },
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
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Actividad", tint = MaterialTheme.colorScheme.onSecondary)
            }
        }
    ) { padding ->
        if (actividades.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay actividades registradas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(actividades) { act ->
                    ActividadCard(
                        actividad = act,
                        onToggle = { completed -> viewModel.toggleActividadCompletada(act, completed) },
                        onDelete = { viewModel.deleteActividad(act) },
                        currentUserRole = currentUserRole
                    )
                }
            }
        }

        if (showAddDialog) {
            AddActividadDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { nombre, desc, fecha, color, rol ->
                    viewModel.addActividad(nombre, desc, fecha, color, rol)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ActividadCard(
    actividad: ActividadEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    currentUserRole: UserRole
) {
    val hexColor = try { android.graphics.Color.parseColor(actividad.color) } catch (e: Exception) { android.graphics.Color.GRAY }
    val canEdit = currentUserRole == actividad.rol_responsable

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(hexColor))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(actividad.nombre_actividad, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(actividad.descripcion, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(actividad.fecha))
                Text("Fecha: $dateStr - Responsable: ${if (actividad.rol_responsable == UserRole.AGRICULTOR) "Agricultor" else "Agrónomo"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Checkbox(
                    checked = actividad.completada,
                    onCheckedChange = { onToggle(it) },
                    enabled = canEdit
                )
                if (canEdit) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActividadDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Long, String, UserRole) -> Unit
) {
    var selectedRole by remember { mutableStateOf(UserRole.AGRICULTOR) }
    var selectedNombre by remember { mutableStateOf("") }
    var isOtro by remember { mutableStateOf(false) }
    var customNombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("#4CAF50") } // Default green
    
    val funcionesAgricultor = listOf("Riego", "Fertilización / Nutrición del suelo", "Control de malezas", "Poda", "Manejo de suelo", "Raleo o aclareo", "Fumigación")
    val funcionesEspecialista = listOf("Siembra y plantación", "Evaluación de crecimiento", "Seguimiento nutricional", "Registro de plagas y enfermedades", "Evaluación del estado hídrico", "Determinación del momento óptimo de cosecha", "Clasificación de producción")
    
    val currentOptions = if (selectedRole == UserRole.AGRICULTOR) funcionesAgricultor else funcionesEspecialista
    if (selectedNombre !in currentOptions && selectedNombre != "Otro...") {
        selectedNombre = currentOptions.first()
        isOtro = false
    }

    var nombreExpanded by remember { mutableStateOf(false) }

    val colors = listOf("#4CAF50", "#2196F3", "#F44336", "#FF9800", "#9C27B0", "#00BCD4")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Actividad") },
        text = {
            Column {
                Text("Responsable:", style = MaterialTheme.typography.titleSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedRole == UserRole.AGRICULTOR, onClick = { selectedRole = UserRole.AGRICULTOR })
                        Text("Agricultor")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedRole == UserRole.ESPECIALISTA, onClick = { selectedRole = UserRole.ESPECIALISTA })
                        Text("Agrónomo")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Actividad:")
                ExposedDropdownMenuBox(
                    expanded = nombreExpanded,
                    onExpandedChange = { nombreExpanded = !nombreExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedNombre,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = nombreExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = nombreExpanded,
                        onDismissRequest = { nombreExpanded = false }
                    ) {
                        currentOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedNombre = option
                                    isOtro = false
                                    nombreExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Otro...") },
                            onClick = {
                                selectedNombre = "Otro..."
                                isOtro = true
                                nombreExpanded = false
                            }
                        )
                    }
                }
                
                if (isOtro) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customNombre,
                        onValueChange = { customNombre = it },
                        label = { Text("Nombre de la actividad") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Color Identificador:", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    colors.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(c)))
                                .clickable { color = c }
                                .padding(4.dp)
                        ) {
                            if (color == c) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val isCustomValid = if (isOtro) customNombre.isNotBlank() else true
            TextButton(
                onClick = {
                    val finalNombre = if (isOtro) customNombre else selectedNombre
                    onAdd(finalNombre, descripcion, System.currentTimeMillis(), color, selectedRole)
                },
                enabled = selectedNombre.isNotBlank() && descripcion.isNotBlank() && isCustomValid
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
