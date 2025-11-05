package com.webpage.vevamos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.webpage.vevamos.databinding.ActivityMainBinding
import com.webpage.vevamos.fragments.MessagesFragment
import com.webpage.vevamos.fragments.ProfileFragment
import com.webpage.vevamos.fragments.SearchFragment
import com.webpage.vevamos.fragments.TripsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Lanzador para pedir permisos de forma moderna y segura
    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Aquí puedes manejar la respuesta del usuario si algún permiso fue denegado.
        // Por ahora, no es necesario hacer nada extra.
        permissions.entries.forEach {
            // Log.d("MainActivityPermissions", "${it.key} = ${it.value}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.navigation_search -> selectedFragment = SearchFragment()
                R.id.navigation_publish -> {
                    startActivity(Intent(this, PublishTripActivity::class.java))
                    return@setOnItemSelectedListener false // No cambia de pestaña
                }
                R.id.navigation_trips -> selectedFragment = TripsFragment()
                R.id.navigation_messages -> selectedFragment = MessagesFragment()
                R.id.navigation_profile -> selectedFragment = ProfileFragment()
            }
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, selectedFragment).commit()
            }
            true
        }

        // Establece la pestaña de Perfil como la inicial
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.navigation_profile
        }

        // Al iniciar, pedimos los permisos necesarios
        askForPermissions()
    }

    private fun askForPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Permiso de ubicación para los mapas
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Permiso de notificaciones (solo para Android 13 y superior)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Si hay permisos en la lista, los pedimos todos a la vez
        if (permissionsToRequest.isNotEmpty()) {
            requestMultiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}