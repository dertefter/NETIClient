package com.dertefter.neticlient.ui.profile

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.data.model.AuthState
import com.dertefter.neticlient.databinding.FragmentProfileBinding
import com.dertefter.neticlient.ui.login.LoginFragment
import com.dertefter.neticlient.ui.login.LoginReasonType
import com.dertefter.neticlient.ui.login.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File


@AndroidEntryPoint
class ProfileFragment : Fragment() {

    lateinit var binding: FragmentProfileBinding


    private val loginViewModel: LoginViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBarLayout.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appBarLayout))


        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.settingsButton.setOnClickListener {
            findNavController().navigate(
                R.id.settingsFragment,
                null,
                Utils.getNavOptions(),
            )
        }

        val studentsAdapter = StudentsAdapter()
        binding.studentsRv.adapter = studentsAdapter
        binding.studentsRv.layoutManager = LinearLayoutManager(requireContext())


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.studentsFlow.collect { students ->
                    if (!students.isNullOrEmpty()) {
                        studentsAdapter.updateStudents(students)
                        binding.expandGroup.visibility = View.VISIBLE

                        binding.expandGroup.setOnClickListener {
                            binding.studentsRv.isGone = false
                            binding.expandGroup.visibility = View.GONE
                            binding.shrinkGroup.visibility = View.VISIBLE
                            val transition = AutoTransition().apply {
                                duration = 200
                            }
                            TransitionManager.beginDelayedTransition(binding.list, transition)
                        }

                        binding.shrinkGroup.setOnClickListener {
                            binding.studentsRv.isGone = true
                            binding.expandGroup.visibility = View.VISIBLE
                            binding.shrinkGroup.visibility = View.GONE
                            val transition = AutoTransition().apply {
                                duration = 200
                            }
                            TransitionManager.beginDelayedTransition(binding.list, transition)
                        }

                    } else {
                        binding.shrinkGroup.visibility = View.GONE
                        binding.expandGroup.visibility = View.GONE
                    }
                }
            }
        }

        binding.statusError.setOnClickListener {
            loginViewModel.tryAuthorize()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.authStateFlow.collect { authState ->


                    val unauthorized = authState == AuthState.UNAUTHORIZED

                    if (unauthorized){
                        binding.loginHelper.isGone = false
                        childFragmentManager.beginTransaction().replace(binding.loginHelper.id, LoginFragment(
                            LoginReasonType.UNAUTHORIZED)
                        ).commit()
                    }else{
                        binding.loginHelper.isGone = true
                    }
                }
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.authStateFlow.collect { authState ->


                    binding.statusError.isGone = authState != AuthState.AUTHORIZED_WITH_ERROR
                    binding.statusLoading.isGone = authState != AuthState.AUTHORIZING
                    binding.statusAuth.isGone = authState != AuthState.AUTHORIZED
                    binding.statusAuth.setOnClickListener {
                        findNavController().navigate(
                            R.id.profileDetailFragment,
                            null,
                            Utils.getNavOptions(),
                        )
                    }

                    if (authState == AuthState.UNAUTHORIZED){
                        binding.moneyButton.setOnClickListener {
                            findNavController().navigate(
                                R.id.loginFragment,
                                null,
                                Utils.getNavOptions(),
                            )
                        }

                        binding.docsButton.setOnClickListener {
                            findNavController().navigate(
                                R.id.loginFragment,
                                null,
                                Utils.getNavOptions(),
                            )
                        }
                    }else{
                        profileViewModel.updateStudents()

                        binding.moneyButton.setOnClickListener {
                            findNavController().navigate(
                                R.id.moneyFragment,
                                null,
                                Utils.getNavOptions(),
                            )
                        }

                        binding.docsButton.setOnClickListener {
                            findNavController().navigate(
                                R.id.documentsFragment,
                                null,
                                Utils.getNavOptions(),
                            )
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.userFlow.collect { user ->
                    binding.groupCart.isVisible = !user?.group.isNullOrEmpty()

                    if (!user?.name.isNullOrEmpty()){
                        if (user.name.split(" ").size >= 2){
                            binding.name.text = user.name.split(" ")[1]
                        }else{
                            binding.name.text = user.name
                        }

                        binding.groupName.text = user.group



                        binding.email.text = user.login

                        if (!user.profilePicPath.isNullOrEmpty()){
                            val file = File(user.profilePicPath)
                            if (file.exists()){
                                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                                binding.avatarShape.setImageBitmap(bitmap)
                            }
                        } else{
                            binding.avatarShape.isGone = true
                        }


                    }

                }
            }
        }




    }

}