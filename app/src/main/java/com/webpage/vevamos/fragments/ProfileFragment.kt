package com.webpage.vevamos.fragments

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.webpage.vevamos.R
import com.webpage.vevamos.User
import com.webpage.vevamos.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val currentUser = Firebase.auth.currentUser
    private val db = Firebase.firestore
    private var userListener: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagerAdapter = ProfilePagerAdapter(this)
        binding.viewPagerProfile.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPagerProfile) { tab, position ->
            tab.text = when (position) {
                0 -> "Información personal"
                else -> "Cuenta"
            }
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        // Empezamos a escuchar los cambios en el perfil cuando el fragmento es visible
        setupUserListener()
    }

    override fun onPause() {
        super.onPause()
        // Dejamos de escuchar para ahorrar batería y recursos cuando el fragmento no es visible
        userListener?.remove()
    }

    private fun setupUserListener() {
        if (currentUser == null) return

        val userDocRef = db.collection("users").document(currentUser.uid)
        userListener = userDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Manejar el error si es necesario
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists() && _binding != null) {
                val user = snapshot.toObject(User::class.java)
                // Actualizamos toda la UI del perfil con los datos más recientes
                updateProfileUI(user)

                val wasNotifiedBefore = snapshot.getBoolean("verificationNotified") ?: false
                val isVerifiedNow = snapshot.getBoolean("fullyVerified") ?: false

                if (!wasNotifiedBefore && isVerifiedNow) {
                    createNotificationChannel()
                    showVerificationNotification()
                    // Marcamos que ya se le notificó para no volver a hacerlo
                    userDocRef.update("verificationNotified", true)
                }
            }
        }
    }

    private fun updateProfileUI(user: User?) {
        user?.let {
            binding.tvFullName.text = it.fullName

            if (!it.profileImageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(it.profileImageUrl)
                    .placeholder(R.drawable.ic_default_profile)
                    .error(R.drawable.ic_default_profile)
                    .circleCrop()
                    .into(binding.ivProfileImage)
            } else {
                binding.ivProfileImage.setImageResource(R.drawable.ic_default_profile)
            }

            binding.ivVerifiedBadge.visibility = if (it.fullyVerified) View.VISIBLE else View.GONE
        }
    }

    private fun showVerificationNotification() {
        val builder = NotificationCompat.Builder(requireContext(), "VERIFICATION_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_verified)
            .setContentTitle("¡Perfil Verificado!")
            .setContentText("Tu cuenta ha sido verificada. ¡Ya puedes publicar y reservar viajes!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(requireContext()).notify(1, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notificaciones de Verificación"
            val descriptionText = "Canal para notificar al usuario cuando su perfil es verificado."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("VERIFICATION_CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = requireActivity().getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}