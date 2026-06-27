package com.example.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Info
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
import com.example.ui.viewmodel.ChatMessage
import com.example.ui.viewmodel.PlantDetailViewModel
import com.example.ui.viewmodel.ViewModelFactory

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plantId: String,
    isMacroTheme: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current as MainActivity
    val viewModel: PlantDetailViewModel = viewModel(
        factory = ViewModelFactory(plantRepository = context.plantRepository, geminiRepository = context.geminiRepository)
    )
    
    LaunchedEffect(plantId) {
        viewModel.loadPlant(plantId)
    }

    val plant by viewModel.plant.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()

    var chatMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plant?.commercialName ?: "Cargando...") },
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
        if (plant == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Plant Info section (scrollable)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        AsyncImage(
                            model = plant!!.imageUrl,
                            contentDescription = plant!!.commercialName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(plant!!.commercialName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text(plant!!.scientificName, style = MaterialTheme.typography.titleMedium, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text("Descripción", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(plant!!.description, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Nivel de cuidado: ${plant!!.caresLevel}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Región: ${plant!!.region}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tipo: ${plant!!.cropType.uppercase()} | Modalidad: ${plant!!.modalities}", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(16.dp))

                            val details = listOf(
                                "Cuidados requeridos" to plant!!.cares,
                                "Tratamientos específicos" to plant!!.treatment,
                                "Preparación del suelo" to plant!!.prepSuelo,
                                "Siembra" to plant!!.siembra,
                                "Riego" to plant!!.riego,
                                "Fertilización" to plant!!.fertilizacion,
                                "Manejo de malezas" to plant!!.malezas,
                                "Poda" to plant!!.poda,
                                "Control Fitosanitario" to plant!!.fitosanitario,
                                "Manejo de suelo" to plant!!.manejoSuelo,
                                "Tutorado" to plant!!.tutorado,
                                "Raleo" to plant!!.raleo
                            )

                            details.filter { it.second.isNotBlank() }.forEach { (title, content) ->
                                ExpandableSection(title, content)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Asistente Virtual IA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    items(chatHistory) { msg ->
                        ChatBubble(msg)
                    }
                    
                    if (isChatLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }

                // Chat Input box
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatMessage,
                            onValueChange = { chatMessage = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Pregunta sobre ${plant!!.commercialName}...") },
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                            enabled = !isChatLoading
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { 
                                viewModel.sendMessage(chatMessage)
                                chatMessage = "" 
                            },
                            enabled = chatMessage.isNotBlank() && !isChatLoading,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(50))
                                .padding(4.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableSection(title: String, content: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(content, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val alignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Surface(
            color = bgColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (msg.isUser) 16.dp else 0.dp,
                bottomEnd = if (msg.isUser) 0.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = msg.text,
                color = textColor,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
