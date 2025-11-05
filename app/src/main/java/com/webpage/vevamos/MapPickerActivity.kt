package com.webpage.vevamos

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.webpage.vevamos.databinding.ActivityMapPickerBinding
import java.io.IOException
import java.util.Locale

class MapPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapPickerBinding
    private lateinit var map: GoogleMap
    private var lastSelectedAddress: Address? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!Places.isInitialized()) {
            // ¡RECUERDA REEMPLAZAR "TU_API_KEY_AQUÍ"!
            Places.initialize(applicationContext, "AIzaSyA66YKBaGOCl7Lp4VakEp0-CfmDZKszRR0")
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupAutocomplete()

        binding.btnConfirmLocation.setOnClickListener { confirmLocation() }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        checkLocationPermission()

        map.setOnCameraIdleListener {
            val centerLatLng = map.cameraPosition.target
            getAddressFromLatLng(centerLatLng)
        }
    }

    private fun setupAutocomplete() {
        val autocompleteFragment = supportFragmentManager
            .findFragmentById(R.id.autocomplete_fragment_map) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment.setHint("Buscar dirección...")
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                place.latLng?.let { latLng ->
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            }
            override fun onError(status: com.google.android.gms.common.api.Status) {
                Log.e("PlacesAPI", "Error: $status")
                Toast.makeText(this@MapPickerActivity, "Error al buscar el lugar.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getAddressFromLatLng(latLng: LatLng) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                lastSelectedAddress = address
                binding.tvSelectedAddress.text = address.getAddressLine(0)
            } else {
                binding.tvSelectedAddress.text = "Dirección no encontrada"
                lastSelectedAddress = null
            }
        } catch (e: IOException) {
            Log.e("GeocoderError", "Error de Geocoder", e)
            binding.tvSelectedAddress.text = "Error al obtener la dirección"
            lastSelectedAddress = null
        }
    }

    private fun confirmLocation() {
        if (lastSelectedAddress != null) {
            val fullAddress = lastSelectedAddress!!.getAddressLine(0)
            val city = getCityFromAddress(lastSelectedAddress!!)
            val requestCode = intent.getIntExtra("request_code", -1)

            val resultIntent = Intent().apply {
                putExtra("selected_address", fullAddress)
                putExtra("selected_city", city)
                putExtra("request_code", requestCode)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } else {
            Toast.makeText(this, "Por favor, selecciona una ubicación válida.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCityFromAddress(address: Address): String {
        return address.locality ?: address.subAdminArea ?: address.adminArea ?: "Ubicación desconocida"
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            map.isMyLocationEnabled = true
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                } else {
                    val defaultLoc = LatLng(40.416775, -3.703790) // Madrid, España
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLoc, 10f))
                    Toast.makeText(this, "No se pudo obtener la ubicación. Mostrando ubicación por defecto.", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: SecurityException) {
            Log.e("LocationError", "Permiso de ubicación denegado", e)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado. Mostrando ubicación por defecto.", Toast.LENGTH_SHORT).show()
                val defaultLoc = LatLng(40.416775, -3.703790)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLoc, 10f))
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}