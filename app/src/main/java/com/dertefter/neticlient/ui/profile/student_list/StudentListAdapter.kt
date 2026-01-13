package com.dertefter.neticlient.ui.profile.student_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.databinding.ItemStudentBinding
import com.dertefter.neticore.features.students.model.Student
import com.squareup.picasso.Picasso

class StudentListAdapter(
    var picasso: Picasso,
    private val onItemClick: (Student) -> Unit
) : ListAdapter<Student, StudentListAdapter.StudentViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean =
            oldItem == newItem
    }

    inner class StudentViewHolder(
        private val binding: ItemStudentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(student: Student) {
            binding.name.text = student.getShortName()
            binding.isGroupMonitor.isGone = student.isGroupMonitor != 1
            picasso.load(student.photo).into(binding.profilePic)

            binding.root.setOnClickListener {
                onItemClick(student)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
