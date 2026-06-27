package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey val id: String,
    val imageUrl: String,
    val commercialName: String,
    val scientificName: String,
    val description: String,
    val caresLevel: String,
    val cares: String,
    val treatment: String,
    val prepSuelo: String,
    val siembra: String,
    val riego: String,
    val fertilizacion: String,
    val malezas: String,
    val poda: String,
    val fitosanitario: String,
    val manejoSuelo: String,
    val tutorado: String,
    val raleo: String,
    val cropType: String, 
    val region: String,
    val modalities: String
)
