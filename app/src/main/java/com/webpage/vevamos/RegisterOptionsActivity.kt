package com.webpage.vevamos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.webpage.vevamos.databinding.ActivityRegisterOptionsBinding

class RegisterOptionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterOptionsBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w("GoogleSignIn", "Google sign in failed", e)
            Toast.makeText(this, "Falló el registro con Google.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.ivClose.setOnClickListener { finish() }

        binding.btnRegisterWithEmail.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnRegisterWithGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        binding.tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val user = authResult.user!!
                val userDocRef = db.collection("users").document(user.uid)

                userDocRef.get()
                    .addOnSuccessListener { document ->
                        if (!document.exists()) {
                            val newUser = User(
                                uid = user.uid,
                                email = user.email,
                                fullName = user.displayName,
                                profileImageUrl = user.photoUrl?.toString(),
                                profileComplete = false,
                                verificationNotified = false // <-- LÍNEA AÑADIDA
                            )
                            userDocRef.set(newUser)
                                .addOnSuccessListener {
                                    startActivity(Intent(this, PhoneAuthActivity::class.java))
                                    finishAffinity()
                                }
                        } else {
                            goToMainActivity()
                        }
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error de autenticación: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finishAffinity()
    }
}