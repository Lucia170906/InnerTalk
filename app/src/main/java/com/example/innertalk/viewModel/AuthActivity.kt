package com.example.innertalk.viewModel

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.innertalk.MainActivity
import com.example.innertalk.databinding.AuthLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthActivity : AppCompatActivity() {

    // 1. Declaramos el ViewBinding y la instancia de FirebaseAuth
    private lateinit var binding: AuthLayoutBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Inicializamos Binding
        binding = AuthLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 3. Inicializamos Firebase Auth
        auth = FirebaseAuth.getInstance()

        setupButtons()
    }

    private fun setupButtons() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etRegisterEmail.text.toString()
            val pass = binding.etRegisterPass.text.toString()
            val name = binding.etRegisterName.text.toString()
            val lastName = binding.etRegisterLastName.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && name.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // 1. Obtenemos el ID único del usuario recién creado
                        val userId = auth.currentUser?.uid

                        // 2. Guardamos los datos extras en Firestore
                        if (userId != null) {
                            saveUserData(userId, name, lastName)
                        }
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, rellena al menos nombre, email y clave", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserData(uid: String, name: String, lastName: String) {
        // Creamos un mapa de datos (clave-valor)
        val userMap = hashMapOf(
            "name" to name,
            "lastName" to lastName,
            "role" to "user",
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        // Guardamos en la colección "users" usando el UID como nombre del documento
        db.collection("users").document(uid)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "¡Perfil creado correctamente!", Toast.LENGTH_SHORT).show()
                goToHome()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar perfil: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun goToHome() {
        // Cuando el usuario entra, lo mandamos a la pantalla principal
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Cerramos esta pantalla para que no pueda volver atrás con el botón del móvil
    }
}