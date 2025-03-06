package com.dertefter.neticlient.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.AuthState
import com.dertefter.neticlient.data.model.messages.Message
import com.dertefter.neticlient.databinding.FragmentMessagesTabBinding
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticlient.utils.Utils
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
        val bundle = Bundle()
        bundle.putString("id", id)
        findNavController().navigate(R.id.messagesDetailFragment, bundle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MessagesRecyclerViewAdapter(fragment = this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val dividerItemDecoration = androidx.recyclerview.widget.DividerItemDecoration(
            binding.recyclerView.context,
            (binding.recyclerView.layoutManager as LinearLayoutManager).orientation
        )
        dividerItemDecoration.setDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.vertical_divider)!!)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)

        binding.authButton.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }

        val tab = arguments?.getString("tab")

        if (!tab.isNullOrEmpty()){
            loginViewModel.authStateLiveData.observe(viewLifecycleOwner){
                if (it == AuthState.UNAUTHORIZED){
                    binding.authCard.visibility = View.VISIBLE
                } else {
                    binding.authCard.visibility = View.GONE
                    messagesViewModel.updateMessages(tab)
                }
            }

            messagesViewModel.getLiveDataForTab(tab).observe(viewLifecycleOwner){
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