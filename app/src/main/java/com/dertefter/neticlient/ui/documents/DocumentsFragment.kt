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
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.databinding.FragmentDocumentsBinding
import com.dertefter.neticlient.ui.documents.document_detail.DocumentDetailBottomSheet
import com.dertefter.neticore.network.ResponseType
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


    fun collectStatus(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                documentsViewModel.status.collect { status ->
                    when (status) {
                        ResponseType.LOADING -> {
                            binding.refreshLayout.startRefreshing()
                        }
                        ResponseType.SUCCESS -> {
                            binding.refreshLayout.stopRefreshing()
                        }
                        ResponseType.ERROR -> {
                            binding.refreshLayout.showError()
                        }
                    }
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DocumentsAdapter(){ it ->
            val dialog = DocumentDetailBottomSheet.newInstance(it)
            dialog.show(parentFragmentManager, DocumentDetailBottomSheet.TAG)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.addFab) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val defaultMargin = resources.getDimensionPixelSize(R.dimen.maximorum)
            val layoutParams = v.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin = (defaultMargin + insets.bottom).toInt()
            layoutParams.rightMargin = (defaultMargin).toInt()
            v.layoutParams = layoutParams

            WindowInsetsCompat.CONSUMED
        }


        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(VerticalSpaceItemDecoration(R.dimen.max, R.dimen.min, R.dimen.micro))

        binding.appBarLayout.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appBarLayout))

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.loadFail.buttonRetry.setOnClickListener {
            documentsViewModel.updateDocumentList()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                documentsViewModel.documentList.collect { documents ->
                    if (documents != null){
                        adapter.updateList(documents)
                    }
                    binding.emptyView.isGone = !documents.isNullOrEmpty()
                }
            }
        }


        collectStatus()

        binding.refreshLayout.setOnRefreshListener {
            documentsViewModel.updateDocumentList()
        }



        binding.addFab.setOnClickListener {
            findNavController().navigate(
                R.id.newDocumentFragment,
                null,
                Utils.getNavOptions(),
            )
        }

    }



}