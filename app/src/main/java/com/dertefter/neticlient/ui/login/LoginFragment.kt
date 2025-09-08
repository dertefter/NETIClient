package com.dertefter.neticlient.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.dertefter.neticlient.data.model.AuthState
import com.dertefter.neticlient.databinding.FragmentLoginBinding
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import com.dertefter.neticlient.common.utils.Utils
import com.dertefter.neticore.features.authorization.model.AuthStatusType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment(val type: LoginReasonType? = null) : Fragment() {

    lateinit var binding: FragmentLoginBinding
    private val loginViewModel: LoginViewModel by activityViewModels()
    private val scheduleViewModel: ScheduleViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginEditText = binding.loginTextField.editText
        val passwordEditText = binding.passwordTextField.editText
        val loginButton = binding.loginButton


        binding.desc.isGone = type != LoginReasonType.UNAUTHORIZED


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

        binding.policyButton.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                "https://docs.google.com/document/d/1D0f0Fp51h_Jj6MZro8nLKvSY2plH1CLZVD4js3ZFSYY/edit?usp=sharing".toUri()
            )
            startActivity(intent)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.authStatus.collect { authStatus ->
                    when (authStatus) {
                        AuthStatusType.UNAUTHORIZED -> {
                            binding.loading.visibility = View.GONE
                            binding.login.visibility = View.VISIBLE
                        }
                        AuthStatusType.AUTHORIZED -> {
                            binding.loading.visibility = View.GONE
                            binding.login.visibility = View.GONE
                            findNavController().popBackStack()
                        }
                        AuthStatusType.AUTHORIZED_WITH_ERROR -> {
                            binding.loading.visibility = View.GONE
                            binding.login.visibility = View.VISIBLE
                            Utils.showToast(requireContext(), "Ошибка авторизации")
                            loginEditText?.setText("")
                            passwordEditText?.setText("")
                        }
                        AuthStatusType.LOADING -> {
                            binding.login.visibility = View.GONE
                            binding.loading.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}
