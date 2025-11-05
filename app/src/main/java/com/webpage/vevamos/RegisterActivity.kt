package com.webpage.vevamos

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.webpage.vevamos.databinding.ActivityRegisterBinding
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var pagerAdapter: RegisterViewPagerAdapter
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private var email = ""
    private var password = ""
    private var fullName = ""
    private var birthDate = ""

    private var isActivityActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarRegister)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        pagerAdapter = RegisterViewPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.isUserInputEnabled = false

        binding.fabNext.setOnClickListener {
            handleNextStep()
        }
    }

    override fun onResume() {
        super.onResume()
        isActivityActive = true
    }

    override fun onPause() {
        super.onPause()
        isActivityActive = false
    }

    override fun onSupportNavigateUp(): Boolean {
        if (binding.viewPager.currentItem == 0) {
            super.onBackPressedDispatcher.onBackPressed()
        } else {
            binding.viewPager.currentItem = binding.viewPager.currentItem - 1
        }
        return true
    }

    private fun handleNextStep() {
        when (binding.viewPager.currentItem) {
            0 -> validateEmailStep()
            1 -> validatePasswordStep()
            2 -> validateNameStep()
            3 -> performRegistration()
        }
    }

    private fun validateEmailStep() {
        val fragment = pagerAdapter.fragments[0] as RegisterEmailFragment
        val emailInput = fragment.binding.etEmail.text.toString().trim()
        if (emailInput.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            fragment.binding.etEmail.error = "Introduce un email válido"
            return
        }
        email = emailInput
        binding.viewPager.currentItem = 1
    }

    private fun validatePasswordStep() {
        val fragment = pagerAdapter.fragments[1] as RegisterPasswordFragment
        val passwordInput = fragment.binding.etPassword.text.toString().trim()
        if (passwordInput.length < 6) {
            fragment.binding.etPassword.error = "La contraseña debe tener al menos 6 caracteres"
            return
        }
        password = passwordInput
        binding.viewPager.currentItem = 2
    }

    private fun validateNameStep() {
        val fragment = pagerAdapter.fragments[2] as RegisterNameFragment
        val nameInput = fragment.binding.etFullName.text.toString().trim()
        if (nameInput.isEmpty()) {
            fragment.binding.etFullName.error = "El nombre es obligatorio"
            return
        }
        fullName = nameInput
        binding.viewPager.currentItem = 3
        setupBirthdateListener()
    }

    private fun setupBirthdateListener() {
        val fragment = pagerAdapter.fragments[3] as RegisterBirthDateFragment
        fragment.binding.tvBirthDate.setOnClickListener {
            showBirthDatePicker()
        }
    }

    private fun showBirthDatePicker() {
        val fragment = pagerAdapter.fragments[3] as RegisterBirthDateFragment
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                birthDate = String.format("%02d/%02d/%d", day, month + 1, year)
                fragment.binding.tvBirthDate.text = birthDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        calendar.add(Calendar.YEAR, -18)
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun performRegistration() {
        if (listOf(email, password, fullName, birthDate).any { it.isEmpty() }) {
            showErrorDialog("Información Incompleta", "Por favor, selecciona tu fecha de nacimiento para continuar.")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user!!.uid
                val user = User(
                    uid = uid,
                    email = email,
                    fullName = fullName,
                    dateOfBirth = birthDate, // Guardamos la fecha de nacimiento
                    profileComplete = false,
                    verificationNotified = false // Inicializamos el campo
                )
                db.collection("users").document(uid).set(user)
                    .addOnSuccessListener {
                        startActivity(Intent(this, PhoneAuthActivity::class.java))
                        finishAffinity()
                    }
                    .addOnFailureListener { e ->
                        showErrorDialog("Error de Base de Datos", "No se pudo guardar tu perfil: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                when (e) {
                    is FirebaseAuthUserCollisionException -> {
                        showErrorDialog("Cuenta Existente", "Ya existe una cuenta registrada con este correo electrónico.")
                    }
                    else -> {
                        showErrorDialog("Error de Registro", e.message ?: "Ocurrió un error inesperado.")
                    }
                }
            }
    }

    private fun showErrorDialog(title: String, message: String) {
        if (isActivityActive) {
            AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Aceptar", null)
                .show()
        }
    }
}