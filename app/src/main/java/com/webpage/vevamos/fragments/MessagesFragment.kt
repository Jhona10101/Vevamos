package com.webpage.vevamos.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.webpage.vevamos.UserNotification
import com.webpage.vevamos.adapters.NotificationAdapter
import com.webpage.vevamos.databinding.FragmentMessagesBinding

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    // CORRECCIÓN 1: Declaramos que usaremos nuestro nuevo NotificationAdapter
    private lateinit var notificationAdapter: NotificationAdapter
    private var listener: ListenerRegistration? = null

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

        // CORRECCIÓN 2: Inicializamos el NotificationAdapter con una lista vacía
        notificationAdapter = NotificationAdapter(emptyList())

        setupRecyclerView()
        loadNotifications()
    }

    private fun setupRecyclerView() {
        binding.rvMessages.layoutManager = LinearLayoutManager(context)
        // CORRECCIÓN 3: Le decimos al RecyclerView que use nuestro nuevo adaptador
        binding.rvMessages.adapter = notificationAdapter
    }

    private fun loadNotifications() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            binding.tvNoMessages.visibility = View.VISIBLE
            return
        }

        // --- CORRECCIÓN MÁS IMPORTANTE ---
        // Ahora sí, escuchamos la colección correcta: "user_notifications"
        val query = db.collection("user_notifications")
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        listener = query.addSnapshotListener { snapshots, error ->
            if (_binding == null || error != null) {
                // Si hay un error o la vista ya no existe, no hacemos nada
                return@addSnapshotListener
            }

            if (snapshots != null) {
                // Usamos nuestro nuevo molde de datos "UserNotification"
                val notifications = snapshots.toObjects(UserNotification::class.java)
                notificationAdapter.updateNotifications(notifications)
                binding.tvNoMessages.visibility = if (notifications.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.remove() // Detenemos la escucha para evitar fugas de memoria
        _binding = null
    }
}