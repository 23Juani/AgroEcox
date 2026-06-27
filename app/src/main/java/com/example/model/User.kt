package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: UserRole = UserRole.AGRICULTOR,
    val location: String = "", // Coordenadas o dirección
    val cropType: CropType = CropType.MICROCULTIVO,
    val paymentMethod: PaymentMethod = PaymentMethod.CARD
)

enum class UserRole {
    AGRICULTOR,
    ESPECIALISTA
}

enum class CropType {
    MICROCULTIVO,
    MACROCULTIVO
}

enum class PaymentMethod {
    CARD,
    QR
}
