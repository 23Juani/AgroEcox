package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.repository.AuthRepository
import com.example.data.repository.CropMonitoringRepository
import com.example.data.repository.GeminiRepository
import com.example.data.repository.PlantRepository
import com.example.ui.AgrinexusNavGraph
import com.example.ui.theme.AgrinexusTheme

class MainActivity : ComponentActivity() {

    // Simple manual DI for prototype
    lateinit var database: AppDatabase
    lateinit var plantRepository: PlantRepository
    lateinit var authRepository: AuthRepository
    lateinit var cropMonitoringRepository: CropMonitoringRepository
    lateinit var geminiRepository: GeminiRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "agrinexus-db")
            .fallbackToDestructiveMigration()
            .build()

        plantRepository = PlantRepository(database.plantDao())
        authRepository = AuthRepository(database.agrinexusDao())
        cropMonitoringRepository = CropMonitoringRepository(database.agrinexusDao())
        geminiRepository = GeminiRepository()

        lifecycleScope.launch {
            while (true) {
                plantRepository.refreshPlantsFromSheets()
                delay(10000L) // 10 seconds polling
            }
        }

        setContent {
            AgrinexusTheme {
                AgrinexusNavGraph()
            }
        }
    }
}
