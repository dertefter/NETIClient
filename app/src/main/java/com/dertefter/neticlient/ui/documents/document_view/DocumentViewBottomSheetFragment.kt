package com.dertefter.neticlient.ui.documents.document_view

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.GridSpacingItemDecoration
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.model.documents.DocumentsItem
import com.dertefter.neticlient.data.model.schedule.FutureOrPastOrNow
import com.dertefter.neticlient.databinding.FragmentDocumentViewBinding
import com.dertefter.neticlient.databinding.FragmentLessonViewBinding
import com.dertefter.neticlient.ui.documents.DocumentsViewModel
import com.dertefter.neticlient.ui.person.PersonListRecyclerViewAdapter
import com.dertefter.neticlient.ui.person.PersonListStyle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DocumentViewBottomSheetFragment : BottomSheetDialogFragment() {

    private val documentsViewModel: DocumentsViewModel by activityViewModels()


    companion object {
        const val TAG = "DocumentBottomSheetFragment"

        fun newInstance(documentItem: DocumentsItem): DocumentViewBottomSheetFragment {
            val fragment = DocumentViewBottomSheetFragment()
            val bundle = Bundle().apply {
                putParcelable("documentItem", documentItem)
            }
            fragment.arguments = bundle
            return fragment
        }
    }


    private var _binding: FragmentDocumentViewBinding? = null
    private val binding get() = _binding!!

    lateinit var _dialog: BottomSheetDialog


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        _dialog = dialog
        return dialog
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDocumentViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val documentItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("documentItem", DocumentsItem::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("documentItem")!!
        }

        documentsViewModel.docCancelable.observe(viewLifecycleOwner){
            if (it == true){
                binding.cancelClaim.visibility = View.VISIBLE
                binding.cancelClaim.setOnClickListener {
                    documentsViewModel.cancelClaim(id = documentItem.number)
                    dismiss()
                }

            } else {
                binding.cancelClaim.visibility = View.GONE
            }
        }
        documentItem.number?.let { documentsViewModel.checkCancelable(it) }

        binding.type.text = documentItem.type
        binding.date.text = documentItem.date
        binding.person.text = documentItem.person?.replace(", ", "\n")
        binding.number.text = documentItem.number
        binding.comment.text = documentItem.comment
        binding.status.text = documentItem.status

        if (documentItem.date.isNullOrEmpty()){
            (binding.date.parent as View).visibility = View.GONE
        }
        if (documentItem.person.isNullOrEmpty()){
            (binding.person.parent as View).visibility = View.GONE
        }
        if (documentItem.comment.isNullOrEmpty()){
            (binding.comment.parent as View).visibility = View.GONE
        }
        if (documentItem.status.isNullOrEmpty()){
            (binding.status.parent as View).visibility = View.GONE
        } else {
            if (documentItem.status?.contains("готово", true) == true){
                (binding.status.parent as MaterialCardView).setCardBackgroundColor(
                    MaterialColors.getColor(binding.status, com.google.android.material.R.attr.colorPrimaryContainer)
                )
                binding.status.setTextColor(
                    MaterialColors.getColor(binding.status, com.google.android.material.R.attr.colorOnPrimaryContainer)
                )
            }else{
                (binding.status.parent as MaterialCardView).setCardBackgroundColor(
                    MaterialColors.getColor(binding.status, com.google.android.material.R.attr.colorSurfaceVariant)
                )
                binding.status.setTextColor(
                    MaterialColors.getColor(binding.status, com.google.android.material.R.attr.colorOnSurfaceVariant)
                )
            }
        }

        binding.cancelClaim.setOnClickListener {
            dismiss()
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        documentsViewModel.docCancelable.postValue(null)
        _binding = null
    }

}



