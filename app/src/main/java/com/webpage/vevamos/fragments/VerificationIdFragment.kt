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
import com.webpage.vevamos.databinding.FragmentVerificationIdBinding // Asegúrate que este es el nombre de tu binding
import java.io.File

class VerificationIdFragment : Fragment() {

    private var _binding: FragmentVerificationIdBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null

    // --- SOLUCIÓN 1: Declarar el ActivityResultLauncher ---
    // Este "lanzador" se encarga de abrir la cámara y recibir el resultado.
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        // Este bloque se ejecuta DESPUÉS de que el usuario toma la foto.
        if (success) {
            // La foto se guardó correctamente en la 'imageUri' que creamos.
            // Aquí puedes mostrar la imagen en un ImageView si quieres.
            // Por ejemplo: binding.imageViewPreview.setImageURI(imageUri)
            Toast.makeText(requireContext(), "Foto capturada con éxito", Toast.LENGTH_SHORT).show()
        } else {
            // El usuario canceló la captura o hubo un error.
            Toast.makeText(requireContext(), "Captura de foto cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerificationIdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Asumo que tienes un botón o un CardView para abrir la cámara
        // Reemplaza 'binding.btnOpenCamera' con el ID de tu botón real
        binding.btnOpenCamera.setOnClickListener {
            // --- SOLUCIÓN 2: Usar el lanzador ---
            // Creamos una URI temporal donde se guardará la foto
            imageUri = createImageUri()
            // Llamamos al método launch() del lanzador para abrir la cámara
            takePictureLauncher.launch(imageUri)
        }
    }

    /**
     * Crea una URI de archivo temporal para guardar la imagen de la cámara.
     * Esto es necesario para que la cámara tenga un lugar donde escribir el archivo.
     */
    private fun createImageUri(): Uri? {
        val context = requireContext()
        val imageFile = File(context.cacheDir, "temp_id_photo_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // Asegúrate que esto coincida con tu FileProvider en el Manifest
            imageFile
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}