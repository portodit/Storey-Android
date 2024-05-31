package com.example.storey.ui.auth.loginregister.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.storey.data.repository.AuthRepository
import com.example.storey.utils.SettingsPreferences
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository,
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {
    val isLoading = MutableLiveData(false)
    val errorMessage = MutableLiveData<String>()

    fun loginUser(email: String, password: String) =
        repository.login(email, password)

    fun saveToken(
        token: String,
    ) {
        viewModelScope.launch {
            settingsPreferences.saveToken(token)
        }
    }

    fun getToken() = settingsPreferences.getToken().asLiveData()
}