package com.dertefter.neticlient.ui.sessia_results

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.sessia_results.SessiaResultItem
import com.google.android.material.progressindicator.CircularProgressIndicator

class SessiaResultAdapter(private val items: List<SessiaResultItem>) :
    RecyclerView.Adapter<SessiaResultAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val score: TextView = view.findViewById(R.id.score_tv)
        val scoreEcts: TextView = view.findViewById(R.id.ects_tv)
        val progress = view.findViewById<CircularProgressIndicator>(R.id.progress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sessia_result, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.score.text = item.score ?: "N/A"
        holder.scoreEcts.text = item.score_ects
        if (!item.score.isNullOrEmpty()){
            holder.progress.progress = item.score.toInt()
        }
        if (item.score_five == "Зач"){
            if (item.score.isNullOrEmpty() || item.score.toInt() == 0){
                holder.progress.progress = 100
                holder.score.text = "100"
                holder.scoreEcts.text = "A"
            }

        }
    }

    override fun getItemCount(): Int = items.size
}
