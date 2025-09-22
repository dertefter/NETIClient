package com.dertefter.neticlient.ui.profile

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.common.item_decoration.HorizontalSpaceItemDecoration
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.databinding.FragmentProfileBinding
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticlient.ui.profile.lks_selector.LksSelectorBottomSheet
import com.dertefter.neticore.features.authorization.model.AuthStatusType
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.loadingindicator.LoadingIndicator
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log


@AndroidEntryPoint
class ProfileFragment : Fragment() {


    @Inject
    lateinit var picasso: Picasso

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

                    binding.loadingIndicator.isGone = authStatus != AuthStatusType.LOADING




                    when (authStatus) {
                        AuthStatusType.AUTHORIZED -> {

                            if (!userDetailMobile?.photoPath.isNullOrEmpty()){

                                picasso
                                    .load(userDetailMobile.photoPath)
                                    .into(object : com.squareup.picasso.Target {
                                        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                                            binding.avatarShape.isGone = false
                                            binding.avatarShape.setImageBitmap(bitmap)
                                        }

                                        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                                            binding.avatarShape.isGone = true
                                        }

                                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                            binding.avatarShape.isGone = true
                                        }
                                    })


                            }

                            binding.statusCard.setOnClickListener {

                            }

                            animateBackgroundColor(
                                binding.statusCard,
                                MaterialColors.getColor(binding.statusCard, com.google.android.material.R.attr.colorPrimaryContainer)
                            )
                            animateIndicatorColor(
                                binding.loadingIndicator,
                                MaterialColors.getColor(binding.statusCard, com.google.android.material.R.attr.colorOnPrimaryContainer)
                            )
                            animateTextColor(
                                binding.statusInfoTitle,
                                MaterialColors.getColor(binding.statusCard, com.google.android.material.R.attr.colorOnPrimaryContainer)
                            )
                            animateTextColor(
                                binding.statusInfoText,
                                MaterialColors.getColor(binding.statusCard, com.google.android.material.R.attr.colorOnPrimaryContainer)
                            )

                            binding.statusInfoTitle.text = userDetail?.name
                            binding.statusInfoText.text = userDetail?.login
                        }

                        AuthStatusType.UNAUTHORIZED -> {

                            binding.statusCard.setOnClickListener {

                            }

                            animateBackgroundColor(
                                binding.statusCard,
                                MaterialColors.getColor(binding.statusCard, com.google.android.material.R.attr.colorSurfaceContainer)
                            )
                            animateIndicatorColor(
                                binding.loadingIndicator,
                                MaterialColors.getColor(binding.statusCard, com.google.android.material.R.attr.colorOnSurface)
                            )
                            animateTextColor(
                                binding.statusInfoTitle,
                                MaterialColors.getColor(binding.statusCard, com.google.android.material.R.attr.colorOnSurface)
                            )
                            animateTextColor(
                                binding.statusInfoText,
                                MaterialColors.getColor(binding.statusCard, com.google.android.material.R.attr.colorOnSurface)
                            )

                            binding.statusInfoTitle.text = getString(R.string.ob_2_title_1)
                            binding.statusInfoText.text = getString(R.string.auth_title)
                        }

                        AuthStatusType.AUTHORIZED_WITH_ERROR -> {

                            binding.statusCard.setOnClickListener {
                                loginViewModel.tryAuthorize()
                            }

                            animateBackgroundColor(
                                binding.statusCard,
                                MaterialColors.getColor(binding.statusCard, com.google.android.material.R.attr.colorErrorContainer)
                            )
                            animateIndicatorColor(
                                binding.loadingIndicator,
                                MaterialColors.getColor(binding.statusCard, com.google.android.material.R.attr.colorOnErrorContainer)
                            )
                            animateTextColor(
                                binding.statusInfoTitle,
                                MaterialColors.getColor(binding.statusCard, com.google.android.material.R.attr.colorOnErrorContainer)
                            )
                            animateTextColor(
                                binding.statusInfoText,
                                MaterialColors.getColor(binding.statusCard, com.google.android.material.R.attr.colorOnErrorContainer)
                            )

                            binding.statusInfoTitle.text = getString(R.string.auth_error)
                            binding.statusInfoText.text = getString(R.string.load_retry)
                        }

                        AuthStatusType.LOADING -> {

                            binding.statusCard.setOnClickListener {

                            }

                            animateBackgroundColor(
                                binding.statusCard,
                                MaterialColors.getColor(binding.statusCard, com.google.android.material.R.attr.colorSecondaryContainer)
                            )
                            animateIndicatorColor(
                                binding.loadingIndicator,
                                MaterialColors.getColor(binding.statusCard, com.google.android.material.R.attr.colorOnSecondaryContainer)
                            )
                            animateTextColor(
                                binding.statusInfoTitle,
                                MaterialColors.getColor(binding.statusCard, com.google.android.material.R.attr.colorOnSecondaryContainer)
                            )
                            animateTextColor(
                                binding.statusInfoText,
                                MaterialColors.getColor(binding.statusCard, com.google.android.material.R.attr.colorOnSecondaryContainer)
                            )

                            binding.statusInfoTitle.text = getString(R.string.authing)
                            binding.statusInfoText.text = userDetail?.login
                        }
                    }
                }
            }
        }
    }


    fun collectLks(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.lksList.collect { lksList ->
                    val selectedLks = lksList.firstOrNull { it.isSelected }
                    binding.swopLks.isGone = selectedLks == null
                    binding.lksTitle.text = selectedLks?.title
                    binding.lksSubtitle.text = selectedLks?.subtitle
                }
            }
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBarLayout.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appBarLayout))

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.settingsButton.setOnClickListener {
            findNavController().navigate(
                R.id.nav_graph_settings,
                null,
                Utils.getNavOptions(),
            )
        }

        val studentsAdapter = StudentsAdapter()
        binding.studentsRv.adapter = studentsAdapter
        binding.studentsRv.layoutManager = LinearLayoutManager(requireContext())

        binding.swopLks.setOnClickListener {
            val modalBottomSheet = LksSelectorBottomSheet()
            modalBottomSheet.show(parentFragmentManager, LksSelectorBottomSheet.TAG)
        }

        collectLks()
        collectStatus()
        profileViewModel.updateLks()
        profileViewModel.updateUserDetail()


    }


    fun animateBackgroundColor(view: View, newColor: Int, duration: Long = 300) {
        val currentColor = (view.backgroundTintList?.defaultColor)
            ?: (if (view is MaterialCardView) view.cardBackgroundColor.defaultColor else null)
            ?: newColor

        if (currentColor == newColor) return

        val animator = ValueAnimator.ofObject(ArgbEvaluator(), currentColor, newColor)
        animator.duration = duration
        animator.addUpdateListener { valueAnimator ->
            val color = valueAnimator.animatedValue as Int
            when (view) {
                is MaterialCardView -> view.setCardBackgroundColor(color)
                else -> view.setBackgroundColor(color)
            }
        }
        animator.start()
    }


    fun animateIndicatorColor(
        indicator: LoadingIndicator,
        newColor: Int,
        duration: Long = 300
    ) {
        val currentColor = indicator.indicatorColor.firstOrNull() ?: newColor
        if (currentColor == newColor) return

        val animator = ValueAnimator.ofObject(ArgbEvaluator(), currentColor, newColor)
        animator.duration = duration
        animator.addUpdateListener { valueAnimator ->
            val color = valueAnimator.animatedValue as Int
            indicator.setIndicatorColor(color)
        }
        animator.start()
    }

    fun animateTextColor(textView: TextView, newColor: Int, duration: Long = 300) {
        val currentColor = textView.currentTextColor
        if (currentColor == newColor) return

        val animator = ValueAnimator.ofObject(ArgbEvaluator(), currentColor, newColor)
        animator.duration = duration
        animator.addUpdateListener { valueAnimator ->
            textView.setTextColor(valueAnimator.animatedValue as Int)
        }
        animator.start()
    }

}