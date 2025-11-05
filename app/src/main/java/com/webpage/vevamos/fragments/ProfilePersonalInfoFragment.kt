package com.webpage.vevamos.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.webpage.vevamos.EditProfileActivity
import com.webpage.vevamos.R
import com.webpage.vevamos.VerificationActivity
import com.webpage.vevamos.databinding.FragmentProfilePersonalInfoBinding

class ProfilePersonalInfoFragment : Fragment() {

    private var _binding: FragmentProfilePersonalInfoBinding? = null
    private val binding get() = _binding!!

    // --- INICIO: CORRECCIÓN CLAVE ---
    // Declaramos las variables como propiedades de la clase
    private val db: FirebaseFirestore = Firebase.firestore
    private val currentUser: FirebaseUser? = Firebase.auth.currentUser
    private var userListener: ListenerRegistration? = null
    // --- FIN: CORRECCIÓN CLAVE ---

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfilePersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvEditProfile.setOnClickListener {
            startActivity(Intent(activity, EditProfileActivity::class.java))
        }

        binding.itemVerifyProfile.setOnClickListener {
            startActivity(Intent(activity, VerificationActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        setupUserStatusListener()
    }

    override fun onPause() {
        super.onPause()
        userListener?.remove()
    }

    private fun setupUserStatusListener() {
        if (currentUser == null) return

        userListener = db.collection("users").document(currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists() || _binding == null) {
                    return@addSnapshotListener
                }

                val status = snapshot.getString("verificationStatus") ?: "none"
                updateVerificationUI(status)
            }
    }

    private fun updateVerificationUI(status: String) {
        val context = context ?: return

        when (status) {
            "pending" -> {
                binding.itemVerifyProfile.isClickable = false
                binding.tvVerificationTitle.text = "Verificación en revisión"
                binding.tvVerificationStatus.visibility = View.VISIBLE
                binding.tvVerificationStatus.text = "Tus datos están siendo revisados."
                binding.ivVerificationStatus.setImageResource(R.drawable.ic_pending)
                binding.ivVerificationStatus.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_orange_dark))
            }
            "verified" -> {
                binding.itemVerifyProfile.isClickable = false
                binding.tvVerificationTitle.text = "Perfil verificado"
                binding.tvVerificationStatus.visibility = View.GONE
                binding.ivVerificationStatus.setImageResource(R.drawable.ic_verified)
                binding.ivVerificationStatus.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary))
            }
            else -> { // "none" o si es rechazado
                binding.itemVerifyProfile.isClickable = true
                binding.tvVerificationTitle.text = "Verificar datos personales"
                binding.tvVerificationStatus.visibility = View.VISIBLE
                binding.tvVerificationStatus.text = "Completa los pasos para inspirar confianza."
                binding.ivVerificationStatus.setImageResource(R.drawable.ic_add_circle)
                binding.ivVerificationStatus.clearColorFilter()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}