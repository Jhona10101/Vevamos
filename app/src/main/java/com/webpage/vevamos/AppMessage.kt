package com.webpage.vevamos

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Modelo para los mensajes dentro de la app (notificaciones, etc.)
data class AppMessage(
    val userId: String? = null,
    val title: String? = null,
    val body: String? = null,
    val isRead: Boolean = false,
    @ServerTimestamp
    val timestamp: Date? = null
) {
    // Constructor vacío requerido por Firestore
    constructor() : this(null, null, null, false, null)
}