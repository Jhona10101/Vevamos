package com.webpage.vevamos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webpage.vevamos.databinding.FragmentRegisterNameBinding

class RegisterNameFragment : Fragment() {
    private var _binding: FragmentRegisterNameBinding? = null
    // Esta propiedad solo es válida entre onCreateView y onDestroyView.
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}