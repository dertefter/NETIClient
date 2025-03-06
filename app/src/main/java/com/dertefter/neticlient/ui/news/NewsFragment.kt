package com.dertefter.neticlient.ui.news

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.news.NewsResponse
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentNewsBinding
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.utils.Utils
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewsFragment : Fragment() {

    lateinit var binding: FragmentNewsBinding
    private val newsViewModel: NewsViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    var page = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewsBinding.inflate(inflater, container, false)

        return binding.root
    }

    fun openNewsDetail(id: String, imageUrl: String?){
        val bundle = Bundle()
        bundle.putString("newsId", id)
        bundle.putString("imageUrl", imageUrl)
        findNavController().navigate(R.id.newsDetailFragment, bundle)
    }


    fun loadNews(){
        newsViewModel.fetchNews(page)
        page++
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        page = 1

        val adapter = NewsAdapter(this)

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
                Utils.basicAnimationOff(binding.toolbar, false).start()
            } else {
                Utils.basicAnimationOn(binding.toolbar).start()
            }
            Log.e("verticalOffset", verticalOffset.toString())
        }

        newsViewModel.fetchNews(page)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        newsViewModel.newsResponseLiveData.observe(viewLifecycleOwner){
            Log.e("news", it.toString())
            if (it.responseType == ResponseType.SUCCESS){
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
            Utils.basicAnimationOn(binding.recyclerView).start()
        }

    }

}