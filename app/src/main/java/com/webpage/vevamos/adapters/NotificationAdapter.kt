package com.webpage.vevamos.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.webpage.vevamos.R
import com.webpage.vevamos.UserNotification
import com.webpage.vevamos.databinding.ItemNotificationBinding

class NotificationAdapter(
    private var notifications: List<UserNotification>
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun getItemCount(): Int = notifications.size

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.binding.tvNotificationTitle.text = notification.title
        holder.binding.tvNotificationBody.text = notification.body

        val iconResId = when (notification.type) {
            "system_verification" -> R.drawable.ic_verified
            "trip_booked" -> R.drawable.ic_pending
            else -> R.drawable.ic_add_circle
        }
        holder.binding.ivNotificationIcon.setImageResource(iconResId)
    }

    fun updateNotifications(newNotifications: List<UserNotification>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }
}