package com.dertefter.neticlient.ui.login

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.databinding.FragmentLoginBinding
import com.dertefter.neticore.features.authorization.model.AuthStatusType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginBottomSheetFragment() : BottomSheetDialogFragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by activityViewModels()

    companion object {
        const val TAG = "LoginBottomSheet"
    }

    lateinit var adapter: UserListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }


    fun collectUserList(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.savedUsersList.collect { savedUsersList ->

                    adapter.submitList(savedUsersList)

                    if (savedUsersList.isEmpty()){
                        loginViewModel.loginScreenState.value = LoginScreenState.ADD_NEW_ACCOUNT
                        binding.toStateSelectAccount.setOnClickListener {
                            dismiss()
                        }
                    } else {
                        loginViewModel.loginScreenState.value = LoginScreenState.SELECT_ACCOUNT
                        binding.toStateSelectAccount.setOnClickListener {
                            loginViewModel.loginScreenState.value = LoginScreenState.SELECT_ACCOUNT
                        }
                    }
                }
            }
        }
    }

    fun collectSelectedUser(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.currentUser.collect { currentUser ->
                    adapter.setSelectedUser(currentUser?.login)
                }
            }
        }
    }

    fun collectAuthStatus(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.authStatus.collect { authStatus ->

                    Log.e("authUserrrr", authStatus.toString())

                    binding.loginButton.isGone = authStatus == AuthStatusType.LOADING
                    binding.loading.isGone = authStatus != AuthStatusType.LOADING
                    binding.loginTextField.isEnabled = authStatus != AuthStatusType.LOADING
                    binding.passwordTextField.isEnabled = authStatus != AuthStatusType.LOADING

                    if (authStatus == AuthStatusType.AUTHORIZED_WITH_ERROR){
                        Toast.makeText(requireContext(), getString(R.string.auth_error), Toast.LENGTH_SHORT).show()
                    }

                    loginViewModel.updateUserDetail()

                }
            }
        }
    }

    fun collectScreenState(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.loginScreenState.collect { loginScreenState ->
                    binding.selectAccount.isVisible = loginScreenState ==   LoginScreenState.SELECT_ACCOUNT
                    binding.addNewAccount.isVisible = loginScreenState ==   LoginScreenState.ADD_NEW_ACCOUNT

                }
            }
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = UserListAdapter(
            onUserClick = {
                loginViewModel.login(it.login, it.password)
            },
            onLogoutClick = {
                loginViewModel.logout()
            },
            onDeleteAccountClick = {
                loginViewModel.removeUser(it.login)
            }
        )

        binding.accountsRecyclerView.adapter = adapter
        binding.accountsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.accountsRecyclerView.addItemDecoration(
            VerticalSpaceItemDecoration()
        )

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
                loginViewModel.login(login, password, true)
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

        binding.toStateAddNew.setOnClickListener {
            loginViewModel.loginScreenState.value = LoginScreenState.ADD_NEW_ACCOUNT
        }

        collectAuthStatus()

        collectUserList()

        collectSelectedUser()

        collectScreenState()
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