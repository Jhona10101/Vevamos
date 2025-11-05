package com.webpage.vevamos

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.webpage.vevamos.databinding.ActivityPublishTripBinding
import java.util.Calendar

class PublishTripActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPublishTripBinding
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private var selectedOriginName: String? = null
    private var selectedDestinationName: String? = null
    private var selectedOriginCity: String? = null
    private var selectedDestinationCity: String? = null

    private val mapPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val address = result.data?.getStringExtra("selected_address")
            val city = result.data?.getStringExtra("selected_city")
            val requestCode = result.data?.getIntExtra("request_code", -1)

            if (!address.isNullOrEmpty() && !city.isNullOrEmpty() && requestCode != -1) {
                if (requestCode == ORIGIN_REQUEST_CODE) {
                    selectedOriginName = address
                    selectedOriginCity = city
                    binding.tvOrigin.text = address
                } else if (requestCode == DESTINATION_REQUEST_CODE) {
                    selectedDestinationName = address
                    selectedDestinationCity = city
                    binding.tvDestination.text = address
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Comprobamos si el perfil está completo ANTES de mostrar la interfaz
        checkProfileCompletion { isComplete ->
            if (isComplete) {
                // Si está completo, cargamos la interfaz y la funcionalidad
                binding = ActivityPublishTripBinding.inflate(layoutInflater)
                setContentView(binding.root)
                initializeActivity()
            } else {
                // Si no está completo, mostramos un mensaje y redirigimos
                Toast.makeText(this, "Por favor, completa tu perfil para publicar un viaje.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, EditProfileActivity::class.java))
                finish() // Cierra esta actividad para que el usuario no pueda volver
            }
        }
    }

    // Toda la lógica original del onCreate ahora vive aquí
    private fun initializeActivity() {
        setSupportActionBar(binding.toolbarPublish)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupDatePicker()
        setupTimePicker()

        binding.tvOrigin.setOnClickListener {
            launchMapPicker(ORIGIN_REQUEST_CODE)
        }

        binding.tvDestination.setOnClickListener {
            launchMapPicker(DESTINATION_REQUEST_CODE)
        }

        binding.buttonPublish.setOnClickListener {
            publishTrip()
        }
    }

    // Función que contacta con Firestore para ver si el perfil está completo
    private fun checkProfileCompletion(callback: (Boolean) -> Unit) {
        val user = Firebase.auth.currentUser
        if (user == null) {
            callback(false) // Si no hay usuario, el perfil no puede estar completo
            return
        }
        Firebase.firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                // El perfil está completo si el campo 'profileComplete' es 'true'
                val isComplete = document.getBoolean("profileComplete") ?: false
                callback(isComplete)
            }
            .addOnFailureListener {
                // Si hay un error al contactar la base de datos, asumimos que no está completo
                callback(false)
            }
    }

    private fun launchMapPicker(requestCode: Int) {
        val intent = Intent(this, MapPickerActivity::class.java).apply {
            putExtra("request_code", requestCode)
        }
        mapPickerLauncher.launch(intent)
    }

    private fun publishTrip() {
        val origin = selectedOriginName
        val destination = selectedDestinationName
        val originCity = selectedOriginCity
        val destinationCity = selectedDestinationCity
        val date = binding.editTextDate.text.toString().trim()
        val time = binding.editTextTime.text.toString().trim()
        val priceStr = binding.editTextPrice.text.toString().trim()
        val seatsStr = binding.editTextSeats.text.toString().trim()

        if (origin.isNullOrEmpty() || destination.isNullOrEmpty() || date.isEmpty() || time.isEmpty() || priceStr.isEmpty() || seatsStr.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }

        val trip = Trip(
            driverId = auth.currentUser?.uid,
            origin = origin,
            destination = destination,
            originCity = originCity,
            destinationCity = destinationCity,
            date = date,
            time = time,
            price = priceStr.toDoubleOrNull() ?: 0.0,
            seats = seatsStr.toIntOrNull() ?: 0
        )

        db.collection("trips").add(trip)
            .addOnSuccessListener {
                Toast.makeText(this, "Viaje publicado con éxito.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al publicar el viaje: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupDatePicker() {
        binding.editTextDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                    binding.editTextDate.setText(formattedDate)
                },
                year,
                month,
                day
            )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()
        }
    }

    private fun setupTimePicker() {
        binding.editTextTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this,
                { _, selectedHour, selectedMinute ->
                    val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                    binding.editTextTime.setText(formattedTime)
                },
                hour,
                minute,
                true // 24-hour format
            )
            timePickerDialog.show()
        }
    }

    companion object {
        private const val ORIGIN_REQUEST_CODE = 1
        private const val DESTINATION_REQUEST_CODE = 2
    }
}