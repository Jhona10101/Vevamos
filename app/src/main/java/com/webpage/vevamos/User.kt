package com.webpage.vevamos

// Modelo para guardar toda la información del perfil de un usuario en Firestore
data class User(
    val uid: String? = null,
    val email: String? = null,
    val fullName: String? = null,
    val dateOfBirth: String? = null,
    val phoneNumber: String? = null,
    val profileImageUrl: String? = null,
    val acceptsPromotions: Boolean = false,
    val profileComplete: Boolean = false,
    val emailVerified: Boolean = false,
    val idUrl: String? = null,
    val backgroundCheckUrl: String? = null,
    val fullyVerified: Boolean = false,
    val verificationNotified: Boolean = false,
    val verificationStatus: String = "none", // Estados: "none", "pending", "verified"
    // Preferencias de viaje
    val conversationPref: String? = null,
    val musicPref: String? = null,
    val petsPref: String? = null,
    val smokingPref: String? = null
) {
    // Constructor vacío requerido por Firestore para la deserialización
    constructor() : this(
        null, null, null, null, null, null, false, false,
        false, null, null, false, false, "none",
        null, null, null, null
    )
}