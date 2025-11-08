package com.webpage.vevamos.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.webpage.vevamos.databinding.ItemMessageBinding // <-- ESTO DARÁ ERROR AHORA, SE SOLUCIONA LUEGO
import java.text.SimpleDateFormat
import java.util.Locale

// --- Modelo de Datos ---
// Representa la estructura de un documento en tu colección "messages" de Firestore
data class AppMessage(
    val text: String = "",
    val userId: String = "",
    val timestamp: Timestamp? = null
    // Añade aquí cualquier otro campo que tengan tus mensajes
)

// --- Adaptador ---
class MessageAdapter(
    private var messages: List<AppMessage>
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    // El ViewHolder contiene las vistas de una sola fila (item_message.xml)
    inner class MessageViewHolder(val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun getItemCount(): Int = messages.size

    // Esta función conecta los datos de un mensaje con las vistas del ViewHolder
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.binding.tvMessageText.text = message.text

        // Convierte el Timestamp de Firebase a un formato de fecha legible
        message.timestamp?.let {
            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            holder.binding.tvMessageTimestamp.text = sdf.format(it.toDate())
        }
    }

    // Esta función es llamada desde el fragment para actualizar la lista de mensajes
    fun updateMessages(newMessages: List<AppMessage>) {
        messages = newMessages
        notifyDataSetChanged() // Refresca la lista visualmente
    }
}