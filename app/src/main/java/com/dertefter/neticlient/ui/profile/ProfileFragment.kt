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
import com.dertefter.neticlient.common.item_decoration.AvatarOverlapItemDecoration
import com.dertefter.neticlient.common.item_decoration.HorizontalSpaceItemDecoration
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.common.utils.Utils.goingTo
import com.dertefter.neticlient.databinding.FragmentProfileBinding
import com.dertefter.neticlient.ui.login.LoginBottomSheetFragment
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

    private lateinit var studentAvatarListAdapter: StudentAvatarListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }


    fun showLoginDialog(){
        val modalBottomSheet = LoginBottomSheetFragment()
        modalBottomSheet.show(childFragmentManager, LoginBottomSheetFragment.TAG)
    }

    fun enabledButtons(s: Boolean){
        binding.docsButton.isEnabled = s
        binding.moneyButton.isEnabled = s
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

                    enabledButtons(authStatus != AuthStatusType.UNAUTHORIZED)

                    binding.statusCardIncluded.statusGuest.isVisible = authStatus == AuthStatusType.UNAUTHORIZED
                    binding.statusCardIncluded.statusError.isVisible = authStatus == AuthStatusType.AUTHORIZED_WITH_ERROR
                    binding.statusCardIncluded.statusLoading.isVisible = authStatus == AuthStatusType.LOADING
                    binding.statusCardIncluded.statusSuccess.isVisible = authStatus == AuthStatusType.AUTHORIZED
                }
            }
        }
    }


    fun collectGroupAndLks() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    profileViewModel.lksList,
                    profileViewModel.studentGroup
                ) { lksList, studentGroup ->
                    lksList to studentGroup
                }.collect { (lksList, studentGroup) ->
                    val selectedLks = lksList.firstOrNull { it.isSelected }
                    binding.swopLks.isGone = selectedLks == null
                    binding.lksTitle.text = selectedLks?.title
                    binding.lksSubtitle.text = selectedLks?.subtitle

                    binding.groupCard.isGone = studentGroup == null || (selectedLks != null && !selectedLks.title.contains(studentGroup.name))
                    binding.groupTv.text = studentGroup?.name
                    studentAvatarListAdapter.submitList(studentGroup?.students ?: emptyList())
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.statusCardIncluded.statusCard.setOnClickListener {
            showLoginDialog()
        }

        studentAvatarListAdapter = StudentAvatarListAdapter(picasso)
        binding.groupStudentsRv.adapter = studentAvatarListAdapter
        binding.groupStudentsRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.groupStudentsRv.addItemDecoration(
            AvatarOverlapItemDecoration(
                requireContext(), R.dimen.d4
            )
        )

        binding.appBarLayout.addOnOffsetChangedListener(AppBarEdgeToEdge( binding.appBarLayout))

        binding.notificationButton.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToSettingsNotificationsFragment()
            findNavController().goingTo(action)
        }

        binding.themeButton.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToSettingsPersonaliztionFragment()
            findNavController().goingTo(action)
        }

        binding.groupCard.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToStudentListFragment()
            findNavController().goingTo(action)
        }

        binding.swopLks.setOnClickListener {
            val modalBottomSheet = LksSelectorBottomSheet()
            modalBottomSheet.show(parentFragmentManager, LksSelectorBottomSheet.TAG)
        }



        binding.moneyButton.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToMoneyFragment()
            findNavController().goingTo(action)
        }

        binding.docsButton.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToDocumentsFragment()
            findNavController().goingTo(action)
        }


        collectGroupAndLks()
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