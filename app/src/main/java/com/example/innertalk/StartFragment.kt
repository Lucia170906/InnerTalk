package com.example.innertalk

import ActivitiesAdapter
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.innertalk.databinding.FragmentStartBinding
import com.example.innertalk.viewModel.ActivityViewModel

class StartFragment : Fragment() {
    private var _binding : FragmentStartBinding? = null
    private  val binding  get ()  =_binding!!

    private lateinit var  viewModel : ActivityViewModel
    private lateinit var  adapter: ActivitiesAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStartBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(ActivityViewModel::class.java)

        adapter = ActivitiesAdapter (
            onPlayClicked = {url ->
                //cogemos la url y entramos en youtube/ navegador
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            },
            onCheckClicked = {id ->
                //marcamos la actividad como completada
                viewModel.activityCompletion(id)

            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@StartFragment.adapter
            // Esto es vital cuando el Recycler está dentro de un Scroll
            isNestedScrollingEnabled = false
        }

        viewModel.activities.observe(viewLifecycleOwner) { lista ->
            lista?.let {
                adapter.submitList(it)
            }
        }

        viewModel.loadActivities()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}