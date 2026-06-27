package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Usuarios_Agricultores")
data class AgricultorEntity(
    @PrimaryKey val id_agricultor: String,
    val nombre: String,
    val correo: String,
    val password_hash: String,
    val ubicacion: String,
    val cropType: CropType,
    val paymentMethod: PaymentMethod
)

@Entity(tableName = "Especialistas_Agronomos")
data class AgronomoEntity(
    @PrimaryKey val id_especialista: String,
    val nombre: String,
    val correo: String,
    val password_hash: String
)

@Entity(tableName = "Monitoreo_Seguimiento")
data class CultivoEntity(
    @PrimaryKey val id_cultivo: String,
    val id_agricultor: String,
    val id_especialista: String?,
    val id_planta: String,
    val fecha_inicio: Long,
    val estado: String // ej. "Activo", "Finalizado"
)

@Entity(tableName = "Calendario_Actividades")
data class ActividadEntity(
    @PrimaryKey val id_actividad: String,
    val id_cultivo: String,
    val nombre_actividad: String,
    val descripcion: String,
    val fecha: Long,
    val color: String, // Hex color code
    val rol_responsable: UserRole,
    val completada: Boolean = false
)
