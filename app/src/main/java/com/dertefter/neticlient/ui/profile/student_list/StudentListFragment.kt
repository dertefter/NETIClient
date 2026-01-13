package com.dertefter.neticlient.ui.profile.student_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.common.utils.Utils.goingTo
import com.dertefter.neticlient.databinding.FragmentStudentListBinding
import com.dertefter.neticlient.ui.profile.ProfileViewModel
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StudentListFragment : Fragment() {

    @Inject
    lateinit var picasso: Picasso


    lateinit var binding: FragmentStudentListBinding

    lateinit var adapter: StudentListAdapter

    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStudentListBinding.inflate(inflater, container, false)
        return binding.root
    }


    fun setupRecyclerView(){
        adapter = StudentListAdapter(
            picasso,
            onItemClick = {
                val action = StudentListFragmentDirections.actionStudentListFragmentToStudentDetailFragment(it)
                findNavController().goingTo(action)
            }
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(
            VerticalSpaceItemDecoration()
        )
    }

    fun collectStudentList(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.studentGroup.collect { studentGroup ->
                    binding.toolbar.title = studentGroup?.name
                    adapter.submitList(studentGroup?.students ?: emptyList())
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        setupRecyclerView()
        collectStudentList()


    }


}