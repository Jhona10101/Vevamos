package com.webpage.vevamos.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.webpage.vevamos.AppMessage
import com.webpage.vevamos.databinding.ItemMessageBinding
import java.text.SimpleDateFormat
import java.util.Locale

class MessageAdapter(private var messages: List<AppMessage>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.binding.tvMessageTitle.text = message.title
        holder.binding.tvMessageBody.text = message.body

        message.timestamp?.let {
            val sdf = SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm", Locale.getDefault())
            holder.binding.tvMessageDate.text = sdf.format(it)
        }
    }

    override fun getItemCount() = messages.size

    fun updateMessages(newMessages: List<AppMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }
}