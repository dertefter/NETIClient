package com.dertefter.neticlient.ui.dashboard.control_weeks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.data.model.control_weeks.ControlSemestr
import com.dertefter.neticlient.databinding.ItemControlSemestrBinding
import com.dertefter.neticlient.databinding.ItemControlWeekBinding

// Adapter class
class ControlSemesterAdapter(
    private var semesters: List<ControlSemestr>
) : RecyclerView.Adapter<ControlSemesterAdapter.SemesterViewHolder>() {

    fun updateSemestrs(semesters: List<ControlSemestr>){
        this.semesters = semesters
        notifyDataSetChanged()
    }

    inner class SemesterViewHolder(
        private val binding: ItemControlSemestrBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(semester: ControlSemestr) {
            binding.title.text = semester.title

            // Очищаем предыдущие элементы
            binding.items.removeAllViews()

            // Добавляем элементы недель
            semester.items?.forEach { week ->
                week.items?.forEach { item ->
                    // Создаем элемент недели
                    val itemBinding = ItemControlWeekBinding.inflate(
                        LayoutInflater.from(binding.root.context),
                        binding.items,
                        false
                    )

                    // Заполняем данные
                    itemBinding.weekNumber.text = week.title
                    itemBinding.title.text = item.title
                    itemBinding.value.text = item.value
                    itemBinding.title.isVisible = !item.title.isNullOrEmpty()
                    itemBinding.value.isVisible = !item.value.isNullOrEmpty()
                    itemBinding.weekNumber.isVisible = !week.title.isNullOrEmpty()
                    // Добавляем в контейнер
                    binding.items.addView(itemBinding.root)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SemesterViewHolder {
        val binding = ItemControlSemestrBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SemesterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SemesterViewHolder, position: Int) {
        holder.bind(semesters[position])
    }

    override fun getItemCount() = semesters.size
}