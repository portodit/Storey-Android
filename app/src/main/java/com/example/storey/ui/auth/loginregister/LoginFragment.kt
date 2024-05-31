package com.example.storey.ui.auth.loginregister

import android.content.Intent
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.storey.R
import com.example.storey.utils.Result
import com.example.storey.databinding.FragmentLoginBinding
import com.example.storey.ui.auth.loginregister.viewmodels.LoginViewModel
import com.example.storey.ui.factory.ViewModelFactory
import com.example.storey.ui.main.MainActivity
import com.example.storey.utils.SettingsPreferences

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val loginViewModel by viewModels<LoginViewModel> {
        ViewModelFactory(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementReturnTransition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        observeViewModel()

        setListeners()

        return binding.root
    }

    private fun observeViewModel() {
        loginViewModel.apply {
            isLoading.observe(viewLifecycleOwner, ::showLoading)
            errorMessage.observe(viewLifecycleOwner, ::showToast)
            getToken().observe(viewLifecycleOwner) {
                if (it != SettingsPreferences.PREFERENCE_DEFAULT_VALUE) {
                    val iMain = Intent(requireActivity(), MainActivity::class.java)
                    requireActivity().finishAffinity()
                    startActivity(iMain)
                }
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            btnLogin.setOnClickListener {
                when {
                    binding.edEmail.error.isNullOrEmpty().not() || binding.edEmail.text.isNullOrEmpty() -> {
                        showToast("Invalid Email")
                    }
                    binding.edPassword.error.isNullOrEmpty().not() || binding.edPassword.text.isNullOrEmpty() -> {
                        showToast("Invalid Password")
                    }
                    else -> {
                        loginViewModel.loginUser(edEmail.text.toString(), edPassword.text.toString())
                            .observe(viewLifecycleOwner) { result ->
                                when (result) {
                                    is Result.Loading -> {
                                        loginViewModel.isLoading.postValue(true)
                                    }

                                    is Result.Success -> {
                                        loginViewModel.isLoading.postValue(false)
                                        loginViewModel.saveToken(result.data.loginResult.token)
                                    }

                                    is Result.Error -> {
                                        loginViewModel.isLoading.postValue(false)
                                        loginViewModel.errorMessage.postValue(result.error)
                                    }
                                }
                            }
                    }
                }
            }

            btnRegister.setOnClickListener {
                switchToRegister()
            }
        }
    }

    private fun switchToRegister() {
        with(parentFragmentManager.beginTransaction()) {
            replace(
                R.id.auth,
                RegisterFragment(),
                RegisterFragment::class.java.simpleName
            )
            addSharedElement(binding.edEmail, "email")
            addSharedElement(binding.edPassword, "password")
            addSharedElement(binding.btnLogin, "button")
            addSharedElement(binding.buttons, "buttons")
            commit()
        }
    }


    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressbar.isVisible = isLoading
            btnLogin.isVisible = !isLoading
            edEmail.isEnabled = !isLoading
            edPassword.isEnabled = !isLoading
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}