package com.example.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.MainActivity
import com.example.model.ActividadEntity
import com.example.model.UserRole
import com.example.ui.viewmodel.CropCalendarViewModel
import com.example.ui.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropVisualCalendarScreen(
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
    val currentUserRole = viewModel.currentUser?.role ?: UserRole.AGRICULTOR

    // Calendar state
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario Visual") },
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MonthHeader(
                currentMonth = currentMonth,
                onPreviousMonth = {
                    val prev = currentMonth.clone() as Calendar
                    prev.add(Calendar.MONTH, -1)
                    currentMonth = prev
                },
                onNextMonth = {
                    val next = currentMonth.clone() as Calendar
                    next.add(Calendar.MONTH, 1)
                    currentMonth = next
                }
            )
            
            DaysOfWeekHeader()

            CalendarGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                actividades = actividades,
                onDateSelected = { date -> selectedDate = date }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            
            val selectedDateActivities = actividades.filter { 
                val actCal = Calendar.getInstance().apply { timeInMillis = it.fecha }
                actCal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                actCal.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR)
            }

            if (selectedDateActivities.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No hay actividades para este día.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Text(
                    text = "Actividades del Día",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedDateActivities) { act ->
                        ActividadCard(
                            actividad = act,
                            onToggle = { completed -> viewModel.toggleActividadCompletada(act, completed) },
                            onDelete = { viewModel.deleteActividad(act) },
                            currentUserRole = currentUserRole
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthHeader(
    currentMonth: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Mes anterior")
        }
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        Text(
            text = monthYearFormat.format(currentMonth.time).replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Mes siguiente")
        }
    }
}

@Composable
fun DaysOfWeekHeader() {
    val daysOfWeek = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
    Row(modifier = Modifier.fillMaxWidth()) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: Calendar,
    selectedDate: Calendar,
    actividades: List<ActividadEntity>,
    onDateSelected: (Calendar) -> Unit
) {
    val monthCal = currentMonth.clone() as Calendar
    monthCal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = monthCal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
    val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val daysList = mutableListOf<Calendar?>()
    for (i in 0 until firstDayOfWeek) {
        daysList.add(null)
    }
    for (i in 1..daysInMonth) {
        val cal = monthCal.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, i)
        daysList.add(cal)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    ) {
        items(daysList) { dayCal ->
            if (dayCal == null) {
                Spacer(modifier = Modifier.size(48.dp))
            } else {
                val isSelected = selectedDate.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                                 selectedDate.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)
                
                val dayActivities = actividades.filter { 
                    val actCal = Calendar.getInstance().apply { timeInMillis = it.fecha }
                    actCal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                    actCal.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)
                }

                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onDateSelected(dayCal) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dayCal.get(Calendar.DAY_OF_MONTH).toString(),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        if (dayActivities.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.Center) {
                                dayActivities.take(3).forEach { act ->
                                    val hexColor = try { android.graphics.Color.parseColor(act.color) } catch (e: Exception) { android.graphics.Color.GRAY }
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .padding(1.dp)
                                            .clip(CircleShape)
                                            .background(Color(hexColor))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
