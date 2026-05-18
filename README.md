# InnerTalk 🧠📱 - Botiquín Emocional de Bolsillo

[![Android Native](https://img.shields.io/badge/Platform-Android%20Native-brightgreen.svg?style=flat-square)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg?style=flat-square)](https://kotlinlang.org/)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg?style=flat-square)](https://firebase.google.com/)
[![Groq AI](https://img.shields.io/badge/AI-Groq%20API%20%28Llama%203.1%29-blueviolet.svg?style=flat-square)](https://groq.com/)

**InnerTalk** es una aplicación móvil nativa para Android diseñada como una solución tecnológica ante la brecha de inmediatez en el soporte de la salud mental juvenil. El proyecto transforma el *smartphone* de un agente estresor pasivo (redes sociales, dinámicas adictivas) en una herramienta activa, privada y accesible 24/7 para el auto-registro y la contención emocional.

---

## 📌 Enlaces del Proyecto
* **Repositorio Oficial:** [https://github.com/Lucia170906/InnerTalk](https://github.com/Lucia170906/InnerTalk)
* **Gestión y Control Ágil (Tablero Kanban):** [ClickUp Proyectos - InnerTalk](https://app.clickup.com/90121611254/v/s/90126959297)

---

## 🚀 Funcionalidades Core & Flujos de Usuario

1. **Asistente Conversacional Inteligente con Memoria Contextual (`ChatFragment`):** Entorno de diálogo adaptativo respaldado por el modelo `llama-3.1-8b-instant` de Groq. Al iniciar sesión, extrae de forma transparente los intereses y el nombre del usuario desde **Cloud Firestore** para inyectarlos en un *System Prompt* máster. Mantiene la memoria del hilo conversacional durante toda la sesión y cuenta con filtros de seguridad para derivar crisis agudas (ideación autolítica) a números de ayuda oficiales.
2. **Diario Emocional y Gestión de Adjuntos (`NewEntryFragment` / `EditEntryFragment`):** Flujo de *journaling* interactivo de baja fricción mediante la selección de emoticonos animados a color. Permite la adición diferida de notas de texto e imágenes desde la galería del dispositivo, almacenándolos en **Firebase Realtime Database**.
3. **Módulo de Gestión Avanzada por Gestos (Long Click):** En la pantalla de edición, una pulsación prolongada sobre la imagen adjunta despliega un diálogo de confirmación para eliminarla, forzando una purga atómica en el servidor.
4. **Panel Analítico de Tendencias (`StatisticsFragment`):** Traduce datos anímicos cualitativos en información estadística objetiva mediante la integración de la librería `AAChartView`. Realiza un seguimiento automatizado de rachas consecutivas de uso y analiza la emoción predominante para ofrecer pautas motivacionales personalizadas.
5. **Repositorio de Autocuidado Diario (`StartFragment`):** Catálogo indexado de 50 micro-actividades conductuales de relajación y *mindfulness*. El sistema aplica un algoritmo de barajado (`shuffled`) y filtros temporales que priorizan actividades específicas según el día de la semana, conectándose vía *Intents* nativos con recursos audiovisuales en YouTube.

---

## 🏗️ Arquitectura del Software & Desafíos Técnicos Resueltos

La aplicación se rige bajo el patrón arquitectónico **MVVM (Model-View-ViewModel)** y componentes de **Android Jetpack**, garantizando la desacoplación estricta de responsabilidades, la testeabilidad y la escalabilidad del sistema.

### ⚡ Desafíos de Ingeniería Resueltos en Código:

* **Persistencia de Sesión Conversacional Volátil:** Los fragmentos de la barra de navegación inferior se destruyen de la memoria RAM al cambiar de pestaña. Para evitar que el chat con la IA se borrara o reiniciara al navegar, se delegó la estructura del historial de mensajes en un `ChatViewModel` acoplado al ciclo de vida global de la actividad mediante `by activityViewModels()`, resolviendo la persistencia en caché durante la sesión sin generar lecturas redundantes en la nube.
* **Control Defensivo de Memoria RAM (Evitando OutOfMemoryError):** Las imágenes nativas de los smartphones actuales saturarían el almacenamiento de Realtime Database y colapsarían el renderizado del listado de notas. Se programó un flujo de compresión forzada en el cliente: captura del `Uri`, escalado del bitmap a resolución fija de 400x400 píxeles y compresión JPEG al 60% antes de codificar el archivo a cadenas de texto Base64.
* **Eliminación Atómica en Firebase mediante Mapas de Nulos:** Para purgar los adjuntos multimedia sin alterar la fecha ni el texto de la nota original, el método de actualización recopila los estados en un `HashMap<String, Any?>()`. Al enviar un valor `null` explícito a la clave `"fotoBase64"`, el método `updateChildren()` de Firebase interpreta la nulidad de forma estricta, destruyendo el nodo en el servidor y evitando datos huérfanos residuales.
* **Automatización en Segundo Plano Inmune a Reinicios (`WorkManager`):** Se implementó un programador de notificaciones push de retención diaria. El sistema captura la hora deseada por el usuario mediante un `MaterialTimePicker`, calcula el desfase matemático (*delay*) en milisegundos y encola la tarea mediante `PeriodicWorkRequestBuilder` utilizando la política `ExistingPeriodicWorkPolicy.UPDATE`. Esto garantiza que, si el usuario reconfigura la hora, la tarea previa se sobrescribe de inmediato, respetando el ahorro de batería global del sistema operativo (*Doze Mode*).

---

## 🛠️ Stack Tecnológico

* **Frontend & UI:** XML Views, Material Design 3, ViewBinding, Animaciones de escala dinámicas, Soporte nativo para cambio de **Modo Oscuro** automático.
* **Lógica & Asincronía:** Kotlin, Kotlin Coroutines para el aislamiento de tareas de red (`Dispatchers.IO`) e hilos de renderizado (`Dispatchers.Main`).
* **Backend Serverless:** Firebase Authentication (Módulo de Login, Registro y Password Reset), Cloud Firestore (Perfiles de usuario), Firebase Realtime Database (Estructura JSON jerárquica del diario).
* **APIs & Librerías Externas:** Groq API Cloud v1, HTTPUrlConnection Nativo, AAChartView Library, Android Jetpack Navigation Component.
* **Accesibilidad:** Internacionalización completa (Soporte multiidioma dinámico Español/Inglés gestionado por recursos distribuidos en `strings.xml`).

---

## 💻 Configuración e Instalación (Entorno de Desarrollo)

Para clonar y compilar este proyecto localmente en Android Studio, es necesario configurar las siguientes variables de entorno:

### 1. Requisitos Previos
* Android Studio Ladybug (o superior) instalado.
* Dispositivo físico o emulador con SDK 34 (Android 14) o superior.

### 2. Configuración de Firebase
1. Crea un proyecto en la [Consola de Firebase](https://console.firebase.google.com/).
2. Añade una aplicación Android utilizando el nombre de paquete `com.example.innertalk`.
3. Descarga el archivo `google-services.json` y colócalo en el directorio del módulo de tu aplicación (`/app/google-services.json`).
4. Habilita **Firebase Authentication** (Proveedor de Correo/Contraseña), **Cloud Firestore** y **Realtime Database**.
5. *Nota crítica de UX:* En la pestaña de configuración de Authentication, accede a **User Actions** y desmarca la casilla **"Email enumeration protection"** para permitir la validación en tiempo real de correos existentes implementada en la `LoginActivity`.

### 3. Inyección de la API Key de Groq
Por motivos de seguridad, la clave de la API de Groq no se encuentra hardcodeada en las clases de Kotlin. 
1. Genera una API Key válida en la consola de desarrolladores de Groq.
2. Crea un archivo de texto plano denominado `config.txt` dentro de la carpeta de recursos de la aplicación: `/app/src/main/assets/config.txt`.
3. Pega tu API Key de Groq directamente en la primera línea de ese archivo de texto, sin comillas ni espacios adicionales. El método `leerApiKey()` del fragmento se encargará de parsearlo de forma segura.

---

## 📊 Metodología de Trabajo

El desarrollo de este software unipersonal se estructuró bajo el marco de una metodología ágil híbrida:
* **Sprints Semanales:** Planificación sistemática de incrementos de código funcionales a través del backlog de ClickUp.
* **Bucle Recursivo Figma-Código:** Prototipado previo de alta fidelidad en Figma de las vistas y flujos UX antes del traslado a layouts XML, optimizando el rendimiento de maquetación.
* **Control de Versiones (GitFlow):** Uso estricto de la rama `main` para código de producción 100% estable y ramas de características individuales (`feature/`) para el aislamiento y testing de cada módulo tecnológico.

---
*Desarrollado de forma nativa por Lucía - 2026.*
