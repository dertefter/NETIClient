package com.dertefter.neticlient.ui.money

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.money.MoneyItem
import com.dertefter.neticlient.data.model.sessia_results.SessiaResultItem
import com.google.android.material.progressindicator.CircularProgressIndicator

class MoneyRecyclerViewAdapter(private val items: List<MoneyItem>) :
    RecyclerView.Adapter<MoneyRecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val text: TextView = view.findViewById(R.id.text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_money, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.text.text = item.text
    }

    override fun getItemCount(): Int = items.size
}
