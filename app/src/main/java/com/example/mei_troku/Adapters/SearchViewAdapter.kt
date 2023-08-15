package com.example.mei_troku.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mei_troku.DataClass.PlaceAddress
import com.example.mei_troku.R
import com.example.mei_troku.ViewHolders.PlaceViewHolder

class SearchViewAdapter(
    var places: List<PlaceAddress> = emptyList(),
    private val onItemSelected: (Double) -> Unit
) : RecyclerView.Adapter<PlaceViewHolder>() {

    fun onUpdateList(placeList: List<PlaceAddress>) {
        this.places = placeList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PlaceViewHolder(layoutInflater.inflate(R.layout.item_searchview, parent, false))
    }

    override fun getItemCount() = places.size

    override fun onBindViewHolder(viewHolder: PlaceViewHolder, position: Int) {
        viewHolder.bind(places[position], onItemSelected)
    }
}