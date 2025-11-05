package com.webpage.vevamos

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.webpage.vevamos.databinding.ItemTripBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileTripAdapter(
    private var trips: List<Trip>,
    private val isPublishedList: Boolean,
    private val onRateClick: (Trip) -> Unit,
    private val onDeleteClick: (Trip) -> Unit
) : RecyclerView.Adapter<ProfileTripAdapter.TripViewHolder>() {

    inner class TripViewHolder(val binding: ItemTripBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val currentTrip = trips[position]
        holder.binding.apply {
            textViewOrigin.text = currentTrip.origin
            textViewDestination.text = currentTrip.destination
            textViewDate.text = "${currentTrip.date} a las ${currentTrip.time}"
            textViewPrice.text = "${currentTrip.price}€ por asiento"

            root.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, TripDetailActivity::class.java).apply {
                    putExtra("EXTRA_TRIP", currentTrip)
                }
                context.startActivity(intent)
            }

            btnRateTrip.visibility = View.GONE
            btnDeleteTrip.visibility = View.GONE

            if (isPublishedList) {
                btnDeleteTrip.visibility = View.VISIBLE
                btnDeleteTrip.setOnClickListener {
                    AlertDialog.Builder(holder.itemView.context)
                        .setTitle("Eliminar Viaje")
                        .setMessage("¿Estás seguro? Esta acción no se puede deshacer.")
                        .setPositiveButton("Eliminar") { _, _ ->
                            onDeleteClick(currentTrip)
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            } else {
                if (hasTripFinished(currentTrip.date)) {
                    btnRateTrip.visibility = View.VISIBLE
                    btnRateTrip.setOnClickListener {
                        onRateClick(currentTrip)
                        it.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun hasTripFinished(tripDateStr: String?): Boolean {
        if (tripDateStr == null) return false
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.isLenient = false
            val tripDate = sdf.parse(tripDateStr)
            val today = Date()
            tripDate?.before(today) == true
        } catch (e: Exception) {
            false
        }
    }

    override fun getItemCount(): Int {
        return trips.size
    }

    fun updateTrips(newTrips: List<Trip>) {
        trips = newTrips
        notifyDataSetChanged()
    }
}