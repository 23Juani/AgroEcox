package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.model.AgricultorEntity
import com.example.model.AgronomoEntity
import com.example.model.CultivoEntity
import com.example.model.ActividadEntity
import com.example.model.Plant

@Database(entities = [
    Plant::class, 
    AgricultorEntity::class, 
    AgronomoEntity::class, 
    CultivoEntity::class, 
    ActividadEntity::class
], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun agrinexusDao(): AgrinexusDao
}
