package com.example.innertalk

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.innertalk.databinding.FragmentNewEntryBinding

class NewEntryFragment : Fragment() {

    private lateinit var binding : FragmentNewEntryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewEntryBinding.inflate(layoutInflater)
        return binding.root
    }


}