package com.dertefter.neticlient.ui.person

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.databinding.ItemPersonBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.features.person_detail.PersonDetailFeature
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.google.android.material.color.MaterialColors
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PersonListAdapter(
    private val personIds: List<String>,
    private val lifecycleOwner: LifecycleOwner,
    private val onClick: (String) -> Unit,
    val personDetailFeature: PersonDetailFeature
) : RecyclerView.Adapter<PersonListAdapter.PersonAvatarViewHolder>() {

    inner class PersonAvatarViewHolder(
        private val binding: ItemPersonBinding,

    ) : RecyclerView.ViewHolder(binding.root) {

        private var dataLoadJob: Job? = null

        fun bind(personId: String) {
            lifecycleOwner.lifecycleScope.launch {
                personDetailFeature.updatePersonById(personId)
            }

            dataLoadJob?.cancel()

            dataLoadJob = lifecycleOwner.lifecycleScope.launch {
                personDetailFeature.personById(personId)
                    .filterNotNull()
                    .collect { person ->
                        binding.name.text = person.getShortName()
                        if (!person.avatarUrl.isNullOrEmpty()) {

                            Picasso.get()
                                .load(person.avatarUrl)
                                .resize(500, 500)
                                .centerCrop(Gravity.TOP)
                                .into(object : com.squareup.picasso.Target {
                                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                                        binding.profilePic.setImageBitmap(bitmap)
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val scaledBitmap: Bitmap = bitmap?.scale(10, 10, false)?: return@launch

                                            val newContext: Context = DynamicColors.wrapContextIfAvailable(
                                                binding.root.context,
                                                DynamicColorsOptions.Builder()
                                                    .setContentBasedSource(scaledBitmap)
                                                    .build()
                                            )

                                            var cardBg = MaterialColors.getColor(newContext, com.google.android.material.R.attr.colorPrimaryContainer, Color.GRAY)
                                            var titleColor = MaterialColors.getColor(newContext, com.google.android.material.R.attr.colorOnPrimaryContainer, Color.GRAY)

                                            withContext(Dispatchers.Main){
                                                binding.bgGradient.imageTintList = ColorStateList.valueOf(cardBg)
                                                binding.name.setTextColor(titleColor)
                                            }

                                        }
                                    }

                                    override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {}

                                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                        binding.profilePic.setImageDrawable(placeHolderDrawable)
                                    }
                                })


                        } else {
                            binding.profilePic.setImageDrawable(null)
                        }
                    }
            }

            binding.root.setOnClickListener {
                onClick(personId)
            }
        }

        fun onViewRecycled() {
            dataLoadJob?.cancel()
            Picasso.get().cancelRequest(binding.profilePic)
            binding.profilePic.setImageDrawable(null)
            binding.root.setOnClickListener(null) // убираем слушатель при рецикле
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonAvatarViewHolder {
        val binding = ItemPersonBinding.inflate(
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
