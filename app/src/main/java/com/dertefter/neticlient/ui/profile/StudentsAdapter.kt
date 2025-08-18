package com.dertefter.neticlient.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R

class StudentsAdapter(
) : RecyclerView.Adapter<StudentsAdapter.StudentViewHolder>() {

    var students: List<String> = emptyList()

    fun updateStudents(students: List<String>){
        this.students = students
        notifyDataSetChanged()
    }

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.nameTextView.text = students[position]
    }

    override fun getItemCount(): Int = students.size
}