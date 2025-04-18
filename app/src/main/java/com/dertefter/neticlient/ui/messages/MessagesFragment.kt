package com.dertefter.neticlient.ui.messages

import android.content.res.Configuration
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.navigation.fragment.findNavController
import com.dertefter.neticlient.R
import com.dertefter.neticlient.databinding.FragmentMessagesBinding
import com.dertefter.neticlient.ui.messages.message_detail.MessagesDetailFragment
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.common.utils.Utils
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MessagesFragment : Fragment() {

    lateinit var binding: FragmentMessagesBinding
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val messagesViewModel: MessagesViewModel by activityViewModels()

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

        with(binding) {
            pager.adapter = adapter
            TabLayoutMediator(tabLayout, pager) { tab, position ->
                tab.text = tabs[position]
            }.attach()

           closeButton.setOnClickListener { detailShowed(false) }
        }
    }

    private fun setupObservers() {

        messagesViewModel.newCountTab1.observe(viewLifecycleOwner){ count ->
            val badge = binding.tabLayout.getTabAt(0)?.orCreateBadge
            badge?.isVisible = count > 0
            badge?.number = count
        }

        messagesViewModel.newCountTab2.observe(viewLifecycleOwner){ count ->
            val badge = binding.tabLayout.getTabAt(1)?.orCreateBadge
            badge?.isVisible = count > 0
            badge?.number = count
        }

        settingsViewModel.insetsViewModel.observe(viewLifecycleOwner) { insets ->
            binding.appBarLayout.updatePadding(
                top = insets[0],
                bottom = 0,
                right = insets[2],
                left = insets[3]
            )
        }

        binding.appBarLayout.setLiftable(true)
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (verticalOffset < 0){
                Utils.basicAnimationOff(binding.toolbar, false).start()
                binding.appBarLayout.isLifted = true
            } else {
                Utils.basicAnimationOn(binding.toolbar).start()
                binding.appBarLayout.isLifted = false
            }
        }
    }

    fun openMessageDetail(messageId: String) {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
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
        detailShowed(true)
    }

    private fun navigateToDetail(messageId: String) {
        val args = bundleOf("id" to messageId)
        findNavController().navigate(R.id.messagesDetailFragment, args)
    }

    fun detailShowed(show: Boolean) {
       binding.detailContainer.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            detailShowed(false)
        }
    }
}