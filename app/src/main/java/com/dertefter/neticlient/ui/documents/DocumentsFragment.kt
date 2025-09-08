package com.dertefter.neticlient.ui.documents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.data.model.documents.DocumentsItem
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentDocumentsBinding
import com.dertefter.neticlient.ui.documents.document_view.DocumentViewBottomSheetFragment
import com.dertefter.neticlient.ui.documents.new_document.NewDocumentFragment
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.addFab) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val defaultMargin = resources.getDimensionPixelSize(R.dimen.margin)
            val layoutParams = v.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin = (defaultMargin + insets.bottom).toInt()
            layoutParams.rightMargin = (defaultMargin).toInt()
            v.layoutParams = layoutParams

            WindowInsetsCompat.CONSUMED
        }


        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            VerticalSpaceItemDecoration(
                R.dimen.margin_max,
                R.dimen.margin_micro
            )
        )
        adapter.setLoading(true)

        binding.appBarLayout.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appBarLayout))

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
            NewDocumentFragment.newInstance().show(parentFragmentManager, "CreateDoc")
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