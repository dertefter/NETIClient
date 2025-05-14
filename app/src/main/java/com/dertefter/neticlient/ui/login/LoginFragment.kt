package com.dertefter.neticlient.ui.login

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.net.toUri
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dertefter.neticlient.data.model.AuthState
import com.dertefter.neticlient.databinding.FragmentLoginBinding
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import com.dertefter.neticlient.common.utils.Utils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

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

        fun isInputValid(): Boolean {
            return !loginEditText?.text.isNullOrEmpty() && !passwordEditText?.text.isNullOrEmpty()
        }

        fun performLogin() {
            if (isInputValid()) {
                val login = loginEditText?.text.toString()
                val password = passwordEditText?.text.toString()
                loginViewModel.auth(login, password)
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

        loginViewModel.authStateLiveData.observe(viewLifecycleOwner) {
            when (it) {
                AuthState.UNAUTHORIZED -> {
                    binding.login.visibility = View.VISIBLE
                }
                AuthState.AUTHORIZED -> {
                    binding.login.visibility = View.GONE
                    findNavController().popBackStack()
                }
                AuthState.AUTHORIZED_WITH_ERROR -> {
                    binding.login.visibility = View.VISIBLE
                    Utils.showToast(requireContext(), "Ошибка авторизации")
                    loginEditText?.setText("")
                    passwordEditText?.setText("")
                }
                AuthState.AUTHORIZING -> {
                    binding.login.visibility = View.GONE
                }
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}
