package com.dertefter.neticlient.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.databinding.ItemPersonAvatarBinding
import com.dertefter.neticore.features.students.model.Student
import com.squareup.picasso.Picasso

class StudentAvatarListAdapter(
    var picasso: Picasso
) : ListAdapter<Student, StudentAvatarListAdapter.StudentViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean =
            oldItem == newItem
    }

    inner class StudentViewHolder(
        private val binding: ItemPersonAvatarBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(student: Student) {


            picasso.load(student.photo).into(binding.profilePic)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemPersonAvatarBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
