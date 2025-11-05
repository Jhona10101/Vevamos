package com.webpage.vevamos

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.webpage.vevamos.databinding.ActivityTripDetailBinding

class TripDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTripDetailBinding
    private val db = Firebase.firestore
    private val currentUser = Firebase.auth.currentUser

    // Declaramos currentTrip como una variable de la clase para poder modificarla
    private var currentTrip: Trip? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Asignamos el viaje a nuestra variable de clase
        currentTrip = getTripFromIntent()

        currentTrip?.let { trip ->
            displayTripDetails(trip)
            checkIfUserHasAlreadyBooked(trip)
        } ?: run {
            // Si el viaje es nulo, mostramos error y cerramos
            Toast.makeText(this, "Error al cargar los detalles.", Toast.LENGTH_LONG).show()
            finish()
        }

        binding.btnBookTrip.setOnClickListener {
            processBooking()
        }
    }

    private fun processBooking() {
        val trip = currentTrip ?: return // Salimos si no hay viaje
        val user = currentUser ?: return // Salimos si no hay usuario

        if (trip.driverId == user.uid) {
            Toast.makeText(this, "No puedes reservar tu propio viaje.", Toast.LENGTH_SHORT).show()
            return
        }
        if ((trip.seats ?: 0) <= 0) {
            Toast.makeText(this, "No quedan asientos disponibles.", Toast.LENGTH_SHORT).show()
            return
        }

        createReservationInFirestore(trip, user.uid)
    }

    private fun createReservationInFirestore(trip: Trip, userId: String) {
        binding.btnBookTrip.isEnabled = false // Desactivamos el botón

        val tripRef = db.collection("trips").document(trip.documentId!!)
        val reservationsRef = db.collection("reservations")

        val reservationData = hashMapOf(
            "userId" to userId,
            "tripId" to trip.documentId,
            "driverId" to trip.driverId
        )

        db.runBatch { batch ->
            batch.set(reservationsRef.document(), reservationData)
            batch.update(tripRef, "seats", (trip.seats ?: 1) - 1)
        }.addOnSuccessListener {
            Toast.makeText(this, "¡Reserva realizada con éxito!", Toast.LENGTH_LONG).show()

            // CORRECCIÓN: Modificamos el objeto 'currentTrip' de la clase, que es una 'var'
            val newSeatCount = (currentTrip?.seats ?: 1) - 1
            currentTrip?.seats = newSeatCount

            // Actualizamos la UI
            binding.tvSeatsDetail.text = newSeatCount.toString()
            binding.btnBookTrip.text = "Reservado"

        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error al realizar la reserva: ${e.message}", Toast.LENGTH_LONG).show()
            binding.btnBookTrip.isEnabled = true // Reactivamos el botón si falla
        }
    }

    private fun checkIfUserHasAlreadyBooked(trip: Trip) {
        val user = currentUser ?: return
        if (trip.documentId == null) return

        db.collection("reservations")
            .whereEqualTo("userId", user.uid)
            .whereEqualTo("tripId", trip.documentId)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    binding.btnBookTrip.isEnabled = false
                    binding.btnBookTrip.text = "Ya has reservado"
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun getTripFromIntent(): Trip? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_TRIP", Trip::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("EXTRA_TRIP")
        }
    }

    private fun displayTripDetails(trip: Trip) {
        binding.tvOriginDetail.text = trip.origin
        binding.tvDestinationDetail.text = trip.destination
        binding.tvDateDetail.text = trip.date
        binding.tvPriceDetail.text = "${trip.price} €"
        binding.tvSeatsDetail.text = trip.seats.toString()

        if ((trip.seats ?: 0) <= 0) {
            binding.btnBookTrip.isEnabled = false
            binding.btnBookTrip.text = "No hay asientos"
        }
    }
}