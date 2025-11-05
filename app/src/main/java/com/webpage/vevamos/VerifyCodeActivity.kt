package com.webpage.vevamos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.webpage.vevamos.databinding.ActivityVerifyCodeBinding

class VerifyCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyCodeBinding
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private var storedVerificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storedVerificationId = intent.getStringExtra("verificationId")

        binding.tvResendCode.setOnClickListener {
            finish()
        }

        binding.etSmsCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 6) {
                    verifyCode(s.toString())
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun verifyCode(code: String) {
        if (storedVerificationId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: ID de verificación no encontrado.", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
        linkPhoneCredential(credential)
    }

    private fun linkPhoneCredential(credential: PhoneAuthCredential) {
        auth.currentUser?.linkWithCredential(credential)
            ?.addOnSuccessListener {
                updateUserProfileWithPhone()
            }
            ?.addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(this, "Error al vincular el teléfono: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateUserProfileWithPhone() {
        val phoneNumber = intent.getStringExtra("phoneNumber")
        val acceptsPromotions = intent.getBooleanExtra("acceptsPromotions", false)
        val currentUser = auth.currentUser

        if (currentUser == null || phoneNumber.isNullOrEmpty()) {
            setLoading(false)
            return
        }

        val userUpdates = mapOf(
            "phoneNumber" to phoneNumber,
            "acceptsPromotions" to acceptsPromotions,
            "profileComplete" to true
        )

        db.collection("users").document(currentUser.uid).update(userUpdates)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(this, "¡Número verificado con éxito!", Toast.LENGTH_SHORT).show()

                if (intent.getBooleanExtra("launched_from_profile", false)) {
                    val resultIntent = Intent().apply {
                        putExtra("verified_phone_number", phoneNumber)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    goToMainActivity()
                }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(this, "Error al guardar tu perfil: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finishAffinity()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}