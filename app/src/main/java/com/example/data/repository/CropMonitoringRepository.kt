package com.example.data.repository

import com.example.data.local.AgrinexusDao
import com.example.model.CultivoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class CropMonitoringRepository(private val dao: AgrinexusDao) {
    
    fun getAllAgronomos(): Flow<List<com.example.model.AgronomoEntity>> {
        return dao.getAllAgronomos()
    }

    fun getMonitoredCropsForUser(uid: String): Flow<List<CultivoEntity>> {
        if (uid.isEmpty()) {
            return emptyFlow()
        }
        return dao.getCultivosByAgricultor(uid)
    }
    
    fun getMonitoredCropsForEspecialista(uid: String): Flow<List<CultivoEntity>> {
        if (uid.isEmpty()) {
            return emptyFlow()
        }
        return dao.getCultivosByEspecialista(uid)
    }

    suspend fun insertCrop(
        agricultorId: String,
        especialistaId: String?,
        plantaId: String
    ): CultivoEntity {
        val cultivo = CultivoEntity(
            id_cultivo = UUID.randomUUID().toString(),
            id_agricultor = agricultorId,
            id_especialista = especialistaId,
            id_planta = plantaId,
            fecha_inicio = System.currentTimeMillis(),
            estado = "Activo"
        )
        dao.insertCultivo(cultivo)
        
        com.example.data.network.GoogleScriptSync.sendData("add_cultivo", mapOf(
            "id_cultivo" to cultivo.id_cultivo,
            "id_agricultor" to cultivo.id_agricultor,
            "id_especialista" to cultivo.id_especialista,
            "id_planta" to cultivo.id_planta,
            "estado" to cultivo.estado
        ))
        
        return cultivo
    }
    
    suspend fun updateCrop(cultivo: CultivoEntity) {
        dao.updateCultivo(cultivo)
    }

    suspend fun deleteCrop(cultivo: CultivoEntity) {
        dao.deleteCultivo(cultivo)
    }
}
