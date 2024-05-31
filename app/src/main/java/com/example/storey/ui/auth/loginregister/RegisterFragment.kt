package com.example.storey.ui.auth.loginregister

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.storey.utils.Result
import androidx.fragment.app.viewModels
import com.example.storey.R
import com.example.storey.databinding.FragmentRegisterBinding
import com.example.storey.ui.auth.loginregister.viewmodels.RegisterViewModel
import com.example.storey.ui.factory.ViewModelFactory

class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding

    private val registerViewModel by viewModels<RegisterViewModel> {
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
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        observeViewModel()
        setListeners()

        return binding.root
    }

    private fun observeViewModel() {
        registerViewModel.apply {
            isLoading.observe(viewLifecycleOwner, ::showLoading)
            errorMessage.observe(viewLifecycleOwner, ::showToast)
        }
    }

    private fun setListeners() {
        binding.apply {
            btnRegister.setOnClickListener {
                when {
                    binding.edName.text.isNullOrEmpty() -> {
                        showToast("Invalid Name")
                    }

                    binding.edEmail.error.isNullOrEmpty()
                        .not() || binding.edEmail.text.isNullOrEmpty() -> {
                        showToast("Invalid Email")
                    }

                    binding.edPassword.error.isNullOrEmpty()
                        .not() || binding.edPassword.text.isNullOrEmpty() -> {
                        showToast("Invalid Password")
                    }

                    else -> {
                        registerViewModel.register(
                            edName.text.toString(),
                            edEmail.text.toString(),
                            edPassword.text.toString()
                        )
                            .observe(viewLifecycleOwner) { result ->
                                when (result) {
                                    is Result.Loading -> {
                                        registerViewModel.isLoading.postValue(true)
                                    }

                                    is Result.Success -> {
                                        registerViewModel.isLoading.postValue(false)
                                        registerViewModel.errorMessage.postValue(result.data.message.toString())
                                        switchToLogin()
                                    }

                                    is Result.Error -> {
                                        registerViewModel.isLoading.postValue(false)
                                        registerViewModel.errorMessage.postValue(result.error)
                                    }
                                }
                            }
                    }
                }
            }
            btnLogin.setOnClickListener {
                switchToLogin()
            }
        }
    }

    private fun switchToLogin() {
        with(parentFragmentManager.beginTransaction()) {
            replace(
                R.id.auth,
                LoginFragment(),
                LoginFragment::class.java.simpleName
            )
            addSharedElement(binding.edName, "name")
            addSharedElement(binding.edEmail, "email")
            addSharedElement(binding.edPassword, "password")
            addSharedElement(binding.btnRegister, "button")
            addSharedElement(binding.buttons, "buttons")
            commit()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressbar.isVisible = isLoading
            btnRegister.isVisible = !isLoading
            edName.isEnabled = !isLoading
            edEmail.isEnabled = !isLoading
            edPassword.isEnabled = !isLoading
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}