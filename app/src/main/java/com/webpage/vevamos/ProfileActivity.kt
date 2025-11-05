package com.webpage.vevamos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.webpage.vevamos.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val db = Firebase.firestore
    private val currentUser = Firebase.auth.currentUser

    private lateinit var publishedTripsAdapter: ProfileTripAdapter
    private lateinit var reservedTripsAdapter: ProfileTripAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarProfile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.tvUserEmail.text = currentUser?.email ?: "No se encontró el email"

        setupRecyclerViews()

        if (currentUser != null) {
            fetchPublishedTrips(currentUser.uid)
            fetchReservedTrips(currentUser.uid)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupRecyclerViews() {
        // Adaptador para viajes publicados
        publishedTripsAdapter = ProfileTripAdapter(
            trips = listOf(),
            isPublishedList = true,
            onRateClick = {},
            onDeleteClick = { tripToDelete ->
                // La lógica del clic ahora llama al diálogo de confirmación
                showDeleteConfirmationDialog(tripToDelete)
            }
        )
        binding.rvPublishedTrips.apply {
            layoutManager = LinearLayoutManager(this@ProfileActivity)
            adapter = publishedTripsAdapter
        }

        // Adaptador para viajes reservados
        reservedTripsAdapter = ProfileTripAdapter(
            trips = listOf(),
            isPublishedList = false,
            onDeleteClick = {},
            onRateClick = { tripToRate ->
                showRatingDialog(tripToRate)
            }
        )
        binding.rvReservedTrips.apply {
            layoutManager = LinearLayoutManager(this@ProfileActivity)
            adapter = reservedTripsAdapter
        }
    }

    // Muestra un diálogo para confirmar si el usuario quiere eliminar el viaje
    private fun showDeleteConfirmationDialog(trip: Trip) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Viaje")
            .setMessage("¿Estás seguro de que quieres eliminar este viaje? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteTripFromFirestore(trip)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Elimina el documento del viaje de la base de datos
    private fun deleteTripFromFirestore(trip: Trip) {
        if (trip.documentId == null) {
            Toast.makeText(this, "Error: No se pudo identificar el viaje.", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("trips").document(trip.documentId!!)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Viaje eliminado con éxito.", Toast.LENGTH_SHORT).show()
                // Volvemos a cargar los viajes publicados para refrescar la lista
                fetchPublishedTrips(currentUser!!.uid)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar el viaje: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Muestra el diálogo para valorar un viaje
    private fun showRatingDialog(trip: Trip) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rate_trip, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val commentEditText = dialogView.findViewById<EditText>(R.id.etRatingComment)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Enviar") { dialog, _ ->
                val ratingValue = ratingBar.rating
                val comment = commentEditText.text.toString().trim()
                if (ratingValue > 0) {
                    submitRating(trip, ratingValue, comment)
                } else {
                    Toast.makeText(this, "Debes seleccionar al menos una estrella.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }

    // Guarda la valoración en la base de datos
    private fun submitRating(trip: Trip, ratingValue: Float, comment: String) {
        if (currentUser == null || trip.driverId == null || trip.documentId == null) return

        val rating = Rating(
            tripId = trip.documentId,
            raterId = currentUser.uid,
            ratedUserId = trip.driverId,
            ratingValue = ratingValue,
            comment = comment.takeIf { it.isNotEmpty() }
        )

        db.collection("ratings").add(rating)
            .addOnSuccessListener {
                Toast.makeText(this, "¡Gracias por tu valoración!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al enviar la valoración: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Obtiene los viajes publicados por el usuario
    private fun fetchPublishedTrips(userId: String) {
        db.collection("trips")
            .whereEqualTo("driverId", userId)
            .get()
            .addOnSuccessListener { documents ->
                // CORRECCIÓN: Mapeamos los resultados para guardar el ID del documento
                val trips = documents.map { doc ->
                    doc.toObject(Trip::class.java).apply { documentId = doc.id }
                }
                publishedTripsAdapter.updateTrips(trips)
            }
            .addOnFailureListener { e ->
                Log.w("ProfileActivity", "Error al obtener viajes publicados", e)
            }
    }

    // Obtiene los viajes que el usuario ha reservado
    private fun fetchReservedTrips(userId: String) {
        db.collection("reservations")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { reservationDocs ->
                if (reservationDocs.isEmpty) {
                    reservedTripsAdapter.updateTrips(emptyList())
                    return@addOnSuccessListener
                }

                // CORRECCIÓN: Lógica completamente nueva y funcional para buscar los viajes
                val tripIds = reservationDocs.mapNotNull { it.getString("tripId") }
                if (tripIds.isEmpty()) {
                    reservedTripsAdapter.updateTrips(emptyList())
                    return@addOnSuccessListener
                }

                // Creamos una tarea de búsqueda para cada ID de viaje
                val tripFetchTasks = tripIds.map { tripId ->
                    db.collection("trips").document(tripId).get()
                }

                // Cuando todas las búsquedas individuales terminan, juntamos los resultados
                Tasks.whenAllSuccess<com.google.firebase.firestore.DocumentSnapshot>(tripFetchTasks)
                    .addOnSuccessListener { tripDocumentSnapshots ->
                        val trips = tripDocumentSnapshots.mapNotNull { doc ->
                            // Convertimos cada documento en un objeto Trip y guardamos su ID
                            doc.toObject(Trip::class.java)?.apply { documentId = doc.id }
                        }
                        reservedTripsAdapter.updateTrips(trips)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("ProfileActivity", "Error al obtener viajes reservados", e)
            }
    }
}