package com.webpage.vevamos

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Este es el nuevo modelo de datos.
// Los nombres de las variables deben coincidir EXACTAMENTE
// con los campos que creas en tus Cloud Functions.
data class UserNotification(
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "", // "system_verification", "trip_booked", etc.
    val isRead: Boolean = false,
    @ServerTimestamp
    val timestamp: Date? = null
)