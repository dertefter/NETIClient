package com.dertefter.neticlient.ui.documents

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
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.data.model.documents.DocumentsItem
import com.dertefter.neticlient.data.model.messages.Message
import com.dertefter.neticlient.data.model.news.PromoItem
import com.dertefter.neticlient.databinding.FragmentDocumentsBinding
import com.dertefter.neticlient.ui.documents.document_view.DocumentViewBottomSheetFragment
import com.dertefter.neticlient.ui.documents.new_document_dialog.NewDocumentDialogFragment
import com.dertefter.neticlient.ui.profile.profile_dialog.ProfileDialogFragment
import com.dertefter.neticlient.ui.sessia_schedule.SessiaScheduleRecyclerViewAdapter
import com.google.android.material.shape.MaterialShapeDrawable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class DocumentsFragment : Fragment() {

    lateinit var binding: FragmentDocumentsBinding
    private val documentsViewModel: DocumentsViewModel by activityViewModels()
    lateinit var adapter: DocumentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDocumentsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DocumentsAdapter(){ it ->
            val dialog = DocumentViewBottomSheetFragment.newInstance(it)
            dialog.show(parentFragmentManager, DocumentViewBottomSheetFragment.TAG)
        }

        binding.recyclerView.adapter = adapter
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.margin_min)
        binding.recyclerView.addItemDecoration(VerticalSpaceItemDecoration(spacingInPixels))
        adapter.setLoading(true)

        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.addFab) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = v.layoutParams as ViewGroup.MarginLayoutParams
            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.margin_max)
            params.updateMargins(bottom = insets.bottom + spacingInPixels)
            WindowInsetsCompat.CONSUMED
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.loadFail.buttonRetry.setOnClickListener {
            documentsViewModel.updateDocumentList()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                documentsViewModel.documentsState.collect { state ->

                    if (state.documentList != null){
                        onSuccess(state.documentList)
                    } else {
                        when (state.responseType) {
                            ResponseType.SUCCESS -> onError()
                            ResponseType.ERROR -> onError()
                            ResponseType.LOADING -> onLoading()
                        }
                    }


                }
            }
        }

        documentsViewModel.updateDocumentList()

        binding.addFab.setOnClickListener {
            NewDocumentDialogFragment.newInstance().show(parentFragmentManager, "CreateDoc")
        }

    }


    fun onSuccess(messages: List<DocumentsItem>){
        adapter.updateList(messages)
        adapter.setLoading(false)
        binding.loadFail.root.isGone = true
    }

    fun onError(){
        adapter.updateList(emptyList())
        adapter.setLoading(false)
        binding.loadFail.root.isGone = false
    }
    fun onLoading(){
        adapter.setLoading(true)
        adapter.updateList(emptyList())
        binding.loadFail.root.isGone = true
    }



}