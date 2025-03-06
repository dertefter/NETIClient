package com.dertefter.neticlient.ui.messages.message_detail

import android.content.Intent
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
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.messages.MessageDetail
import com.dertefter.neticlient.data.model.news.NewsDetail
import com.dertefter.neticlient.data.model.person.Person
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentMessagesDetailBinding
import com.dertefter.neticlient.databinding.FragmentNewsDetailBinding
import com.dertefter.neticlient.ui.person.PersonViewModel
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.utils.Utils
import com.dertefter.neticlient.utils.Utils.displayHtml
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
            binding.appBarLayout.updatePadding(
                top = it[0],
                bottom = 0,
                right = it[2],
                left = it[3]
            )
        }

        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (verticalOffset < 0){
                Utils.basicAnimationOff(binding.profileCard, false).start()
            } else {
                Utils.basicAnimationOn(binding.profileCard).start()
            }
            Log.e("verticalOffset", verticalOffset.toString())
        }


        val id = arguments?.getString("id")

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

                messageDetail.personId?.let { it1 ->
                    personViewModel.getLiveDataForId(it1).observe(viewLifecycleOwner){
                        Log.e("person", it.toString())
                        if (it.responseType == ResponseType.SUCCESS){
                            val person = it.data as Person
                            Picasso.get().load(person.avatarUrl).into(binding.profilePic)
                            binding.nameTextView.text = person.name
                            binding.profileCard.visibility = View.VISIBLE
                            binding.profileCard.setOnClickListener {
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

        if (id != null) {
            messagesDetailViewModel.fetchMessageDetail(id)
        }



    }

}