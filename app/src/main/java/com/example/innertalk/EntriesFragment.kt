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

    private val entriesList = mutableListOf<EntryModel>()
    private lateinit var adapter: EntriesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEntriesBinding.bind(view)

        setupRecyclerView()
        cargarEntradasDesdeFirebase()
    }

    private fun setupRecyclerView() {
        adapter = EntriesAdapter(entriesList)
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter
    }

    private fun cargarEntradasDesdeFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance("https://innertalk-ca928-default-rtdb.europe-west1.firebasedatabase.app/")
            .reference.child("usuarios").child(uid).child("diario")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                entriesList.clear()
                for (data in snapshot.children) {
                    val entry = data.getValue(EntryModel::class.java)
                    entry?.let { entriesList.add(it) }
                }

                // Invertimos la lista para que la más reciente salga primero
                entriesList.reverse()

                if (entriesList.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                } else {
                    binding.emptyState.visibility = View.GONE
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}