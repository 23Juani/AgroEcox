![Texto alternativo](/assets/banner.png)
# AgroEcox

Este proyecto es una aplicación Android desarrollada para la gestión agrícola, integrando inteligencia artificial y servicios de Google.

## Herramientas utilizadas
- **AI Studio**: Para el desarrollo de una app con Android.
- **Gemini API**: Para el chatbot.
- **API de Google Sheets**: Para la gestión de los registros.
- **Firebase**: Para autenticación con correo y contraseña.
- **Apps Script**: Mini back-end para agregar los registros a Google Sheets.
- **Gemini**: Para depuración en Android Studio.
- **Google Drive**: Para almacenar imágenes.

## Lenguajes de programación utilizados
- **Kotlin**
- **JavaScript**

## Contribuidores
- **Diana Gabriela Arias Ilimuri**
- **Carles Romy Andres Quispe Tarqui**
- **Juan Gabriel Tantani Patti**

---

## Ejecución Local

**Requisitos previos:** [Android Studio](https://developer.android.com/studio)

1. Abrir Android Studio.
2. Seleccionar **Open** y elegir el directorio que contiene este proyecto.
3. Permitir que Android Studio corrija cualquier incompatibilidad durante la importación.
4. Crear un archivo llamado `.env` en el directorio del proyecto y configurar `GEMINI_API_KEY` con tu clave de API de Gemini (ver `.env.example` como ejemplo).
5. Eliminar esta línea del archivo `build.gradle.kts` de la aplicación: `signingConfig = signingConfigs.getByName("debugConfig")`.
6. Ejecutar la aplicación en un emulador o dispositivo físico.

---

View your app in AI Studio: [https://ai.studio/apps/749e28e9-89fe-4694-9260-f5240d41c045](https://ai.studio/apps/749e28e9-89fe-4694-9260-f5240d41c045)
