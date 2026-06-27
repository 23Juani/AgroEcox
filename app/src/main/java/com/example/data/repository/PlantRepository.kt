package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.PlantDao
import com.example.data.network.RetrofitClient
import com.example.model.Plant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class PlantRepository(private val plantDao: PlantDao) {
    
    val allPlants: Flow<List<Plant>> = plantDao.getAllPlants()

    fun getPlantsByRegion(region: String): Flow<List<Plant>> = plantDao.getPlantsByRegion(region)

    fun searchPlants(query: String): Flow<List<Plant>> = plantDao.searchPlants(query)

    fun getPlantByIdFlow(id: String): Flow<Plant?> = plantDao.getAllPlants().map { list -> list.find { it.id == id } }

    suspend fun refreshPlantsFromSheets() {
        try {
            val apiKey = BuildConfig.GOOGLE_SHEETS_API_KEY
            val sheetId = BuildConfig.GOOGLE_SHEETS_ID
            
            if (apiKey.isNotEmpty() && apiKey != "YOUR_SHEETS_API_KEY" && sheetId.isNotEmpty() && sheetId != "YOUR_GOOGLE_SHEETS_ID") {
                // Fetch from the exact columns the user described
                val response = RetrofitClient.sheetsService.getSheetValues(sheetId, "Sheet1!A2:U", apiKey)
                
                val plants = response.values?.mapNotNull { row ->
                    val idStr = row.getOrNull(0) ?: return@mapNotNull null
                    Plant(
                        id = idStr,
                        imageUrl = row.getOrNull(1) ?: "",
                        commercialName = row.getOrNull(2) ?: "",
                        scientificName = row.getOrNull(3) ?: "",
                        description = row.getOrNull(4) ?: "",
                        caresLevel = row.getOrNull(5) ?: "",
                        cares = row.getOrNull(6) ?: "",
                        treatment = row.getOrNull(7) ?: "",
                        prepSuelo = row.getOrNull(8) ?: "",
                        siembra = row.getOrNull(9) ?: "",
                        riego = row.getOrNull(10) ?: "",
                        fertilizacion = row.getOrNull(11) ?: "",
                        malezas = row.getOrNull(12) ?: "",
                        poda = row.getOrNull(13) ?: "",
                        fitosanitario = row.getOrNull(14) ?: "",
                        manejoSuelo = row.getOrNull(15) ?: "",
                        tutorado = row.getOrNull(16) ?: "",
                        raleo = row.getOrNull(17) ?: "",
                        cropType = row.getOrNull(18) ?: "",
                        region = row.getOrNull(19) ?: "",
                        modalities = row.getOrNull(20) ?: ""
                    )
                } ?: emptyList()

                if (plants.isNotEmpty()) {
                    plantDao.clearAll()
                    plantDao.insertAll(plants)
                }
            } else {
                insertMockDataIfEmpty()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            insertMockDataIfEmpty()
        }
    }

    private suspend fun insertMockDataIfEmpty() {
        val current = plantDao.getAllPlants().firstOrNull()
        if (current.isNullOrEmpty()) {
            val mockData = listOf(
                Plant(
                    id = "1",
                    imageUrl = "https://images.unsplash.com/photo-1595856754026-613d94101eb6?w=400",
                    commercialName = "Tomate Cherry",
                    scientificName = "Solanum lycopersicum",
                    description = "Planta pequeña que produce tomates pequeños y dulces.",
                    caresLevel = "Medio",
                    cares = "Riego diario, exposición al sol moderada.",
                    treatment = "Fertilizante orgánico cada 15 días.",
                    prepSuelo = "Suelo suelto con buen drenaje.",
                    siembra = "A 1cm de profundidad.",
                    riego = "Diario, sin encharcar.",
                    fertilizacion = "Rica en potasio.",
                    malezas = "Control manual semanal.",
                    poda = "Eliminar chupones axilares.",
                    fitosanitario = "Preventivo contra hongos.",
                    manejoSuelo = "Acolchado recomendado.",
                    tutorado = "Necesario, estacas de 1.5m.",
                    raleo = "No necesario.",
                    cropType = "micro",
                    region = "Templado",
                    modalities = "micro"
                ),
                Plant(
                    id = "2",
                    imageUrl = "https://images.unsplash.com/photo-1601493700631-2b16ec4b4716?w=400",
                    commercialName = "Maíz",
                    scientificName = "Zea mays",
                    description = "Cultivo principal para consumo humano y animal.",
                    caresLevel = "Bajo",
                    cares = "Riego abundante, pleno sol.",
                    treatment = "Control de plagas preventivo mensual.",
                    prepSuelo = "Arado profundo.",
                    siembra = "Directa, a 3-5cm.",
                    riego = "Por gravedad o aspersión.",
                    fertilizacion = "Nitrogenada en etapas tempranas.",
                    malezas = "Herbicidas selectivos.",
                    poda = "No aplica.",
                    fitosanitario = "Control de gusano cogollero.",
                    manejoSuelo = "Rotación de cultivos.",
                    tutorado = "No requiere.",
                    raleo = "Si la densidad es muy alta.",
                    cropType = "macro",
                    region = "Tropical, Templado",
                    modalities = "macro"
                )
            )
            plantDao.insertAll(mockData)
        }
    }
}
