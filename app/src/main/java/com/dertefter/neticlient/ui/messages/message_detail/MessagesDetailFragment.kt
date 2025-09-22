package com.dertefter.neticlient.ui.messages.message_detail

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView.Orientation
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.data.model.messages.MessageDetail
import com.dertefter.neticlient.data.model.news.NewsDetail
import com.dertefter.neticore.features.person_detail.model.Person
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentMessagesDetailBinding
import com.dertefter.neticlient.databinding.FragmentNewsDetailBinding
import com.dertefter.neticlient.ui.person.PersonViewModel
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.common.utils.Utils.displayHtml
import com.dertefter.neticore.features.inbox.model.Message
import com.google.android.material.carousel.CarouselLayoutManager
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class MessagesDetailFragment : Fragment() {

    lateinit var binding: FragmentMessagesDetailBinding
    private val messagesDetailViewModel: MessagesDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesDetailBinding.inflate(inflater, container, false)

        return binding.root
    }

    fun formatMessageDate(dateTime: LocalDateTime?): String {
        if (dateTime == null) return ""
        val now = LocalDateTime.now()

        return when {
            dateTime.toLocalDate() == now.toLocalDate() -> {
                dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            }
            dateTime.year == now.year -> {
                dateTime.format(DateTimeFormatter.ofPattern("d MMM"))
            }
            else -> {
                dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val message: Message? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("message", Message::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("message")
        }

        binding.appBarLayout.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appBarLayout))

        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val alpha = (-verticalOffset.toFloat() / totalScrollRange)
            binding.name.alpha = alpha
        }



        ViewCompat.setOnApplyWindowInsetsListener(binding.nestedScrollView) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )

            binding.nestedScrollView.updatePadding(
                bottom = bars.bottom
            )
            WindowInsetsCompat.CONSUMED
        }



        val isContainer = arguments?.getBoolean("isContainer") ?: false

        if (isContainer){
            binding.backButton.visibility = View.GONE
        }

        if (message != null){
            messagesDetailViewModel.readMessage(message.idStudent, message.id, 1)
            binding.title.text = message.title
            binding.content.text = HtmlCompat.fromHtml(message.text, HtmlCompat.FROM_HTML_MODE_LEGACY)
            binding.content.movementMethod = LinkMovementMethod.getInstance()
            binding.name.text = message.fioAuthor
            if (!message.portraitUrl.isNullOrEmpty()) {
                binding.profilePic.isGone = false
                Picasso.get().load(message.portraitUrl).into(binding.profilePic)
            } else {
                binding.profilePic.isGone = true
            }
            binding.profilePic.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("personId", message.idAuthor.toString())
                requireActivity().findNavController(R.id.nav_host_container).navigate(R.id.personViewFragment, bundle)
            }

            binding.name.setOnClickListener {
                if (binding.name.alpha == 0f) {return@setOnClickListener}
                val bundle = Bundle()
                bundle.putString("personId", message.idAuthor.toString())
                requireActivity().findNavController(R.id.nav_host_container).navigate(R.id.personViewFragment, bundle)
            }

            binding.date.text = formatMessageDate(message.getLocalDateTime())

        }






        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }




    }

}