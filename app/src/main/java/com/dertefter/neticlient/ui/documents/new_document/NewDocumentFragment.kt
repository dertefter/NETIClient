package com.dertefter.neticlient.ui.documents.new_document

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.databinding.FragmentNewDocumentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewDocumentFragment : Fragment() {

    private val newDocumentViewModel: NewDocumentViewModel by viewModels()
    private lateinit var binding: FragmentNewDocumentBinding

    var selectedValue = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewDocumentBinding.inflate(inflater, container, false)
        return binding.root
    }


    fun collectOptionList(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                newDocumentViewModel.optionList.collect { optionList ->
                    if (optionList != null){

                        val displayList = optionList.map { item -> item.text }

                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            displayList
                        )
                        binding.docSelection.setAdapter(adapter)

                        if (optionList.isNotEmpty()) {
                            binding.docSelection.setText(displayList[0], false)
                            val firstItem = optionList[0]
                            selectedValue = firstItem.value
                            newDocumentViewModel.fetchDocumentRequest(selectedValue)
                        }

                        binding.docSelection.setOnItemClickListener { _, _, position, _ ->
                            val selectedItem = optionList[position]
                            val selectedText = selectedItem.text
                            selectedValue = selectedItem.value
                            newDocumentViewModel.fetchDocumentRequest(selectedValue)
                        }
                    }
                }
            }
        }
    }


    fun collectSelectedRequestItem(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                newDocumentViewModel.selectedRequestItem.collect { selectedRequestItem ->
                    if (selectedRequestItem != null){
                        binding.additionalInfo.visibility = View.VISIBLE
                        binding.additionalInfo.editText?.hint = selectedRequestItem.text_comm
                        binding.claim.isEnabled = true
                        binding.text.text = selectedRequestItem.text_doc
                        if (selectedRequestItem.text_doc.isNullOrEmpty() || selectedRequestItem.text_doc == "null"){
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
            }
        }
    }

    fun collectClaimStatus(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                newDocumentViewModel.claimSuccess.collect { claimSuccess ->
                    if (claimSuccess == true){
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newDocumentViewModel.updateOptionList()
        binding.appBarLayout.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appBarLayout))
        collectOptionList()
        collectSelectedRequestItem()
        collectClaimStatus()

        binding.claim.setOnClickListener {
            newDocumentViewModel.claimNewDocument(selectedValue, comment = binding.additionalInfo.editText!!.text.toString())
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }



    }
}