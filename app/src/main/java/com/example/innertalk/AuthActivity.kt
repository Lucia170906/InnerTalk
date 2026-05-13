package com.example.innertalk

import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.innertalk.databinding.AuthLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthActivity : AppCompatActivity() {

    // 1. Declaramos el ViewBinding y la instancia de FirebaseAuth
    private lateinit var binding: AuthLayoutBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private var fechaSeleccionada: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Si existe, saltamos directamente a la Main
            goToHome()
            return // Importante para que no cargue el resto de esta pantalla
        }

        // 2. Inicializamos Binding
        binding = AuthLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 3. Inicializamos Firebase Auth
        auth = FirebaseAuth.getInstance()

        setupButtons()
    }

    private fun obtenerInteresesSeleccionados(): List<String> {
        val intereses = mutableListOf<String>()
        if (binding.cbEstres.isChecked) intereses.add(getString(R.string.auth_interest_stress))
        if (binding.cbAnsiedad.isChecked) intereses.add(getString(R.string.auth_interest_anxiety))
        if (binding.cbSueno.isChecked) intereses.add(getString(R.string.auth_interest_sleep))
        if (binding.cbAnimo.isChecked) intereses.add(getString(R.string.auth_interest_mood))
        return intereses
    }

    private fun setupButtons() {
        // Evento para abrir el calendario
        binding.etRegisterBirth.setOnClickListener { showDatePicker() }

        binding.btnRegister.setOnClickListener {
            val email = binding.etRegisterEmail.text.toString().trim()
            val pass = binding.etRegisterPass.text.toString().trim()
            val name = binding.etRegisterName.text.toString().trim()
            val lastName = binding.etRegisterLastName.text.toString().trim()
            val gender = binding.etRegisterGender.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty() && name.isNotEmpty() && fechaSeleccionada != null) {

                if (!esMayorDeEdad(fechaSeleccionada!!)) {
                    Toast.makeText(this, getString(R.string.auth_error_underage), Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // 1. Obtenemos el ID único del usuario recién creado
                        val userId = auth.currentUser?.uid

                        // 2. Guardamos los datos extras en Firestore
                        if (userId != null) {
                            saveUserData(userId, name, lastName, gender)
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.auth_error_general), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.auth_error_fill_fields), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etLoginUser.text.toString().trim()
            val pass = binding.etLoginPass.text.toString().trim()

            // 1. Validaciones previas
            if (email.isEmpty()) {
                binding.etLoginUser.error = getString(R.string.auth_error_login_email)
                return@setOnClickListener
            }
            if (pass.isEmpty()) {
                binding.etLoginPass.error = getString(R.string.auth_error_login_pass)
                return@setOnClickListener
            }

            // 2. Comprobación con Firebase
            auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.auth_msg_welcome), Toast.LENGTH_SHORT).show()
                    goToHome()
                } else {
                    Toast.makeText(this, getString(R.string.auth_error_credentials), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = android.app.DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            fechaSeleccionada = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay)
            }
            binding.etRegisterBirth.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
        }, year, month, day)

        datePicker.show()
    }

    private fun esMayorDeEdad(fechaNacimiento: Calendar): Boolean {
        val hoy = Calendar.getInstance()
        var edad = hoy.get(Calendar.YEAR) - fechaNacimiento.get(Calendar.YEAR)

        if (hoy.get(Calendar.DAY_OF_YEAR) < fechaNacimiento.get(Calendar.DAY_OF_YEAR)) {
            edad--
        }
        return edad >= 18
    }

    private fun saveUserData(uid: String, name: String, lastName: String, gender: String) {
        val birthDate = binding.etRegisterBirth.text.toString()
        val intereses = obtenerInteresesSeleccionados()

        val userMap = hashMapOf(
            "name" to name,
            "lastName" to lastName,
            "gender" to gender,
            "birthDate" to birthDate,
            "intereses" to intereses
        )

        db.collection("users").document(uid)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.auth_msg_register_success), Toast.LENGTH_SHORT).show()
                goToHome()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error al guardar: ${e.message}")
                Toast.makeText(this, getString(R.string.auth_error_db, e.message), Toast.LENGTH_LONG).show()
            }
    }

    private fun goToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}