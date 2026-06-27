package com.example.data.repository

import com.example.data.local.AgrinexusDao
import com.example.model.AgricultorEntity
import com.example.model.AgronomoEntity
import com.example.model.User
import com.example.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

class AuthRepository(private val dao: AgrinexusDao) {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private suspend fun syncWithScript(action: String, payload: Map<String, Any?>) {
        com.example.data.network.GoogleScriptSync.sendData(action, payload)
    }

    suspend fun login(email: String, pass: String): Result<User> {
        return try {
            val hashed = hashPassword(pass)
            
            // Try agricultor (simulando lectura de Google Sheets localmente)
            val agricultor = dao.getAgricultorByCorreo(email)
            if (agricultor != null) {
                if (agricultor.password_hash == hashed) {
                    val user = User(
                        uid = agricultor.id_agricultor,
                        fullName = agricultor.nombre,
                        email = agricultor.correo,
                        role = UserRole.AGRICULTOR,
                        location = agricultor.ubicacion,
                        cropType = agricultor.cropType,
                        paymentMethod = agricultor.paymentMethod
                    )
                    _currentUser.value = user
                    // Notificar al script del login
                    syncWithScript("login", mapOf("email" to email, "password_hash" to hashed, "role" to "AGRICULTOR"))
                    return Result.success(user)
                } else {
                    return Result.failure(Exception("Contraseña incorrecta."))
                }
            }

            // Try agronomo
            val agronomo = dao.getAgronomoByCorreo(email)
            if (agronomo != null) {
                if (agronomo.password_hash == hashed) {
                    val user = User(
                        uid = agronomo.id_especialista,
                        fullName = agronomo.nombre,
                        email = agronomo.correo,
                        role = UserRole.ESPECIALISTA,
                        location = "",
                        cropType = com.example.model.CropType.MICROCULTIVO,
                        paymentMethod = com.example.model.PaymentMethod.CARD
                    )
                    _currentUser.value = user
                    syncWithScript("login", mapOf("email" to email, "password_hash" to hashed, "role" to "ESPECIALISTA"))
                    return Result.success(user)
                } else {
                    return Result.failure(Exception("Contraseña incorrecta."))
                }
            }

            Result.failure(Exception("Usuario no registrado en el sistema. Por favor, verifique sus credenciales o regístrese."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(user: User, pass: String): Result<User> {
        return try {
            val uid = try {
                val result = auth.createUserWithEmailAndPassword(user.email, pass).await()
                result.user?.uid ?: UUID.randomUUID().toString()
            } catch (e: Exception) {
                UUID.randomUUID().toString()
            }
            
            val newUser = user.copy(uid = uid)
            val hashed = hashPassword(pass)
            
            try {
                firestore.collection("users").document(uid).set(newUser).await()
            } catch (e: Exception) {
                // ignore
            }

            val payload = mutableMapOf<String, Any?>(
                "uid" to uid,
                "nombre" to user.fullName,
                "correo" to user.email,
                "password_hash" to hashed,
                "rol" to user.role.name
            )

            if (user.role == UserRole.AGRICULTOR) {
                val agricultor = AgricultorEntity(
                    id_agricultor = uid,
                    nombre = user.fullName,
                    correo = user.email,
                    password_hash = hashed,
                    ubicacion = user.location,
                    cropType = user.cropType,
                    paymentMethod = user.paymentMethod
                )
                dao.insertAgricultor(agricultor)
                payload["ubicacion"] = user.location
                payload["cropType"] = user.cropType.name
                payload["paymentMethod"] = user.paymentMethod.name
                
                syncWithScript("register_agricultor", payload)
            } else {
                val agronomo = AgronomoEntity(
                    id_especialista = uid,
                    nombre = user.fullName,
                    correo = user.email,
                    password_hash = hashed
                )
                dao.insertAgronomo(agronomo)
                syncWithScript("register_especialista", payload)
            }
            
            _currentUser.value = newUser
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(user: User): Result<User> {
        try {
            firestore.collection("users").document(user.uid).set(user).await()
        } catch (e: Exception) {
            // ignore
        }
        _currentUser.value = user
        return Result.success(user)
    }

    fun logout() {
        try { auth.signOut() } catch(e: Exception) {}
        _currentUser.value = null
    }
}
