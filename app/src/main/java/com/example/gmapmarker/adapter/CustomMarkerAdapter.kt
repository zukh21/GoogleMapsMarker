package com.example.gmapmarker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gmapmarker.databinding.MarkerListItemBinding
import com.example.gmapmarker.dto.MarkerDataEntity
import com.example.gmapmarker.interfaces.ItemListener

class CustomMarkerAdapter(private val listener: ItemListener) :
    ListAdapter<MarkerDataEntity, CustomMarkerAdapter.CustomViewHolder>(ItemComparator()) {
    class CustomViewHolder(private val binding: MarkerListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(markerDataEntity: MarkerDataEntity, listener: ItemListener) = with(binding) {
            title.text = markerDataEntity.title
            description.text = markerDataEntity.description
            lat.text = markerDataEntity.lat.toString()
            lng.text = markerDataEntity.lng.toString()
            edit.setOnClickListener {
                updateGroup.visibility = View.VISIBLE
            }
            ready.setOnClickListener {
                listener.markerUpdate(markerDataEntity, descEditText.text.toString())
                updateGroup.visibility = View.VISIBLE
            }

            itemView.setOnClickListener {
                listener.markerItemOnClick(markerDataEntity)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding =
            MarkerListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }

    class ItemComparator : DiffUtil.ItemCallback<MarkerDataEntity>() {
        override fun areItemsTheSame(
            oldItem: MarkerDataEntity,
            newItem: MarkerDataEntity,
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: MarkerDataEntity,
            newItem: MarkerDataEntity,
        ): Boolean = oldItem == newItem

    }
}