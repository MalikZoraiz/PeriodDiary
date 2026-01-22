package com.nexadev.perioddiary.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nexadev.perioddiary.R

data class CycleInfo(val value: String, val label: String)

class CycleInfoAdapter(private val items: List<CycleInfo>) : RecyclerView.Adapter<CycleInfoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val valueText: TextView = view.findViewById(R.id.info_value_text)
        val labelText: TextView = view.findViewById(R.id.info_label_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cycle_info_page, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.valueText.text = item.value
        holder.labelText.text = item.label
    }

    override fun getItemCount() = items.size
}