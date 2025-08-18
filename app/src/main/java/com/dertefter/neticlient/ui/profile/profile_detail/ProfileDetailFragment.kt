package com.dertefter.neticlient.ui.profile.profile_detail

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.data.model.profile_detail.ProfileDetail
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentProfileDetailBinding
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticlient.ui.profile.ProfileViewModel
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.ui.fullscreen_image_dialog.FullscreenImageDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class ProfileDetailFragment : Fragment() {

    lateinit var binding: FragmentProfileDetailBinding

    private val loginViewModel: LoginViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val profileDetailViewModel: ProfileDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileDetailBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBarLayout.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appBarLayout))

        binding.agreeCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.saveButton.isEnabled = isChecked
        }

        binding.saveButton.setOnClickListener {
            val n_email = binding.mail.editText?.text.toString()
            val n_address = binding.adress.editText?.text.toString()
            val n_phone = binding.phone.editText?.text.toString()
            val n_snils = binding.snils.editText?.text.toString()
            val n_oms = binding.polis.editText?.text.toString()
            val n_vk = binding.vk.editText?.text.toString()
            val n_tg = binding.tg.editText?.text.toString()
            val n_leader = binding.leaderId.editText?.text.toString()
            profileDetailViewModel.saveData(n_email, n_address, n_phone, n_snils, n_oms, n_vk, n_tg, n_leader)
        }

        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (verticalOffset < 0){
                Utils.basicAnimationOff(binding.toolbar, false).start()
            } else {
                Utils.basicAnimationOn(binding.toolbar).start()
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileDetailViewModel.uiStateFlow.collect { uiState ->
                    val enabledInput = uiState.responseType != ResponseType.LOADING
                    binding.saveButton.isGone = !enabledInput
                    binding.mail.isEnabled = enabledInput
                    binding.adress.isEnabled = enabledInput
                    binding.phone.isEnabled = enabledInput
                    binding.snils.isEnabled = enabledInput
                    binding.polis.isEnabled = enabledInput
                    binding.vk.isEnabled = enabledInput
                    binding.tg.isEnabled = enabledInput
                    binding.leaderId.isEnabled = enabledInput

                    val details = uiState.data as ProfileDetail?
                    if (details != null){
                        binding.mail.editText?.setText(details.email)
                        binding.adress.editText?.setText(details.address)
                        binding.phone.editText?.setText(details.phone)
                        binding.snils.editText?.setText(details.snils)
                        binding.polis.editText?.setText(details.polis)
                        binding.vk.editText?.setText(details.vk)
                        binding.tg.editText?.setText(details.telegram)
                        binding.leaderId.editText?.setText(details.leaderId)
                    }
                }
            }
        }

        profileDetailViewModel.updateProfileDetails()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.userFlow.collect { it ->
                    if (it != null){
                        binding.nameTv.text = it.name
                        binding.loginTv.text = it.login
                        if (!it.profilePicPath.isNullOrEmpty()){
                            val file = File(it.profilePicPath)
                            if (file.exists()){
                                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                                binding.profilePic.setImageBitmap(bitmap)
                                binding.profilePic.setOnClickListener {
                                    val dialog = FullscreenImageDialog().apply {arguments = Bundle().apply { putParcelable("bitmap", bitmap) } }
                                    dialog.show(childFragmentManager, "FullscreenImageDialog")
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.logOutButton.setOnClickListener {
            loginViewModel.logout()
            findNavController().popBackStack()
        }

    }

}