package com.dertefter.neticlient.ui.profile

import android.graphics.BitmapFactory
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.AuthState
import com.dertefter.neticlient.databinding.FragmentProfileBinding
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.utils.GridSpacingItemDecoration
import com.dertefter.neticlient.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    lateinit var binding: FragmentProfileBinding

    private val loginViewModel: LoginViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                Utils.basicAnimationOff(binding.topContainer, false).start()
            } else {
                Utils.basicAnimationOn(binding.topContainer).start()
            }
            Log.e("verticalOffset", verticalOffset.toString())
        }

        val adapter = ProfileMenuAdapter(fragment = this)
        val spanCount = 2
        val layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, 8))
        binding.recyclerView.adapter = adapter

        loginViewModel.authStateLiveData.observe(viewLifecycleOwner){
            if (it == AuthState.UNAUTHORIZED){
                binding.authCard.visibility = View.VISIBLE
            } else {
                binding.authCard.visibility = View.GONE
            }

            if (it == AuthState.AUTHORIZED){
                profileViewModel.updateMenuItems(true)
            }else{
                profileViewModel.updateMenuItems(false)
            }

            if (it == AuthState.AUTHORIZED_WITH_ERROR){
                binding.errorCard.visibility = View.VISIBLE
                binding.retryButton.setOnClickListener {
                    loginViewModel.tryAuthorize()
                }
            } else {
                binding.errorCard.visibility = View.GONE
            }

        }

        profileViewModel.menuItems.observe(viewLifecycleOwner){
            adapter.updateItems(it)
        }


        binding.authButton.setOnClickListener {
            findNavController().navigate(
                R.id.loginFragment,
                null,
                Utils.basicTransitionAnimations(),
            )
        }

        loginViewModel.fetchUser()

        binding.settingsButton.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        loginViewModel.userLiveData.observe(viewLifecycleOwner){
            if (!it?.name.isNullOrEmpty()){
                if (!it?.profilePicPath.isNullOrEmpty()){
                    val file = File(it?.profilePicPath!!)
                    if (file.exists()){
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        binding.profilePic.setImageBitmap(bitmap)
                    }
                }
            }
        }



    }

}