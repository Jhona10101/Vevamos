package com.webpage.vevamos

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.webpage.vevamos.databinding.ActivityVerificationBinding
import java.util.UUID

class VerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerificationBinding
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    // Variables para guardar las URIs de las imágenes seleccionadas
    private var selfieUri: Uri? = null
    private var documentUri: Uri? = null

    // --- TU LÓGICA PARA SELECCIONAR IMÁGENES (INTEGRADA) ---
    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selfieUri = uri
            binding.ivSelfieCheck.visibility = View.VISIBLE
        }
    }

    private val selectDocumentLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            documentUri = uri
            binding.ivDocumentCheck.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Listeners para abrir la galería
        binding.cardSelfie.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }
        binding.cardDocument.setOnClickListener {
            selectDocumentLauncher.launch("image/*")
        }

        // Listener del botón principal con la nueva lógica
        binding.btnVerify.setOnClickListener {
            if (selfieUri == null || documentUri == null) {
                Toast.makeText(this, "Por favor, selecciona ambas imágenes", Toast.LENGTH_SHORT).show()
            } else {
                uploadDataAndImages()
            }
        }
    }

    private fun uploadDataAndImages() {
        val userId = currentUser?.uid ?: return

        // 1. Mostramos el ProgressBar y desactivamos el botón para evitar clics múltiples
        binding.progressBar.visibility = View.VISIBLE
        binding.btnVerify.isEnabled = false

        val selfieRef = storage.reference.child("verification_files/$userId/selfie_${UUID.randomUUID()}.jpg")
        val documentRef = storage.reference.child("verification_files/$userId/document_${UUID.randomUUID()}.jpg")

        // Tareas de subida para ambas imágenes
        val selfieUploadTask = selfieRef.putFile(selfieUri!!)
        val documentUploadTask = documentRef.putFile(documentUri!!)

        // 2. Esperamos a que AMBAS subidas terminen y luego obtenemos sus URLs
        Tasks.whenAllSuccess<UploadTask.TaskSnapshot>(selfieUploadTask, documentUploadTask).continueWithTask {
            // Este bloque se ejecuta SOLO si AMBAS imágenes se subieron
            val selfieDownloadUrlTask = selfieRef.downloadUrl
            val documentDownloadUrlTask = documentRef.downloadUrl
            Tasks.whenAllSuccess<Uri>(selfieDownloadUrlTask, documentDownloadUrlTask)
        }.addOnCompleteListener { task ->
            // 3. Este bloque se ejecuta al final, ya sea con éxito o con error
            binding.progressBar.visibility = View.GONE
            binding.btnVerify.isEnabled = true

            if (task.isSuccessful) {
                // ÉXITO TOTAL: Tenemos las URLs, ahora actualizamos Firestore
                val downloadUrls = task.result
                val selfieUrl = downloadUrls[0].toString()
                val documentUrl = downloadUrls[1].toString()
                updateFirestore(userId, selfieUrl, documentUrl)
            } else {
                // ERROR: Alguna de las tareas falló
                Toast.makeText(this, "Error al subir las imágenes: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateFirestore(userId: String, selfieUrl: String, documentUrl: String) {
        val userUpdates = hashMapOf(
            "selfieUrl" to selfieUrl,
            "documentUrl" to documentUrl,
            "verificationStatus" to "pending" // Establecemos el estado a "en revisión"
        )

        db.collection("users").document(userId)
            .update(userUpdates as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Datos enviados. Recibirás una notificación cuando sean revisados.", Toast.LENGTH_LONG).show()
                finish() // Cierra la actividad
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar tus datos: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}