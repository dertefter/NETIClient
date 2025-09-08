package com.dertefter.neticlient.ui.person_search

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticore.features.person_detail.model.Person
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.ui.person.PersonListStyle
import com.dertefter.neticlient.ui.person.PersonViewModel
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso

class PersonSearchRecyclerViewAdapter(
    var personIds: List<Pair<String, String>> = emptyList(),
    val fragment: Fragment,
    val listStyle: PersonListStyle,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<PersonSearchRecyclerViewAdapter.PersonViewHolder>() {

    fun setData(newList: List<Pair<String, String>>) {
        this.personIds = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val layoutId = when (listStyle) {
            PersonListStyle.AVATARS_ONLY -> R.layout.item_person_avatar
            PersonListStyle.SMALL_TEXT -> R.layout.item_person_small_text
            PersonListStyle.LIST -> R.layout.item_person_list
            else -> R.layout.item_person_card
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return PersonViewHolder(view, onClick, fragment)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val item = personIds[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = personIds.size

    override fun getItemId(position: Int): Long {
        return personIds[position].hashCode().toLong()
    }

    override fun onViewRecycled(holder: PersonViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    class PersonViewHolder(
        itemView: View,
        private val onClick: (String) -> Unit,
        fragment: Fragment
    ) : RecyclerView.ViewHolder(itemView) {

        private val name: TextView? = itemView.findViewById(R.id.name)
        private val profilePic: ShapeableImageView? = itemView.findViewById(R.id.profilePic)

        private val personViewModel: PersonViewModel by fragment.activityViewModels()

        private var currentLiveData: LiveData<Any>? = null
        private var currentObserver: Observer<Any>? = null

        fun bind(personId: Pair<String, String>) {
            unbind()
            name?.text = personId.first
            profilePic?.visibility = View.GONE
            @Suppress("UNCHECKED_CAST")


            val observer = Observer<Any> { response ->
                val resp = response as? ResponseResult
                if (resp != null && resp.responseType == ResponseType.SUCCESS) {
                    val person = resp.data as Person
                    name?.text = person.name
                    if (!person.avatarUrl.isNullOrEmpty() && profilePic != null) {
                        profilePic?.visibility = View.VISIBLE
                        Picasso.get()
                            .load(person.avatarUrl)
                            .resize(200, 200)
                            .centerCrop()
                            .into(profilePic)
                    }else{
                        profilePic?.visibility = View.GONE
                    }
                }
            } as Observer<Any>
            currentObserver = observer

            itemView.setOnClickListener {
                onClick(personId.second)
            }
        }

        fun unbind() {
            currentObserver?.let { currentLiveData?.removeObserver(it) }
            currentLiveData = null
            currentObserver = null
        }
    }
}
