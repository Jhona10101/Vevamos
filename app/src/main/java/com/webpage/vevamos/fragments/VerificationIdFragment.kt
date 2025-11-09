package com.webpage.vevamos.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.webpage.vevamos.databinding.FragmentVerificationIdBinding
import java.io.File

class VerificationIdFragment : Fragment() {

    private var _binding: FragmentVerificationIdBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null

    // --- LÓGICA AÑADIDA: Declarar el "lanzador" que abre la cámara ---
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // La foto se guardó en la 'imageUri'.
            // Mostramos el check de éxito en la UI.
            binding.ivCheckId.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "Foto del documento capturada", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Captura cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerificationIdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- CORRECCIÓN PRINCIPAL ---
        // Usamos el ID correcto de tu CardView ('cardId')
        binding.cardId.setOnClickListener {
            // Creamos una URI temporal para la foto
            imageUri = createImageUri()
            // Usamos el lanzador para abrir la cámara
            takePictureLauncher.launch(imageUri)
        }
    }

    // --- LÓGICA AÑADIDA: Función para crear una URI segura ---
    private fun createImageUri(): Uri? {
        val context = requireContext()
        val imageFile = File.createTempFile("temp_id_photo_", ".jpg", context.cacheDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // Esto debe coincidir con tu Manifest
            imageFile
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}