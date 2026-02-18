package com.example.innertalk

import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        if (currentUser != null) {
//            // Si existe, saltamos directamente a la Main
//            goToHome()
//            return // Importante para que no cargue el resto de esta pantalla
//        }

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
                        Toast.makeText(
                            this,
                            "Error: ${task.exception?.message}",
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
                        // SI LAS CREDENCIALES SON INCORRECTAS (O no hay internet, etc.):
                        // task.exception te da el motivo (contraseña mal, usuario no existe...)
                        Toast.makeText(this, "Error: las credenciales son incorrectas", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun saveUserData(uid: String, name: String, lastName: String) {
        // Creamos un mapa de datos (clave-valor)
        val userMap = hashMapOf(
            "name" to name,
            "lastName" to lastName,
            "role" to "user",
            "createdAt" to Timestamp.now()
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