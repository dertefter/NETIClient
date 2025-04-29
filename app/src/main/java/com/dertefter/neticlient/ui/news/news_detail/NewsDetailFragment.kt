package com.dertefter.neticlient.ui.news.news_detail

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
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
import com.dertefter.neticlient.data.model.news.NewsDetail
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentNewsDetailBinding
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.common.utils.Utils.displayHtml
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

@AndroidEntryPoint
class NewsDetailFragment : Fragment() {

    lateinit var binding: FragmentNewsDetailBinding
    private val newsDetailViewModel: NewsDetailViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    var color: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        color = arguments?.getInt("color")
        if (color == null || !DynamicColors.isDynamicColorAvailable()){
            binding = FragmentNewsDetailBinding.inflate(inflater, container, false)
            return binding.root
        } else {
            val context: Context = DynamicColors.wrapContextIfAvailable(
                requireContext(),
                DynamicColorsOptions.Builder()
                    .setContentBasedSource(color!!)
                    .build()
            )
            val themedInflater = layoutInflater.cloneInContext(context)
            binding = FragmentNewsDetailBinding.inflate(themedInflater, container, false)
            return binding.root
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val newsId = arguments?.getString("newsId")
        val imageUrl = arguments?.getString("imageUrl")
        val isContainer = arguments?.getBoolean("isContainer") ?: false

        ViewCompat.setOnApplyWindowInsetsListener(binding.nestedScrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

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




        if (isContainer){
            binding.backButton.visibility = View.GONE
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        if (newsId.isNullOrEmpty()){
            binding.shareButton.visibility = View.GONE
        }else {
            binding.shareButton.setOnClickListener {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "https://www.nstu.ru/news/news_more?idnews=${newsId}")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }
        }


        newsDetailViewModel.newsDetailLiveData.observe(viewLifecycleOwner){
            Log.e("newsDetailLiveData", it.toString())
            if (it.responseType == ResponseType.SUCCESS){
                Utils.basicAnimationOn(binding.nestedScrollView).start()
                binding.skeleton.visibility = View.GONE
                binding.errorView.visibility = View.GONE
                if (imageUrl.isNullOrEmpty()){
                    binding.backgroundNews.visibility = View.GONE
                }
                Picasso.get().load(imageUrl).into(binding.backgroundNews)
                val newsDetail = it.data as NewsDetail
                newsDetail.contentHtml?.let { it1 -> binding.content.displayHtml(it1) }
                if (newsDetail.contentHtml.isNullOrEmpty()){
                    binding.content.visibility = View.GONE
                }
                binding.title.text = newsDetail.title
                val adapter = ImagesCarouselAdapter(fragment = this)
                if (newsDetail.imageUrls.isNotEmpty()){
                    adapter.setData(newsDetail.imageUrls)
                } else {
                    binding.carouselRecyclerView.visibility = View.GONE
                }
                binding.carouselRecyclerView.setLayoutManager(CarouselLayoutManager())
                binding.carouselRecyclerView.adapter = adapter
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
                    if (newsId != null) {
                        newsDetailViewModel.fetchNewsDetail(newsId)
                    }
                }
            }
        }

        if (newsId != null) {
            newsDetailViewModel.fetchNewsDetail(newsId)
        }

    }

}