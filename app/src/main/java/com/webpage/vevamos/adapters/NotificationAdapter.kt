package com.webpage.vevamos.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.webpage.vevamos.R
import com.webpage.vevamos.UserNotification
import com.webpage.vevamos.databinding.ItemNotificationBinding

class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    // --- CORRECCIÓN CLAVE 1 ---
    // La lista ahora es interna y privada (private var) para un mejor control.
    // Se inicializa como una lista vacía mutable.
    private var notificationList = mutableListOf<UserNotification>()

    // El ViewHolder no necesita cambios, está perfecto.
    inner class NotificationViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    // --- CORRECCIÓN CLAVE 2 ---
    // El tamaño ahora se basa en la lista interna 'notificationList'.
    override fun getItemCount(): Int = notificationList.size

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        // Obtenemos la notificación de nuestra lista interna.
        val notification = notificationList[position]
        holder.binding.tvNotificationTitle.text = notification.title
        holder.binding.tvNotificationBody.text = notification.body

        // La lógica para los íconos está perfecta.
        val iconResId = when (notification.type) {
            "system_verification" -> R.drawable.ic_verified
            "trip_booked" -> R.drawable.ic_pending
            else -> R.drawable.ic_add_circle
        }
        holder.binding.ivNotificationIcon.setImageResource(iconResId)
    }

    // --- CORRECCIÓN CLAVE 3 ---
    // Esta función ahora es la forma correcta de actualizar los datos.
    // Borra la lista antigua, añade todos los datos nuevos y notifica al RecyclerView
    // para que se redibuje por completo.
    fun updateNotifications(newNotifications: List<UserNotification>) {
        notificationList.clear()
        notificationList.addAll(newNotifications)
        notifyDataSetChanged()
    }
}