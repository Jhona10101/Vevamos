package com.webpage.vevamos.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
// import androidx.glance.visibility // <-- ERROR CORREGIDO: Línea eliminada
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.webpage.vevamos.AppMessage
import com.webpage.vevamos.adapters.MessageAdapter
import com.webpage.vevamos.databinding.FragmentMessagesBinding

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    // ERROR CORREGIDO: Se elimina la propiedad `binding` para usar solo `_binding` de forma segura.
    // private val binding get() = _binding!!

    private val db = Firebase.firestore
    private val currentUser = Firebase.auth.currentUser

    // El adapter se puede inicializar de forma perezosa para mayor seguridad.
    private val messageAdapter: MessageAdapter by lazy { MessageAdapter(emptyList()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        // Se accede a la raíz de la vista (que no es nula aquí) para devolverla.
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SOLUCIÓN: Usamos el operador `let` para acceder a `_binding` de forma segura.
        // Todo el código que dependa de las vistas va dentro de este bloque.
        _binding?.let { binding ->
            setupRecyclerView(binding)
            loadMessages(binding)
        }
    }

    // Se pasa `binding` como parámetro para asegurar que no es nulo.
    private fun setupRecyclerView(binding: FragmentMessagesBinding) {
        binding.rvMessages.adapter = messageAdapter
        binding.rvMessages.layoutManager = LinearLayoutManager(context)
    }

    // Se pasa `binding` como parámetro.
    private fun loadMessages(binding: FragmentMessagesBinding) {
        if (currentUser == null) {
            binding.tvNoMessages.visibility = View.VISIBLE
            return
        }

        db.collection("messages")
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                // IMPORTANTE: Se comprueba que el binding siga existiendo antes de usarlo.
                // Si el usuario sale de la pantalla, _binding será nulo y esto evitará un crash.
                val currentBinding = _binding ?: return@addSnapshotListener

                if (error != null) {
                    // Aquí podrías mostrar un error al usuario.
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val messages = snapshots.toObjects(AppMessage::class.java)
                    messageAdapter.updateMessages(messages)
                    currentBinding.tvNoMessages.visibility = if (messages.isEmpty()) View.VISIBLE else View.GONE
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpia la referencia al binding para evitar fugas de memoria.
        _binding = null
    }
}
