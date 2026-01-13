package com.dertefter.neticlient.ui.login

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.databinding.ItemUserBinding
import com.dertefter.neticore.features.authorization.model.User
import com.google.android.material.color.MaterialColors

class UserListAdapter(
    private val onUserClick: (User) -> Unit,
    private val onLogoutClick: (User) -> Unit,
    private val onDeleteAccountClick: (User) -> Unit
) : ListAdapter<User, UserListAdapter.UserViewHolder>(DiffCallback()) {

    private var selectedLogin: String? = null

    fun setSelectedUser(login: String?) {
        val previous = selectedLogin
        selectedLogin = login
        if (previous != null) {
            val oldIndex = currentList.indexOfFirst { it.login == previous }
            if (oldIndex != -1) notifyItemChanged(oldIndex)
        }
        val newIndex = currentList.indexOfFirst { it.login == login }
        if (newIndex != -1) notifyItemChanged(newIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.login.text = user.login
            if (user.login == selectedLogin){
                binding.logoutButton.isVisible = true
                binding.deleteAccountButton.isGone = true
                binding.root.setOnClickListener {}
                val cardColor = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorSecondary)
                val textColor = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOnSecondary)
                binding.root.setCardBackgroundColor(cardColor)
                binding.login.setTextColor(textColor)


            } else {
                binding.logoutButton.isGone = true
                binding.deleteAccountButton.isVisible = true
                binding.root.setOnClickListener {
                    onUserClick(user)
                }
                val cardColor = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorSurfaceVariant)
                val textColor = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOnSurfaceVariant)
                binding.root.setCardBackgroundColor(cardColor)
                binding.login.setTextColor(textColor)
            }

            binding.logoutButton.setOnClickListener {
                onLogoutClick(user)
            }
            binding.deleteAccountButton.setOnClickListener {
                onDeleteAccountClick(user)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.login == newItem.login

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem == newItem
    }
}
