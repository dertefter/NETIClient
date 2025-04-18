package com.dertefter.neticlient.ui.messages.message_detail

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView.Orientation
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.messages.MessageDetail
import com.dertefter.neticlient.data.model.news.NewsDetail
import com.dertefter.neticlient.data.model.person.Person
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentMessagesDetailBinding
import com.dertefter.neticlient.databinding.FragmentNewsDetailBinding
import com.dertefter.neticlient.ui.person.PersonViewModel
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.common.utils.Utils.displayHtml
import com.google.android.material.carousel.CarouselLayoutManager
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MessagesDetailFragment : Fragment() {

    lateinit var binding: FragmentMessagesDetailBinding
    private val messagesDetailViewModel: MessagesDetailViewModel by viewModels()
    private val personViewModel: PersonViewModel by viewModels()

    private val settingsViewModel: SettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesDetailBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsViewModel.insetsViewModel.observe(viewLifecycleOwner){
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
                binding.appBarLayout.updatePadding(
                    top = it[0],
                    bottom = 0,
                    right = it[2],
                    left = it[3]
                )
            } else{
                binding.appBarLayout.updatePadding(
                    top = 0,
                    bottom = 0,
                    right = 0,
                    left = 0
                )
            }

        }

        binding.appBarLayout.setLiftable(true)
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val alpha = 1f - (-verticalOffset.toFloat() / totalScrollRange)
            binding.topContainer.alpha = alpha
            if (verticalOffset < 0) {
                binding.appBarLayout.isLifted = true
            } else {
                binding.appBarLayout.isLifted = false
            }
        }


        val id = arguments?.getString("id")

        val isContainer = arguments?.getBoolean("isContainer") ?: false

        if (isContainer){
            binding.backButton.visibility = View.GONE
        }


        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        messagesDetailViewModel.messageDetailLiveData.observe(viewLifecycleOwner){
            Log.e("newsDetailLiveData", it.toString())
            if (it.responseType == ResponseType.SUCCESS){
                Utils.basicAnimationOn(binding.nestedScrollView).start()
                binding.skeleton.visibility = View.GONE
                binding.errorView.visibility = View.GONE

                val messageDetail = it.data as MessageDetail
                messageDetail.contentHtml?.let { it1 -> binding.content.displayHtml(it1) }
                if (messageDetail.contentHtml.isNullOrEmpty()){
                    binding.content.visibility = View.GONE
                }
                binding.title.text = messageDetail.title
                binding.date.text = messageDetail.date
                messageDetail.personId?.let { it1 ->
                    personViewModel.getLiveDataForId(it1).observe(viewLifecycleOwner){
                        Log.e("person", it.toString())
                        if (it.responseType == ResponseType.SUCCESS){
                            val person = it.data as Person
                            Picasso.get().load(person.avatarUrl).into(binding.profilePic)
                            binding.person.setOnClickListener {
                                val bundle = Bundle()
                                bundle.putString("personId", messageDetail.personId)
                                findNavController().navigate(R.id.personViewFragment, bundle)
                            }
                        }
                    }
                }
                messageDetail.personId?.let { it1 -> personViewModel.fetchPerson(it1) }

            }
            if (it.responseType == ResponseType.LOADING){
                Utils.basicAnimationOn(binding.skeleton).start()
                binding.nestedScrollView.visibility = View.GONE
                binding.errorView.visibility = View.GONE
            }
            if (it.responseType == ResponseType.ERROR){
                Utils.basicAnimationOn(binding.errorView).start()
                binding.skeleton.visibility = View.GONE
                binding.nestedScrollView.visibility = View.GONE
                binding.retryButton.setOnClickListener {
                    if (id != null) {
                        messagesDetailViewModel.fetchMessageDetail(id)
                    }
                }
            }
        }

        if (id != null && messagesDetailViewModel.messageDetailLiveData.value?.data == null) {
            messagesDetailViewModel.fetchMessageDetail(id)
        }



    }

}