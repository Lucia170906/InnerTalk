package com.example.innertalk.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.innertalk.model.ActivityModel
import com.example.innertalk.repository.ActivityRepository

class ActivityViewModel : ViewModel() {

    private val repository = ActivityRepository() //variable para el repository
    private val _activities = MutableLiveData<List<ActivityModel>>()
    val activities : LiveData<List<ActivityModel>> get() = _activities

    //funcion para cargar las actividades
    fun loadActivities(){
//        val listaPrueba = listOf(
//            ActivityModel(1, "Meditación", "Relaja tu mente", "10 min", R.drawable.ic_meditation),
//            ActivityModel(2, "Caminar", "Sal a dar una vuelta", "30 min", R.drawable.ic_walk)
//        )
        _activities.value = repository.getSuggestedActivities()
    }

    //funcion para actualizar el estado de la tarea ( heha o no)

    fun activityCompletion(activityId : Int) {
        val currentList = _activities.value.toMutableList()
        val index =
            currentList.indexOfFirst { it.id == activityId } // esto recorre toda la lista hasta hasta encontrar el id que
        //coincide con la actividad pulsada
        if (index != null && index != -1) {
            val activity = currentList[index]

            // cambiamso estado del check
            currentList[index] = activity.copy(isCompleted = !activity.isCompleted)// usamos copy porque al cambair tan solo una variable el sistema puede ignorarlo y np actuañizar la lista
            //al ser una copia nos aseguramos que actulice al momento y con el nuevo atributo
            _activities.value = currentList
        }
    }



}