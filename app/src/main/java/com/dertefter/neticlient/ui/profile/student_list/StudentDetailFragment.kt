package com.dertefter.neticlient.ui.profile.student_list

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import androidx.navigation.fragment.navArgs
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticore.features.person_detail.model.Person
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentPersonViewBinding
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.common.utils.Utils.displayHtml
import com.dertefter.neticlient.databinding.FragmentStudentDetailBinding
import com.dertefter.neticlient.ui.fullscreen_image_dialog.FullscreenImageDialog
import com.dertefter.neticlient.ui.person.PersonViewModel
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StudentDetailFragment : Fragment() {

    lateinit var binding: FragmentStudentDetailBinding

    private val args: StudentDetailFragmentArgs by navArgs()

    @Inject
    lateinit var picasso: Picasso


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStudentDetailBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBarLayout.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appBarLayout))

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val student = args.student

        binding.name.text = student.getFullName()

        binding.isMonitor.isGone = student.isGroupMonitor != 1

        binding.mail.setTextOrHide(student.email)
        binding.dob.setTextOrHide(student.birthday)
        binding.phone.setTextOrHide(student.phone)
        binding.address.setTextOrHide(student.address)
        binding.vk.setTextOrHide(student.vk)
        binding.tg.setTextOrHide(student.telegram)
        binding.leaderId.setTextOrHide(student.leaderId?.toString())

        picasso.load(student.photo).into(binding.profilePic)


    }

    fun Button.setTextOrHide(value: String?) {
        if (value.isNullOrBlank()) {
            isGone = true
        } else {
            isGone = false
            text = value
        }
    }

}