package com.dertefter.neticlient.ui.messages

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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

@AndroidEntryPoint
class MessagesTabFragment : Fragment() {

    lateinit var binding: FragmentMessagesTabBinding

    private val loginViewModel: LoginViewModel by activityViewModels()
    private val messagesViewModel: MessagesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesTabBinding.inflate(inflater, container, false)

        return binding.root
    }

    fun openMessageDetail(id: String){
        (parentFragment as MessagesFragment).openMessageDetail(id)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spanCount = resources.getInteger(R.integer.span_count)

        val adapter = MessagesRecyclerViewAdapter(fragment = this)
        adapter.setLoading()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)

        val vertival_offset_dimension = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            R.dimen.margin
        } else {
            R.dimen.margin_min
        }

        binding.recyclerView.addItemDecoration(
            GridSpacingItemDecoration(
                requireContext(),
                spanCount,
                R.dimen.margin, vertival_offset_dimension
            )
        )

        binding.authButton.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }

        val tab = arguments?.getString("tab")

        if (!tab.isNullOrEmpty()){
            loginViewModel.authStateLiveData.observe(viewLifecycleOwner){
                if (it == AuthState.UNAUTHORIZED){
                    binding.authCard.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                } else {
                    binding.authCard.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    messagesViewModel.updateMessages(tab)
                }
            }

            messagesViewModel.getLiveDataForTab(tab).observe(viewLifecycleOwner){

                if (it.responseType == ResponseType.LOADING){
                    adapter.setLoading()
                }

                if (it.data != null){
                    if ((it.data as List<Message>) != adapter.messagesList){
                        adapter.setData(it.data as List<Message>)
                        Utils.basicAnimationOn(binding.recyclerView).start()
                    }

                }
            }
        }



    }

}