package com.dertefter.neticlient.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.databinding.FragmentMessagesBinding
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticore.features.inbox.model.Message
import com.dertefter.neticore.network.ResponseType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MessagesFragment : Fragment() {

    lateinit var binding: FragmentMessagesBinding

    lateinit var adapter: MessagesAdapter

    private val messagesViewModel: MessagesViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }


    fun setupRecyclerView(){
        adapter = MessagesAdapter(
            onItemClick = {message ->
                navigateToDetail(message = message)
            },
        )
        binding.messagesRv.adapter = adapter
        binding.messagesRv.layoutManager = LinearLayoutManager(requireContext())
        binding.messagesRv.addItemDecoration(
            VerticalSpaceItemDecoration( R.dimen.max, R.dimen.min, R.dimen.micro)
        )
    }

    fun collectMessages(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                messagesViewModel.messagesList.collect { messages ->
                    if (messages != null){
                        (binding.messagesRv.adapter as MessagesAdapter).updateData(messages)
                    }
                }
            }
            }
    }

    fun collectStatus(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                messagesViewModel.status.collect { status ->
                    when (status){
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
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.appbar.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appbar))


        setupRecyclerView()
        collectMessages()
        collectStatus()

        binding.refreshLayout.setOnRefreshListener {
            messagesViewModel.updateMessages()
        }

        messagesViewModel.updateMessages()

        binding.filterChips?.setOnCheckedStateChangeListener { group, checkedIds ->
            val checked = checkedIds.first()
            when (checked){
                R.id.filter_all -> {
                    adapter.setFilter(MessageFilter.ALL)
                }
                R.id.filter_unread -> {
                    adapter.setFilter(MessageFilter.UNREAD)
                }
                R.id.filter_teacher -> {
                    adapter.setFilter(MessageFilter.TEACHER)
                }
                R.id.filter_tutor -> {
                    adapter.setFilter(MessageFilter.TUTOR)
                }
                R.id.filter_decan -> {
                    adapter.setFilter(MessageFilter.DECAN)
                }
                R.id.filter_sluzb -> {
                    adapter.setFilter(MessageFilter.SLUZHB)
                }
                R.id.filter_other -> {
                    adapter.setFilter(MessageFilter.OTHER)
                }
            }
            binding.messagesRv.doOnNextLayout {
                (binding.messagesRv.layoutManager as? LinearLayoutManager)
                    ?.scrollToPositionWithOffset(0, 0)
            }
        }




    }





    private fun navigateToDetail(message: Message) {
        val args = bundleOf("message" to message)
        findNavController().navigate(
            R.id.messagesDetailFragment,
            args,
            Utils.getNavOptions(),
        )
    }

}