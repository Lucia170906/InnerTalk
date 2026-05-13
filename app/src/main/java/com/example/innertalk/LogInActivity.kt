package com.example.innertalk

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.innertalk.databinding.LoginLayoutBinding
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {

    private  lateinit var binding: LoginLayoutBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            goToHome()
            return
        }

        binding = LoginLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            // No hacemos finish() aquí para que el usuario pueda darle al botón de 'Atrás'
        }

        binding.btnLogin.setOnClickListener {
            //primero comprobamo si hay internet para hacer la consulta a firebase
            if (!isNetworkAvailable()) {
                Toast.makeText(this, getString(R.string.error_no_internet), Toast.LENGTH_LONG).show()
                return@setOnClickListener // Cortamos la ejecución aquí, no intentamos hacer login
            }
            // Restaurar fondos
            binding.etLoginUser.setBackgroundResource(R.drawable.bg_input_field)
            binding.etLoginPass.setBackgroundResource(R.drawable.bg_input_field)

            val email = binding.etLoginUser.text.toString().trim()
            val pass = binding.etLoginPass.text.toString().trim()

            var isValid = true

            if (email.isEmpty()) {
                binding.etLoginUser.setBackgroundResource(R.drawable.bg_input_field_error)
                binding.etLoginUser.error = getString(R.string.error_field_required)
                isValid = false
            }
            if (pass.isEmpty()) {
                binding.etLoginPass.setBackgroundResource(R.drawable.bg_input_field_error)
                binding.etLoginPass.error = getString(R.string.error_field_required)
                isValid = false
            }

            if (!isValid) return@setOnClickListener

            auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.login_msg_welcome), Toast.LENGTH_SHORT).show()
                    goToHome()
                } else {
                    Toast.makeText(this, getString(R.string.login_error_credentials), Toast.LENGTH_LONG).show()
                }
            }
        }
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

    private fun goToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}