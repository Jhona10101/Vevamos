package com.webpage.vevamos.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webpage.vevamos.VerificationActivity
import com.webpage.vevamos.databinding.FragmentVerificationSelfieBinding

class VerificationSelfieFragment : Fragment() {

    private var _binding: FragmentVerificationSelfieBinding? = null
    val binding get() = _binding!!
    var imageUri: Uri? = null // Variable para guardar la URI de la foto

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerificationSelfieBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnTakeSelfie.setOnClickListener {
            // Llama a la función de la Activity para abrir la cámara
            (activity as? VerificationActivity)?.launchCamera()
        }
    }

    // Función para que la Activity actualice la imagen
    fun setImage(uri: Uri?) {
        imageUri = uri
        binding.ivSelfie.setImageURI(uri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}