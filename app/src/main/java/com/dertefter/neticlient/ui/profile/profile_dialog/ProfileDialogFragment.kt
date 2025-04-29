package com.dertefter.neticlient.ui.profile.profile_dialog

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.GridSpacingItemDecoration
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.data.model.AuthState
import com.dertefter.neticlient.databinding.FragmentProfileDialogBinding
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticlient.ui.profile.ProfileMenuAdapter
import com.dertefter.neticlient.ui.profile.ProfileViewModel
import java.io.File

class ProfileDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentProfileDialogBinding

    private val loginViewModel: LoginViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.SomeDialogTheme)
    }
    

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.profileCard.setOnClickListener {
            findNavController().navigate(
                R.id.profileDetailFragment,
                null,
                Utils.getNavOptions(),
            )
            dismiss()
        }

        val adapter = ProfileMenuAdapter(fragment = this)
        val spanCount = (resources.getInteger(R.integer.span_count) * 2)
        val layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            GridSpacingItemDecoration(
                requireContext(),
                spanCount,
                R.dimen.margin_min
            )
        )

        loginViewModel.authStateLiveData.observe(viewLifecycleOwner){
            if (it == AuthState.UNAUTHORIZED){
                binding.authCard.visibility = View.VISIBLE
                binding.profileCard.visibility = View.GONE
            } else {
                binding.authCard.visibility = View.GONE
                binding.profileCard.visibility = View.VISIBLE
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

        loginViewModel.userLiveData.observe(viewLifecycleOwner){ it->
            if (it != null){
                if (it.name.split(" ").size >= 2){
                    binding.name.text = it.name.split(" ")[1]
                }else{
                    binding.name.text = it.name
                }

                binding.login.text = it.login
            }
        }


        binding.authButton.setOnClickListener {
            findNavController().navigate(
                R.id.loginFragment,
                null,
                Utils.getNavOptions(),
            )
            dismiss()
        }

        binding.settingsButton.setOnClickListener {
            findNavController().navigate(
                R.id.settingsFragment,
                null,
                Utils.getNavOptions(),
            )
            dismiss()
        }

        loginViewModel.userLiveData.observe(viewLifecycleOwner){
            if (it != null){
                if (!it.profilePicPath.isNullOrEmpty()){
                    val file = File(it.profilePicPath)
                    if (file.exists()){
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        binding.profilePic.setImageBitmap(bitmap)
                    }
                } else{
                    binding.profilePic.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.transparent))
                }
            } else {
                binding.profilePic.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.transparent))
            }
        }

        loginViewModel.fetchUser()

    }


    companion object {
        fun newInstance(): ProfileDialogFragment {
            return ProfileDialogFragment()
        }
    }
}