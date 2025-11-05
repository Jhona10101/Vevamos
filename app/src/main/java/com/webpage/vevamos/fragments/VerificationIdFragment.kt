package com.webpage.vevamos.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webpage.vevamos.VerificationActivity
import com.webpage.vevamos.databinding.FragmentVerificationIdBinding

class VerificationIdFragment : Fragment() {

    private var _binding: FragmentVerificationIdBinding? = null
    val binding get() = _binding!!
    var imageUri: Uri? = null // Variable para guardar la URI de la foto

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerificationIdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnTakePhotoId.setOnClickListener {
            // Llama a la función de la Activity para abrir la cámara
            (activity as? VerificationActivity)?.launchCamera()
        }
    }

    // Función para que la Activity actualice la imagen
    fun setImage(uri: Uri?) {
        imageUri = uri
        binding.ivIdCard.setImageURI(uri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}