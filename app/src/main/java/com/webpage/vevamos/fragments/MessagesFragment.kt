package com.example.vevamos.fragments// Asegúrate de que este sea tu paquete correcto

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
import com.webpage.vevamos.databinding.FragmentMessagesBinding

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private var currentUser: FirebaseUser? = null
    private lateinit var messageAdapter: MessageAdapter // Declarado aquí

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

        db = FirebaseFirestore.getInstance()
        currentUser = FirebaseAuth.getInstance().currentUser

        // --- ESTA ES LA LÍNEA QUE SOLUCIONA EL CRASH ---
        // Se inicializa el adaptador con una lista vacía antes de usarlo.
        messageAdapter = MessageAdapter(emptyList())
        // --- FIN DE LA SOLUCIÓN ---

        setupRecyclerView() // Ahora esta línea es segura de llamar
        loadMessages()
    }

    // Esta función ahora recibe un adaptador que SÍ existe.
    private fun setupRecyclerView() {
        binding.rvMessages.layoutManager = LinearLayoutManager(context)
        binding.rvMessages.adapter = messageAdapter
    }

    private fun loadMessages() {
        if (currentUser == null) {
            binding.tvNoMessages.visibility = View.VISIBLE
            return
        }

        val query = db.collection("messages")
            .whereEqualTo("userId", currentUser!!.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        messagesListener = query.addSnapshotListener { snapshots, error ->
            if (_binding == null) return@addSnapshotListener
            if (error != null) {
                // Manejar error
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val messages = snapshots.toObjects(AppMessage::class.java)
                messageAdapter.updateMessages(messages)
                binding.tvNoMessages.visibility = if (messages.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        messagesListener?.remove()
        _binding = null
    }
}