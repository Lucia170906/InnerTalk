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
import com.google.firebase.Timestamp
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
        if (binding.cbEstres.isChecked) intereses.add("Estrés")
        if (binding.cbAnsiedad.isChecked) intereses.add("Ansiedad")
        if (binding.cbSueno.isChecked) intereses.add("Sueño")
        if (binding.cbAnimo.isChecked) intereses.add("Ánimo")
        return intereses
    }

    private fun setupButtons() {
        // Evento para abrir el calendario
        binding.etRegisterBirth.setOnClickListener { showDatePicker() }

        binding.btnRegister.setOnClickListener {
            val email = binding.etRegisterEmail.text.toString()
            val pass = binding.etRegisterPass.text.toString()
            val name = binding.etRegisterName.text.toString()

            val lastName = binding.etRegisterLastName.text.toString()
            val gender = binding.etRegisterGender.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty() && name.isNotEmpty() && fechaSeleccionada!=null) {

                if (!esMayorDeEdad(fechaSeleccionada!!)) {
                    Toast.makeText(this, "Lo sentimos, debes ser mayor de 18 años para usar InnerTalk", Toast.LENGTH_LONG).show()
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
                        Toast.makeText(
                            this,
                            "Error: ",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "Por favor, rellena al menos nombre, email y clave",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
            binding.btnLogin.setOnClickListener {
                val email = binding.etLoginUser.text.toString().trim()
                val pass = binding.etLoginPass.text.toString().trim()

                // 1. Validaciones previas (no enviar campos vacíos a Firebase)
                if (email.isEmpty()) {
                    binding.etLoginUser.error = "Introduce tu correo"
                    return@setOnClickListener
                }
                if (pass.isEmpty()) {
                    binding.etLoginPass.error = "Introduce tu contraseña"
                    return@setOnClickListener
                }

                // 2. Comprobación con Firebase
                // signInWithEmailAndPassword es la función que verifica las credenciales
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // SI LAS CREDENCIALES SON VÁLIDAS:
                        Toast.makeText(this, "¡Bienvenido de nuevo!", Toast.LENGTH_SHORT).show()
                        goToHome() // Función que hace el salto a MainActivity
                    } else {
                        // task.exception te da el motivo (contraseña mal, usuario no existe...)
                        Toast.makeText(this, "Error: las credenciales son incorrectas", Toast.LENGTH_LONG).show()
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
            // Guardamos la fecha elegida
            fechaSeleccionada = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay)
            }
            // Mostramos la fecha en el EditText
            binding.etRegisterBirth.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
        }, year, month, day)

        datePicker.show()
    }

    private fun esMayorDeEdad(fechaNacimiento: Calendar): Boolean {
        val hoy = Calendar.getInstance()
        var edad = hoy.get(Calendar.YEAR) - fechaNacimiento.get(Calendar.YEAR)

        // Ajuste si aún no ha cumplido años en el mes actual
        if (hoy.get(Calendar.DAY_OF_YEAR) < fechaNacimiento.get(Calendar.DAY_OF_YEAR)) {
            edad--
        }
        return edad >= 18
    }

    private fun saveUserData(uid: String, name: String, lastName: String, gender: String) {
        // Obtenemos la fecha del EditText y los intereses
        val birthDate = binding.etRegisterBirth.text.toString()
        val intereses = obtenerInteresesSeleccionados()

        val userMap = hashMapOf(
            "name" to name,
            "lastName" to lastName,
            "gender" to gender,
            "birthDate" to birthDate,
            "intereses" to intereses,
            //"createdAt" to com.google.firebase.Timestamp.now()
        )

        // Esta línea creará automáticamente la colección "users" en tu 2ª foto
        db.collection("users").document(uid)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "¡Cuenta creada con éxito!", Toast.LENGTH_SHORT).show()
                goToHome()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error al guardar: ${e.message}")
                Toast.makeText(this, "Error en base de datos: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun goToHome() {
        // Cuando el usuario entra, lo mandamos a la pantalla principal
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Cerramos esta pantalla para que no pueda volver atrás con el botón del móvil
    }
}