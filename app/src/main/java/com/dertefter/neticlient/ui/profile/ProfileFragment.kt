package com.dertefter.neticlient.ui.profile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.dertefter.neticlient.common.item_decoration.HorizontalSpaceItemDecoration
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.data.model.AuthState
import com.dertefter.neticlient.databinding.FragmentProfileBinding
import com.dertefter.neticlient.ui.login.LoginFragment
import com.dertefter.neticlient.ui.login.LoginReasonType
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticore.features.authorization.model.AuthStatusType
import com.squareup.picasso.Picasso
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

        profileViewModel.updateLks()
        profileViewModel.updateUserDetail()
        val lksAdapter = LksAdapter(
            onItemClick = { lks ->
                if (lks.id != null){
                    profileViewModel.setLksById(lks.id!!)
                }else{
                    Toast.makeText(requireContext(), "Ошибка", Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.lksRecyclerView?.adapter = lksAdapter
        binding.lksRecyclerView?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.lksRecyclerView?.addItemDecoration(
            HorizontalSpaceItemDecoration(R.dimen.margin_min)
        )


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.lksList.collect { lksList ->
                    Log.e("lksList", lksList.toString())
                    lksAdapter.updateItems(lksList)
                    binding.lksRecyclerView?.isGone = lksList.isEmpty()
                }
            }
        }


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




        binding.statusError.setOnClickListener {
            loginViewModel.tryAuthorize()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.authStatus.collect { authStatus ->


                    val unauthorized = authStatus == AuthStatusType.UNAUTHORIZED

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
                loginViewModel.authStatus.collect { authStatus ->


                    binding.statusError.isGone = authStatus != AuthStatusType.AUTHORIZED_WITH_ERROR
                    binding.statusLoading.isGone = authStatus != AuthStatusType.LOADING
                    binding.editButton?.setOnClickListener {
                        findNavController().navigate(
                            R.id.profileDetailFragment,
                            null,
                            Utils.getNavOptions(),
                        )
                    }

                    if (authStatus == AuthStatusType.UNAUTHORIZED){
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
                loginViewModel.userDetail.collect { user ->
                    binding.groupCart.isVisible = !user?.symGroup.isNullOrEmpty()

                    if (!user?.name.isNullOrEmpty()){
                        binding.name.text = user.name
                        binding.groupName.text = user.symGroup
                        binding.email.text = user.login
                    }

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.userDetailMobile.collect { userDetail ->
                    Log.e("userMoble", userDetail.toString())
                    if (!userDetail?.photoPath.isNullOrEmpty()){

                        Picasso.get()
                            .load(userDetail.photoPath)
                            .into(object : com.squareup.picasso.Target {
                                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                                    binding.avatarShape.setImageBitmap(bitmap)
                                }

                                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                                    // обработка ошибки
                                }

                                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                    // можно показать плейсхолдер
                                }
                            })


                    }
                }
            }
        }




    }

}