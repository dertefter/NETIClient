package com.dertefter.neticlient.ui.messages.dispace_messages

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.AuthState
import com.dertefter.neticlient.data.model.dispace.messages.Companion
import com.dertefter.neticlient.data.model.messages.Message
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentMessagesBinding
import com.dertefter.neticlient.databinding.FragmentMessagesTabBinding
import com.dertefter.neticlient.databinding.FragmentProfileBinding
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import com.dertefter.neticlient.utils.GridSpacingItemDecoration
import com.dertefter.neticlient.utils.Utils
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class DispaceMessagesTabFragment : Fragment() {

    lateinit var binding: FragmentMessagesTabBinding

    private val loginViewModel: LoginViewModel by activityViewModels()
    private val dispaceMessagesViewModel: DispaceMessagesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesTabBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = DispaceMessagesRecyclerViewAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val dividerItemDecoration = androidx.recyclerview.widget.DividerItemDecoration(
            binding.recyclerView.context,
            (binding.recyclerView.layoutManager as LinearLayoutManager).orientation
        )
        dividerItemDecoration.setDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.vertical_divider)!!)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)


        dispaceMessagesViewModel.senderListLiveData.observe(viewLifecycleOwner){
            if (it.responseType == ResponseType.SUCCESS){
                adapter.setData(it.data as List<Companion>)
            }
        }

        dispaceMessagesViewModel.fetchSenderList()

    }

}