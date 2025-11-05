package com.example.vevamos // Asegúrate de que este sea tu paquete correcto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.webpage.vevamos.adapters.MessageAdapter
import com.webpage.vevamos.databinding.FragmentMessagesBinding

// Asumo que tienes clases como estas, ajusta los nombres si es necesario
// import com.example.vevamos.adapters.MessageAdapter
// import com.example.vevamos.models.AppMessage

class MessagesFragment : Fragment() {

    // --- MEJORA 1: Patrón de View Binding seguro para Fragments ---
    // Esto evita crashes si intentas acceder a la vista después de que haya sido destruida.
    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!

    // --- VARIABLES DE CLASE ---
    // Mueve las variables que necesitas en todo el fragmento aquí.
    private lateinit var db: FirebaseFirestore
    private var currentUser: FirebaseUser? = null
    private lateinit var messageAdapter: MessageAdapter // Asegúrate de inicializar tu adaptador

    // --- SOLUCIÓN: Referencia para el Listener de Firestore ---
    // Esta variable guardará nuestro "oyente" para poder apagarlo después.
    private var messagesListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa tus variables aquí
        db = FirebaseFirestore.getInstance()
        currentUser = FirebaseAuth.getInstance().currentUser
        // messageAdapter = MessageAdapter(...) // Aquí debes crear la instancia de tu adaptador

        // Llama a tus funciones de configuración
        setupRecyclerView()
        loadMessages()
    }

    private fun setupRecyclerView() {
        binding.rvMessages.layoutManager = LinearLayoutManager(context)
        binding.rvMessages.adapter = messageAdapter
    }

    private fun loadMessages() {
        // Si el usuario no ha iniciado sesión, no hagas nada.
        if (currentUser == null) {
            binding.tvNoMessages.visibility = View.VISIBLE
            return
        }

        val query = db.collection("messages")
            .whereEqualTo("userId", currentUser!!.uid) // Usamos !! porque ya comprobamos que no es nulo
            .orderBy("timestamp", Query.Direction.DESCENDING)

        // --- SOLUCIÓN: Asigna el listener a tu variable ---
        messagesListener = query.addSnapshotListener { snapshots, error ->
            // Si la vista ya no existe (el usuario se fue), no hagas nada.
            if (_binding == null) {
                return@addSnapshotListener
            }

            // Manejo de errores de Firestore
            if (error != null) {
                // Aquí podrías mostrar un Toast o un log con el error.
                // Log.e("FirestoreError", "Listen failed.", error)
                return@addSnapshotListener
            }

            // Procesar los datos recibidos
            if (snapshots != null) {
                // val messages = snapshots.toObjects(AppMessage::class.java)
                // messageAdapter.updateMessages(messages)
                // binding.tvNoMessages.visibility = if (messages.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // --- SOLUCIÓN: Desconecta el listener ---
        // Esto es CRUCIAL. Evita que el listener siga funcionando en segundo plano,
        // previniendo crashes y fugas de memoria (memory leaks).
        messagesListener?.remove()

        // --- MEJORA 1 (continuación): Limpia la referencia al binding ---
        _binding = null
    }
}