package com.example.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.MainActivity
import com.example.model.Plant
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.ViewModelFactory

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isMacro: Boolean,
    onNavigateToPlantDetail: (String) -> Unit,
    onNavigateToMonitoring: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current as MainActivity
    val viewModel: HomeViewModel = viewModel(factory = ViewModelFactory(plantRepository = context.plantRepository, geminiRepository = context.geminiRepository))
    val searchQuery by viewModel.searchQuery.collectAsState()
    val plants by viewModel.plants.collectAsState()
    val recommendedPlants by viewModel.recommendedPlants.collectAsState()

    var showGlobalChat by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AgroEcox - ${if(isMacro) "Macrocultivos" else "Microcultivos"}") },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = onNavigateToMonitoring,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Monitoreo", tint = MaterialTheme.colorScheme.onSecondary)
                }
                // Global Chatbot FAB placeholder, could navigate to a global chat screen
                FloatingActionButton(
                    onClick = { showGlobalChat = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Email, contentDescription = "Asistente Global", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                label = { Text("Buscar cultivo...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            )

            if (searchQuery.isBlank() && recommendedPlants.isNotEmpty()) {
                Text(
                    text = "Recomendados según tu Región",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(recommendedPlants) { plant ->
                        PlantCard(plant = plant, onClick = { onNavigateToPlantDetail(plant.id) })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = if (searchQuery.isBlank()) "Catálogo General" else "Resultados",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(plants) { plant ->
                    PlantListTile(plant = plant, onClick = { onNavigateToPlantDetail(plant.id) })
                }
            }
        }
    }

    if (showGlobalChat) {
        GlobalChatBottomSheet(
            viewModel = viewModel,
            onDismiss = { showGlobalChat = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalChatBottomSheet(
    viewModel: HomeViewModel,
    onDismiss: () -> Unit
) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()
    var message by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.clearGlobalChat()
            onDismiss()
        },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .fillMaxHeight(0.8f)
        ) {
            Text("Asistente Agrícola", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = false
            ) {
                items(chatHistory) { msg ->
                    com.example.ui.screens.ChatBubble(msg = msg)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (isLoading) {
                    item {
                        CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe tu duda...") },
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        viewModel.sendGlobalMessage(message)
                        message = ""
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PlantCard(plant: Plant, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = plant.imageUrl,
                contentDescription = plant.commercialName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(plant.commercialName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(plant.scientificName, style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun PlantListTile(plant: Plant, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = plant.imageUrl,
                contentDescription = plant.commercialName,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(plant.commercialName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(plant.scientificName, style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
