package com.webpage.vevamos

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Parcelize
@IgnoreExtraProperties
data class Trip(
    val driverId: String? = null,
    // Dirección completa y exacta
    val origin: String? = null,
    val destination: String? = null,
    // CAMPOS AÑADIDOS: Dirección resumida
    val originCity: String? = null,
    val destinationCity: String? = null,
    val date: String? = null,
    val time: String? = null,
    val price: Double? = null,
    var seats: Int? = null,
    var documentId: String? = null
) : Parcelable {
    // Constructor vacío requerido por Firestore
    constructor() : this(null, null, null, null, null, null, null, null, null, null)
}