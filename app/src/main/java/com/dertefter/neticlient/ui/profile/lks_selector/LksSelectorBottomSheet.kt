package com.dertefter.neticlient.ui.profile.lks_selector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.databinding.FragmentLksSelectorBinding
import com.dertefter.neticlient.ui.profile.ProfileViewModel
import com.dertefter.neticore.features.documents.model.DocumentsItem
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class LksSelectorBottomSheet : BottomSheetDialogFragment() {

    private val profileViewModel: ProfileViewModel by activityViewModels()


    companion object {
        const val TAG = "LksSelectorBottomSheet"

        fun newInstance(documentItem: DocumentsItem): LksSelectorBottomSheet {
            val fragment = LksSelectorBottomSheet()
            val bundle = Bundle().apply {
                putParcelable("documentItem", documentItem)
            }
            fragment.arguments = bundle
            return fragment
        }
    }


    private var _binding: FragmentLksSelectorBinding? = null
    private val binding get() = _binding!!

    lateinit var _dialog: BottomSheetDialog

    lateinit var lksAdapter: LksAdapter

    fun collectLks(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.lksList.collect { lksList ->
                    lksAdapter.updateItems(lksList)
                }
            }
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLksSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectLks()
        lksAdapter = LksAdapter(emptyList()) { lks ->
            if (lks.id != null){
                profileViewModel.setLksById(lks.id!!)
                dismiss()
            }
        }

        binding.lksRv.adapter = lksAdapter
        binding.lksRv.layoutManager = LinearLayoutManager(requireContext())
        binding.lksRv.addItemDecoration(
            VerticalSpaceItemDecoration( R.dimen.max, R.dimen.min, R.dimen.micro)
        )

    }


}



