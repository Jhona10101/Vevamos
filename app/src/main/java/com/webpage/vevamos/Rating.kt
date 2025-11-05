package com.webpage.vevamos

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Rating(
    val tripId: String? = null,
    val raterId: String? = null,      // Quien valora (el pasajero)
    val ratedUserId: String? = null,  // A quien valoran (el conductor)
    val ratingValue: Float = 0f,
    val comment: String? = null,
    @ServerTimestamp
    val timestamp: Date? = null
) {
    // Constructor vacío requerido por Firestore
    constructor() : this(null, null, null, 0f, null, null)
}