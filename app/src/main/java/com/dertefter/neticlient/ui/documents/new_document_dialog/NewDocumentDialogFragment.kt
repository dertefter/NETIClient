package com.dertefter.neticlient.ui.documents.new_document_dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.documents.DocumentOptionItem
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentNewDocumentDialogBinding
import com.dertefter.neticlient.ui.documents.DocumentsViewModel

class NewDocumentDialogFragment : DialogFragment() {

    var selectedValue = ""

    private lateinit var binding: FragmentNewDocumentDialogBinding

    private val documentsViewModel: DocumentsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewDocumentDialogBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onDestroyView() {
        documentsViewModel.selectedRequestItem.postValue(null)
        super.onDestroyView()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cancel.setOnClickListener { dismiss() }
        documentsViewModel.optionListLiveData.observe(viewLifecycleOwner) {
            if (it.responseType == ResponseType.SUCCESS && it.data != null) {

                val optionList = it.data as List<DocumentOptionItem>
                val displayList = optionList.map { item -> item.text }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, displayList)
                binding.docSelection.setAdapter(adapter)

                if (optionList.isNotEmpty()) {
                    binding.docSelection.setText(displayList[0], false)
                    val firstItem = optionList[0]
                    selectedValue = firstItem.value
                    documentsViewModel.fetchDocumentRequest(selectedValue)
                }

                binding.docSelection.setOnItemClickListener { _, _, position, _ ->
                    val selectedItem = optionList[position]
                    val selectedText = selectedItem.text
                    selectedValue = selectedItem.value
                    documentsViewModel.fetchDocumentRequest(selectedValue)
                }
            }
        }


        documentsViewModel.selectedRequestItem.observe(viewLifecycleOwner){
            if (it != null){
                binding.additionalInfo.visibility = View.VISIBLE
                binding.additionalInfo.editText?.hint = it.text_comm
                binding.claim.isEnabled = true
                binding.text.text = it.text_doc
                if (it.text_doc.isNullOrEmpty() || it.text_doc == "null"){
                    binding.text.visibility = View.GONE
                } else{
                    binding.text.visibility = View.VISIBLE
                }
            }else {
                binding.additionalInfo.visibility = View.GONE
                binding.claim.isEnabled = false
                binding.text.visibility = View.GONE
            }
        }

        binding.claim.setOnClickListener {
            documentsViewModel.claimNewDocument(selectedValue, comment = binding.additionalInfo.editText!!.text.toString())
            dismiss()
        }

        documentsViewModel.fetchOptionList()


    }


    companion object {
        fun newInstance(): NewDocumentDialogFragment {
            return NewDocumentDialogFragment()
        }
    }
}