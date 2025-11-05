package com.webpage.vevamos

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.webpage.vevamos.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val currentUser = Firebase.auth.currentUser
    private val db = Firebase.firestore
    private val storage = Firebase.storage("gs://veeevamos.firebasestorage.app")
    private var selectedImageUri: Uri? = null
    private var existingImageUrl: String? = null
    private var verifiedPhoneNumber: String? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let {
                selectedImageUri = it
                Glide.with(this).load(it).into(binding.ivProfileImage)
            }
        }
    }

    private val phoneVerificationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val phoneNumber = result.data?.getStringExtra("verified_phone_number")
            if (!phoneNumber.isNullOrEmpty()) {
                verifiedPhoneNumber = phoneNumber
                binding.etPhoneNumber.setText(phoneNumber)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarEditProfile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadUserProfile()

        binding.ivProfileImage.setOnClickListener {
            openImagePicker()
        }

        binding.etPhoneNumber.isFocusable = false
        binding.etPhoneNumber.isClickable = true
        binding.etPhoneNumber.setOnClickListener {
            val intent = Intent(this, PhoneAuthActivity::class.java)
            intent.putExtra("launched_from_profile", true)
            phoneVerificationLauncher.launch(intent)
        }

        binding.btnSaveChanges.setOnClickListener {
            saveProfile()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
        imagePickerLauncher.launch(intent)
    }

    private fun loadUserProfile() {
        if (currentUser == null) return
        setLoading(true)
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                setLoading(false)
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    binding.etFullName.setText(user?.fullName)
                    binding.etPhoneNumber.setText(user?.phoneNumber ?: "Toca para verificar")
                    verifiedPhoneNumber = user?.phoneNumber
                    existingImageUrl = user?.profileImageUrl
                    if (!existingImageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(existingImageUrl).into(binding.ivProfileImage)
                    }
                }
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(this, "Error al cargar el perfil.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfile() {
        val fullName = binding.etFullName.text.toString().trim()
        val phoneNumber = verifiedPhoneNumber

        if (fullName.isEmpty() || phoneNumber.isNullOrEmpty()) {
            Toast.makeText(this, "Nombre y teléfono verificado son obligatorios.", Toast.LENGTH_SHORT).show()
            return
        }
        setLoading(true)

        if (selectedImageUri != null) {
            uploadImageAndSaveData(fullName, phoneNumber)
        } else {
            saveDataToFirestore(fullName, phoneNumber, existingImageUrl)
        }
    }

    private fun uploadImageAndSaveData(fullName: String, phoneNumber: String) {
        if (currentUser == null || selectedImageUri == null) {
            setLoading(false)
            return
        }
        val imageRef = storage.reference.child("profile_images/${currentUser.uid}")

        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveDataToFirestore(fullName, phoneNumber, uri.toString())
                }
                    .addOnFailureListener { e -> onSaveFailure(e) }
            }
            .addOnFailureListener { e -> onSaveFailure(e) }
    }

    private fun saveDataToFirestore(fullName: String, phoneNumber: String, imageUrl: String?) {
        if (currentUser == null) {
            setLoading(false)
            return
        }
        val userDocRef = db.collection("users").document(currentUser.uid)

        val userData = mapOf(
            "fullName" to fullName,
            "phoneNumber" to phoneNumber,
            "profileImageUrl" to (imageUrl ?: ""),
            "profileComplete" to true
        )

        userDocRef.set(userData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener { onSaveSuccess() }
            .addOnFailureListener { onSaveFailure(it) }
    }

    private fun onSaveSuccess() {
        setLoading(false)
        Toast.makeText(this, "Perfil guardado con éxito.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun onSaveFailure(e: Exception) {
        setLoading(false)
        Toast.makeText(this, "Error al guardar el perfil: ${e.message}", Toast.LENGTH_LONG).show()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarEditProfile.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSaveChanges.isEnabled = !isLoading
    }
}