package com.webpage.vevamos.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.webpage.vevamos.Trip
import com.webpage.vevamos.TripAdapter
import com.webpage.vevamos.databinding.FragmentSearchBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    // AHORA binding es nulable para poder comprobarlo
    private val binding get() = _binding

    private val db = Firebase.firestore
    private lateinit var tripAdapter: TripAdapter
    private var allTrips = listOf<Trip>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadAllTrips()

        binding?.btnSearch?.setOnClickListener {
            performSearch()
        }
    }

    private fun setupRecyclerView() {
        tripAdapter = TripAdapter(listOf())
        binding?.tripsRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tripAdapter
        }
    }

    private fun loadAllTrips() {
        binding?.progressBar?.visibility = View.VISIBLE

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val todayDate = sdf.format(Date())

        db.collection("trips")
            .whereGreaterThanOrEqualTo("date", todayDate)
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                // --- INICIO: CORRECCIÓN DE SEGURIDAD CLAVE ---
                // Si el binding es nulo (la vista ha sido destruida), no hacemos nada.
                if (_binding == null || e != null) {
                    Log.w("SearchFragment", "Listen failed or fragment destroyed.", e)
                    return@addSnapshotListener
                }
                // --- FIN: CORRECCIÓN DE SEGURIDAD CLAVE ---

                binding?.progressBar?.visibility = View.GONE

                val tripList = snapshots?.map { document ->
                    val trip = document.toObject(Trip::class.java)
                    trip.documentId = document.id
                    trip
                } ?: listOf()

                allTrips = tripList
                tripAdapter.updateTrips(allTrips)
            }
    }

    private fun performSearch() {
        val originQuery = binding?.etOriginSearch?.text.toString().trim().lowercase()
        val destinationQuery = binding?.etDestinationSearch?.text.toString().trim().lowercase()

        val filteredList = allTrips.filter { trip ->
            val matchesOrigin = if (originQuery.isNotEmpty()) {
                trip.originCity?.lowercase()?.contains(originQuery) == true
            } else {
                true
            }

            val matchesDestination = if (destinationQuery.isNotEmpty()) {
                trip.destinationCity?.lowercase()?.contains(destinationQuery) == true
            } else {
                true
            }

            matchesOrigin && matchesDestination
        }

        tripAdapter.updateTrips(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}