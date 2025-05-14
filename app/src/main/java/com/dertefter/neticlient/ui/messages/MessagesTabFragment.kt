package com.dertefter.neticlient.ui.messages

import android.content.res.Configuration
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.AuthState
import com.dertefter.neticlient.data.model.messages.Message
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentMessagesTabBinding
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticlient.common.item_decoration.GridSpacingItemDecoration
import com.dertefter.neticlient.common.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MessagesTabFragment : Fragment() {

    private lateinit var binding: FragmentMessagesTabBinding

    private val messagesViewModel: MessagesViewModel by activityViewModels()

    private lateinit var adapter: MessagesRecyclerViewAdapter
    private var currentTab: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun openMessageDetail(id: String) {
        (parentFragment as MessagesFragment).openMessageDetail(id)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        val spanCount = resources.getInteger(R.integer.span_count)

        adapter = MessagesRecyclerViewAdapter(fragment = this)
        adapter.setLoading()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)

        val verticalOffset = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            R.dimen.margin
        } else {
            R.dimen.margin_min
        }

        binding.recyclerView.addItemDecoration(
            GridSpacingItemDecoration(
                requireContext(),
                spanCount,
                R.dimen.margin, verticalOffset
            )
        )

        val tab = arguments?.getString("tab")
        currentTab = tab

        binding.loadFailed.buttonRetry.setOnClickListener {
            if (tab != null) {
                messagesViewModel.updateMessages(tab)
            }
        }


        if (!tab.isNullOrEmpty()) {
            messagesViewModel.updateMessages(tab)
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    messagesViewModel.tabStates.collect { tabStates ->
                        val state = tabStates[tab] ?: return@collect

                        if (state.messages != null){
                            onSuccess(state.messages)
                        } else {
                            when (state.responseType) {
                                ResponseType.SUCCESS -> onError()
                                ResponseType.ERROR -> onError()
                                ResponseType.LOADING -> onLoading()
                            }
                        }

                    }
                }
            }
        }
    }

    fun onSuccess(messages: List<Message>){
        adapter.setData(messages)
        binding.loadFailed.root.isGone = true
    }

    fun onError(){
        binding.loadFailed.root.isGone = false
    }
    fun onLoading(){
        adapter.setLoading()
        binding.loadFailed.root.isGone = true
    }

}
