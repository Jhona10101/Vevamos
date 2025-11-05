package com.webpage.vevamos

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.webpage.vevamos.databinding.ActivityEmailLoginBinding

class EmailLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarEmailLogin)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = Firebase.auth

        binding.buttonLogin.setOnClickListener {
            performLogin()
        }
    }

    // Maneja el clic en la flecha de "Atrás" de la Toolbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    // --- INICIO: FUNCIONES TRASLADADAS DEL ANTIGUO LOGIN ---

    private fun performLogin() {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        if (!validateInput(email, password)) {
            return
        }

        setLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    goToMainActivity()
                } else {
                    handleLoginError(task.exception)
                }
            }
    }

    private fun validateInput(email: String, pass: String): Boolean {
        binding.editTextEmail.error = null
        binding.editTextPassword.error = null

        if (email.isEmpty()) {
            binding.editTextEmail.error = "El correo es obligatorio"
            binding.editTextEmail.requestFocus()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.error = "Introduce un correo válido"
            binding.editTextEmail.requestFocus()
            return false
        }
        if (pass.isEmpty()) {
            binding.editTextPassword.error = "La contraseña es obligatoria"
            binding.editTextPassword.requestFocus()
            return false
        }
        return true
    }

    private fun handleLoginError(exception: Exception?) {
        try {
            throw exception!!
        } catch (e: FirebaseAuthInvalidUserException) {
            binding.editTextEmail.error = "No existe una cuenta con este correo"
            binding.editTextEmail.requestFocus()
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            binding.editTextPassword.error = "La contraseña es incorrecta"
            binding.editTextPassword.requestFocus()
        } catch (e: Exception) {
            Toast.makeText(baseContext, "Fallo en la autenticación: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBarLogin.visibility = View.VISIBLE
            binding.buttonLogin.isEnabled = false
        } else {
            binding.progressBarLogin.visibility = View.GONE
            binding.buttonLogin.isEnabled = true
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        // Cierra todas las actividades anteriores para que el usuario no pueda volver al login
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // --- FIN: FUNCIONES TRASLADADAS ---
}