package com.example.data.local

import androidx.room.*
import com.example.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AgrinexusDao {
    
    // Agricultor
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgricultor(agricultor: AgricultorEntity)

    @Query("SELECT * FROM Usuarios_Agricultores WHERE correo = :correo LIMIT 1")
    suspend fun getAgricultorByCorreo(correo: String): AgricultorEntity?
    
    // Agronomo
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgronomo(agronomo: AgronomoEntity)

    @Query("SELECT * FROM Especialistas_Agronomos WHERE correo = :correo LIMIT 1")
    suspend fun getAgronomoByCorreo(correo: String): AgronomoEntity?

    @Query("SELECT * FROM Especialistas_Agronomos")
    fun getAllAgronomos(): Flow<List<AgronomoEntity>>

    // Cultivos
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCultivo(cultivo: CultivoEntity)

    @Update
    suspend fun updateCultivo(cultivo: CultivoEntity)

    @Delete
    suspend fun deleteCultivo(cultivo: CultivoEntity)

    @Query("SELECT * FROM Monitoreo_Seguimiento WHERE id_agricultor = :idAgricultor")
    fun getCultivosByAgricultor(idAgricultor: String): Flow<List<CultivoEntity>>

    @Query("SELECT * FROM Monitoreo_Seguimiento WHERE id_especialista = :idEspecialista")
    fun getCultivosByEspecialista(idEspecialista: String): Flow<List<CultivoEntity>>

    // Actividades
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActividad(actividad: ActividadEntity)
    
    @Update
    suspend fun updateActividad(actividad: ActividadEntity)

    @Delete
    suspend fun deleteActividad(actividad: ActividadEntity)

    @Query("SELECT * FROM Calendario_Actividades WHERE id_cultivo = :idCultivo ORDER BY fecha ASC")
    fun getActividadesByCultivo(idCultivo: String): Flow<List<ActividadEntity>>
}
