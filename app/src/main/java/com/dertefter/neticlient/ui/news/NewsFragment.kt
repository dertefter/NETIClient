package com.dertefter.neticlient.ui.news

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.news.NewsResponse
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentNewsBinding
import com.dertefter.neticlient.ui.news.news_detail.NewsDetailFragment
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.common.item_decoration.GridSpacingItemDecoration
import com.dertefter.neticlient.common.utils.Utils
import com.google.android.material.chip.Chip
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

            binding.closeButton.setOnClickListener {
                detailShowed(false)
            }

            binding.shareButton.setOnClickListener {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "https://www.nstu.ru/news/news_more?idnews=${id}")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }

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
        detailShowed(false)
        page = 1

        adapter = NewsAdapter(this)

        settingsViewModel.insetsViewModel.observe(viewLifecycleOwner) {
            binding.appBarLayout.updatePadding(
                top = it[0],
                bottom = 0,
                right = it[2],
                left = it[3]
            )
            binding.detailContainer.updatePadding(
                top = it[0],
                right = it[2],
            )

        }

        binding.appBarLayout.setLiftable(true)
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (verticalOffset < 0){
                Utils.basicAnimationOff(binding.toolbar, false).start()
                binding.appBarLayout.isLifted = true
            } else {
                Utils.basicAnimationOn(binding.toolbar).start()
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

        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val filter = group.findViewById<Chip>(checkedIds[0]).text.toString()
            adapter.setFilter(filter)
            page = 1
            loadNews()
            Utils.basicAnimationOn(binding.recyclerView).start()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateLayoutManager() // Обновляем макет при изменении ориентации
    }
}