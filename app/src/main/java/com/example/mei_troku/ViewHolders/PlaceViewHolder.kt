package com.example.mei_troku.ViewHolders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.mei_troku.DataClass.PlaceAddress
import com.example.mei_troku.databinding.ItemSearchviewBinding

class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemSearchviewBinding.bind(view)

    fun bind(place: PlaceAddress, onItemSelected: (Double) -> Unit) {
        binding.placeTitle.text = place.displayName
        binding.placeDescription.text = "Pais: " + place.countryCode

        binding.root.setOnClickListener {
            onItemSelected(place.latitude)
        }
    }
}