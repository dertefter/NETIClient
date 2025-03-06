package com.dertefter.neticlient.ui.person

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.person.Person
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.ui.schedule.week.day.DayFragment
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso

class PersonListRecyclerViewAdapter(
    var personIds: List<String> = emptyList(),
    val fragment: DayFragment,
    val listStyle: PersonListStyle
) : RecyclerView.Adapter<PersonListRecyclerViewAdapter.PersonViewHolder>() {

    fun setData(newList: List<String>) {
        this.personIds = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val view = if (listStyle == PersonListStyle.AVATARS_ONLY){
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_person_avatar, parent, false)
        } else {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_person, parent, false)
        }

        return PersonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val item = personIds[position]
        holder.bind(item, fragment)
    }

    override fun getItemCount(): Int = personIds.size



    class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView? = itemView.findViewById(R.id.name)
        private val profilePic: ShapeableImageView = itemView.findViewById(R.id.profilePic)
        fun bind(personId: String, fragment: DayFragment) {
            val personViewModel: PersonViewModel by fragment.activityViewModels()
            personViewModel.getLiveDataForId(personId).observeForever {
                if (it.responseType == ResponseType.SUCCESS){
                    val person = it.data as Person
                    name?.text = person.name
                    if (!person.avatarUrl.isNullOrEmpty()){
                        Picasso.get()
                            .load(person.avatarUrl)
                            .resize(200,200)
                            .centerCrop()
                            .into(profilePic)
                    }
                }
            }
            personViewModel.fetchPerson(personId)


        }
    }
}
