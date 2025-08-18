package com.dertefter.neticlient.ui.person

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.data.model.person.Person
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentPersonViewBinding
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.common.utils.Utils.displayHtml
import com.dertefter.neticlient.ui.fullscreen_image_dialog.FullscreenImageDialog
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PersonViewFragment : Fragment() {

    lateinit var binding: FragmentPersonViewBinding
    private val personViewModel: PersonViewModel by viewModels()

    private val settingsViewModel: SettingsViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPersonViewBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBarLayout.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appBarLayout))

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }


        val personId = arguments?.getString("personId")

        if (personId != null) {
            personViewModel.getLiveDataForId(personId).observe(viewLifecycleOwner){
                if (it.responseType == ResponseType.SUCCESS){
                    binding.skeleton.visibility = View.GONE
                    val person = it.data as Person
                    binding.name.text = person.name

                    if (!person.avatarUrl.isNullOrEmpty()){
                        Picasso.get().load(person.avatarUrl).into(binding.profilePic)
                        binding.profilePic.setOnClickListener {
                            val dialog = FullscreenImageDialog().apply {arguments = Bundle().apply { putString("image_url", person.avatarUrl) } }
                            dialog.show(childFragmentManager, "FullscreenImageDialog")
                        }
                    }

                    if (!person.about_disc.isNullOrEmpty()){
                        binding.aboutDisc.text = person.about_disc
                        (binding.aboutDisc).visibility = View.VISIBLE
                    } else {
                        (binding.aboutDisc).visibility = View.GONE
                    }

                    if (person.hasTimetable){
                        binding.scheduleButton.visibility = View.VISIBLE
                        binding.scheduleButton.setOnClickListener {
                            val link = "https://ciu.nstu.ru/kaf/persons/${personId}/edu_actions/timetables/lessons"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                            startActivity(intent)
                        }
                    } else {
                        binding.scheduleButton.visibility = View.GONE
                    }

                    binding.websiteButton.setOnClickListener {
                        val link = "https://ciu.nstu.ru/kaf/persons/${personId}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                        startActivity(intent)
                    }

                    if (!person.email.isNullOrEmpty()){
                        binding.mail.text = person.email
                        binding.emailCard.visibility = View.VISIBLE
                    } else {
                        binding.emailCard.visibility = View.GONE
                    }

                    if (!person.address.isNullOrEmpty()){
                        binding.location.text = person.address
                        binding.locationCard.visibility = View.VISIBLE
                    } else {
                        binding.locationCard.visibility = View.GONE
                    }

                    if (!person.phone.isNullOrEmpty()){
                        binding.phone.text = person.phone
                        binding.phoneCard.visibility = View.VISIBLE
                    } else {
                        binding.phoneCard.visibility = View.GONE
                    }

                    if (!person.profiles.isNullOrEmpty()){
                        binding.profiles.displayHtml(person.profiles)
                        binding.profilesCard.visibility = View.VISIBLE
                    } else {
                        binding.profilesCard.visibility = View.GONE
                    }

                    if (!person.disceplines.isNullOrEmpty()){
                        binding.disceplines.displayHtml(person.disceplines)
                        binding.discCard.visibility = View.VISIBLE
                    } else {
                        binding.discCard.visibility = View.GONE
                    }
                }
            }

            personViewModel.fetchPerson(personId, false)
        }


    }

}