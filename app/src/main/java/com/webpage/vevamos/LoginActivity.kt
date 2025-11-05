package com.webpage.vevamos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.webpage.vevamos.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private var loadingDialog: AlertDialog? = null

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            dismissLoadingDialog()
            Log.w("GoogleSignIn", "Google sign in failed", e)
            Toast.makeText(this, "Falló el inicio de sesión con Google.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.ivClose.setOnClickListener { finish() }

        binding.btnContinueWithEmail.setOnClickListener {
            startActivity(Intent(this, EmailLoginActivity::class.java))
        }

        binding.btnContinueWithGoogle.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterOptionsActivity::class.java))
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        showLoadingDialog("Iniciando sesión con Google...")

        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val user = authResult.user!!
                val userDocRef = db.collection("users").document(user.uid)

                userDocRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists() && document.getBoolean("profileComplete") == true) {
                            goToMainActivity()
                        } else {
                            val newUser = User(
                                uid = user.uid,
                                email = user.email,
                                fullName = user.displayName,
                                profileImageUrl = user.photoUrl?.toString(),
                                profileComplete = false
                            )
                            userDocRef.set(newUser, com.google.firebase.firestore.SetOptions.merge())
                                .addOnSuccessListener { goToPhoneVerification() }
                                .addOnFailureListener { e ->
                                    dismissLoadingDialog()
                                    Toast.makeText(this, "Error al crear perfil: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        dismissLoadingDialog()
                        Toast.makeText(this, "Error al verificar perfil: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                dismissLoadingDialog()
                Toast.makeText(this, "Error de autenticación: ${e.message}", Toast.LENGTH_LONG).show()
            }
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

    private fun goToMainActivity() {
        dismissLoadingDialog()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finishAffinity()
    }

    private fun goToPhoneVerification() {
        dismissLoadingDialog()
        val intent = Intent(this, PhoneAuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finishAffinity()
    }
}