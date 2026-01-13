package com.dertefter.neticlient.ui.on_boarding.pages

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.common.utils.Utils.goingTo
import com.dertefter.neticlient.databinding.FragmentOnboardingPage2Binding
import com.dertefter.neticlient.ui.login.LoginBottomSheetFragment
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticlient.ui.profile.ProfileFragmentDirections
import com.dertefter.neticlient.ui.profile.ProfileViewModel
import com.dertefter.neticore.features.authorization.model.AuthStatusType
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.loadingindicator.LoadingIndicator
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingPage2 : Fragment() {


    @Inject
    lateinit var picasso: Picasso

    private var _binding: FragmentOnboardingPage2Binding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by activityViewModels()

    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingPage2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.appbar.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appbar))

        collectStatus()

        binding.statusCardIncluded.statusCard.setOnClickListener {
            showLoginDialog()
        }

        binding.policyButton.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                "https://docs.google.com/document/d/1D0f0Fp51h_Jj6MZro8nLKvSY2plH1CLZVD4js3ZFSYY/edit?usp=sharing".toUri()
            )
            startActivity(intent)
        }


    }

    fun showLoginDialog(){
        val modalBottomSheet = LoginBottomSheetFragment()
        modalBottomSheet.show(childFragmentManager, LoginBottomSheetFragment.TAG)
    }

    fun collectStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    loginViewModel.mobileAuthStatus,
                    loginViewModel.userDetail,
                    loginViewModel.userDetailMobile
                ) { authStatus, userDetail, userDetailMobile ->
                    Triple(authStatus, userDetail, userDetailMobile)
                }.collect { (authStatus, userDetail, userDetailMobile) ->

                    binding.statusCardIncluded.loadingIndicator.isGone = authStatus != AuthStatusType.LOADING

                    binding.statusCardIncluded.name.text = userDetail?.name
                    binding.statusCardIncluded.email.text = userDetail?.login

                    if (!userDetailMobile?.photoPath.isNullOrEmpty()){
                        picasso
                            .load(userDetailMobile.photoPath)
                            .into(object : com.squareup.picasso.Target {
                                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                                    binding.statusCardIncluded.avatarShape.isGone = false
                                    bitmap?.let{
                                        binding.statusCardIncluded.avatarShape.setBitmap(bitmap)
                                    }
                                }

                                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                                    binding.statusCardIncluded.avatarShape.isGone = true
                                }

                                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                    binding.statusCardIncluded.avatarShape.isGone = true
                                }
                            })


                    }

                    binding.statusCardIncluded.statusGuest.isVisible = authStatus == AuthStatusType.UNAUTHORIZED
                    binding.statusCardIncluded.statusError.isVisible = authStatus == AuthStatusType.AUTHORIZED_WITH_ERROR
                    binding.statusCardIncluded.statusLoading.isVisible = authStatus == AuthStatusType.LOADING
                    binding.statusCardIncluded.statusSuccess.isVisible = authStatus == AuthStatusType.AUTHORIZED
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
