package com.webpage.vevamos

import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.webpage.vevamos.databinding.ActivityVerificationBinding
import com.webpage.vevamos.fragments.VerificationIdFragment
import com.webpage.vevamos.fragments.VerificationPagerAdapter
import com.webpage.vevamos.fragments.VerificationPreferencesFragment
import com.webpage.vevamos.fragments.VerificationSelfieFragment
import java.io.File
import java.net.URLEncoder

class VerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerificationBinding
    private lateinit var pagerAdapter: VerificationPagerAdapter
    private var tempImageUri: Uri? = null

    private val storage = Firebase.storage("gs://veeevamos.firebasestorage.app")
    private val db = Firebase.firestore
    private val currentUser = Firebase.auth.currentUser
    private var loadingDialog: AlertDialog? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            when (binding.viewPagerVerification.currentItem) {
                0 -> (pagerAdapter.fragments[0] as? VerificationSelfieFragment)?.setImage(tempImageUri)
                1 -> (pagerAdapter.fragments[1] as? VerificationIdFragment)?.setImage(tempImageUri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarVerification)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        pagerAdapter = VerificationPagerAdapter(this)
        binding.viewPagerVerification.adapter = pagerAdapter
        binding.viewPagerVerification.isUserInputEnabled = false

        binding.btnNextStep.setOnClickListener {
            handleNextStep()
        }
    }

    fun launchCamera() {
        tempImageUri = getTmpFileUri()
        takePictureLauncher.launch(tempImageUri)
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(applicationContext, "${BuildConfig.APPLICATION_ID}.provider", tmpFile)
    }

    private fun handleNextStep() {
        val currentPosition = binding.viewPagerVerification.currentItem
        if (currentPosition < pagerAdapter.itemCount - 1) {
            binding.viewPagerVerification.currentItem = currentPosition + 1
            if (binding.viewPagerVerification.currentItem == pagerAdapter.itemCount - 1) {
                binding.btnNextStep.text = "Finalizar Verificación"
            }
        } else {
            submitVerificationData()
        }
    }

    private fun submitVerificationData() {
        showLoadingDialog("Enviando verificación...")

        val selfieFragment = pagerAdapter.fragments[0] as VerificationSelfieFragment
        val idFragment = pagerAdapter.fragments[1] as VerificationIdFragment
        val preferencesFragment = pagerAdapter.fragments[2] as VerificationPreferencesFragment

        val selfieUri = selfieFragment.imageUri
        val idUri = idFragment.imageUri
        val preferences = preferencesFragment.getSelectedPreferences()

        if (selfieUri == null || idUri == null) {
            Toast.makeText(this, "Debes tomar ambas fotos para continuar.", Toast.LENGTH_SHORT).show()
            dismissLoadingDialog()
            return
        }

        if (currentUser == null) {
            dismissLoadingDialog()
            return
        }

        val selfieRef = storage.reference.child("verification_files/${currentUser.uid}/selfie.jpg")
        val idRef = storage.reference.child("verification_files/${currentUser.uid}/id_card.jpg")

        val selfieUploadTask = selfieRef.putFile(selfieUri)
        val idUploadTask = idRef.putFile(idUri)

        Tasks.whenAllSuccess<Any>(selfieUploadTask, idUploadTask).addOnSuccessListener {
            val selfieUrlTask = selfieRef.downloadUrl
            val idUrlTask = idRef.downloadUrl

            Tasks.whenAllSuccess<Uri>(selfieUrlTask, idUrlTask).addOnSuccessListener { uris ->
                val selfieUrl = uris[0].toString()
                val idUrl = uris[1].toString()
                updateUserDocument(selfieUrl, idUrl, preferences)
            }.addOnFailureListener { e -> onSaveFailure(e) }
        }.addOnFailureListener { e -> onSaveFailure(e) }
    }

    private fun updateUserDocument(selfieUrl: String, idUrl: String, preferences: Map<String, String>) {
        if (currentUser == null) return

        val userUpdates = mapOf(
            "profileImageUrl" to selfieUrl,
            "idUrl" to idUrl,
            "conversationPref" to (preferences["conversationPref"] ?: ""),
            "musicPref" to (preferences["musicPref"] ?: ""),
            "petsPref" to (preferences["petsPref"] ?: ""),
            "smokingPref" to (preferences["smokingPref"] ?: ""),
            "verificationStatus" to "pending"
        )

        db.collection("users").document(currentUser.uid).update(userUpdates)
            .addOnSuccessListener {
                createVerificationPendingMessage()
            }
            .addOnFailureListener { e -> onSaveFailure(e) }
    }

    private fun createVerificationPendingMessage() {
        if (currentUser == null) return
        val message = AppMessage(
            userId = currentUser.uid,
            title = "Verificación en proceso",
            body = "Hemos recibido tus documentos. Revisaremos tu perfil y te notificaremos en breve. ¡Gracias por tu paciencia!"
        )
        db.collection("messages").add(message)
            .addOnSuccessListener { onSaveSuccess() }
            .addOnFailureListener { onSaveFailure(it) }
    }

    private fun onSaveSuccess() {
        dismissLoadingDialog()
        Toast.makeText(this, "Verificación enviada. Te notificaremos cuando tus datos sean revisados.", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun onSaveFailure(e: Exception) {
        dismissLoadingDialog()
        Toast.makeText(this, "Error al enviar la verificación: ${e.message}", Toast.LENGTH_SHORT).show()
    }

    private fun showLoadingDialog(message: String) {
        if (loadingDialog == null) {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_loading, null)
            val textView = dialogView.findViewById<TextView>(R.id.tvLoadingMessage)
            textView.text = message
            builder.setView(dialogView)
            builder.setCancelable(false)
            loadingDialog = builder.create()
        }
        loadingDialog?.show()
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
    }

    override fun onSupportNavigateUp(): Boolean {
        if (binding.viewPagerVerification.currentItem == 0) {
            super.onBackPressedDispatcher.onBackPressed()
        } else {
            binding.viewPagerVerification.currentItem = binding.viewPagerVerification.currentItem - 1
            binding.btnNextStep.text = "Siguiente"
        }
        return true
    }
}