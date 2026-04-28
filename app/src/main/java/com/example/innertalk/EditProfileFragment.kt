package com.example.innertalk

import java.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.fragment.app.Fragment
import com.example.innertalk.databinding.FragmentEditProfileBinding // Se generará solo al crear el XML
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.concurrent.timerTask

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    //Variables de Firebase
    private  lateinit var auth : FirebaseAuth
    private  lateinit var db : FirebaseFirestore

    //Variable para guardar la fecha de nacimiento
    private var fechaSeleccionada : Calendar?=null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Inicializamos Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        //Nada más abrir la pantalla de pedimos los datos a Firebased
        cargarDatosUsuario()

        //Configuramos el click del calendario
        binding.etEditBirth.setOnClickListener {
            showDatePicker()
        }

        binding.btnSaveProfile.setOnClickListener {
            guardarCambios()
        }

    }

    //Función para cargar los datos del usuario
    private  fun cargarDatosUsuario(){
        //Sacamos el usuario que está usando ahora mismo la aplicación
        val uid = auth.currentUser?.uid ?:return // Por si acaso no hay usuario volvemos en vez de que explote la app

        //Vamos a "users" y buscamos su documento
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if(document!=null && document.exists()){
                    //Rellenamos los campos con los datos actuales
                    binding.etEditName.setText(document.getString("name"))
                    binding.etEditLastName.setText(document.getString("lastName"))
                    binding.etEditBirth.setText(document.getString("birthDate"))
                    binding.etEditGender.setText(document.getString("gender"))
                    binding.etEditEmail.setText(document.getString("email"))

                    //La contraseña se deja en blanco por seguridad

                    //Recuperamos la lista de intereses
                    //En Firebase la guardamos como un Array, por lo que la recuperamos como una lista de String
                    val intereses = document.get("intereses") as? List<String> ?: emptyList() // si no hay intereses dejamos la lista vacía

                    //Marcamos los checkbox si los nombres están en la lista
                    binding.cbEditEstres.isChecked = intereses.contains("Estrés")
                    binding.cbEditAnsiedad.isChecked = intereses.contains("Ansiedad")
                    binding.cbEditSueno.isChecked = intereses.contains("Sueño")
                    binding.cbEditAnimo.isChecked = intereses.contains("Ánimo")
                }else{
                    //Si no encontramos datos del usuario hacemos un Toast
                    Toast.makeText(requireContext(), "No se encontraron datos del perfil", LENGTH_SHORT).show()

                }
            }
            //Por si da error
            .addOnFailureListener { e ->
                Log.e("EditProfile", "Error al cargar los datos", e)
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDatePicker() {
        val calendar =Calendar.getInstance()
        val year = calendar.get(android.icu.util.Calendar.YEAR)
        val month = calendar.get(android.icu.util.Calendar.MONTH)
        val day = calendar.get(android.icu.util.Calendar.DAY_OF_MONTH)

        val datePicker = android.app.DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            // Me guardo la fecha real en mi variable de clase
            fechaSeleccionada = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay)
            }
            // Pinto la fecha formateada en el campo de texto (sumo 1 al mes porque empieza en 0)
            binding.etEditBirth.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
        }, year, month, day)

        datePicker.show()
    }

    //Función para guardar los datos en Firebase
    private fun guardarCambios(){
        val appContext = requireContext().applicationContext

        val uid = auth.currentUser?.uid ?: return //cogemos el uid del usuario, si no hay volvemos

        //Recogemos los datos que ha escrito el usuario
        val name = binding.etEditName.text.toString().trim()
        val lastName = binding.etEditLastName.text.toString().trim()
        val birth = binding.etEditBirth.text.toString().trim()
        val gender = binding.etEditGender.text.toString().trim()
        val email = binding.etEditEmail.text.toString().trim()
        val pass = binding.etEditPass.text.toString().trim()

        // Si fechaSeleccionada NO es nula, significa que el usuario ha tocado el calendario para cambiarla
        if (fechaSeleccionada != null) {
            if (!esMayorDeEdad(fechaSeleccionada!!)) {
                Toast.makeText(appContext, "No puedes poner una fecha de menor de 18 años.", Toast.LENGTH_LONG).show()
                return // Cortamos de raíz, no guardamos nada
            }
        }

        // Usamos un mapa mutable para ir metiendo solo los campos que tengan información
        val updates = mutableMapOf<String, Any>()

        if (name.isNotEmpty()) updates["name"] = name
        if (lastName.isNotEmpty()) updates["lastName"] = lastName
        if (birth.isNotEmpty()) updates["birthDate"] = birth
        if (gender.isNotEmpty()) updates["gender"] = gender
        if (email.isNotEmpty()) updates["email"] = email

        //Recogemos los checks
        val intereses = mutableListOf<String>()
        if (binding.cbEditEstres.isChecked) intereses.add("Estrés")
        if (binding.cbEditAnsiedad.isChecked) intereses.add("Ansiedad")
        if (binding.cbEditSueno.isChecked) intereses.add("Sueño")
        if (binding.cbEditAnimo.isChecked) intereses.add("Ánimo")

        updates["intereses"] = intereses

        //Preparamos el paquete de datos para Firestore
        //Usamos mapOf para que solo actualice lo necesario y no borre lo que no se cambia

        //Subimos los datos a Firestore usando el .update
        db.collection("users").document(uid) //coje el documento de este usuario
            .update(updates)//actualiza los campos
            .addOnSuccessListener {
                //si se guardaron bien los cambios pasamosa cambiar contraseña y correo
                actualizarCredenciales(email, pass)

            }
            .addOnFailureListener {
                Toast.makeText(appContext, "Error al actualizar perfil en la base de datos", Toast.LENGTH_SHORT).show()
            }
    }

    //Función para actualizar el correo y la contraseña
    private  fun actualizarCredenciales(nuevoEmail : String, nuevaPass : String ){
        val appContext = requireContext().applicationContext
        val user =auth.currentUser ?: return
        //Comprobamos que el usuario haya escerito un correo distinto al actual
        if(nuevoEmail != user.email && nuevoEmail.isNotEmpty()){
            user.updateEmail(nuevoEmail).addOnCompleteListener{ task ->
                if(!task.isSuccessful){
                    Toast.makeText(appContext, "Aviso: No se pudo cambiar el correo. Por seguridad, necesitas cerrar sesión y volver a entrar para hacer esto.", Toast.LENGTH_LONG).show()
                }

            }
        }
        //comprobamos que el usuario haya escrito una nueva contraseña, si esta vacio no hacemos nada
        if(nuevaPass.isNotEmpty()){
            user.updatePassword(nuevaPass).addOnCompleteListener { task ->
                if(!task.isSuccessful){
                    Toast.makeText(requireContext(), "Aviso: No se pudo cambiar la contraseña. Por seguridad, necesitas cerrar sesión y volver a entrar.", Toast.LENGTH_LONG).show()
                }
             }
        }
        Toast.makeText(requireContext(), "¡Perfil actualizado con éxito!", Toast.LENGTH_SHORT).show()

        // Volvemos a la pantalla anterior automáticamente
        //parentFragmentManager.popBackStack()

    }

    //Funcion para comprobar si es mayor de edad

    private fun esMayorDeEdad(fechaNacimiento: Calendar): Boolean {
        val hoy = android.icu.util.Calendar.getInstance()
        var edad = hoy.get(android.icu.util.Calendar.YEAR) - fechaNacimiento.get(android.icu.util.Calendar.YEAR)

        // Le resto un año si aún no ha llegado el día de su cumpleaños en el año actual
        if (hoy.get(android.icu.util.Calendar.DAY_OF_YEAR) < fechaNacimiento.get(android.icu.util.Calendar.DAY_OF_YEAR)) {
            edad--
        }
        return edad >= 18
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}