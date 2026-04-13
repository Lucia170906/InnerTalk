package com.example.innertalk

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.innertalk.adapter.EntriesAdapter
import com.example.innertalk.databinding.FragmentEntriesBinding
import com.example.innertalk.model.EntryModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EntriesFragment : Fragment(R.layout.fragment_entries) {

    private var _binding: FragmentEntriesBinding? = null
    private val binding get() = _binding!!

    // La lista donde guardaremos las notas de la nube
    private val entriesList = mutableListOf<EntryModel>()
    private lateinit var adapter: EntriesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEntriesBinding.bind(view)

        // Preparamos el RecyclerView y lanzamos la carga de datos
        setupRecyclerView()
        cargarEntradasDesdeFirebase()
    }

    private fun setupRecyclerView() {
        // Inicializamos el adapter y le pasamos nuestra lista al principio vacía
        adapter = EntriesAdapter(entriesList)
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter
    }

    private fun cargarEntradasDesdeFirebase() {
        // Pillamos el ID del usuario actual para saber qué diario leer
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Apuntamos a la URL exacta de nuestra Realtime Database en Europa
        val dbRef = FirebaseDatabase.getInstance("https://innertalk-ca928-default-rtdb.europe-west1.firebasedatabase.app/")
            .reference.child("usuarios").child(uid).child("diario")

        // Escuchamos los cambios: si el usuario añade una nota nueva
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Limpiamos la lista para no duplicar datos al refrescar
                entriesList.clear()

                // Recorremos cada cada nota que hay en el nodo diario
                for (data in snapshot.children) {
                    val entry = data.getValue(EntryModel::class.java)
                    entry?.let { entriesList.add(it) }
                }

                // Las entradas de Firebase vienen de vieja a nueva, así que les damos la vuelta
                // para que el usuario vea lo que escribió hoy al principio de la lista
                entriesList.reverse()

                // Si no hay nada escrito, mostramos el "empty state" (el dibujito de que está vacío)
                if (entriesList.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                } else {
                    binding.emptyState.visibility = View.GONE
                }

                // Le avisamos al adapter de que los datos han cambiado para que repinte la lista
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}