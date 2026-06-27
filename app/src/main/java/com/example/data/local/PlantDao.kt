package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.Plant
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {
    @Query("SELECT * FROM plants")
    fun getAllPlants(): Flow<List<Plant>>

    @Query("SELECT * FROM plants WHERE region LIKE '%' || :region || '%'")
    fun getPlantsByRegion(region: String): Flow<List<Plant>>

    @Query("SELECT * FROM plants WHERE commercialName LIKE '%' || :query || '%' OR scientificName LIKE '%' || :query || '%'")
    fun searchPlants(query: String): Flow<List<Plant>>
    
    @Query("SELECT * FROM plants WHERE id = :id LIMIT 1")
    suspend fun getPlantById(id: String): Plant?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plants: List<Plant>)
    
    @Query("DELETE FROM plants")
    suspend fun clearAll()
}
