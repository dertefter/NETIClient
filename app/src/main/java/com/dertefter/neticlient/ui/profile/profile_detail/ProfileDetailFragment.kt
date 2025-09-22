package com.dertefter.neticlient.ui.profile.profile_detail

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
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.databinding.FragmentProfileDetailBinding
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticore.network.ResponseType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileDetailFragment : Fragment() {

    lateinit var binding: FragmentProfileDetailBinding

    private val loginViewModel: LoginViewModel by activityViewModels()
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


        ViewCompat.setOnApplyWindowInsetsListener(binding.nestedScrollView) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )

            binding.nestedScrollView.updatePadding(
                bottom = bars.bottom
            )
            WindowInsetsCompat.CONSUMED
        }

        profileDetailViewModel.updateUserDetail()

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
                profileDetailViewModel.userDetail.collect { userDetail ->
                    Log.e("userDetail", userDetail.toString())
                    val enabledInput = userDetail != null
                    binding.saveButton.isGone = !enabledInput
                    binding.mail.isEnabled = enabledInput
                    binding.adress.isEnabled = enabledInput
                    binding.phone.isEnabled = enabledInput
                    binding.snils.isEnabled = enabledInput
                    binding.polis.isEnabled = enabledInput
                    binding.vk.isEnabled = enabledInput
                    binding.tg.isEnabled = enabledInput
                    binding.leaderId.isEnabled = enabledInput

                    if (userDetail != null){
                        binding.mail.editText?.setText(userDetail.email)
                        binding.adress.editText?.setText(userDetail.address)
                        binding.phone.editText?.setText(userDetail.mobilePhoneNumber)
                        binding.snils.editText?.setText(userDetail.snils)
                        binding.polis.editText?.setText(userDetail.polis)
                        binding.vk.editText?.setText(userDetail.vk)
                        binding.tg.editText?.setText(userDetail.tg)
                        binding.leaderId.editText?.setText(userDetail.leaderId)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileDetailViewModel.status.collect { status ->
                    when(status) {
                        ResponseType.LOADING -> {
                            binding.refreshLayout.startRefreshing()
                        }
                        ResponseType.SUCCESS -> {
                            binding.refreshLayout.stopRefreshing()
                        }
                        ResponseType.ERROR -> {
                            binding.refreshLayout.showError()
                        }
                    }
                }
            }
        }

        binding.refreshLayout.setOnRefreshListener {
            profileDetailViewModel.updateUserDetail()
        }



        binding.logOutButton.setOnClickListener {
            findNavController().popBackStack()
        }

    }

}