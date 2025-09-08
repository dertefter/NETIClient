package com.dertefter.neticlient.ui.person

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticore.features.person_detail.model.Person
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentPersonViewBinding
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.common.utils.Utils.displayHtml
import com.dertefter.neticlient.ui.fullscreen_image_dialog.FullscreenImageDialog
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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

    fun collectingPersonDetail(personId: String){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                personViewModel.personById(personId).collect { person ->
                    binding.skeleton.isGone = person != null

                    if (person != null){
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
                            binding.profiles.displayHtml(person.profiles!!)
                            binding.profilesCard.visibility = View.VISIBLE
                        } else {
                            binding.profilesCard.visibility = View.GONE
                        }

                        if (!person.disceplines.isNullOrEmpty()){
                            binding.disceplines.displayHtml(person.disceplines!!)
                            binding.discCard.visibility = View.VISIBLE
                        } else {
                            binding.discCard.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBarLayout.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appBarLayout))

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }


        ViewCompat.setOnApplyWindowInsetsListener(binding.nestedscrollview) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            binding.appBarLayout.updatePadding(
                top = bars.top
            )

            binding.nestedscrollview.updatePadding(
                bottom = bars.bottom
            )
            WindowInsetsCompat.CONSUMED
        }

        val personId = arguments?.getString("personId")

        if (personId != null) {
            collectingPersonDetail(personId)
            personViewModel.updatePersonById(personId)
        }


    }

}