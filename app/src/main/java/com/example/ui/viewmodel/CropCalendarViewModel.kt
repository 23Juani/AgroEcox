package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AgrinexusDao
import com.example.data.repository.AuthRepository
import com.example.model.ActividadEntity
import com.example.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class CropCalendarViewModel(
    private val dao: AgrinexusDao,
    private val authRepository: AuthRepository,
    private val cropId: String
) : ViewModel() {
    
    val currentUser = authRepository.currentUser.value

    val actividades: StateFlow<List<ActividadEntity>> = dao.getActividadesByCultivo(cropId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addActividad(nombre: String, descripcion: String, dateMillis: Long, color: String, role: UserRole) {
        viewModelScope.launch {
            val act = ActividadEntity(
                id_actividad = UUID.randomUUID().toString(),
                id_cultivo = cropId,
                nombre_actividad = nombre,
                descripcion = descripcion,
                fecha = dateMillis,
                color = color,
                rol_responsable = role,
                completada = false
            )
            dao.insertActividad(act)
            
            com.example.data.network.GoogleScriptSync.sendData("add_actividad", mapOf(
                "id_actividad" to act.id_actividad,
                "id_cultivo" to act.id_cultivo,
                "nombre_actividad" to act.nombre_actividad,
                "descripcion" to act.descripcion,
                "fecha" to act.fecha,
                "rol_responsable" to act.rol_responsable.name,
                "color" to act.color
            ))
        }
    }

    fun toggleActividadCompletada(act: ActividadEntity, completada: Boolean) {
        viewModelScope.launch {
            dao.updateActividad(act.copy(completada = completada))
        }
    }

    fun deleteActividad(act: ActividadEntity) {
        viewModelScope.launch {
            dao.deleteActividad(act)
        }
    }
}
