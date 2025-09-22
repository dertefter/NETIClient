package com.dertefter.neticlient.ui.settings

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.databinding.FragmentSettingsBinding
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticore.features.authorization.model.AuthStatusType
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private val loginViewModel: LoginViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun collectLoginState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    loginViewModel.authStatus,
                    loginViewModel.userDetailMobile
                ) { authStatus, userDetail ->
                    authStatus to userDetail
                }.collect { (authStatus, userDetail) ->

                    binding.statusDone.isGone = authStatus != AuthStatusType.AUTHORIZED
                    binding.statusError.isGone = authStatus != AuthStatusType.AUTHORIZED_WITH_ERROR
                    binding.statusLoading.isGone = authStatus != AuthStatusType.LOADING
                    binding.statusGuest.isGone = authStatus != AuthStatusType.UNAUTHORIZED
                    binding.logout.isGone = authStatus == AuthStatusType.UNAUTHORIZED

                    if (authStatus == AuthStatusType.AUTHORIZED && userDetail == null) {
                        binding.statusError.isGone = false
                        binding.statusDone.isGone = true
                    }

                    if (userDetail != null) {
                        binding.login.text = userDetail.login
                        binding.name.text = userDetail.name
                        Picasso.get()
                            .load(userDetail.photoPath)
                            .into(object : com.squareup.picasso.Target {
                                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                                    binding.avatarShape.setImageBitmap(bitmap)
                                }

                                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {

                                }

                                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

                                }
                            })
                    }
                }
            }
        }
    }



    fun setupRecyclerView(){
        collectLoginState()
        val settings = listOf<SettingsListItem>(
            SettingsListItem(
                titleRes = R.string.settings_notifications_title,
                subtitleRes = R.string.settings_notifications_subtitle,
                iconRes = R.drawable.notifications,
                navId = R.id.settingsNotificationsFragment
            ),
            SettingsListItem(
                titleRes = R.string.settings_personaliztion_title,
                subtitleRes = R.string.settings_personaliztion_subtitle,
                iconRes = R.drawable.palette_icon,
                navId = R.id.settingsPersonaliztionFragment
            ),
            SettingsListItem(
                titleRes = R.string.settings_labs_title,
                subtitleRes = R.string.settings_labs_subtitle,
                iconRes = R.drawable.experiment,
                navId = R.id.settingsLabsFragment
            ),
            SettingsListItem(
                titleRes = R.string.settings_about_title,
                subtitleRes = R.string.settings_about_subtitle,
                iconRes = R.drawable.info,
                navId = R.id.settingsAboutFragment
            )
        )

        val adapter = SettingsListAdapter(
            onItemClick = {navId ->
                findNavController().navigate(
                    navId,
                    null,
                    Utils.getNavOptions(),
                )
            },
            items = settings
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(
            VerticalSpaceItemDecoration( R.dimen.max, R.dimen.min, R.dimen.micro)
        )

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBarLayout.addOnOffsetChangedListener(AppBarEdgeToEdge(binding.appBarLayout))

        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerView) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )

            binding.recyclerView.updatePadding(
                bottom = bars.bottom
            )
            WindowInsetsCompat.CONSUMED
        }

        setupRecyclerView()


        binding.logout.setOnClickListener {
            loginViewModel.logout()
        }

        binding.statusError.setOnClickListener {
            loginViewModel.tryAuthorize()
        }

        binding.statusGuest.setOnClickListener {
            findNavController().navigate(
                R.id.loginFragment,
                null,
                Utils.getNavOptions(),
            )
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }





    }





}

