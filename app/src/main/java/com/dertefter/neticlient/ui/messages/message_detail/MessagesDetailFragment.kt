package com.dertefter.neticlient.ui.messages.message_detail

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.nestedScrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
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
                        if (it.responseType == ResponseType.SUCCESS){
                            val person = it.data as Person
                            Picasso.get().load(person.avatarUrl).into(binding.profilePic)
                            binding.person.setOnClickListener {
                                val bundle = Bundle()
                                bundle.putString("personId", messageDetail.personId)

                                findNavController().navigate(
                                    R.id.personViewFragment,
                                    bundle,
                                    Utils.getNavOptions(),

                                )

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