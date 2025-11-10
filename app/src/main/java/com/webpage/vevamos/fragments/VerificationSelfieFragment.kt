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
import com.webpage.vevamos.databinding.FragmentVerificationSelfieBinding
import java.io.File

class VerificationSelfieFragment : Fragment() {

    private var _binding: FragmentVerificationSelfieBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            binding.ivCheckSelfie.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "Selfie capturada", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Captura cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerificationSelfieBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardSelfie.setOnClickListener {
            imageUri = createImageUri()
            takePictureLauncher.launch(imageUri)
        }
    }

    private fun createImageUri(): Uri? {
        val context = requireContext()
        val imageFile = File.createTempFile("temp_selfie_photo_", ".jpg", context.cacheDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}