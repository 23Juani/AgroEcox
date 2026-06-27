package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class CropMonitoring(
    var id: String = "",
    val uid: String = "", // Which user owns this
    val plantId: String = "", // Associated plant from catalog
    val customName: String = "",
    val plantDateMillis: Long = 0,
    val progressPercent: Float = 0f, // 0 to 1
    val caresChecklistJson: String = "[]" // JSON representation of tasks
)
