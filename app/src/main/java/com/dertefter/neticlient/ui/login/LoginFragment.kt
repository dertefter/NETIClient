package com.dertefter.neticlient.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dertefter.neticlient.data.model.AuthState
import com.dertefter.neticlient.databinding.FragmentLoginBinding
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import com.dertefter.neticlient.ui.settings.SettingsViewModel
import com.dertefter.neticlient.utils.Utils
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

        binding.loginTextField.editText?.doOnTextChanged { _, _, _, count ->
            binding.loginButton.isEnabled = count >= 0 && binding.passwordTextField.editText?.text?.isNotEmpty() == true
        }

        binding.passwordTextField.editText?.doOnTextChanged { _, _, _, count ->
            binding.loginButton.isEnabled = count >= 0 && binding.loginTextField.editText?.text?.isNotEmpty() == true
        }

        binding.loginButton.setOnClickListener {
            val login = binding.loginTextField.editText?.text.toString()
            val password = binding.passwordTextField.editText?.text.toString()
            loginViewModel.auth(login, password)
        }


        loginViewModel.authStateLiveData.observe(viewLifecycleOwner){
            when(it){
                AuthState.UNAUTHORIZED -> {
                    binding.login.visibility = View.VISIBLE
                }
                AuthState.AUTHORIZED -> {
                    scheduleViewModel.getSelectedGroup()
                    binding.login.visibility = View.GONE
                    findNavController().popBackStack()
                }
                AuthState.AUTHORIZED_WITH_ERROR -> {
                    binding.login.visibility = View.VISIBLE
                    Utils.showToast(requireContext(), "Ошибка авторизации")
                    binding.loginTextField.editText?.setText("")
                    binding.passwordTextField.editText?.setText("")
                }
                AuthState.AUTHORIZING -> {
                    binding.login.visibility = View.GONE
                }
            }
        }

    }

}