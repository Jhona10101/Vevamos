package com.webpage.vevamos.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import com.webpage.vevamos.databinding.FragmentVerificationPreferencesBinding

class VerificationPreferencesFragment : Fragment() {

    private var _binding: FragmentVerificationPreferencesBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerificationPreferencesBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Función para obtener las preferencias seleccionadas
    fun getSelectedPreferences(): Map<String, String> {
        val preferences = mutableMapOf<String, String>()

        preferences["conversationPref"] = getSelectedRadioButtonText(binding.rgConversation.checkedRadioButtonId)
        preferences["musicPref"] = getSelectedRadioButtonText(binding.rgMusic.checkedRadioButtonId)
        preferences["petsPref"] = getSelectedRadioButtonText(binding.rgPets.checkedRadioButtonId)
        preferences["smokingPref"] = getSelectedRadioButtonText(binding.rgSmoking.checkedRadioButtonId)

        return preferences
    }

    private fun getSelectedRadioButtonText(checkedId: Int): String {
        return if (checkedId != -1) {
            view?.findViewById<RadioButton>(checkedId)?.text?.toString() ?: ""
        } else {
            ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}