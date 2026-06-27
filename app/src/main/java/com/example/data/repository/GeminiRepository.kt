package com.example.data.repository

import com.example.BuildConfig
import com.example.data.network.Content
import com.example.data.network.GenerateContentRequest
import com.example.data.network.Part
import com.example.data.network.RetrofitClient

class GeminiRepository {

    // Store recent history. (e.g., last 4 messages to keep context short).
    private val history = mutableListOf<Content>()

    suspend fun sendMessage(
        userMessage: String,
        plant: com.example.model.Plant
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY") {
            return "El servicio de Inteligencia Artificial no está configurado (falta GEMINI_API_KEY)."
        }

        val systemPrompt = """
            Eres un asistente agrícola experto integrado en la app Agrinexus.
            Estás ayudando al usuario con la planta: ${plant.commercialName} (${plant.scientificName}).
            
            Información de la planta extraída de la base de datos:
            Descripción: ${plant.description}
            Cuidados: ${plant.cares}
            Tratamientos específicos: ${plant.treatment}
            Región ideal: ${plant.region}
            Tipo de cultivo: ${plant.cropType}
            Modalidad: ${plant.modalities}
            
            Responde a la duda del usuario de forma concisa, basándote estrictamente en esta información. Si la pregunta no está relacionada con la planta o la agricultura, indica amablemente que tu función es ayudar con el cultivo de esta planta.
        """.trimIndent()

        val systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))

        // Add user message to history
        val newContent = Content(parts = listOf(Part(text = "Usuario: $userMessage")))
        history.add(newContent)
        if (history.size > 4) {
            history.removeAt(0)
        }

        val request = GenerateContentRequest(
            contents = history.toList(),
            systemInstruction = systemInstruction
        )

        return try {
            val response = RetrofitClient.geminiService.generateContent(apiKey, request)
            val aiResponseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Lo siento, no pude procesar la respuesta."
            
            // Add AI response to history
            history.add(Content(parts = listOf(Part(text = "Asistente: $aiResponseText"))))
            if (history.size > 4) {
                history.removeAt(0)
            }
            
            aiResponseText
        } catch (e: Exception) {
            e.printStackTrace()
            "Ocurrió un error al contactar al asistente virtual: ${e.message}"
        }
    }

    suspend fun sendGlobalMessage(
        userMessage: String,
        plants: List<com.example.model.Plant>
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY") {
            return "Por favor, configura tu API Key de Gemini en el panel de Secrets (GEMINI_API_KEY)."
        }

        val plantsSummary = plants.joinToString(separator = "\n") { 
            "- ${it.commercialName} (${it.scientificName}): ${it.description.take(50)}..."
        }

        val systemPrompt = """
            Eres un asistente agrícola experto integrado en la app Agrinexus.
            Estás ayudando al usuario con la base de datos completa de plantas.
            
            Información de la base de datos actual:
            $plantsSummary
            
            Responde a la duda del usuario de forma concisa, basándote en la base de datos proporcionada. Si te preguntan por una planta, búscala en la lista y brinda la información.
        """.trimIndent()

        val systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))

        // Add user message to history
        val newContent = Content(parts = listOf(Part(text = "Usuario: $userMessage")))
        history.add(newContent)
        if (history.size > 4) {
            history.removeAt(0)
        }

        val request = GenerateContentRequest(
            contents = history.toList(),
            systemInstruction = systemInstruction
        )

        return try {
            val response = RetrofitClient.geminiService.generateContent(apiKey, request)
            val aiResponseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Lo siento, no pude procesar la respuesta."
            
            // Add AI response to history
            history.add(Content(parts = listOf(Part(text = "Asistente: $aiResponseText"))))
            if (history.size > 4) {
                history.removeAt(0)
            }
            aiResponseText
        } catch (e: Exception) {
            "Hubo un error de conexión con el asistente: ${e.message}"
        }
    }

    fun clearHistory() {
        history.clear()
    }
}
