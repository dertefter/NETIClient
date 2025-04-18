package com.dertefter.neticlient.ui.news

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AutoScrollHelper
import com.dertefter.neticlient.data.model.news.NewsResponse
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentNewsBinding
import com.dertefter.neticlient.ui.news.news_detail.NewsDetailFragment
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.common.item_decoration.GridSpacingItemDecoration
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.data.model.news.PromoItem
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.android.material.chip.Chip
import com.google.android.material.shape.MaterialShapeDrawable
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class NewsFragment : Fragment() {

    lateinit var binding: FragmentNewsBinding
    private val newsViewModel: NewsViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    var page = 1

    lateinit var adapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun openNewsDetail(id: String, imageUrl: String?) {
        if (resources.configuration.orientation == ORIENTATION_LANDSCAPE) {
            childFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.detail, NewsDetailFragment::class.java,
                    bundleOf("newsId" to id, "imageUrl" to imageUrl, "isContainer" to true))
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            }
            detailShowed(true)


        } else {
            val bundle = Bundle()
            bundle.putString("newsId", id)
            bundle.putString("imageUrl", imageUrl)
            findNavController().navigate(R.id.newsDetailFragment, bundle)
        }
    }

    fun detailShowed(i: Boolean) {
        if (i) {
            binding.detailContainer.visibility = View.VISIBLE
        } else {
            binding.detailContainer.visibility = View.GONE
        }
        updateLayoutManager()
    }

    private fun updateLayoutManager() {
        val orientation = resources.configuration.orientation
        val isDetailVisible = binding.detailContainer.isVisible
        val spanCount = if (isDetailVisible)
        { 1 }
        else
        { resources.getInteger(R.integer.span_count) }

        val layoutManager = binding.recyclerView.layoutManager
        val scrollPosition = (layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: 0

        binding.recyclerView.apply {
            while (itemDecorationCount > 0) {
                removeItemDecorationAt(0)
            }
        }

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount).apply {
            scrollToPosition(scrollPosition)
        }

        binding.recyclerView.addItemDecoration(
            GridSpacingItemDecoration(
                requireContext(),
                resources.getInteger(R.integer.span_count),
                R.dimen.margin
            )
        )
    }

    fun loadNews() {
        if (page == 1){
            adapter.clearItems()
        }
        newsViewModel.fetchNews(page)
        page++
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.promoRecyclerView.setLayoutManager(LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false))

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.promoRecyclerView)

        val autoScrollHelper = AutoScrollHelper(binding.promoRecyclerView)
        autoScrollHelper.startAutoScroll()

        newsViewModel.promoListLiveData.observe(viewLifecycleOwner){
            if (it.responseType == ResponseType.SUCCESS){
                val promoAdapter = PromoAdapter(it.data as List<PromoItem>){
                    promoItem ->
                    val intent = Intent(Intent.ACTION_VIEW,
                        promoItem.link.toUri())
                    startActivity(intent)

                }
                binding.promoRecyclerView.adapter = promoAdapter
            }
        }

        newsViewModel.fetchPromoList()


        detailShowed(false)
        page = 1

        adapter = NewsAdapter(this)

        settingsViewModel.insetsViewModel.observe(viewLifecycleOwner) {
            if (resources.configuration.orientation == ORIENTATION_PORTRAIT){
                binding.appBarLayout.updatePadding(
                    top = it[0],
                    bottom = 0,
                    right = it[2],
                    left = it[3]
                )
            }else {
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

        newsViewModel.fetchNews(page)
        binding.recyclerView.adapter = adapter
        updateLayoutManager()

        newsViewModel.newsResponseLiveData.observe(viewLifecycleOwner) {
            if (it.responseType == ResponseType.SUCCESS) {
                val newsResponse = it.data as NewsResponse
                val newsList = newsResponse.items
                adapter.addItems(newsList)
            }
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (lastVisibleItemPosition >= totalItemCount - 2) {
                    loadNews()
                }
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateLayoutManager() // Обновляем макет при изменении ориентации
    }
}