package com.dertefter.neticlient.ui.person

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.databinding.ItemPersonAvatarBinding
import com.dertefter.neticore.NETICore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class AvatarListAdapter(
    private val personIds: List<String>,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<AvatarListAdapter.PersonAvatarViewHolder>() {

    inner class PersonAvatarViewHolder(
        private val binding: ItemPersonAvatarBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var dataLoadJob: Job? = null

        fun bind(personId: String) {
            lifecycleOwner.lifecycleScope.launch {
                NETICore.personDetailFeature.updatePersonById(personId)
            }

            dataLoadJob?.cancel()

            dataLoadJob = lifecycleOwner.lifecycleScope.launch {
                NETICore.personDetailFeature.personById(personId)
                    .filterNotNull()
                    .collect { person ->
                        if (!person.avatarUrl.isNullOrEmpty()) {
                            Picasso.get()
                                .load(person.avatarUrl)
                                .resize(500,500)
                                .centerCrop(Gravity.TOP)
                                .into(binding.profilePic)
                        } else {
                            binding.profilePic.setImageDrawable(null)
                        }
                    }
            }
        }

        fun onViewRecycled() {
            dataLoadJob?.cancel()
            Picasso.get().cancelRequest(binding.profilePic)
            binding.profilePic.setImageDrawable(null)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonAvatarViewHolder {
        val binding = ItemPersonAvatarBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PersonAvatarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PersonAvatarViewHolder, position: Int) {
        holder.bind(personIds[position])
    }

    override fun getItemCount(): Int = personIds.size

    override fun onViewRecycled(holder: PersonAvatarViewHolder) {
        super.onViewRecycled(holder)
        holder.onViewRecycled()
    }
}
