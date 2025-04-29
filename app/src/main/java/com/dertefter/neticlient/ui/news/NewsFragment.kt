package com.dertefter.neticlient.ui.news

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
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
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentNewsBinding
import com.dertefter.neticlient.ui.news.news_detail.NewsDetailFragment
import com.dertefter.neticlient.common.item_decoration.GridSpacingItemDecoration
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.data.model.news.PromoItem
import com.google.android.material.shape.MaterialShapeDrawable
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class NewsFragment : Fragment() {

    lateinit var binding: FragmentNewsBinding
    private val newsViewModel: NewsViewModel by activityViewModels()

    var adapter: NewsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun openNewsDetail(id: String, imageUrl: String?, color: Int) {
        if (resources.configuration.orientation == ORIENTATION_LANDSCAPE) {
            childFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.detail, NewsDetailFragment::class.java,
                    bundleOf("newsId" to id, "imageUrl" to imageUrl, "isContainer" to true, "color" to color))
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            }
            detailShowed(true)


        } else {
            val bundle = Bundle()
            bundle.putString("newsId", id)
            bundle.putString("imageUrl", imageUrl)
            bundle.putInt("color", color)
            findNavController().navigate(
                R.id.newsDetailFragment,
                bundle,
                Utils.getNavOptions(),
            )

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

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)

        val layoutManager = binding.recyclerView.layoutManager as? GridLayoutManager
        val scrollPosition = (layoutManager )?.findFirstVisibleItemPosition() ?: 0

        binding.recyclerView.apply {
            while (itemDecorationCount > 0) {
                removeItemDecorationAt(0)
            }
        }



        binding.recyclerView.addItemDecoration(
            GridSpacingItemDecoration(
                requireContext(),
                resources.getInteger(R.integer.span_count),
                R.dimen.margin_min
            )
        )

        binding.recyclerView.doOnLayout {
            val lastVisibleItemPosition = layoutManager?.findFirstCompletelyVisibleItemPosition()
            if (lastVisibleItemPosition == 0){
                binding.appBarLayout.setExpanded(true, false)
            } else {
                binding.appBarLayout.setExpanded(false, false)
            }
        }



    }

    fun loadNews() {
        newsViewModel.fetchNews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.promoRecyclerView.setLayoutManager(LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false))
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.promoRecyclerView)

        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val autoScrollHelper = AutoScrollHelper(binding.promoRecyclerView)
        autoScrollHelper.startAutoScroll()
        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(context)
        binding.filterChips.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds[0]
            if (checkedId == R.id.filter_all) {adapter?.updateFilter(null)}
            else if (checkedId == R.id.filter_news) {adapter?.updateFilter("новости")}
            else if (checkedId == R.id.filter_obj) {adapter?.updateFilter("объявления")}
            else if (checkedId == R.id.filter_interview) {adapter?.updateFilter("интервью")}
            else if (checkedId == R.id.filter_photo) {adapter?.updateFilter("фото")}
            else {adapter?.updateFilter(null)}
            binding.recyclerView.scrollToPosition(0)
            loadNews()
        }

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

        adapter = NewsAdapter({ newsItem, color ->
            openNewsDetail(newsItem.id, newsItem.imageUrl, color)
        }, 6)

        binding.recyclerView.adapter = adapter

        updateLayoutManager()

        newsViewModel.newsListLiveData.observe(viewLifecycleOwner) { it ->
            if (it != null){
                Log.e("newsItems", it.toString())
                adapter?.updateItems(it)
                if (newsViewModel.page <= 2){
                    binding.recyclerView.scrollToPosition(0)
                }
                if (adapter!!.itemCount <= adapter!!.loadingViewsCount){
                    loadNews()
                }
            }
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount - 6

                if (lastVisibleItemPosition >= totalItemCount - 1) {
                    loadNews()
                }
            }
        })

        if (adapter!!.itemCount <= adapter!!.loadingViewsCount){
            loadNews()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateLayoutManager()
    }
}