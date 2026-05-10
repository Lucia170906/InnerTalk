package com.example.innertalk.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.innertalk.model.ActivityModel
import com.example.innertalk.repository.ActivityRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ActivityViewModel : ViewModel() {

    private val repository = ActivityRepository() //variable para el repository
    private val _activities = MutableLiveData<List<ActivityModel>>()
    val activities : LiveData<List<ActivityModel>> get() = _activities

    // 1. Nos da el catálogo sin checks siempre que lo necesitemos
    fun getCatalogoLimpio(): List<ActivityModel> {
        return repository.getSuggestedActivities()
    }

    // 2. Guarda la lista actual (la que viene de Firebase) para que no se borren los checks
    fun actualizarListaMostrada(lista: List<ActivityModel>) {
        _activities.value = lista
    }

    // 3. Cuando marcas/desmarcas una casilla
    fun activityCompletion(activityId: String) {
        val listaActual = _activities.value?.toMutableList() ?: return
        val index = listaActual.indexOfFirst { it.id == activityId }

        if (index != -1) {
            val nuevaActividad = listaActual[index].copy(isCompleted = !listaActual[index].isCompleted)
            listaActual[index] = nuevaActividad
            _activities.value = listaActual // Actualiza la UI de hoy conservando el resto

            guardarActividadEnFirebase(activityId, nuevaActividad.isCompleted)
        }
    }

    private fun guardarActividadEnFirebase(activityId: String, isChecked: Boolean) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
        val fechaHoy = sdf.format(java.util.Date())

        val dbRef =
            FirebaseDatabase.getInstance("https://innertalk-ca928-default-rtdb.europe-west1.firebasedatabase.app/")
                .reference
                .child("usuarios")
                .child(uid)
                .child("actividades_diarias")
                .child(fechaHoy)

        if (isChecked) {
            dbRef.child(activityId).setValue(true)
        } else {
            dbRef.child(activityId).removeValue()
        }
    }


        fun sincronizarChecksDeHoy(
            catalogoMaestro: List<ActivityModel>,
            idsDeFirebase: List<String>
        ): List<ActivityModel> {
            val idsLimpias = idsDeFirebase.map { it.trim().lowercase() }

            // Recorremos todas las actividades del catálogo maestro una por una
            return catalogoMaestro.map { actividad ->
                val idLocal = actividad.id.trim().lowercase()

                // Comparamos los IDs limpios
                actividad.copy(isCompleted = idsLimpias.contains(idLocal))
            }
        }
    }