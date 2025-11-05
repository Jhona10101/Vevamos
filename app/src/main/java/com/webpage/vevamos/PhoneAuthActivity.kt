package com.webpage.vevamos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.webpage.vevamos.databinding.ActivityPhoneAuthBinding
import java.util.concurrent.TimeUnit

class PhoneAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhoneAuthBinding
    private val auth = Firebase.auth
    private var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ccp.setDefaultCountryUsingNameCode("EC")
        binding.ccp.registerCarrierNumberEditText(binding.etPhoneNumber)

        binding.fabNext.setOnClickListener {
            sendVerificationCode()
        }

        binding.tvSkip.setOnClickListener {
            goToMainActivity()
        }
    }

    private fun sendVerificationCode() {
        if (!binding.ccp.isValidFullNumber) {
            binding.etPhoneNumber.error = "Número de teléfono no válido"
            return
        }
        val fullPhoneNumber = binding.ccp.fullNumberWithPlus
        setLoading(true)

        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(fullPhoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)

        forceResendingToken?.let {
            optionsBuilder.setForceResendingToken(it)
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            setLoading(false)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            setLoading(false)
            Toast.makeText(applicationContext, "Falló la verificación: ${e.message}", Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            setLoading(false)
            forceResendingToken = token

            val intent = Intent(applicationContext, VerifyCodeActivity::class.java).apply {
                putExtra("verificationId", verificationId)
                putExtra("phoneNumber", binding.ccp.fullNumberWithPlus)
                putExtra("acceptsPromotions", binding.cbPromotions.isChecked)
                putExtra("launched_from_profile", getIntent().getBooleanExtra("launched_from_profile", false))
            }
            startActivity(intent)
        }
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.fabNext.isEnabled = !isLoading
    }
}