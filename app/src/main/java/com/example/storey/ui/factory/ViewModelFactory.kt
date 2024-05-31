package com.example.storey.ui.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.storey.data.local.StoryDatabase
import com.example.storey.data.remote.ApiConfig
import com.example.storey.data.repository.AuthRepository
import com.example.storey.data.repository.MainRepository
import com.example.storey.ui.addstory.AddStoryViewModel
import com.example.storey.ui.auth.loginregister.viewmodels.LoginViewModel
import com.example.storey.ui.auth.loginregister.viewmodels.RegisterViewModel
import com.example.storey.ui.main.MainViewModel
import com.example.storey.ui.map.MapsViewModel
import com.example.storey.utils.SettingsPreferences
import com.example.storey.utils.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ViewModelFactory(
    private val context: Context
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val storyDatabase = StoryDatabase.getInstance(context)
        val settingsPreferences = SettingsPreferences.getInstance(context.dataStore)
        val token = runBlocking { settingsPreferences.getToken().first() }

        val mainRepository = MainRepository(ApiConfig.getApiService(token), storyDatabase)
        val authRepository = AuthRepository(ApiConfig.getApiService(token))

        return if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            LoginViewModel(
                authRepository,
                settingsPreferences
            ) as T
        } else if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            RegisterViewModel(
                authRepository
            ) as T
        } else if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            MainViewModel(
                mainRepository,
                settingsPreferences
            ) as T
        } else if (modelClass.isAssignableFrom(AddStoryViewModel::class.java)) {
            AddStoryViewModel(
                mainRepository
            ) as T
        } else if (modelClass.isAssignableFrom(MapsViewModel::class.java)) {
            MapsViewModel(
                mainRepository
            ) as T
        } else
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}