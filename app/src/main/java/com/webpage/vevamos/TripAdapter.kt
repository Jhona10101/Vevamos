package com.webpage.vevamos

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.webpage.vevamos.databinding.ItemTripBinding

class TripAdapter(private var trips: List<Trip>) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    inner class TripViewHolder(val binding: ItemTripBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val currentTrip = trips[position]
        holder.binding.apply {
            // Mostramos la ciudad en lugar de la dirección completa
            textViewOrigin.text = currentTrip.originCity ?: currentTrip.origin
            textViewDestination.text = currentTrip.destinationCity ?: currentTrip.destination

            textViewDate.text = "${currentTrip.date} a las ${currentTrip.time}"
            textViewPrice.text = "${currentTrip.price}€ por asiento"
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, TripDetailActivity::class.java).apply {
                putExtra("EXTRA_TRIP", currentTrip)
            }
            context.startActivity(intent)
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