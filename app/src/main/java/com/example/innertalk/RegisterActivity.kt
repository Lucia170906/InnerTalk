package com.example.innertalk

import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import com.example.innertalk.databinding.RegisterLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    // Mis variables principales para la vista, la autenticación y la base de datos
    private lateinit var binding: RegisterLayoutBinding
    private lateinit var auth: FirebaseAuth
    // Inicializo mi base de datos Firestore
    private val db = FirebaseFirestore.getInstance()
    // Variable para guardar la fecha real y poder calcular bien la edad
    private var fechaSeleccionada: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Preparo Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Llamo a mi función que configura todos los clicks de los botones
        setupButtons()
    }

    private fun setupButtons() {
        // Si el usuario ya tiene cuenta y se ha equivocado de pantalla, lo devuelvo al Login
        binding.tvGoToLogin.setOnClickListener {
            finish()
        }

        // Al pulsar el campo de fecha, abro calendario
        binding.etRegisterBirth.setOnClickListener { showDatePicker() }

        // Lógica para crear la cuenta
        binding.btnRegister.setOnClickListener {
            if (!isNetworkAvailable()) {
                // Texto de "No hay internet" desde strings
                Toast.makeText(this, getString(R.string.error_no_internet), Toast.LENGTH_LONG).show()
                return@setOnClickListener // Cortamos la ejecución aquí, no intentamos hacer login
            }

            // 1. Lo primero es resetear los bordes rojos por si el usuario ya los había puesto en rojo en un intento anterior
            resetearFondos()

            // 2. Recojo todos los datos que ha escrito el usuario
            val email = binding.etRegisterEmail.text.toString().trim()
            val pass = binding.etRegisterPass.text.toString().trim()
            val name = binding.etRegisterName.text.toString().trim()
            val lastName = binding.etRegisterLastName.text.toString().trim()
            val gender = binding.etRegisterGender.text.toString().trim()

            // 3. Compruebo que no se haya dejado campos obligatorios vacíos (UX)
            var camposValidos = true
            var mensajeError = ""

            if (name.isEmpty()) {
                marcarError(binding.etRegisterName)
                camposValidos = false
                mensajeError = getString(R.string.error_name_required)
            }
            if (fechaSeleccionada == null) {
                marcarError(binding.etRegisterBirth)
                camposValidos = false
                if (mensajeError.isEmpty()) mensajeError = getString(R.string.error_birth_required)
            }
            if (email.isEmpty()) {
                marcarError(binding.etRegisterEmail)
                camposValidos = false
                if (mensajeError.isEmpty()) mensajeError = getString(R.string.error_email_required)
            }
            if (pass.isEmpty()) {
                marcarError(binding.etRegisterPass)
                camposValidos = false
                if (mensajeError.isEmpty()) mensajeError = getString(R.string.error_pass_required)
            }

            // Si hay algo vacío, aviso con un Toast y corto la ejecución aquí
            if (!camposValidos) {
                Toast.makeText(this, mensajeError, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!binding.cbPoliticas.isChecked) {
                // Texto de aviso de políticas
                Toast.makeText(this, getString(R.string.error_accept_policies), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // 4. Verifico que el usuario sea mayor de edad con la fecha que he guardado
            if (!esMayorDeEdad(fechaSeleccionada!!)) {
                // Texto de mayoría de edad
                Toast.makeText(this, getString(R.string.error_underage), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // 5. Todo validado, procedo a crear el usuario en Firebase Auth
            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Si se crea bien en Auth, saco su ID único para usarlo en Firestore
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Llamo a mi función para guardar el resto del perfil en la base de datos
                        saveUserData(userId, name, lastName, gender, email)
                    }
                } else {
                    // Si falla, compruebo si es porque el correo ya está registrado
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        marcarError(binding.etRegisterEmail) // Pinto el correo de rojo
                        Toast.makeText(this, getString(R.string.error_email_exists), Toast.LENGTH_LONG).show()
                    } else {
                        // Para cualquier otro error (ej. contraseña muy corta...)
                        Toast.makeText(this, "${getString(R.string.error_general)} ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // --- MIS FUNCIONES AUXILIARES ---

    // Función para crear mi "tabla" (documento) en Firestore y guardar los datos
    private fun saveUserData(uid: String, name: String, lastName: String, gender: String, email: String) {
        val birthDate = binding.etRegisterBirth.text.toString()
        val intereses = obtenerInteresesSeleccionados()

        // Creo un mapa con la estructura de datos que quiero guardar
        val userMap = hashMapOf(
            "uid" to uid,
            "name" to name,
            "lastName" to lastName,
            "email" to email,
            "gender" to gender,
            "birthDate" to birthDate,
            "intereses" to intereses,
            "createdAt" to com.google.firebase.Timestamp.now() // Guardo la fecha exacta de registro
        )

        // Inserto este mapa en la colección "users" usando el UID del usuario como nombre del documento
        db.collection("users").document(uid)
            .set(userMap)
            .addOnSuccessListener {
                // Texto de éxito
                Toast.makeText(this, getString(R.string.auth_msg_success), Toast.LENGTH_SHORT).show()
                goToHome() // Si se guarda bien en base de datos, lo mando a la pantalla principal
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error al guardar en BD: ${e.message}")
                Toast.makeText(this, getString(R.string.error_db_save), Toast.LENGTH_LONG).show()
            }
    }

    // Función para recoger qué checkboxes ha marcado el usuario
    private fun obtenerInteresesSeleccionados(): List<String> {
        val intereses = mutableListOf<String>()
        // Usamos los strings del sistema para los nombres de los intereses si es necesario
        if (binding.cbEstres.isChecked) intereses.add(getString(R.string.auth_interest_stress))
        if (binding.cbAnsiedad.isChecked) intereses.add(getString(R.string.auth_interest_anxiety))
        if (binding.cbSueno.isChecked) intereses.add(getString(R.string.auth_interest_sleep))
        if (binding.cbAnimo.isChecked) intereses.add(getString(R.string.auth_interest_mood))
        return intereses
    }

    // Función que abre el popup del calendario
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = android.app.DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Me guardo la fecha real en mi variable de clase
            fechaSeleccionada = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay)
            }
            // Pinto la fecha formateada en el campo de texto (sumo 1 al mes porque empieza en 0)
            binding.etRegisterBirth.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
        }, year, month, day)

        datePicker.show()
    }

    // Comprobar si tiene 18 años o más
    private fun esMayorDeEdad(fechaNacimiento: Calendar): Boolean {
        val hoy = Calendar.getInstance()
        var edad = hoy.get(Calendar.YEAR) - fechaNacimiento.get(Calendar.YEAR)

        // Le resto un año si aún no ha llegado el día de su cumpleaños en el año actual
        if (hoy.get(Calendar.DAY_OF_YEAR) < fechaNacimiento.get(Calendar.DAY_OF_YEAR)) {
            edad--
        }
        return edad >= 18
    }

    // Funciones visuales (UX) para poner los bordes en rojo o volverlos a su estado normal
    private fun marcarError(editText: EditText) {
        editText.setBackgroundResource(R.drawable.bg_input_field_error)
    }

    private fun resetearFondos() {
        binding.etRegisterName.setBackgroundResource(R.drawable.bg_input_field)
        binding.etRegisterLastName.setBackgroundResource(R.drawable.bg_input_field)
        binding.etRegisterBirth.setBackgroundResource(R.drawable.bg_input_field)
        binding.etRegisterGender.setBackgroundResource(R.drawable.bg_input_field)
        binding.etRegisterEmail.setBackgroundResource(R.drawable.bg_input_field)
        binding.etRegisterPass.setBackgroundResource(R.drawable.bg_input_field)
    }
    //Función para comprobar si hay internet
    private fun isNetworkAvailable() : Boolean{
        //Obtenemos el servio de conectividad que este usando el telfono (wifi o datos)
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //Obtenemos la red activa
        val network = connectivityManager.activeNetwork ?: return false // si no hay red activa devolvemos false

        //Comprobamos si la red atual tiene capacidad de transporte de datos
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return  when{
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    // Navegación final
    private fun goToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        // Hago finish para destruir la pantalla de registro y que no pueda volver a ella con el botón atrás
        finish()
    }
}