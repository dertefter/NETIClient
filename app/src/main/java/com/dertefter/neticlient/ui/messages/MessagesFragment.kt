package com.dertefter.neticlient.ui.messages

import android.content.res.Configuration
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.databinding.FragmentMessagesBinding
import com.dertefter.neticlient.ui.messages.message_detail.MessagesDetailFragment
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.data.model.AuthState
import com.dertefter.neticlient.ui.login.LoginFragment
import com.dertefter.neticlient.ui.login.LoginReasonType
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MessagesFragment : Fragment() {

    lateinit var binding: FragmentMessagesBinding
    private val messagesViewModel: MessagesViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()

    }

    private fun setupUI() {

        val tabs = listOf("Преподаватели и службы", "Прочее")
        val adapter = MessagesPagerAdapter(this)
        adapter.setData(tabs)

        binding.pager.adapter = adapter

        binding.filterP1.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) return@setOnCheckedChangeListener
            binding.pager.currentItem = 0
        }

        binding.filterP2.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) return@setOnCheckedChangeListener
            binding.pager.currentItem = 1
        }


    }

    private fun setupObservers() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.authStateFlow.collect { authState ->


                    val unauthorized = authState == AuthState.UNAUTHORIZED
                    binding.pager.isGone = unauthorized

                    if (unauthorized){
                        binding.loginHelper.isGone = false
                        childFragmentManager.beginTransaction().replace(binding.loginHelper.id, LoginFragment(
                            LoginReasonType.UNAUTHORIZED)
                        ).commit()
                    }else{
                        binding.loginHelper.isGone = true
                    }
                }
            }
        }

        messagesViewModel.newCountTab1.observe(viewLifecycleOwner){ count ->
            //TODO
        }

        messagesViewModel.newCountTab2.observe(viewLifecycleOwner){ count ->
            //TODO
        }

        binding.appBarLayout.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appBarLayout))
    }

    fun openMessageDetail(messageId: String) {
        if (binding.detail != null) {
            showDetailInContainer(messageId)
        } else {
            navigateToDetail(messageId)
        }
    }

    private fun showDetailInContainer(messageId: String) {
        childFragmentManager.commit {
            replace(R.id.detail, MessagesDetailFragment::class.java, bundleOf("id" to messageId, "isContainer" to true))
            setReorderingAllowed(true)
        }
    }

    private fun navigateToDetail(messageId: String) {
        val args = bundleOf("id" to messageId)
        findNavController().navigate(
            R.id.messagesDetailFragment,
            args,
            Utils.getNavOptions(),
        )
    }

}