package com.dertefter.neticlient.ui.on_boarding.pages

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.AppBarEdgeToEdge
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticlient.data.model.AuthState
import com.dertefter.neticlient.databinding.FragmentOnboardingPage2Binding
import com.dertefter.neticlient.ui.login.LoginViewModel
import com.dertefter.neticore.features.authorization.model.AuthStatusType
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.io.File

class OnboardingPage2 : Fragment() {

    private var _binding: FragmentOnboardingPage2Binding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by activityViewModels()

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
        val loginEditText = binding.loginTextField.editText
        val passwordEditText = binding.passwordTextField.editText
        val loginButton = binding.loginButton

        fun isInputValid(): Boolean {
            return !loginEditText?.text.isNullOrEmpty() && !passwordEditText?.text.isNullOrEmpty()
        }

        fun performLogin() {
            if (isInputValid()) {
                val login = loginEditText?.text.toString()
                val password = passwordEditText?.text.toString()
                loginViewModel.login(login, password)
                hideKeyboard()
            }
        }

        loginEditText?.doOnTextChanged { _, _, _, _ ->
            loginButton.isEnabled = isInputValid()
        }

        passwordEditText?.doOnTextChanged { _, _, _, _ ->
            loginButton.isEnabled = isInputValid()
        }

        passwordEditText?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                performLogin()
                return@setOnKeyListener true
            }
            false
        }

        loginButton.setOnClickListener {
            performLogin()
        }

        binding.toLogin.setOnClickListener {
            binding.info.isGone = true
            binding.action.isVisible = true
        }

        binding.toInfo.setOnClickListener {
            binding.info.isVisible = true
            binding.action.isGone = true
        }

        binding.policyButton.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                "https://docs.google.com/document/d/1D0f0Fp51h_Jj6MZro8nLKvSY2plH1CLZVD4js3ZFSYY/edit?usp=sharing".toUri()
            )
            startActivity(intent)
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
                                    binding.profilePic.setImageBitmap(bitmap)
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.userDetail.collect { user ->
                    binding.profileCard.isVisible = user != null

                    if (!user?.name.isNullOrEmpty()){
                        binding.nameTv.text = user.name
                    }

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.authStatus.collect { authStatus ->

                    binding.loading.isVisible = authStatus == AuthStatusType.LOADING
                    binding.authed.isVisible = authStatus == AuthStatusType.AUTHORIZED
                    binding.login.isVisible = authStatus == AuthStatusType.UNAUTHORIZED || authStatus == AuthStatusType.AUTHORIZED_WITH_ERROR


                    when (authStatus){
                        AuthStatusType.AUTHORIZED -> {
                            binding.title.text = getString(R.string.ob_2_title_auhorized)
                            loginViewModel.updateUserDetail()
                        }
                        AuthStatusType.UNAUTHORIZED -> binding.title.text = getString(R.string.ob_2_title_1)
                        AuthStatusType.AUTHORIZED_WITH_ERROR -> binding.title.text = getString(R.string.auth_error)
                        AuthStatusType.LOADING -> binding.title.text = getString(R.string.authing)
                    }

                    if ( authStatus == AuthStatusType.AUTHORIZED_WITH_ERROR){
                        Utils.showToast(requireContext(),getString(R.string.auth_error))
                        loginEditText?.setText("")
                        passwordEditText?.setText("")
                    }

                    if ( authStatus == AuthStatusType.AUTHORIZED || authStatus == AuthStatusType.LOADING){
                        binding.info.isGone = true
                        binding.action.isVisible = true
                    }

                }
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
